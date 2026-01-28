package com.nikitos.main.vertex_bueffer;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.VRAMobject;
import com.nikitos.platformBridge.VertexBridge;


public class VertexBuffer extends VRAMobject {
    private final int vao;
    private final int[] vbo;

    private final int vboNum;
    private final VertexBridge vertexBridge;

    public VertexBuffer(int vboNum, GamePageClass creator) {
        super(creator);
        this.vertexBridge = CoreRenderer.engine.getPlatformBridge().getVertexBridge();
        this.vboNum = vboNum;
        vbo = new int[vboNum];

        vertexBridge.glGenBuffers(vboNum, vbo, 0);

        int[] x = new int[1];
        vertexBridge.glGenVertexArrays(1, x, 0);
        vao = x[0];
    }

    public void bindVbo(int vboInd) {
        vertexBridge.glBindBuffer(vertexBridge.GL_ARRAY_BUFFER(), vbo[vboInd]);
    }

    public void bindDefaultVbo() {
        vertexBridge.glBindBuffer(vertexBridge.GL_ARRAY_BUFFER(), 0);
    }

    public void bindVao() {
        vertexBridge.glBindVertexArray(vao);
    }

    public void bindDefaultVao() {
        vertexBridge.glBindVertexArray(0);
    }

    public int getVboAdress(int vboIndex) {
        return vbo[vboIndex];
    }

    public void delete() {
        vertexBridge.glDeleteBuffers(vboNum, vbo, 0);
        int[] x = {vao};
        vertexBridge.glDeleteVertexArrays(1, x, 0);
    }

    @Override
    public void reload() {
        // пустая реализация
    }

}
