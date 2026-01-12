package com.seal.gl_engine;

import android.opengl.GLSurfaceView;

public class AndroidLauncher {
    public static void run(AndroidLauncherParams androidLauncherParams){
        AndroidBridge androidBridge = new AndroidBridge();
        GLSurfaceView surfaceView = androidBridge.launch(androidLauncherParams);
    }
}
