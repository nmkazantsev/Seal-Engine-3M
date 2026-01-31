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

    public abstract void glTexParameteri(int textureType, int filter, int interpolation);

    public abstract void glGenTextures(int number, int[] textureIds, int offset);

    public abstract void glDeleteTextures(int number, int[] ids, int offset);
    public abstract void texImage2D(int target, int level, PImage image, int border);

    public abstract void glDepthMask(boolean on);
    public abstract void glDeleteFramebuffers(int number, int [] framebuffers, int offset);
    public abstract void glDeleteRenderbuffers(int number, int [] buffers, int offset);
    public abstract void glClear(int mask);
    public abstract void glGenFramebuffers(int num, int [] buffers, int offset);
    public abstract void glBindFramebuffer(int type, int id);
    public abstract void glTexImage2D(int type, int level, int internalFormat, int width, int height, int border, int texType,int localDataType, FloatBuffer pixels);

    public abstract  void texParameterf(int target, int pname, float param);
    public abstract  void bindTexture(int target, int texture);

    // --- framebuffer ---
    public abstract  void bindFramebuffer(int target, int framebuffer);
    public abstract  void framebufferTexture2D(
            int target,
            int attachment,
            int textarget,
            int texture,
            int level
    );

    // --- renderbuffer ---
    public abstract   void genRenderbuffers(int n, int[] buffers, int offset);
    public abstract  void bindRenderbuffer(int target, int renderbuffer);
    public abstract void renderbufferStorage(int target, int internalformat, int width, int height);
    public abstract void framebufferRenderbuffer(
            int target,
            int attachment,
            int renderbuffertarget,
            int renderbuffer
    );

    public abstract int GL_RGBA16F();
    public abstract int GL_RGBA();
    public abstract int GL_TEXTURE_MAG_FILTER();
    public abstract int GL_TEXTURE_MIN_FILTER();

}
