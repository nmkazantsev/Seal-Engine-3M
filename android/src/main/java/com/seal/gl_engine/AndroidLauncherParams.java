package com.seal.gl_engine;

import android.content.Context;

import com.nikitos.platformBridge.LauncherParams;

public class AndroidLauncherParams extends LauncherParams {
    private final Context context;

    public AndroidLauncherParams(Context context) {
        this.setDebug(false);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
