package com.seal.gl_engine;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glViewport;
import static javax.microedition.khronos.opengles.GL10.GL_DEPTH_BUFFER_BIT;

import android.opengl.GLSurfaceView.Renderer;
import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.seal.gl_engine.platform.AndroidFrameCaptureSource;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements Renderer {

    private RenderCore coreRenderer;
    private final Engine engine;
    private final AndroidFrameCaptureSource frameCaptureSource;
    private final CoreRendererFactory coreRendererFactory;
    private final GlApi gl;

    public OpenGLRenderer(float width, float height, Engine engine) {
        this(width, height, engine, null);
    }

    public OpenGLRenderer(
            float width,
            float height,
            Engine engine,
            AndroidFrameCaptureSource frameCaptureSource
    ) {
        this(
                engine,
                frameCaptureSource,
                CoreRendererAdapter::new,
                new GlesApi()
        );
    }

    OpenGLRenderer(
            Engine engine,
            AndroidFrameCaptureSource frameCaptureSource,
            CoreRendererFactory coreRendererFactory,
            GlApi gl
    ) {
        this.engine = engine;
        this.frameCaptureSource = frameCaptureSource;
        this.coreRendererFactory = coreRendererFactory;
        this.gl = gl;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        coreRenderer = null;
        if (frameCaptureSource != null) {
            frameCaptureSource.onSurfaceCreated();
        }
        gl.log("surface created");
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        gl.viewport(width, height);
        if (frameCaptureSource != null) {
            frameCaptureSource.onSurfaceChanged(width, height);
        }
        gl.log("\n=========\n\nsurface changed, resolution " + width + " " + height);
        coreRenderer = coreRendererFactory.create(width, height, engine);
        coreRenderer.onSurfaceCreated();
    }


    @Override
    public void onDrawFrame(GL10 arg0) {
        RenderCore currentRenderer = coreRenderer;
        if (currentRenderer == null) {
            return;
        }
        gl.clear();
        currentRenderer.draw();
    }

    interface CoreRendererFactory {
        RenderCore create(int width, int height, Engine engine);
    }

    interface RenderCore {
        void onSurfaceCreated();

        void draw();
    }

    interface GlApi {
        void viewport(int width, int height);

        void clear();

        void log(String message);
    }

    private static final class CoreRendererAdapter implements RenderCore {
        private final CoreRenderer coreRenderer;

        private CoreRendererAdapter(int width, int height, Engine engine) {
            coreRenderer = new CoreRenderer(width, height, engine);
        }

        @Override
        public void onSurfaceCreated() {
            coreRenderer.onSurfaceCreated();
        }

        @Override
        public void draw() {
            coreRenderer.draw();
        }
    }

    private static final class GlesApi implements GlApi {
        @Override
        public void viewport(int width, int height) {
            glViewport(0, 0, width, height);
        }

        @Override
        public void clear() {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        @Override
        public void log(String message) {
            android.util.Log.i("engine", message);
        }
    }
}
