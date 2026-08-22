package com.nikitos.main.shaders;

import com.nikitos.platformBridge.Platform;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShaderPathResolverTest {
    @Test
    void resolvesLogicalPathsForTheRuntimePlatform() {
        assertEquals("shaders/desktop/vertex_shader.glsl",
                Shader.resolveAssetPath("vertex_shader.glsl", Platform.DESKTOP));
        assertEquals("shaders/android/scene/effects/fragment.glsl",
                Shader.resolveAssetPath("scene/effects/fragment.glsl", Platform.MOBILE));
    }

    @Test
    void rejectsPathsOutsideTheLogicalShaderRoot() {
        for (String path : new String[]{
                "", " ", "/absolute.glsl", "C:/absolute.glsl", "scene\\shader.glsl",
                "../shader.glsl", "scene/../shader.glsl", "scene/./shader.glsl", "scene//shader.glsl",
                "shaders/desktop/shader.glsl", "shaders/android/shader.glsl",
                "desktop/shader.glsl", "android/shader.glsl"
        }) {
            assertThrows(IllegalArgumentException.class,
                    () -> Shader.resolveAssetPath(path, Platform.DESKTOP), path);
        }
        assertThrows(IllegalArgumentException.class,
                () -> Shader.resolveAssetPath(null, Platform.DESKTOP));
        assertThrows(IllegalArgumentException.class,
                () -> Shader.resolveAssetPath("shader.glsl", null));
    }

    @Test
    void androidAndDesktopShaderTreesHaveTheSameLogicalFiles() throws IOException {
        Path shaderRoot = Path.of("src/main/resources/shaders");
        Set<Path> android = relativeFiles(shaderRoot.resolve("android"));
        Set<Path> desktop = relativeFiles(shaderRoot.resolve("desktop"));

        assertFalse(android.isEmpty());
        assertEquals(android, desktop);
    }

    private static Set<Path> relativeFiles(Path root) throws IOException {
        try (var files = Files.walk(root)) {
            return files.filter(Files::isRegularFile)
                    .map(root::relativize)
                    .collect(Collectors.toSet());
        }
    }
}
