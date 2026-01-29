package com.nikitos.platformBridge;

import com.nikitos.main.images.PImage;

import java.nio.FloatBuffer;

public abstract class GeneralPlatformBridge {
    public abstract void glDrawArrays(int type, int offest, int count);

    public abstract int glGetUniformLocation(int program, String name);

    public abstract void glUniform3f(int location, float x, float y, float z);

    public abstract void glActiveTexture(int texture);

    public abstract void glBindTexture(int texture, int location);

    public abstract void glUniform1i(int textureLocation, int slot);

    public abstract void glGenerateMipmap(int type);

    public abstract void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, FloatBuffer vertexData);

    public abstract void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, int vertexData);

    public abstract void glEnableVertexAttribArray(int aPositionLocation);

    public abstract int glGetAttribLocation(int programId, String name);

    public abstract void glBufferData(int type, int length, FloatBuffer vertexData, int mode);

    public abstract void glBindBuffer(int type, int address);

    public abstract void glEnable(int mode);

    public abstract void glDisable(int mode);

    public abstract void texImage2D(int mode, int level, int type, PImage bitmap, int memSize, int i);

    public abstract void glTexParameteri(int textureType, int filter,int interpolation);
    public abstract void glGenTextures(int number,int [] textureIds, int offset);
    public abstract void glDeleteTextures(int number, int [] ids, int offset);
}
