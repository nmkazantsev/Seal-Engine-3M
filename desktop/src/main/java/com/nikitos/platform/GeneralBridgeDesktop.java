package com.nikitos.platform;

import com.nikitos.platformBridge.GeneralPlatformBridge;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

public class GeneralBridgeDesktop extends GeneralPlatformBridge {
    @Override
    public void glDrawArrays(int type, int offest, int count) {
        GL30.glDrawArrays(type, offest, count);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return GL30.glGetUniformLocation(program, name);
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z) {
        GL30.glUniform3f(location, x, y, z);
    }

    @Override
    public void glActiveTexture(int texture) {
        GL30.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int texture, int location) {
        GL30.glBindTexture(texture, location);
    }

    @Override
    public void glUniform1i(int textureLocation, int slot) {
        GL30.glUniform1i(textureLocation, slot);
    }

    @Override
    public void glGenerateMipmap(int type) {
        GL30.glGenerateMipmap(type);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, FloatBuffer vertexData) {
        GL33.glVertexAttribPointer(aPositionLocation, step, type, normalized, size, vertexData);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, int vertexData) {
        GL33.glVertexAttribPointer(aPositionLocation, step, type, false, size, vertexData);
    }

    @Override
    public void glEnableVertexAttribArray(int aPositionLocation) {
        GL33.glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public int glGetAttribLocation(int programId, String name) {
        return GL33.glGetAttribLocation(programId, name);
    }

    @Override
    public void glBufferData(int type, int length, FloatBuffer vertexData, int mode) {
        GL33.glBufferData(type, vertexData, mode);
    }

    @Override
    public void glBindBuffer(int type, int address) {
        GL33.glBindBuffer(type, address);
    }


}
