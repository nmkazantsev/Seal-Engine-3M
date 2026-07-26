package com.seal.gl_engine.platform;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import com.nikitos.Engine;
import com.nikitos.GamePageClass;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.images.AbstractImage;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.platformBridge.*;
import com.nikitos.runtime.FrameCaptureSource;
import com.nikitos.utils.Utils;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.images.PImageAndroid;
import com.seal.gl_engine.mp3.AndroidAudioPLayer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import java.util.Locale;
import java.util.function.Function;

public class AndroidBridge extends PlatformBridge {
    private Context context;
    private GLSurfaceView glSurfaceView;
    protected Function<Void, GamePageClass> startPage;
    private SealAssetManager assetManager;
    private AudioPlayer audioPlayer;
    private RuntimeFileBridge runtimeFileBridge;
    private final MouseControlBridge mouseControlBridge = new AndroidMouseControlBridge();
    private AndroidFrameCaptureSource frameCaptureSource;

    GLSurfaceView launch(AndroidLauncherParams androidLauncherParams, Engine engine) {
        startPage = androidLauncherParams.getStartPage();
        this.context = androidLauncherParams.getContext();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        Log.i("engine version ", Engine.getVersion());
        Log.i("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.i("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.i("version", String.format("%X", configurationInfo.reqGlEsVersion));
        if (!supportES2()) {
            Toast.makeText(context, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            return null;
        }
        glSurfaceView = new GLSurfaceView(context);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(new MyConfigChooser(androidLauncherParams.getMSAA() ? 4 : 1));

        // Keyboard forwarding (if the view has focus).
        glSurfaceView.setFocusable(true);
        glSurfaceView.setFocusableInTouchMode(true);
        glSurfaceView.requestFocus();
        glSurfaceView.setOnKeyListener((v, keyCode, event) -> {
            String keyName;
            int unicode = event.getUnicodeChar();
            if (unicode != 0) {
                keyName = String.valueOf(Character.toUpperCase((char) unicode));
            } else {
                String s = KeyEvent.keyCodeToString(keyCode);
                if (s != null && s.startsWith("KEYCODE_")) {
                    s = s.substring("KEYCODE_".length());
                }
                keyName = s == null ? null : s.toUpperCase(Locale.ROOT);
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyboardProcessor.onKeyPressed(keyName);
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyboardProcessor.onKeyReleased(keyName);
            }
            return false;
        });
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        float widthPixels = displayMetrics.widthPixels;
        float heightPixels = displayMetrics.heightPixels;
        /*if (androidLauncherParams.isLandscape() && widthPixels < heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(heightPixels, widthPixels, engine));
        } else if (!androidLauncherParams.isLandscape() && widthPixels > heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(heightPixels, widthPixels, engine));
        } else {
            glSurfaceView.setRenderer(new OpenGLRenderer(widthPixels, heightPixels, engine));
        }*/

        AndroidFrameCaptureSource captureSource =
                engine.getRuntimeObserver() == null
                        ? null
                        : getAndroidFrameCaptureSource();
        glSurfaceView.setRenderer(new OpenGLRenderer(
                widthPixels,
                heightPixels,
                engine,
                captureSource
        ));
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

    @Override
    public void onPause() {
        glSurfaceView.onPause();
    }

    @Override
    public void onResume() {
        glSurfaceView.onResume();
    }

    @Override
    public MatrixPlatformBridge getMatrixPlatformBridge() {
        return new MatrixBridgeAndroid();
    }

    @Override
    public ShaderBridge getShaderBridge() {
        return new ShaderBridgeAndroid();
    }

    @Override
    public VertexBridge getVertexBridge() {
        return new VertexBridgeAndroid();
    }

    @Override
    public GeneralPlatformBridge getGeneralPlatformBridge() {
        return new GeneralBridgeAndroid();
    }

    @Override
    public GLConstBridge getGLConstBridge() {
        return new GLConstBridgeAndroid();
    }

    @Override
    public ImgBridge getImgBridge() {
        return new ImgBridgeAndroid();
    }

    @Override
    public AbstractImage getAbstractImage() {
        return new PImageAndroid();
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

    @Override
    public void glClearColor(float r, float g, float b, float a) {
        GLES30.glClearColor(r, g, b, a);
    }

    @Override
    public int glGetError() {
        return GLES30.glGetError();
    }

    @Override
    public void log_e(String tag, String message) {
        Log.e(tag, message);
    }

    @Override
    public void log_i(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void print(String text) {
        Log.i("print", text);
    }

    @Override
    public Platform getPlatform() {
        return Platform.MOBILE;
    }

    @Override
    public ErrorPrinter getErrorPrinter() {
        return new ErrorPrinterAndroid();
    }

    @Override
    public SealAssetManager getAssetManager() {
        if (assetManager == null) {
            assetManager = new AndroidSealAssetManager(context);
        }
        return assetManager;
    }

    @Override
    public AudioPlayer getAudioPlayer() {
        if (audioPlayer == null) {
            audioPlayer = new AndroidAudioPLayer(context);
        }
        return audioPlayer;
    }

    @Override
    public FontBridge getFontBridge() {
        return new FontBridgeAndroid();
    }

    public Context getContext(){
        return context;
    }

    @Override
    public RuntimeFileBridge getRuntimeFileBridge() {
        if (runtimeFileBridge == null) {
            runtimeFileBridge = new AndroidRuntimeFileBridge(context);
        }
        return runtimeFileBridge;
    }

    @Override
    public MouseControlBridge getMouseControlBridge() {
        return mouseControlBridge;
    }

    @Override
    public FrameCaptureSource getFrameCaptureSource() {
        return getAndroidFrameCaptureSource();
    }

    private AndroidFrameCaptureSource getAndroidFrameCaptureSource() {
        if (frameCaptureSource == null) {
            frameCaptureSource = new AndroidFrameCaptureSource();
        }
        return frameCaptureSource;
    }
}
