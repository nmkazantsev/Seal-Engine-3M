package com.nikitos.main.shaders;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PBRShaderResourceTest {
    @Test
    void desktopAndAndroidVariantsShareTheMaterialInterface() throws IOException {
        String desktopVertex = read("shaders/desktop/pbr/vertex.glsl");
        String androidVertex = read("shaders/android/pbr/vertex.glsl");
        String desktopFragment = read("shaders/desktop/pbr/fragment.glsl");
        String androidFragment = read("shaders/android/pbr/fragment.glsl");

        assertEquals(uniforms(desktopVertex), uniforms(androidVertex));
        assertEquals(uniforms(desktopFragment), uniforms(androidFragment));
        assertTrue(desktopFragment.contains("metallicMap"));
        assertTrue(desktopFragment.contains("roughnessMap"));
        assertTrue(desktopFragment.contains("aoMap"));
        assertTrue(desktopFragment.contains("normalMap"));
    }

    private static String read(String resource) throws IOException {
        try (InputStream input = PBRShaderResourceTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static List<String> uniforms(String shader) {
        return Arrays.stream(shader.split("\\R"))
                .map(String::trim)
                .filter(line -> line.startsWith("uniform "))
                .sorted()
                .toList();
    }
}
