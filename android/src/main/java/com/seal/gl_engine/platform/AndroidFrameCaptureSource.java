package com.seal.gl_engine.platform;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import com.nikitos.runtime.CapturedFrame;
import com.nikitos.runtime.FrameCaptureSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class AndroidFrameCaptureSource implements FrameCaptureSource {
    private final GlApi gl;
    private volatile Thread glThread;
    private volatile int width;
    private volatile int height;

    AndroidFrameCaptureSource() {
        this(new Gles30Api());
    }

    AndroidFrameCaptureSource(GlApi gl) {
        this.gl = gl;
    }

    public void onSurfaceCreated() {
        glThread = Thread.currentThread();
        width = 0;
        height = 0;
    }

    public void onSurfaceChanged(int width, int height) {
        requireGlThread();
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean isAvailable() {
        return glThread == Thread.currentThread()
                && width > 0
                && height > 0
                && gl.isContextCurrent();
    }

    @Override
    public CapturedFrame capture() {
        requireGlThread();
        if (!gl.isContextCurrent()) {
            throw new IllegalStateException(
                    "Android framebuffer capture requires a current EGL context"
            );
        }

        int captureWidth = width;
        int captureHeight = height;
        int byteCount = checkedRgbaByteCount(captureWidth, captureHeight);
        ByteBuffer bottomLeftRgba = ByteBuffer
                .allocateDirect(byteCount)
                .order(ByteOrder.nativeOrder());

        int previousReadFramebuffer =
                gl.getInteger(GLES30.GL_READ_FRAMEBUFFER_BINDING);
        int previousPackAlignment =
                gl.getInteger(GLES30.GL_PACK_ALIGNMENT);
        int previousPackRowLength =
                gl.getInteger(GLES30.GL_PACK_ROW_LENGTH);
        int previousPackSkipRows =
                gl.getInteger(GLES30.GL_PACK_SKIP_ROWS);
        int previousPackSkipPixels =
                gl.getInteger(GLES30.GL_PACK_SKIP_PIXELS);
        int previousPixelPackBuffer =
                gl.getInteger(GLES30.GL_PIXEL_PACK_BUFFER_BINDING);

        int previousReadBuffer = GLES30.GL_NONE;
        boolean readBufferCaptured = false;
        Throwable failure = null;
        try {
            gl.bindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, 0);
            previousReadBuffer = gl.getInteger(GLES30.GL_READ_BUFFER);
            readBufferCaptured = true;
            gl.readBuffer(GLES30.GL_BACK);
            gl.pixelStore(GLES30.GL_PACK_ALIGNMENT, 1);
            gl.pixelStore(GLES30.GL_PACK_ROW_LENGTH, 0);
            gl.pixelStore(GLES30.GL_PACK_SKIP_ROWS, 0);
            gl.pixelStore(GLES30.GL_PACK_SKIP_PIXELS, 0);
            gl.bindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
            gl.readPixels(
                    0,
                    0,
                    captureWidth,
                    captureHeight,
                    GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE,
                    bottomLeftRgba
            );
        } catch (RuntimeException | Error operationFailure) {
            failure = operationFailure;
        }

        if (readBufferCaptured) {
            int capturedReadBuffer = previousReadBuffer;
            failure = attemptRestore(
                    failure,
                    () -> gl.readBuffer(capturedReadBuffer)
            );
        }
        failure = attemptRestore(
                failure,
                () -> gl.pixelStore(
                        GLES30.GL_PACK_ALIGNMENT,
                        previousPackAlignment
                )
        );
        failure = attemptRestore(
                failure,
                () -> gl.pixelStore(
                        GLES30.GL_PACK_ROW_LENGTH,
                        previousPackRowLength
                )
        );
        failure = attemptRestore(
                failure,
                () -> gl.pixelStore(
                        GLES30.GL_PACK_SKIP_ROWS,
                        previousPackSkipRows
                )
        );
        failure = attemptRestore(
                failure,
                () -> gl.pixelStore(
                        GLES30.GL_PACK_SKIP_PIXELS,
                        previousPackSkipPixels
                )
        );
        failure = attemptRestore(
                failure,
                () -> gl.bindBuffer(
                        GLES30.GL_PIXEL_PACK_BUFFER,
                        previousPixelPackBuffer
                )
        );
        failure = attemptRestore(
                failure,
                () -> gl.bindFramebuffer(
                        GLES30.GL_READ_FRAMEBUFFER,
                        previousReadFramebuffer
                )
        );
        if (failure != null) {
            rethrow(failure);
        }

        return new CapturedFrame(
                captureWidth,
                captureHeight,
                toTopLeftRgba(
                        bottomLeftRgba,
                        captureWidth,
                        captureHeight
                )
        );
    }

    static int checkedRgbaByteCount(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException(
                    "Cannot capture an empty framebuffer: "
                            + width + "x" + height
            );
        }
        try {
            long byteCount = Math.multiplyExact(
                    Math.multiplyExact((long) width, (long) height),
                    4L
            );
            if (byteCount > Integer.MAX_VALUE) {
                throw new IllegalStateException(
                        "Framebuffer is too large to capture"
                );
            }
            return (int) byteCount;
        } catch (ArithmeticException overflow) {
            throw new IllegalStateException(
                    "Framebuffer dimensions overflow the capture byte count",
                    overflow
            );
        }
    }

    static byte[] toTopLeftRgba(
            ByteBuffer bottomLeftRgba,
            int width,
            int height
    ) {
        int rowLength = Math.multiplyExact(width, 4);
        byte[] topLeftRgba =
                new byte[Math.multiplyExact(rowLength, height)];
        for (int topRow = 0; topRow < height; topRow++) {
            int sourceOffset = (height - 1 - topRow) * rowLength;
            int targetOffset = topRow * rowLength;
            for (int index = 0; index < rowLength; index++) {
                topLeftRgba[targetOffset + index] =
                        bottomLeftRgba.get(sourceOffset + index);
            }
        }
        return topLeftRgba;
    }

    private static Throwable attemptRestore(
            Throwable existingFailure,
            Runnable restore
    ) {
        try {
            restore.run();
        } catch (RuntimeException | Error restoreFailure) {
            if (existingFailure == null) {
                return restoreFailure;
            }
            if (existingFailure != restoreFailure) {
                existingFailure.addSuppressed(restoreFailure);
            }
        }
        return existingFailure;
    }

    private static void rethrow(Throwable failure) {
        if (failure instanceof RuntimeException) {
            throw (RuntimeException) failure;
        }
        throw (Error) failure;
    }

    private void requireGlThread() {
        if (glThread == null) {
            throw new IllegalStateException(
                    "Android framebuffer capture requires a created surface"
            );
        }
        if (glThread != Thread.currentThread()) {
            throw new IllegalStateException(
                    "Android framebuffer capture must run on the GLSurfaceView GL thread"
            );
        }
    }

    interface GlApi {
        boolean isContextCurrent();

        int getInteger(int name);

        void bindFramebuffer(int target, int framebuffer);

        void readBuffer(int buffer);

        void pixelStore(int name, int value);

        void bindBuffer(int target, int buffer);

        void readPixels(
                int x,
                int y,
                int width,
                int height,
                int format,
                int type,
                ByteBuffer target
        );
    }

    private static final class Gles30Api implements GlApi {
        @Override
        public boolean isContextCurrent() {
            EGLContext context = EGL14.eglGetCurrentContext();
            return context != null && context.getNativeHandle() != 0L;
        }

        @Override
        public int getInteger(int name) {
            int[] value = new int[1];
            GLES30.glGetIntegerv(name, value, 0);
            return value[0];
        }

        @Override
        public void bindFramebuffer(int target, int framebuffer) {
            GLES30.glBindFramebuffer(target, framebuffer);
        }

        @Override
        public void readBuffer(int buffer) {
            GLES30.glReadBuffer(buffer);
        }

        @Override
        public void pixelStore(int name, int value) {
            GLES30.glPixelStorei(name, value);
        }

        @Override
        public void bindBuffer(int target, int buffer) {
            GLES30.glBindBuffer(target, buffer);
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
            GLES30.glReadPixels(
                    x,
                    y,
                    width,
                    height,
                    format,
                    type,
                    target
            );
        }
    }
}
