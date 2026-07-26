package com.seal.gl_engine.platform;

import android.app.Application;
import android.content.Context;

import com.nikitos.platformBridge.LauncherParams;

final class AndroidLaunchSettings extends LauncherParams {
    private final Context context;

    private AndroidLaunchSettings(Context context) {
        this.context = context;
    }

    static AndroidLaunchSettings snapshot(AndroidLauncherParams source) {
        if (source == null || source.getContext() == null) {
            throw new IllegalArgumentException(
                    "Android launch parameters require a context"
            );
        }
        Context sourceContext = source.getContext();
        Context applicationContext = sourceContext.getApplicationContext();
        if (applicationContext == null) {
            if (sourceContext instanceof Application) {
                applicationContext = sourceContext;
            } else {
                throw new IllegalArgumentException(
                        "Android Activity must expose an application context"
                );
            }
        }

        AndroidLaunchSettings snapshot =
                new AndroidLaunchSettings(applicationContext);
        snapshot.setDebug(source.isDebug());
        snapshot.setMSAA(source.getMSAA());
        snapshot.setUseBSOD(source.getUseBSOD());
        snapshot.setStartPage(source.getStartPage());
        snapshot.setRuntimeObserver(source.getRuntimeObserver());
        return snapshot;
    }

    Context getContext() {
        return context;
    }
}
