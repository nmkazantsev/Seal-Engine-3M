package com.seal.gl_engine.platform;

import android.content.Context;

import com.nikitos.platformBridge.RuntimeFileBridge;

import java.io.File;

public class AndroidRuntimeFileBridge extends RuntimeFileBridge {
    private final Context context;

    public AndroidRuntimeFileBridge(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected File getRelativeRoot() {
        return context.getExternalFilesDir(null);
    }
}
