package com.nikitos.platformBridge;

import com.nikitos.CoreRenderer;

public abstract class VertexBridge {
    private final GeneralPlatformBridge gl;
    public VertexBridge(){
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
    }
    // Буферы
    public abstract void glGenBuffers(int n, int[] buffers, int offset);
    //duplicate code for usability
    public void glBindBuffer(int target, int buffer){
        gl.glBindBuffer(target, buffer);
    }
    public abstract void glDeleteBuffers(int n, int[] buffers, int offset);

    // Vertex Array Objects
    public abstract void glGenVertexArrays(int n, int[] arrays, int offset);
    public abstract void glBindVertexArray(int array);
    public abstract void glDeleteVertexArrays(int n, int[] arrays, int offset);
}
