package com.nikitos.main.shaders.default_adaptors;

import com.nikitos.main.shaders.Adaptor;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class SectionShaderAdaptor extends Adaptor {
    private int aPositionLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;
    private int modelMtrixLocation;

    @Override
    public int bindData(Face[] faces) {
        return 0;
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
        return 0;
    }

    @Override
    public void bindDataLine(PVector a, PVector b, PVector color) {
        float[] vertices = new float[]{a.x, a.y, a.z, b.x, b.y, b.z, color.x, color.y, color.z};
        int vertexesNumber = 0;

            FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        // координаты вершин
        vertexData.position(0);
        gl.glVertexAttribPointer(aPositionLocation, 3, glConstBridge.GL_FLOAT(),
                false, 3 * 4, vertexData);
        gl.glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void updateLocations() {
        aPositionLocation = gl.glGetAttribLocation(programId, "aPos");
        projectionMatrixLoation = gl.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = gl.glGetUniformLocation(programId, "view");
        modelMtrixLocation = gl.glGetUniformLocation(programId, "model");
    }

    @Override
    public int getTransformMatrixLocation() {
        return modelMtrixLocation;
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
        return -1;
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
        return -1;
    }
}
