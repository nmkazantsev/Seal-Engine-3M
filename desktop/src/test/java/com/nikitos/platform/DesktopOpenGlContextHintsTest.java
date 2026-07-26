package com.nikitos.platform;

import com.nikitos.platformBridge.LauncherParams;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;

class DesktopOpenGlContextHintsTest {

    @Test
    void defaultRequestEmitsNoContextHints() {
        List<Hint> hints = new ArrayList<>();

        DesktopOpenGlContextHints.apply(
                new LauncherParams(),
                (name, value) -> hints.add(new Hint(name, value))
        );

        assertTrue(hints.isEmpty());
    }

    @Test
    void enabledRequestEmitsOnlyOpenGl33CoreHintsInOrder() {
        List<Hint> hints = new ArrayList<>();

        DesktopOpenGlContextHints.apply(
                new LauncherParams().setDesktopOpenGl33CoreContext(true),
                (name, value) -> hints.add(new Hint(name, value))
        );

        assertEquals(List.of(
                new Hint(GLFW_CONTEXT_VERSION_MAJOR, 3),
                new Hint(GLFW_CONTEXT_VERSION_MINOR, 3),
                new Hint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        ), hints);
    }

    private record Hint(int name, int value) {
    }
}
