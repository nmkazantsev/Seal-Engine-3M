package com.nikitos.platform;

import com.nikitos.runtime.CapturedFrame;
import com.nikitos.runtime.FrameCaptureSource;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_READ_BUFFER;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_READ_FRAMEBUFFER_BINDING;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

final class DesktopFrameCaptureSource implements FrameCaptureSource {
    private long window = NULL;

    void attachWindow(long window) {
        this.window = window;
    }

    @Override
    public boolean isAvailable() {
        return window != NULL;
    }

    @Override
    public CapturedFrame capture() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "Desktop framebuffer capture requires an attached GLFW window"
            );
        }

        int width;
        int height;
        try (MemoryStack stack = stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            width = widthBuffer.get(0);
            height = heightBuffer.get(0);
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalStateException(
                    "Cannot capture an empty framebuffer: " + width + "x" + height
            );
        }

        int byteCount = checkedRgbaByteCount(width, height);
        ByteBuffer bottomLeftRgba = BufferUtils.createByteBuffer(byteCount);

        int previousReadFramebuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        try {
            int previousReadBuffer = glGetInteger(GL_READ_BUFFER);
            int previousPackAlignment = glGetInteger(GL_PACK_ALIGNMENT);
            try {
                glReadBuffer(GL_BACK);
                glPixelStorei(GL_PACK_ALIGNMENT, 1);
                glReadPixels(
                        0,
                        0,
                        width,
                        height,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        bottomLeftRgba
                );
            } finally {
                glPixelStorei(GL_PACK_ALIGNMENT, previousPackAlignment);
                glReadBuffer(previousReadBuffer);
            }
        } finally {
            glBindFramebuffer(GL_READ_FRAMEBUFFER, previousReadFramebuffer);
        }

        return new CapturedFrame(
                width,
                height,
                toTopLeftRgba(bottomLeftRgba, width, height)
        );
    }

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

    static int checkedRgbaByteCount(int width, int height) {
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
}
