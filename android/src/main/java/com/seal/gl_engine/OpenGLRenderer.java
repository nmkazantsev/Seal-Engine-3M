package com.seal.gl_engine;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    private CoreRenderer coreRenderer;
    private final Engine engine;

    public OpenGLRenderer(float width, float height, Engine engine) {
        coreRenderer = new CoreRenderer(width, height, engine);
        this.engine = engine;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        coreRenderer.onSurfaceCreated();

    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        coreRenderer = new CoreRenderer(width, height, engine);
        coreRenderer.onSurfaceCreated();
        Log.i("surface_changed", String.valueOf(width) + " " + String.valueOf(height));
    }


    @Override
    public void onDrawFrame(GL10 arg0) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        coreRenderer.draw();
    }
}
