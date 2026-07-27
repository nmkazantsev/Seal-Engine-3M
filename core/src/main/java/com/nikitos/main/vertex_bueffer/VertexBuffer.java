package com.nikitos.main.vertex_bueffer;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.VRAMobject;
import com.nikitos.platformBridge.VertexBridge;


public class VertexBuffer extends VRAMobject {
    private int vao;
    private final int[] vbo;

    private final int vboNum;
    private final VertexBridge vertexBridge;

    private boolean dynamicDraw = false;
    private boolean allocated;

    public VertexBuffer(int vboNum, GamePageClass creator) {
        this(vboNum, creator, true);
    }

    protected VertexBuffer(
            int vboNum,
            GamePageClass creator,
            boolean registerForLifecycle
    ) {
        super(creator, registerForLifecycle);
        this.vertexBridge = CoreRenderer.engine.getPlatformBridge().getVertexBridge();
        this.vboNum = vboNum;
        vbo = new int[vboNum];
        allocate();
    }

    protected final void allocate() {
        vertexBridge.glGenBuffers(vboNum, vbo, 0);

        int[] x = new int[1];
        vertexBridge.glGenVertexArrays(1, x, 0);
        vao = x[0];
        allocated = true;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
    }

    public boolean getDynamicDraw() {
        return dynamicDraw;
    }

    public void bindVbo(int vboInd) {
        vertexBridge.glBindBuffer(glc.GL_ARRAY_BUFFER(), vbo[vboInd]);
    }

    public void bindDefaultVbo() {
        vertexBridge.glBindBuffer(glc.GL_ARRAY_BUFFER(), 0);
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
        if (!allocated) {
            return;
        }
        vertexBridge.glDeleteBuffers(vboNum, vbo, 0);
        int[] x = {vao};
        vertexBridge.glDeleteVertexArrays(1, x, 0);
        allocated = false;
    }

    @Override
    public void reload() {
        allocate();
    }

}
