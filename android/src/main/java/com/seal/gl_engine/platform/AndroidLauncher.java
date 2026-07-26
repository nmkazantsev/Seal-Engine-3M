package com.seal.gl_engine.platform;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.nikitos.Engine;

import java.lang.ref.WeakReference;

public class AndroidLauncher {
    private static final AndroidSessionRegistry SESSIONS =
            new AndroidSessionRegistry();
    private final WeakReference<Context> activityContext;
    private final AndroidProcessSession session;

    public AndroidLauncher(AndroidLauncherParams androidLauncherParams) {
        activityContext = new WeakReference<>(
                androidLauncherParams.getContext()
        );
        AndroidLaunchSettings settings =
                AndroidLaunchSettings.snapshot(androidLauncherParams);
        session = SESSIONS.acquire(
                settings,
                AndroidProcessSession::create
        );
    }

    public Engine getEngine() {
        return session.getEngine();
    }

    public GLSurfaceView launch() {
        Context context = activityContext.get();
        if (context == null) {
            throw new IllegalStateException(
                    "Android Activity was released before launch"
            );
        }
        return session.launch(context);
    }

    public void onPause(GLSurfaceView expectedView) {
        session.onPause(expectedView);
    }

    public void onResume(GLSurfaceView expectedView) {
        session.onResume(expectedView);
    }

    public void detach(GLSurfaceView expectedView) {
        session.detach(expectedView);
    }
}
