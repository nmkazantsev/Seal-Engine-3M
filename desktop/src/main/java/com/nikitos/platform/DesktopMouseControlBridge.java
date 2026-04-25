package com.nikitos.platform;

import com.nikitos.platformBridge.MouseControlBridge;

import static org.lwjgl.glfw.GLFW.*;

public class DesktopMouseControlBridge extends MouseControlBridge {
    private long window;
    private boolean mouseEnabled=true;

    public void attachWindow(long window) {
        this.window = window;
    }

    @Override
    public void disableMouseCursor() {
        if (window == 0) {
            return;
        }
        mouseEnabled=false;
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    @Override
    public void enableMouseCursor() {
        if (window == 0) {
            return;
        }
        mouseEnabled=true;
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    @Override
    public void setMousePosition(float x, float y) {
        if (window == 0) {
            throw new RuntimeException("window is not bind");
        }
        glfwSetCursorPos(window, x, y);
    }
}
