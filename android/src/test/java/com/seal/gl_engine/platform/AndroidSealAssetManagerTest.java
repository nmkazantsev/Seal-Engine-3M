package com.seal.gl_engine.platform;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class AndroidSealAssetManagerTest {
    @Test
    public void readsUtf8WithoutDependingOnAndroidApiLevel() throws Exception {
        String shader = "#version 300 es\n// Привет";

        assertEquals(shader, AndroidSealAssetManager.readUtf8(
                new ByteArrayInputStream(shader.getBytes(StandardCharsets.UTF_8))));
    }
}
