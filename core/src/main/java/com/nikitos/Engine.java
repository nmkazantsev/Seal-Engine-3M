package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.debugger.BSODScreen;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.*;
import com.nikitos.runtime.PageTransition;
import com.nikitos.runtime.RuntimeFailure;
import com.nikitos.runtime.RuntimeObserver;
import com.nikitos.utils.Utils;

import java.util.function.Function;

public class Engine {
    public static String getVersion() {
        return "v3.2.6";
    }

    public float fps;
    private long prevFps;
    private int cadrs;

    private final PlatformBridge platformBridge;

    private GamePageClass gamePage;
    private static long prevPageChangeTime = 0;
    private final LauncherParams launcherParams;
    private final RuntimeObserver runtimeObserver;
    private final Function<Exception, GamePageClass> bsodPageFactory;

    private final GeneralPlatformBridge generalPlatformBridge;
    private final GLConstBridge glconstBridge;

    public Engine(PlatformBridge platformBridge, LauncherParams launcherParams) {
        this(platformBridge, launcherParams, null);
    }

    Engine(
            PlatformBridge platformBridge,
            LauncherParams launcherParams,
            Function<Exception, GamePageClass> bsodPageFactory
    ) {
        this.platformBridge = platformBridge;
        this.launcherParams = launcherParams;
        this.runtimeObserver = launcherParams.getRuntimeObserver();
        this.bsodPageFactory = bsodPageFactory;
        this.generalPlatformBridge = platformBridge.getGeneralPlatformBridge();
        this.glconstBridge = platformBridge.getGLConstBridge();
        Matrix.init(platformBridge);
        Utils.programStartTime = System.currentTimeMillis();
        String version = System.getProperty("java.version");
        platformBridge.log_i("engine", "engine is running at java " + version);
    }

    public void onSurfaceChanged(int x, int y) {
        if (x <= 0 || y <= 0) {
            platformBridge.log_i(
                    "engine",
                    "ignoring non-positive surface size " + x + "x" + y
            );
            return;
        }
        if (gamePage == null) {
            platformBridge.log_e("engine", "on surface changed called, but game page is null");
            return;
        }
        Debugger.onResChange(x, y);
        gamePage.onSurfaceChanged(x, y);
    }

    public boolean getBsodAllowed() {
        return launcherParams.getUseBSOD();
    }

    public void calculateFps() {
        if (Utils.millis() - prevFps > 100) {
            fps = 1000.0f / (int) ((Utils.millis() - prevFps) / (float) cadrs);
            prevFps = Utils.millis();
            cadrs = 0;
        }
        Utils.findTimeK();
        cadrs++;
    }

    public void onPause() {
        if (gamePage != null) {
            gamePage.onPause();
        }
        Utils.onPause();
        platformBridge.onPause();
    }

    public void onResume() {
        if (gamePage != null) {
            gamePage.onResume();
        }
        Utils.onResume();
        platformBridge.onResume();
    }

    private boolean switching = false;
    private boolean observedFrameActive;

    public void startNewPage(GamePageClass newPage) {
        startNewPage(newPage, observedFrameActive);
    }

    private void startNewPage(GamePageClass newPage, boolean markObserverFailures) {
        GamePageClass previousPage = gamePage;
        try {
            switching = true;
            platformBridge.log_i("engine", "start new page");
            Utils.unfreezeMillis();
            gamePage = null;
            System.gc();
            gamePage = newPage;
            resetPageMillis();
            newPage.onInstalled();
            newPage.onSurfaceChanged((int) Utils.getX(), (int) Utils.getY());
            Debugger.onResChange((int) Utils.getX(), (int) Utils.getY());
            VRAMobject.onPageChange();
            Shader.onPageChange();
            TouchProcessor.onPageChange();
            KeyboardProcessor.onPageChange();
            switching = false;
        } catch (Exception e) {
            if (runtimeObserver != null) {
                notifyObserver(markObserverFailures, () -> runtimeObserver.onFailure(new RuntimeFailure(
                        RuntimeFailure.Stage.PAGE_TRANSITION,
                        e,
                        null,
                        newPage
                )));
            }
            if (launcherParams.getUseBSOD()) {
                startNewPage(createBsodPage(e), markObserverFailures);
                return;
            } else {
                if (markObserverFailures && runtimeObserver != null) {
                    throw new ObservedLifecycleException(e);
                }
                throw e;
            }
        } catch (Error error) {
            if (runtimeObserver == null) {
                throw error;
            }
            notifyObserver(markObserverFailures, () -> runtimeObserver.onFailure(new RuntimeFailure(
                    RuntimeFailure.Stage.PAGE_TRANSITION,
                    error,
                    null,
                    newPage
            )));
            if (markObserverFailures) {
                throw new ObservedLifecycleException(error);
            }
            throw error;
        }
        if (runtimeObserver != null) {
            notifyObserver(
                    markObserverFailures,
                    () -> runtimeObserver.onPageChanged(new PageTransition(previousPage, gamePage))
            );
        }
    }

    void startDefaultPage() {
        if (launcherParams.getStartPage() == null) {
            throw new IllegalStateException("Start page can not be null!\nDeclare it in LaunchParams.");
        }
        if (gamePage != null) {
            platformBridge.log_i("engine", "asked to start default page, but it exists");
            return;
        }
        GamePageClass defaultPage;
        try {
            defaultPage = launcherParams.getStartPage().apply(null);
        } catch (Exception e) {
            if (launcherParams.getUseBSOD()) {
                startNewPage(createBsodPage(e), observedFrameActive);
            } else {
                throw e;
            }
            return;
        }
        startNewPage(defaultPage, observedFrameActive);
    }

    private GamePageClass createBsodPage(Exception cause) {
        if (bsodPageFactory != null) {
            return bsodPageFactory.apply(cause);
        }
        return new BSODScreen(cause);
    }

    private void notifyObserver(boolean markFailure, Runnable callback) {
        try {
            callback.run();
        } catch (RuntimeException | Error observerFailure) {
            if (markFailure) {
                throw new ObservedLifecycleException(observerFailure);
            }
            throw observerFailure;
        }
    }

    static final class ObservedLifecycleException extends RuntimeException {
        private final Throwable originalFailure;

        private ObservedLifecycleException(Throwable originalFailure) {
            super(null, null, false, false);
            this.originalFailure = originalFailure;
        }

        RuntimeException propagate() {
            if (originalFailure instanceof RuntimeException runtimeException) {
                return runtimeException;
            }
            throw (Error) originalFailure;
        }
    }

    void beginObservedFrame() {
        observedFrameActive = true;
    }

    void endObservedFrame() {
        observedFrameActive = false;
    }

    public boolean switchingNewGamePage(){
        return switching;
    }
    public void resetPageMillis() {
        platformBridge.log_i("engine", "reset page millis");
        prevPageChangeTime = Utils.millis();
    }

    public long pageMillis() {
        return Utils.millis() - prevPageChangeTime;
    }

    public Class<?> getPageClass() {
        return gamePage.getClass();
    }

    GamePageClass getGamePage() {
        return gamePage;
    }

    public RuntimeObserver getRuntimeObserver() {
        return runtimeObserver;
    }

    public PlatformBridge getPlatformBridge() {
        return platformBridge;
    }

    public void glClear() {
        platformBridge.getGeneralPlatformBridge().glClear(
                platformBridge.getGLConstBridge().GL_COLOR_BUFFER_BIT() | platformBridge.getGLConstBridge().GL_DEPTH_BUFFER_BIT()
        );
    }

    public void disableBlend() {
        generalPlatformBridge.glDisable(glconstBridge.GL_BLEND());
    }

    public void enableBlend() {
        generalPlatformBridge.glEnable(glconstBridge.GL_BLEND());
    }

    public Platform getPlatform() {
        return platformBridge.getPlatform();
    }

    public String loadTextFile(String path) {
        return platformBridge.getRuntimeFileBridge().loadTextFile(path);
    }

    public void saveTextFile(String path, String text) {
        platformBridge.getRuntimeFileBridge().saveTextFile(path, text);
    }

    public boolean fileExists(String path) {
        return platformBridge.getRuntimeFileBridge().fileExists(path);
    }

    public boolean folderExists(String path) {
        return platformBridge.getRuntimeFileBridge().folderExists(path);
    }

    public boolean createFolder(String path) {
        return platformBridge.getRuntimeFileBridge().createFolder(path);
    }

    public void disableMouseCursor() {
        platformBridge.getMouseControlBridge().disableMouseCursor();
    }

    public void enableMouseCursor() {
        platformBridge.getMouseControlBridge().enableMouseCursor();
    }

    public void setMousePosition(float x, float y) {
        platformBridge.getMouseControlBridge().setMousePosition(x, y);
    }
}
