package com.seal.gl_engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.nikitos.Engine;
import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.PlatformBridge;
import com.seal.gl_engine.engine.main.debugger.Debugger;

import java.util.function.Function;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class AndroidBridge extends PlatformBridge {
    private Context context;
    private GLSurfaceView glSurfaceView;
    protected static Function<Void, GamePageClass> startPage;

    GLSurfaceView launch(AndroidLauncherParams androidLauncherParams) {
        startPage = androidLauncherParams.getStartPage();
        this.context = androidLauncherParams.getContext();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        Log.i("engine version ", Engine.getVersion());
        Log.i("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.i("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.i("version", String.format("%X", configurationInfo.reqGlEsVersion));
        Log.i("engine version", Engine.getVersion());
        if (!supportES2()) {
            Toast.makeText(context, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            return null;
        }
        glSurfaceView = new GLSurfaceView(context);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(new MyConfigChooser(androidLauncherParams.getMSAA() ? 4 : 1));
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        float widthPixels = displayMetrics.widthPixels;
        float heightPixels = displayMetrics.heightPixels;
        if (androidLauncherParams.isLandscape() && widthPixels < heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, heightPixels, widthPixels));
        } else if (!androidLauncherParams.isLandscape() && widthPixels > heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, heightPixels, widthPixels));
        } else {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, widthPixels, heightPixels));
        }
        if (androidLauncherParams.isDebug()) {
            Debugger.debuggerInit();
        }
        return glSurfaceView;
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }


    static class MyConfigChooser implements GLSurfaceView.EGLConfigChooser {
        private final int antiAliasMode;

        protected MyConfigChooser(int antiAlismode) {
            this.antiAliasMode = antiAlismode;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] attribs = {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, 4,  // EGL_OPENGL_ES2_BIT
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, 1,
                    EGL10.EGL_SAMPLES, antiAliasMode,  // This is for 4x MSAA.
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            egl.eglChooseConfig(display, attribs, configs, 1, configCounts);

            if (configCounts[0] == 0) {
                // Failed! Error handling.
                return null;
            } else {
                return configs[0];
            }
        }
    }

}
