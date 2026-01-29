package com.nikitos.main.shaders;


import com.nikitos.CoreRenderer;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Adaptor {
    private static final ArrayList<ShaderData> shaderData = new ArrayList<>();
    protected int programId;

    protected final GLConstBridge glConstBridge;
    protected final GeneralPlatformBridge gl;

    public Adaptor(){
        this.gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        this.glConstBridge = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
    }

    protected static void addLightAdaptor(ShaderData shaderData) {
        Adaptor.shaderData.add(shaderData);
    }

    public static void updateShaderDataLocations() {
        Iterator<ShaderData> iterator = shaderData.iterator();
        while (iterator.hasNext()) {
            ShaderData e = iterator.next();
            if (e == null) {
                iterator.remove();
            } else if (e.getCreatorClass() != null && !(e.getCreatorClass() == CoreRenderer.engine.getPageClass())) {
                e.delete();
                iterator.remove();
            } else {
                e.getLocations(Shader.getActiveShader().getAdaptor().getProgramId());
            }
        }
    }

    public static void forwardData() {
        for (ShaderData e : shaderData) {
            e.forwardData();
        }
    }


    public void setProgramId(int id) {
        this.programId = id;
    }

    public int getProgramId() {
        return this.programId;
    }

    public abstract int bindData(Face[] faces);

    public abstract int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded);

    public abstract void bindDataLine(PVector a, PVector b, PVector color);

    public abstract void updateLocations();

    public abstract int getTransformMatrixLocation();

    public abstract int getCameraLocation();

    public abstract int getProjectionLocation();

    public abstract int getTextureLocation();

    public abstract int getNormalTextureLocation();

    public abstract int getNormalMapEnableLocation();

    public abstract int getCameraPosLlocation();
}
