package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.debugger.BSODScreen;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.*;
import com.nikitos.utils.Utils;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.EnumSet;

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

    private final GeneralPlatformBridge generalPlatformBridge;
    private final GLConstBridge glconstBridge;
    private final EnumSet<EngineRunState> activeRunStates = EnumSet.noneOf(EngineRunState.class);
    private boolean shutdownRequested;
    private boolean closed;
    private final AtomicReference<FrameCaptureRequest> frameCaptureRequest = new AtomicReference<>();
    private volatile FrameCaptureDataProvider frameCaptureDataProvider;

    public Engine(PlatformBridge platformBridge, LauncherParams launcherParams) {
        this.platformBridge = platformBridge;
        this.launcherParams = launcherParams;
        this.generalPlatformBridge = platformBridge.getGeneralPlatformBridge();
        this.glconstBridge = platformBridge.getGLConstBridge();
        Matrix.init(platformBridge);
        Utils.programStartTime = System.currentTimeMillis();
        String version = System.getProperty("java.version");
        platformBridge.log_i("engine", "engine is running at java " + version);
    }

    public void onSurfaceChanged(int x, int y) {
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

    public EngineRunState getRunState() {
        if (closed || activeRunStates.contains(EngineRunState.CLOSED)) return EngineRunState.CLOSED;
        if (activeRunStates.contains(EngineRunState.RENDERING_SUSPENDED)) return EngineRunState.RENDERING_SUSPENDED;
        if (activeRunStates.contains(EngineRunState.SIMULATION_PAUSED)) return EngineRunState.SIMULATION_PAUSED;
        return EngineRunState.RUNNING;
    }

    public void pauseSimulation() { activeRunStates.add(EngineRunState.SIMULATION_PAUSED); }
    public void resumeSimulation() { activeRunStates.remove(EngineRunState.SIMULATION_PAUSED); }
    public void suspendRendering() { activeRunStates.add(EngineRunState.RENDERING_SUSPENDED); }
    public void resumeRendering() { activeRunStates.remove(EngineRunState.RENDERING_SUSPENDED); }

    public void requestShutdown() { shutdownRequested = true; }
    public boolean isShutdownRequested() { return shutdownRequested; }
    public void close() {
        if (closed) return;
        closed = true;
        activeRunStates.clear();
        activeRunStates.add(EngineRunState.CLOSED);
    }

    public void requestFrameCapture() { requestFrameCapture(null); }
    public void requestFrameCapture(Path outputDirectory) {
        frameCaptureRequest.set(new FrameCaptureRequest(outputDirectory));
    }
    public void setFrameCaptureDataProvider(FrameCaptureDataProvider provider) { frameCaptureDataProvider = provider; }
    FrameCaptureRequest consumeFrameCaptureRequest() { return frameCaptureRequest.getAndSet(null); }
    public FrameCaptureDataProvider getFrameCaptureDataProvider() { return frameCaptureDataProvider; }
    public boolean getFullScreen() { return launcherParams.getFullScreen(); }

    static final class FrameCaptureRequest {
        final Path outputDirectory;

        FrameCaptureRequest(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
    }

    private boolean switching = false;

    public void startNewPage(GamePageClass newPage) {
        try {
            switching = true;
            platformBridge.log_i("engine", "start new page");
            Utils.unfreezeMillis();
            gamePage = null;
            System.gc();
            gamePage = newPage;
            resetPageMillis();
            newPage.onSurfaceChanged((int) Utils.getX(), (int) Utils.getY());
            Debugger.onResChange((int) Utils.getX(), (int) Utils.getY());
            VRAMobject.onPageChange();
            Shader.onPageChange();
            TouchProcessor.onPageChange();
            KeyboardProcessor.onPageChange();
            switching = false;
        } catch (Exception e) {
            if (launcherParams.getUseBSOD()) {
                startNewPage(new BSODScreen(e));
            } else {
                throw e;
            }
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
        try {
            startNewPage(launcherParams.getStartPage().apply(null));
        } catch (Exception e) {
            if (launcherParams.getUseBSOD()) {
                startNewPage(new BSODScreen(e));
            } else {
                throw e;
            }
        }
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
