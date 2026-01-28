package com.seal.gl_engine.platform;

import android.opengl.GLES30;
import com.nikitos.platformBridge.VertexBridge;

public class VertexBridgeAndroid extends VertexBridge {
    @Override
    public void glGenBuffers(int n, int[] buffers, int offset) {
        GLES30.glGenBuffers(n, buffers, offset);
    }

    @Override
    public void glBindBuffer(int target, int buffer) {
        GLES30.glBindBuffer(target, buffer);
    }

    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        GLES30.glDeleteBuffers(n, buffers, offset);
    }

    @Override
    public void glGenVertexArrays(int n, int[] arrays, int offset) {
        GLES30.glGenVertexArrays(n, arrays, offset);
    }

    @Override
    public void glBindVertexArray(int array) {
        GLES30.glBindVertexArray(array);
    }

    @Override
    public void glDeleteVertexArrays(int n, int[] arrays, int offset) {
        GLES30.glDeleteVertexArrays(n, arrays, offset);
    }
}
