package com.nikitos.main.shaders.default_adaptors;



import com.nikitos.main.shaders.Adaptor;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MainShaderAdaptor extends Adaptor {

    private int aPositionLocation;
    private int aTextureLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;
    private int modelMtrixLocation;
    private int normalLocation;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT = 3;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT + NORMAL_COUNT) * 4;

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[faces.length * faces[0].verticesNumber()];
        int vertexesNumber = 0;
        for (int i = 0; i < faces.length; i++) {
            System.arraycopy(faces[i].getArrayRepresentation(), 0, vertices, i * faces[i].verticesNumber(), faces[i].verticesNumber());
            vertexesNumber++;
        }
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        // координаты вершин
        vertexData.position(0);
        gl.glVertexAttribPointer(aPositionLocation, POSITION_COUNT, glConstBridge.GL_FLOAT(),
                false, STRIDE, vertexData);
        gl.glEnableVertexAttribArray(aPositionLocation);

        // координаты текстур
        vertexData.position(POSITION_COUNT);
        gl.glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT,glConstBridge. GL_FLOAT(),
                false, STRIDE, vertexData);
        gl.glEnableVertexAttribArray(aTextureLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT);
        gl.glVertexAttribPointer(normalLocation, NORMAL_COUNT, glConstBridge.GL_FLOAT(),
                false, STRIDE, vertexData);
        gl.glEnableVertexAttribArray(normalLocation);
        return vertexesNumber;
    }

    private void loadDataToBuffer(float[] vertices, int bufferIndex, VertexBuffer vertexBuffer) {
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        vertexBuffer.bindVao();
        vertexBuffer.bindVbo(bufferIndex);//vertex coords
        vertexData.position(0);
        gl.glBufferData(glConstBridge.GL_ARRAY_BUFFER(), vertices.length * 4, vertexData, glConstBridge.GL_STATIC_DRAW());
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
        if (!vboLoaded) {
            //set up positions
            float[] vertices = new float[faces.length * faces[0].vertices.length * 3];//3 because 3angle
            for (int i = 0; i < faces.length; i++) {
                vertices[i * 9] = faces[i].vertices[0].x;
                vertices[i * 9 + 1] = faces[i].vertices[0].y;
                vertices[i * 9 + 2] = faces[i].vertices[0].z;
                vertices[i * 9 + 3] = faces[i].vertices[1].x;
                vertices[i * 9 + 4] = faces[i].vertices[1].y;
                vertices[i * 9 + 5] = faces[i].vertices[1].z;
                vertices[i * 9 + 6] = faces[i].vertices[2].x;
                vertices[i * 9 + 7] = faces[i].vertices[2].y;
                vertices[i * 9 + 8] = faces[i].vertices[2].z;
            }
            loadDataToBuffer(vertices, 0, vertexBuffer);

            //set up uv
            vertices = new float[faces.length * 2 * 3];//3 because 3angle
            for (int i = 0; i < faces.length; i++) {
                vertices[i * 6] = faces[i].textureCoordinates[0].x;
                vertices[i * 6 + 1] = 1 - faces[i].textureCoordinates[0].y;
                vertices[i * 6 + 2] = faces[i].textureCoordinates[1].x;
                vertices[i * 6 + 3] = 1 - faces[i].textureCoordinates[1].y;
                vertices[i * 6 + 4] = faces[i].textureCoordinates[2].x;
                vertices[i * 6 + 5] = 1 - faces[i].textureCoordinates[2].y;
            }
            loadDataToBuffer(vertices, 1, vertexBuffer);

            //set up normals
            vertices = new float[faces.length * 3];//3 because 3 coords in normal
            for (int i = 0; i < faces.length; i++) {
                vertices[i * faces[i].vertices.length] = faces[i].normal.x;
                vertices[i * faces[i].vertices.length + 1] = faces[i].normal.y;
                vertices[i * faces[i].vertices.length + 2] = faces[i].normal.z;

            }
            loadDataToBuffer(vertices, 2, vertexBuffer);
        }
        vertexBuffer.bindVao();
        gl.glEnableVertexAttribArray(aPositionLocation);
        gl.glEnableVertexAttribArray(aTextureLocation);
        // glEnableVertexAttribArray(normalLocation);
        gl.glBindBuffer(glConstBridge.GL_ARRAY_BUFFER(), vertexBuffer.getVboAdress(0));
        gl.glVertexAttribPointer(aPositionLocation, 3, glConstBridge.GL_FLOAT(), false, 0, 0);
        gl.glBindBuffer(glConstBridge.GL_ARRAY_BUFFER(), vertexBuffer.getVboAdress(1));
        gl.glVertexAttribPointer(aTextureLocation, 2, glConstBridge.GL_FLOAT(), false, 0, 0);
        // glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(2));
        //glVertexAttribPointer(normalLocation, 3, GL_FLOAT, false, 0, 0);
        vertexBuffer.bindDefaultVbo();//vertex coords
        vertexBuffer.bindDefaultVao();
        return 0;
    }

    @Override
    public void bindDataLine(PVector a, PVector b , PVector color) {

    }

    @Override
    public void updateLocations() {
        aPositionLocation = gl.glGetAttribLocation(programId, "aPos");
        aTextureLocation = gl.glGetAttribLocation(programId, "aTexCoord");
        normalLocation = gl.glGetAttribLocation(programId, "normal");
        uTextureUnitLocation = gl.glGetUniformLocation(programId, "u_TextureUnit");
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
        return -1;
    }
}
