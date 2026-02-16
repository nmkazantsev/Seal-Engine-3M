package com.nikitos.main.shaders.default_adaptors;


import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.nikitos.main.shaders.Adaptor;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;


public class SkyBoxShaderAdaptor extends Adaptor {

    private int aPositionLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;

    private final static int POSITION_COUNT = 3;
    private static final int STRIDE = (POSITION_COUNT) * 4;
    private final Engine engine;
    private final PlatformBridge pf;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;

    public SkyBoxShaderAdaptor() {
        engine = CoreRenderer.engine;
        pf = engine.getPlatformBridge();
        gl = pf.getGeneralPlatformBridge();
        glc = pf.getGLConstBridge();
    }

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[12 * 3 * 3];
        int vertexesNumber = 0;
        for (int i = 0; i < 12; i++) {
            System.arraycopy(faces[i].getArrayRepresentationVertexes(), 0, vertices, i * 9, 9);
            vertexesNumber++;
        }
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        // координаты вершин
        vertexData.position(0);
        gl.glVertexAttribPointer(aPositionLocation, POSITION_COUNT, glc.GL_FLOAT(),
                false, STRIDE, vertexData);

       gl.glEnableVertexAttribArray(aPositionLocation);
        return vertexesNumber;
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
        //todo here
        return 0;
    }

    @Override
    public void bindDataLine(PVector a, PVector b, PVector color) {

    }

    @Override
    public void updateLocations() {
        aPositionLocation = gl.glGetAttribLocation(programId, "aPos");
        uTextureUnitLocation = gl.glGetUniformLocation(programId, "skybox");
        projectionMatrixLoation = gl.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = gl.glGetUniformLocation(programId, "view");
    }

    @Override
    public int getTransformMatrixLocation() {
        return -1;
    }

    @Override
    public int getCameraLocation() {
        return viewMatrixLocation;
    }

    @Override
    public int getProjectionLocation() {
        return projectionMatrixLoation;
    }

    @Override
    public int getTextureLocation() {
        return uTextureUnitLocation;
    }

    @Override
    public int getNormalTextureLocation() {
        return -1;
    }

    @Override
    public int getNormalMapEnableLocation() {
        return -1;
    }

    @Override
    public int getCameraPosLlocation() {
        return viewMatrixLocation;
    }
}


