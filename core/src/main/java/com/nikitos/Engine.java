package com.nikitos;

import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.LauncherParams;
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

    protected void resetPrevPageChangeTime() {
        prevPageChangeTime = Utils.millis();
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
        Utils.unfreezeMillis();
        gamePage = null;
        System.gc();
        gamePage = newPage;
        resetPageMillis();
        //VRAMobject.onPageChange();
        //Shader.onPageChange();
        //TouchProcessor.onPageChange();
    }

    void startDefaultPage() {
        if (launcherParams.getStartPage() == null) {
            throw new IllegalStateException("Start page can not be null!\nDeclare it in LaunchParams.");
        }
        startNewPage(launcherParams.getStartPage().apply(null));
    }

    public void resetPageMillis() {
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
}
