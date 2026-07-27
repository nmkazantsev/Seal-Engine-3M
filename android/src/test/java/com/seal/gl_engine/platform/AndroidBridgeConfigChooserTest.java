package com.seal.gl_engine.platform;

import static org.junit.Assert.assertEquals;

import android.opengl.EGLExt;

import org.junit.Test;

import javax.microedition.khronos.egl.EGL10;

public class AndroidBridgeConfigChooserTest {
    @Test
    public void requestsAnOpenGlEs3CompatibleConfig() {
        int[] attributes = AndroidBridge.MyConfigChooser.attributes(4);

        assertEquals(
                EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                valueOf(attributes, EGL10.EGL_RENDERABLE_TYPE)
        );
        assertEquals(4, valueOf(attributes, EGL10.EGL_SAMPLES));
        assertEquals(1, valueOf(attributes, EGL10.EGL_SAMPLE_BUFFERS));

        int[] fallback = AndroidBridge.MyConfigChooser.attributes(0);
        assertEquals(
                EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                valueOf(fallback, EGL10.EGL_RENDERABLE_TYPE)
        );
        assertEquals(0, valueOf(fallback, EGL10.EGL_SAMPLE_BUFFERS));
        assertEquals(0, valueOf(fallback, EGL10.EGL_SAMPLES));
    }

    private static int valueOf(int[] attributes, int name) {
        for (int index = 0; index < attributes.length - 1; index += 2) {
            if (attributes[index] == name) {
                return attributes[index + 1];
            }
        }
        throw new AssertionError("Missing EGL attribute " + name);
    }
}
