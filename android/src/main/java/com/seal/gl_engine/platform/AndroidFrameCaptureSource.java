package com.seal.gl_engine.platform;

import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLES30;
import com.nikitos.runtime.CapturedFrame;
import com.nikitos.runtime.FrameCaptureSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Захватывает экранный framebuffer Android прямо в GL-потоке GLSurfaceView.
 * Перед чтением временно выбирает back buffer, затем возвращает затронутое GL-состояние.
 */
public final class AndroidFrameCaptureSource implements FrameCaptureSource {
    private volatile Thread glThread;
    private volatile int width;
    private volatile int height;

    /** Запоминает поток нового GL-контекста и сбрасывает размер старой поверхности. */
    public void onSurfaceCreated() {
        glThread = Thread.currentThread();
        width = 0;
        height = 0;
    }

    /** Сохраняет размер, с которым следующий снимок прочитает framebuffer. */
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
                && isContextCurrent();
    }

    @Override
    public CapturedFrame capture() {
        requireGlThread();
        if (!isContextCurrent()) {
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

        int previousReadFramebuffer = getInteger(GLES30.GL_READ_FRAMEBUFFER_BINDING);
        int previousPackAlignment = getInteger(GLES30.GL_PACK_ALIGNMENT);
        int previousPackRowLength = getInteger(GLES30.GL_PACK_ROW_LENGTH);
        int previousPackSkipRows = getInteger(GLES30.GL_PACK_SKIP_ROWS);
        int previousPackSkipPixels = getInteger(GLES30.GL_PACK_SKIP_PIXELS);
        int previousPixelPackBuffer = getInteger(GLES30.GL_PIXEL_PACK_BUFFER_BINDING);

        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, 0);
        int previousReadBuffer = getInteger(GLES30.GL_READ_BUFFER);
        try {
            GLES30.glReadBuffer(GLES30.GL_BACK);
            GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, 1);
            GLES30.glPixelStorei(GLES30.GL_PACK_ROW_LENGTH, 0);
            GLES30.glPixelStorei(GLES30.GL_PACK_SKIP_ROWS, 0);
            GLES30.glPixelStorei(GLES30.GL_PACK_SKIP_PIXELS, 0);
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0);
            GLES30.glReadPixels(
                    0, 0, captureWidth, captureHeight,
                    GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, bottomLeftRgba
            );
        } finally {
            // Возврат состояния важнее удобства тестирования: после снимка кадр продолжает жить.
            GLES30.glReadBuffer(previousReadBuffer);
            GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, previousPackAlignment);
            GLES30.glPixelStorei(GLES30.GL_PACK_ROW_LENGTH, previousPackRowLength);
            GLES30.glPixelStorei(GLES30.GL_PACK_SKIP_ROWS, previousPackSkipRows);
            GLES30.glPixelStorei(GLES30.GL_PACK_SKIP_PIXELS, previousPackSkipPixels);
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, previousPixelPackBuffer);
            GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, previousReadFramebuffer);
        }

        return new CapturedFrame(
                captureWidth,
                captureHeight,
                toTopLeftRgba(bottomLeftRgba, captureWidth, captureHeight)
        );
    }

    static int checkedRgbaByteCount(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException(
                    "Cannot capture an empty framebuffer: " + width + "x" + height
            );
        }
        try {
            long byteCount = Math.multiplyExact(
                    Math.multiplyExact((long) width, (long) height), 4L
            );
            if (byteCount > Integer.MAX_VALUE) {
                throw new IllegalStateException("Framebuffer is too large to capture");
            }
            return (int) byteCount;
        } catch (ArithmeticException overflow) {
            throw new IllegalStateException(
                    "Framebuffer dimensions overflow the capture byte count", overflow
            );
        }
    }

    /** OpenGL возвращает строки снизу вверх; публичный контракт кадра — сверху вниз. */
    static byte[] toTopLeftRgba(ByteBuffer bottomLeftRgba, int width, int height) {
        int rowLength = Math.multiplyExact(width, 4);
        byte[] topLeftRgba = new byte[Math.multiplyExact(rowLength, height)];
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

    private static int getInteger(int name) {
        int[] value = new int[1];
        GLES30.glGetIntegerv(name, value, 0);
        return value[0];
    }

    private static boolean isContextCurrent() {
        EGLContext context = EGL14.eglGetCurrentContext();
        return context != null && context.getNativeHandle() != 0L;
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
}
