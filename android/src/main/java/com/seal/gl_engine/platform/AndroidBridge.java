package com.seal.gl_engine.platform;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;
import android.opengl.EGLExt;
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
    /**
     * @deprecated Start-page ownership belongs to the process launch settings.
     * Kept for source and binary compatibility with existing Android adapters.
     */
    @Deprecated
    protected Function<Void, GamePageClass> startPage;
    private volatile Context context;
    private final AndroidViewBinding<GLSurfaceView> views =
            new AndroidViewBinding<>(
                    new AndroidViewBinding.Operations<>() {
                        @Override
                        public void pause(GLSurfaceView view) {
                            view.onPause();
                        }

                        @Override
                        public void resume(GLSurfaceView view) {
                            view.onResume();
                        }
                    }
            );
    private SealAssetManager assetManager;
    private AudioPlayer audioPlayer;
    private RuntimeFileBridge runtimeFileBridge;
    private final MouseControlBridge mouseControlBridge = new AndroidMouseControlBridge();
    private AndroidFrameCaptureSource frameCaptureSource;

    public AndroidBridge() {
        this(null);
    }

    AndroidBridge(Context context) {
        this.context = context;
    }

    GLSurfaceView createView(
            Context activityContext,
            AndroidLaunchSettings settings,
            Engine engine
    ) {
        bindApplicationContext(activityContext);
        bindLaunchSettings(settings);
        ActivityManager activityManager = (ActivityManager) activityContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        Log.i("engine version ", Engine.getVersion());
        Log.i("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.i("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.i("version", String.format("%X", configurationInfo.reqGlEsVersion));
        if (!supportES2(activityContext)) {
            Toast.makeText(activityContext, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            return null;
        }
        GLSurfaceView view = new GLSurfaceView(activityContext);
        view.setEGLContextClientVersion(3);
        view.setEGLConfigChooser(new MyConfigChooser(settings.getMSAA() ? 4 : 1));

        // Keyboard forwarding (if the view has focus).
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
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
        WindowManager wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        float widthPixels = displayMetrics.widthPixels;
        float heightPixels = displayMetrics.heightPixels;
        AndroidFrameCaptureSource captureSource =
                engine.getRuntimeObserver() == null
                        ? null
                        : getAndroidFrameCaptureSource();
        if (settings.isDebug()) {
            Debugger.debuggerInit();
        }
        view.setRenderer(new OpenGLRenderer(
                widthPixels,
                heightPixels,
                engine,
                captureSource
        ));

        return view;
    }

    private boolean supportES2(Context activityContext) {
        ActivityManager activityManager =
                (ActivityManager) activityContext.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    @Override
    public void onPause() {
        views.pauseCurrent();
    }

    @Override
    public void onResume() {
        views.resumeCurrent();
    }

    void attachView(
            GLSurfaceView view,
            boolean attachPaused
    ) {
        views.attach(view, attachPaused);
    }

    void pauseView(GLSurfaceView expectedView) {
        views.pause(expectedView);
    }

    void detachView(GLSurfaceView expectedView) {
        views.detach(expectedView);
    }

    void restoreView(
            GLSurfaceView view,
            boolean paused
    ) {
        views.restore(view, paused);
    }

    void bindLaunchSettings(AndroidLaunchSettings settings) {
        startPage = settings.getStartPage();
    }

    synchronized void bindApplicationContext(
            Context activityContext
    ) {
        Context applicationContext =
                activityContext.getApplicationContext();
        if (applicationContext == null) {
            if (activityContext instanceof Application) {
                applicationContext = activityContext;
            } else {
                throw new IllegalArgumentException(
                        "Android Activity must expose an application context"
                );
            }
        }
        if (context == null) {
            context = applicationContext;
        }
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
            EGLConfig config = chooseConfig(egl, display, attributes(antiAliasMode));
            if (config == null && antiAliasMode > 1) {
                config = chooseConfig(egl, display, attributes(0));
            }
            return config;
        }

        private static EGLConfig chooseConfig(
                EGL10 egl,
                EGLDisplay display,
                int[] attributes
        ) {
            EGLConfig[] configs = new EGLConfig[1];
            int[] configCounts = new int[1];
            if (!egl.eglChooseConfig(
                    display,
                    attributes,
                    configs,
                    configs.length,
                    configCounts
            ) || configCounts[0] == 0) {
                return null;
            }
            return configs[0];
        }

        static int[] attributes(int antiAliasMode) {
            return new int[] {
                    EGL10.EGL_LEVEL, 0,
                    EGL10.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                    EGL10.EGL_COLOR_BUFFER_TYPE, EGL10.EGL_RGB_BUFFER,
                    EGL10.EGL_RED_SIZE, 8,
                    EGL10.EGL_GREEN_SIZE, 8,
                    EGL10.EGL_BLUE_SIZE, 8,
                    EGL10.EGL_DEPTH_SIZE, 16,
                    EGL10.EGL_SAMPLE_BUFFERS, antiAliasMode > 1 ? 1 : 0,
                    EGL10.EGL_SAMPLES, Math.max(antiAliasMode, 0),
                    EGL10.EGL_NONE
            };
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
