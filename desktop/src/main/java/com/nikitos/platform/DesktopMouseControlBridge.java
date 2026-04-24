package com.nikitos.platform;

import com.nikitos.platformBridge.MouseControlBridge;

import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_HIDDEN;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;

public class DesktopMouseControlBridge extends MouseControlBridge {
    private long window;

    public void attachWindow(long window) {
        this.window = window;
    }

    @Override
    public void hideMouseCursor() {
        if (window == 0) {
            return;
        }
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    @Override
    public void showMouseCursor() {
        if (window == 0) {
            return;
        }
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    @Override
    public void setMousePosition(float x, float y) {
        if (window == 0) {
            return;
        }
        glfwSetCursorPos(window, x, y);
    }
}
