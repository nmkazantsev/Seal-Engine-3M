package com.seal.gl_engine;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.seal.gl_engine.platform.AndroidFrameCaptureSource;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    private CoreRenderer coreRenderer;
    private final Engine engine;
    private final AndroidFrameCaptureSource frameCaptureSource;

    public OpenGLRenderer(float width, float height, Engine engine) {
        this(width, height, engine, null);
    }

    public OpenGLRenderer(
            float width,
            float height,
            Engine engine,
            AndroidFrameCaptureSource frameCaptureSource
    ) {
        coreRenderer = new CoreRenderer(width, height, engine);
        this.engine = engine;
        this.frameCaptureSource = frameCaptureSource;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        coreRenderer.onSurfaceCreated();
        if (frameCaptureSource != null) {
            // Новый GL-контекст делает старые размеры источника снимков недействительными.
            frameCaptureSource.onSurfaceCreated();
        }
        Log.i("engine", "surface created");
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        if (frameCaptureSource != null) {
            // Размер берётся из callback GLSurfaceView, а не из метрик Activity.
            frameCaptureSource.onSurfaceChanged(width, height);
        }
        Log.i("engine", "\n=========\n\nsurface changed, resolution " + String.valueOf(width) + " " + String.valueOf(height));
        coreRenderer = new CoreRenderer(width, height, engine);
        coreRenderer.onSurfaceCreated();
    }


    @Override
    public void onDrawFrame(GL10 arg0) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        coreRenderer.draw();
    }
}
