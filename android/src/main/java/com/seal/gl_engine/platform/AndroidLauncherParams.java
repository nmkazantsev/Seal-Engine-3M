package com.seal.gl_engine.platform;

import android.content.Context;

import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.LauncherParams;

import java.util.function.Function;

public class AndroidLauncherParams extends LauncherParams {
    private final Context context;

    public AndroidLauncherParams(Context context) {
        this.setDebug(false);
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
    public AndroidLauncherParams setLandscape(boolean landscape) {
        this.landscape = landscape;
        return this;
    }

    public AndroidLauncherParams setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public AndroidLauncherParams setMSAA(boolean MSAA) {
        this.MSAA = MSAA;
        return this;
    }

    public AndroidLauncherParams setStartPage(Function<Void, GamePageClass> startPage) {
        this.startPage = startPage;
        return this;
    }
}
