package com.seal.gl_engine.platform;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.nikitos.Engine;

final class AndroidProcessSession {
    private final AndroidLaunchSettings settings;
    private final AndroidBridge bridge;
    private final Engine engine;
    private final AndroidViewLifecycle<GLSurfaceView> lifecycle;

    static AndroidProcessSession create(AndroidLaunchSettings settings) {
        AndroidBridge bridge = new AndroidBridge(settings.getContext());
        Engine engine = new Engine(bridge, settings);
        return new AndroidProcessSession(settings, bridge, engine);
    }

    AndroidProcessSession(
            AndroidLaunchSettings settings,
            AndroidBridge bridge,
            Engine engine
    ) {
        this.settings = settings;
        this.bridge = bridge;
        this.engine = engine;
        lifecycle = new AndroidViewLifecycle<>(
                new AndroidViewLifecycle.Callbacks<>() {
                    @Override
                    public void attach(
                            GLSurfaceView view,
                            boolean paused
                    ) {
                        AndroidProcessSession.this.bridge.attachView(
                                view,
                                paused
                        );
                    }

                    @Override
                    public void pause(GLSurfaceView view) {
                        AndroidProcessSession.this.bridge.pauseView(view);
                        AndroidProcessSession.this.engine.onPause();
                    }

                    @Override
                    public void resume(GLSurfaceView view) {
                        AndroidProcessSession.this.engine.onResume();
                    }

                    @Override
                    public void detach(GLSurfaceView view) {
                        AndroidProcessSession.this.bridge.detachView(view);
                    }
                }
        );
    }

    GLSurfaceView launch(Context activityContext) {
        GLSurfaceView view = bridge.createView(
                activityContext,
                settings,
                engine
        );
        if (view != null) {
            lifecycle.attach(view);
        }
        return view;
    }

    void onPause(GLSurfaceView expectedView) {
        lifecycle.pause(expectedView);
    }

    void onResume(GLSurfaceView expectedView) {
        lifecycle.resume(expectedView);
    }

    void detach(GLSurfaceView expectedView) {
        lifecycle.detach(expectedView);
    }

    Engine getEngine() {
        return engine;
    }

    AndroidBridge getBridge() {
        return bridge;
    }
}
