package com.seal.gl_engine;

import static android.opengl.GLES20.glViewport;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.seal.gl_engine.utils.Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    private final CoreRenderer coreRenderer;

    public OpenGLRenderer(float width, float height, Engine engine) {
        coreRenderer = new CoreRenderer(width, height, engine);

    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        coreRenderer.onSurfaceCreated();

    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Log.e("surface changed", String.valueOf(Utils.x));
    }


    @Override
    public void onDrawFrame(GL10 arg0) {
        coreRenderer.draw();
    }
}
