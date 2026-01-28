package com.nikitos.platformBridge;

public abstract class VertexBridge {
    // Буферы
    public abstract void glGenBuffers(int n, int[] buffers, int offset);
    public abstract void glBindBuffer(int target, int buffer);
    public abstract void glDeleteBuffers(int n, int[] buffers, int offset);

    // Vertex Array Objects
    public abstract void glGenVertexArrays(int n, int[] arrays, int offset);
    public abstract void glBindVertexArray(int array);
    public abstract void glDeleteVertexArrays(int n, int[] arrays, int offset);
}
