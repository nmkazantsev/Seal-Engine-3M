package com.seal.gl_engine.platform;

import android.opengl.GLSurfaceView;

import com.nikitos.Engine;

public class AndroidLauncher {
    private final Engine engine;
    private final AndroidBridge androidBridge;
    private final AndroidLauncherParams androidLauncherParams;

    public AndroidLauncher(AndroidLauncherParams androidLauncherParams) {
        androidBridge = new AndroidBridge();
        engine = new Engine(androidBridge, androidLauncherParams);
        this.androidLauncherParams = androidLauncherParams;
    }

    public Engine getEngine() {
        return engine;
    }

    public GLSurfaceView launch() {
        return androidBridge.launch(androidLauncherParams, engine);
    }
}
