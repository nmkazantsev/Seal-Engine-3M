package com.seal.gl_engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.nikitos.Engine;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;
import com.nikitos.platformBridge.PlatformBridge;
import com.seal.gl_engine.engine.main.shaders.Shader;

import java.util.function.Function;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import static android.opengl.GLES20.glUniform3f;
import static android.opengl.GLES20.glUniformMatrix4fv;

public class AndroidBridge extends PlatformBridge {
    private Context context;
    private static GLSurfaceView glSurfaceView;
    protected static Function<Void, GamePageClass> startPage;

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
            glSurfaceView.setRenderer(new OpenGLRenderer(heightPixels, widthPixels, engine));
        } else if (!androidLauncherParams.isLandscape() && widthPixels > heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(heightPixels, widthPixels, engine));
        } else {
            glSurfaceView.setRenderer(new OpenGLRenderer(widthPixels, heightPixels, engine));
        }
        if (androidLauncherParams.isDebug()) {
            //Debugger.debuggerInit();
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

    public void bindAllMatrix(CameraSettings c, ProjectionMatrixSettings p, float[] mMatrix) {
        applyMatrix(mMatrix);
        applyProjectionMatrix(p);
        applyCameraSettings(c);
    }

    public void applyProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled) {
        //choose wehther use perspective or not
        float[] mProjectionMatrix = new float[16];
        passProjectionMatrix(p, perspectiveEnabled, mProjectionMatrix);
    }

    public static void passProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled, float[] mProjectionMatrix) {
        if (perspectiveEnabled) {
            Matrix.frustumM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        } else {
            Matrix.orthoM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        }
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(), 1, false, mProjectionMatrix, 0);
    }

    public void applyProjectionMatrix(ProjectionMatrixSettings p) {
        float[] mProjectionMatrix = new float[16];
        //perspective is always enabled here
        Matrix.frustumM(mProjectionMatrix, 0, p.left, p.right, p.bottom, p.top, p.near, p.far);
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getProjectionLocation(), 1, false, mProjectionMatrix, 0);
    }

    public void applyCameraSettings(CameraSettings cam) {
        float[] mViewMatrix = new float[16];
        Matrix.setLookAtM(mViewMatrix, 0, cam.eyeX, cam.eyeY, cam.eyeZ, cam.centerX, cam.centerY, cam.centerZ, cam.upX, cam.upY, cam.upZ);
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getCameraLocation(), 1, false, mViewMatrix, 0);
        glUniform3f(Shader.getActiveShader().getAdaptor().getCameraPosLlocation(), cam.eyeX, cam.eyeY, cam.eyeZ);
    }

    public void applyMatrix(float[] mMatrix) {
        glUniformMatrix4fv(Shader.getActiveShader().getAdaptor().getTransformMatrixLocation(), 1, false, mMatrix, 0);
    }

    @Override
    public void setLookAtM(float[] rm, int rmOffset,
                           float eyeX, float eyeY, float eyeZ,
                           float centerX, float centerY, float centerZ,
                           float upX, float upY, float upZ) {
        android.opengl.Matrix.setLookAtM(rm, rmOffset, eyeX, eyeY, eyeZ,
                centerX, centerY, centerZ, upX, upY, upZ);
    }

    @Override
    public void orthoM(float[] m, int offset,
                       float left, float right, float bottom, float top,
                       float near, float far) {
        android.opengl.Matrix.orthoM(m, offset, left, right, bottom, top, near, far);
    }

    @Override
    public void frustumM(float[] m, int offset,
                         float left, float right, float bottom, float top,
                         float near, float far) {
        android.opengl.Matrix.frustumM(m, offset, left, right, bottom, top, near, far);
    }

    @Override
    public void multiplyMM(float[] result, int resultOffset,
                           float[] lhs, int lhsOffset,
                           float[] rhs, int rhsOffset) {
        android.opengl.Matrix.multiplyMM(result, resultOffset, lhs, lhsOffset, rhs, rhsOffset);
    }

    @Override
    public void multiplyMV(float[] resultVec, int resultVecOffset,
                           float[] lhsMat, int lhsMatOffset,
                           float[] rhsVec, int rhsVecOffset) {
        android.opengl.Matrix.multiplyMV(resultVec, resultVecOffset, lhsMat, lhsMatOffset, rhsVec, rhsVecOffset);
    }

    @Override
    public void translateM(float[] m, int mOffset,
                           float x, float y, float z) {
        android.opengl.Matrix.translateM(m, mOffset, x, y, z);
    }

    @Override
    public void rotateM(float[] m, int mOffset,
                        float a, float x, float y, float z) {
        android.opengl.Matrix.rotateM(m, mOffset, a, x, y, z);
    }

    @Override
    public void scaleM(float[] m, int mOffset,
                       float x, float y, float z) {
        android.opengl.Matrix.scaleM(m, mOffset, x, y, z);
    }

    @Override
    public void setIdentityM(float[] sm, int smOffset) {
        android.opengl.Matrix.setIdentityM(sm, smOffset);
    }

    @Override
    public boolean invertM(float[] mInv, int mInvOffset,
                           float[] m, int mOffset) {
        return android.opengl.Matrix.invertM(mInv, mInvOffset, m, mOffset);
    }

    @Override
    public void transposeM(float[] mTrans, int mTransOffset,
                           float[] m, int mOffset) {
        android.opengl.Matrix.transposeM(mTrans, mTransOffset, m, mOffset);
    }

    @Override
    public void glClearColor(float r, float g, float b, float a) {
        GLES30.glClearColor(r, g, b, a);
    }
}
