package com.nikitos.platform;

import com.nikitos.platformBridge.VertexBridge;
import org.lwjgl.opengl.GL30;
public class VertexBridgeDesktop extends VertexBridge {
    @Override
    public void glGenBuffers(int n, int[] buffers, int offset) {
        for (int i = 0; i < n; i++) {
            buffers[offset + i] = GL30.glGenBuffers();
        }
    }
    @Override
    public void glDeleteBuffers(int n, int[] buffers, int offset) {
        for (int i = 0; i < n; i++) {
            GL30.glDeleteBuffers(buffers[offset + i]);
        }
    }

    @Override
    public void glGenVertexArrays(int n, int[] arrays, int offset) {
        for (int i = 0; i < n; i++) {
            arrays[offset + i] = GL30.glGenVertexArrays();
        }
    }

    @Override
    public void glBindVertexArray(int array) {
        GL30.glBindVertexArray(array);
    }

    @Override
    public void glDeleteVertexArrays(int n, int[] arrays, int offset) {
        for (int i = 0; i < n; i++) {
            GL30.glDeleteVertexArrays(arrays[offset + i]);
        }
    }
}
