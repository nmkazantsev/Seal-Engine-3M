package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.platformBridge.Platform;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.Utils;

public class Engine {
    public static String getVersion() {
        return "v3.2.0";
    }

    public float fps;
    private long prevFps;
    private int cadrs;

    private final PlatformBridge platformBridge;

    private GamePageClass gamePage;
    private static long prevPageChangeTime = 0;
    private final LauncherParams launcherParams;

    public Engine(PlatformBridge platformBridge, LauncherParams launcherParams) {
        this.platformBridge = platformBridge;
        this.launcherParams = launcherParams;
        Matrix.init(platformBridge);
        Utils.programStartTime = System.currentTimeMillis();
    }

    public void onSurfaceChanged(int x, int y) {
        if (gamePage == null) {
            platformBridge.log_e("engine", "on surface changed called, but game page is null");
            return;
        }
        Debugger.onResChange(x, y);
        gamePage.onSurfaceChanged(x, y);
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

    public void startNewPage(GamePageClass newPage) {
        platformBridge.log_i("engine", "start new page");
        Utils.unfreezeMillis();
        gamePage = null;
        System.gc();
        gamePage = newPage;
        resetPageMillis();
        newPage.onSurfaceChanged((int) Utils.x, (int) Utils.y);
        Debugger.onResChange((int) Utils.x, (int) Utils.y);
        VRAMobject.onPageChange();
        Shader.onPageChange();
        TouchProcessor.onPageChange();
    }

    void startDefaultPage() {
        if (launcherParams.getStartPage() == null) {
            throw new IllegalStateException("Start page can not be null!\nDeclare it in LaunchParams.");
        }
        if (gamePage != null) {
            platformBridge.log_i("engine", "asked to start default page, but it exists");
            return;
        }
        startNewPage(launcherParams.getStartPage().apply(null));
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

    public Platform getPlatform(){
        return PlatformBridge.getPlatform();
    }
}
