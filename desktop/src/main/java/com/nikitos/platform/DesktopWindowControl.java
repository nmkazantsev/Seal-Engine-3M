package com.nikitos.platform;

import java.util.Objects;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSize;

final class DesktopWindowControl {
    private final NativeWindowOperations nativeWindow;
    private final Thread ownerThread;
    private long window;

    DesktopWindowControl() {
        this(new GlfwNativeWindowOperations(), Thread.currentThread());
    }

    DesktopWindowControl(
            NativeWindowOperations nativeWindow,
            Thread ownerThread
    ) {
        this.nativeWindow = Objects.requireNonNull(nativeWindow);
        this.ownerThread = Objects.requireNonNull(ownerThread);
    }

    void attachWindow(long window) {
        requireOwnerThread();
        if (window == 0L) {
            throw new IllegalArgumentException(
                    "Cannot attach a null GLFW window"
            );
        }
        if (this.window != 0L) {
            throw new IllegalStateException(
                    "A GLFW window is already attached"
            );
        }
        this.window = window;
    }

    void detachWindow() {
        requireOwnerThread();
        window = 0L;
    }

    void requestStop() {
        nativeWindow.setWindowShouldClose(requireActiveWindow(), true);
    }

    void requestWindowSize(int width, int height) {
        long activeWindow = requireActiveWindow();
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Window width and height must be positive"
            );
        }
        nativeWindow.setWindowSize(activeWindow, width, height);
    }

    void requireActiveOwnerThread() {
        requireActiveWindow();
    }

    private long requireActiveWindow() {
        requireOwnerThread();
        if (window == 0L) {
            throw new IllegalStateException(
                    "Desktop window control is not active"
            );
        }
        return window;
    }

    private void requireOwnerThread() {
        if (Thread.currentThread() != ownerThread) {
            throw new IllegalStateException(
                    "Desktop window control must run on its owner render thread"
            );
        }
    }

    interface NativeWindowOperations {
        void setWindowShouldClose(long window, boolean shouldClose);

        void setWindowSize(long window, int width, int height);
    }

    private static final class GlfwNativeWindowOperations
            implements NativeWindowOperations {
        @Override
        public void setWindowShouldClose(long window, boolean shouldClose) {
            glfwSetWindowShouldClose(window, shouldClose);
        }

        @Override
        public void setWindowSize(long window, int width, int height) {
            glfwSetWindowSize(window, width, height);
        }
    }
}
