package com.seal.gl_engine;

import com.nikitos.Engine;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class OpenGLRendererLifecycleTest {

    @Test
    public void rendererIsCreatedOnlyByPositiveSurfaceCallbackAndDrawBeforeInitIsSafe() {
        new OpenGLRenderer(640, 480, null);
        RecordingFactory factory = new RecordingFactory();
        RecordingGl gl = new RecordingGl();
        OpenGLRenderer renderer = new OpenGLRenderer(
                null,
                null,
                factory,
                gl
        );

        assertEquals(0, factory.created.size());
        renderer.onDrawFrame(null);
        renderer.onSurfaceCreated(null, null);
        renderer.onSurfaceChanged(null, 0, 480);
        assertEquals(0, factory.created.size());
        assertEquals(0, gl.clearCount);

        Thread callbackThread = Thread.currentThread();
        renderer.onSurfaceChanged(null, 640, 480);

        assertEquals(1, factory.created.size());
        RecordingCore core = factory.created.get(0);
        assertEquals(640, factory.width);
        assertEquals(480, factory.height);
        assertSame(callbackThread, factory.creationThread);
        assertTrue(core.surfaceCreated);

        renderer.onDrawFrame(null);
        assertEquals(1, gl.clearCount);
        assertEquals(1, core.drawCount);
    }

    @Test
    public void eachPositiveSurfaceCallbackReplacesTheLocalRenderer() {
        RecordingFactory factory = new RecordingFactory();
        OpenGLRenderer renderer = new OpenGLRenderer(
                (Engine) null,
                null,
                factory,
                new RecordingGl()
        );

        renderer.onSurfaceChanged(null, 640, 480);
        RecordingCore first = factory.created.get(0);
        renderer.onSurfaceChanged(null, 800, 600);
        RecordingCore second = factory.created.get(1);
        renderer.onDrawFrame(null);

        assertFalse(first == second);
        assertEquals(0, first.drawCount);
        assertEquals(1, second.drawCount);
    }

    @Test
    public void recreatedSurfaceInvalidatesRendererUntilPositiveResize() {
        RecordingFactory factory = new RecordingFactory();
        RecordingGl gl = new RecordingGl();
        OpenGLRenderer renderer = new OpenGLRenderer(
                (Engine) null,
                null,
                factory,
                gl
        );
        renderer.onSurfaceChanged(null, 640, 480);
        RecordingCore oldCore = factory.created.get(0);

        renderer.onSurfaceCreated(null, null);
        renderer.onDrawFrame(null);

        assertEquals(0, oldCore.drawCount);
        assertEquals(0, gl.clearCount);
    }

    private static final class RecordingFactory
            implements OpenGLRenderer.CoreRendererFactory {
        private final List<RecordingCore> created = new ArrayList<>();
        private int width;
        private int height;
        private Thread creationThread;

        @Override
        public OpenGLRenderer.RenderCore create(
                int width,
                int height,
                Engine engine
        ) {
            this.width = width;
            this.height = height;
            creationThread = Thread.currentThread();
            RecordingCore core = new RecordingCore();
            created.add(core);
            return core;
        }
    }

    private static final class RecordingCore
            implements OpenGLRenderer.RenderCore {
        private boolean surfaceCreated;
        private int drawCount;

        @Override
        public void onSurfaceCreated() {
            surfaceCreated = true;
        }

        @Override
        public void draw() {
            drawCount++;
        }
    }

    private static final class RecordingGl implements OpenGLRenderer.GlApi {
        private int clearCount;

        @Override
        public void viewport(int width, int height) {
        }

        @Override
        public void clear() {
            clearCount++;
        }

        @Override
        public void log(String message) {
        }
    }
}
