package com.nikitos.platform;

import com.nikitos.platformBridge.LauncherParams;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;

final class DesktopOpenGlContextHints {

    @FunctionalInterface
    interface Sink {
        void set(int name, int value);
    }

    private DesktopOpenGlContextHints() {
    }

    static void apply(LauncherParams launcherParams, Sink sink) {
        if (!launcherParams.isDesktopOpenGl33CoreContext()) {
            return;
        }

        sink.set(GLFW_CONTEXT_VERSION_MAJOR, 3);
        sink.set(GLFW_CONTEXT_VERSION_MINOR, 3);
        sink.set(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    }
}
