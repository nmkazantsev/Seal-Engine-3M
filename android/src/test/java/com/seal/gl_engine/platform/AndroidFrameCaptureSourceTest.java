package com.seal.gl_engine.platform;

import android.opengl.GLES30;
import com.nikitos.runtime.CapturedFrame;
import com.nikitos.runtime.FrameCaptureSource;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AndroidFrameCaptureSourceTest {

    @Test
    public void captureIsLazyAndReturnsDetachedTopLeftRgba() {
        FakeGl gl = new FakeGl(new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16
        });
        AndroidFrameCaptureSource source = readySource(gl, 2, 2);

        assertTrue(source.isAvailable());
        assertEquals(0, gl.readCount);

        CapturedFrame frame = source.capture();

        assertEquals(2, frame.getWidth());
        assertEquals(2, frame.getHeight());
        assertArrayEquals(
                new byte[]{
                        9, 10, 11, 12, 13, 14, 15, 16,
                        1, 2, 3, 4, 5, 6, 7, 8
                },
                frame.getRgba()
        );
        assertEquals(1, gl.readCount);
        assertEquals(0, gl.framebufferDuringRead);
        assertEquals(1, gl.packAlignmentDuringRead);
        assertEquals(17, gl.readFramebuffer);
        assertEquals(8, gl.packAlignment);
    }

    @Test
    public void captureRestoresFramebufferAndPackAlignmentWhenReadbackFails() {
        FakeGl gl = new FakeGl(new byte[4]);
        RuntimeException failure = new RuntimeException("readback failed");
        gl.readFailure = failure;
        AndroidFrameCaptureSource source = readySource(gl, 1, 1);

        try {
            source.capture();
            fail("Expected readback failure");
        } catch (RuntimeException thrown) {
            assertSame(failure, thrown);
        }

        assertEquals(17, gl.readFramebuffer);
        assertEquals(8, gl.packAlignment);
    }

    @Test
    public void captureRejectsMissingOrNonCurrentContextWithoutReadback() {
        FakeGl gl = new FakeGl(new byte[4]);
        AndroidFrameCaptureSource source = new AndroidFrameCaptureSource(gl);

        assertIllegalState(source::capture);
        assertEquals(0, gl.readCount);

        source.onSurfaceCreated();
        source.onSurfaceChanged(1, 1);
        gl.contextCurrent = false;

        assertFalse(source.isAvailable());
        assertIllegalState(source::capture);
        assertEquals(0, gl.readCount);
    }

    @Test
    public void captureRejectsWrongThreadWithoutReadback() throws InterruptedException {
        FakeGl gl = new FakeGl(new byte[4]);
        AndroidFrameCaptureSource source = readySource(gl, 1, 1);
        AtomicReference<Throwable> thrown = new AtomicReference<>();
        Thread otherThread = new Thread(
                () -> {
                    try {
                        source.capture();
                    } catch (Throwable failure) {
                        thrown.set(failure);
                    }
                },
                "not-the-gl-thread"
        );

        otherThread.start();
        otherThread.join();

        assertTrue(thrown.get() instanceof IllegalStateException);
        assertEquals(0, gl.readCount);
    }

    @Test
    public void captureRejectsEmptyAndOverflowingSurfacesBeforeGlStateChanges() {
        FakeGl gl = new FakeGl(new byte[0]);
        AndroidFrameCaptureSource source = new AndroidFrameCaptureSource(gl);
        source.onSurfaceCreated();

        assertIllegalState(source::capture);

        source.onSurfaceChanged(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertIllegalState(source::capture);

        assertEquals(0, gl.stateQueryCount);
        assertEquals(0, gl.readCount);
    }

    @Test
    public void surfaceRecreationInvalidatesOldDimensionsUntilNextResize() {
        FakeGl gl = new FakeGl(new byte[8]);
        AndroidFrameCaptureSource source = readySource(gl, 2, 1);

        source.capture();
        source.onSurfaceCreated();

        assertFalse(source.isAvailable());
        assertIllegalState(source::capture);

        source.onSurfaceChanged(1, 2);
        CapturedFrame recreated = source.capture();

        assertEquals(1, recreated.getWidth());
        assertEquals(2, recreated.getHeight());
        assertEquals(2, gl.readCount);
    }

    @Test
    public void androidBridgeExposesOneStableAndroidCaptureSource() {
        AndroidBridge bridge = new AndroidBridge();

        FrameCaptureSource first = bridge.getFrameCaptureSource();
        FrameCaptureSource second = bridge.getFrameCaptureSource();

        assertTrue(first instanceof AndroidFrameCaptureSource);
        assertSame(first, second);
    }

    private static AndroidFrameCaptureSource readySource(
            FakeGl gl,
            int width,
            int height
    ) {
        AndroidFrameCaptureSource source = new AndroidFrameCaptureSource(gl);
        source.onSurfaceCreated();
        source.onSurfaceChanged(width, height);
        return source;
    }

    private static void assertIllegalState(Runnable operation) {
        try {
            operation.run();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // Expected.
        }
    }

    private static final class FakeGl
            implements AndroidFrameCaptureSource.GlApi {
        private final byte[] pixels;
        private boolean contextCurrent = true;
        private int readFramebuffer = 17;
        private int packAlignment = 8;
        private int framebufferDuringRead = -1;
        private int packAlignmentDuringRead = -1;
        private int stateQueryCount;
        private int readCount;
        private RuntimeException readFailure;

        private FakeGl(byte[] pixels) {
            this.pixels = pixels;
        }

        @Override
        public boolean isContextCurrent() {
            return contextCurrent;
        }

        @Override
        public int getInteger(int name) {
            stateQueryCount++;
            if (name == GLES30.GL_READ_FRAMEBUFFER_BINDING) {
                return readFramebuffer;
            }
            if (name == GLES30.GL_PACK_ALIGNMENT) {
                return packAlignment;
            }
            throw new AssertionError("Unexpected GL state query: " + name);
        }

        @Override
        public void bindFramebuffer(int target, int framebuffer) {
            assertEquals(GLES30.GL_READ_FRAMEBUFFER, target);
            readFramebuffer = framebuffer;
        }

        @Override
        public void pixelStore(int name, int value) {
            assertEquals(GLES30.GL_PACK_ALIGNMENT, name);
            packAlignment = value;
        }

        @Override
        public void readPixels(
                int x,
                int y,
                int width,
                int height,
                int format,
                int type,
                ByteBuffer target
        ) {
            readCount++;
            framebufferDuringRead = readFramebuffer;
            packAlignmentDuringRead = packAlignment;
            assertEquals(0, x);
            assertEquals(0, y);
            assertEquals(GLES30.GL_RGBA, format);
            assertEquals(GLES30.GL_UNSIGNED_BYTE, type);
            if (readFailure != null) {
                throw readFailure;
            }
            assertEquals(pixels.length, width * height * 4);
            for (int index = 0; index < pixels.length; index++) {
                target.put(index, pixels[index]);
            }
        }
    }
}
