package com.seal.gl_engine.platform;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.seal.gl_engine.engine.main.images.PImageAndroid;

import java.nio.FloatBuffer;

public class GeneralBridgeAndroid extends GeneralPlatformBridge {
    @Override
    public void glDrawArrays(int type, int offest, int count) {
        GLES30.glDrawArrays(type, offest, count);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        return GLES30.glGetUniformLocation(program, name);
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z) {
        GLES30.glUniform3f(location, x, y, z);
    }

    @Override
    public void glActiveTexture(int texture) {
        GLES30.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int texture, int location) {
        GLES30.glBindTexture(texture, location);
    }

    @Override
    public void glUniform1i(int textureLocation, int slot) {
        GLES30.glUniform1i(textureLocation, slot);
    }

    @Override
    public void glGenerateMipmap(int type) {
        GLES30.glGenerateMipmap(type);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, FloatBuffer vertexData) {
        GLES30.glVertexAttribPointer(aPositionLocation, step, type, normalized, size, vertexData);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, int vertexData) {
        GLES30.glVertexAttribPointer(aPositionLocation, step, type, false, size, vertexData);
    }

    @Override
    public void glEnableVertexAttribArray(int aPositionLocation) {
        GLES30.glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public int glGetAttribLocation(int programId, String name) {
        return GLES30.glGetAttribLocation(programId, name);
    }

    @Override
    public void glBufferData(int type, int length, FloatBuffer vertexData, int mode) {
        GLES30.glBufferData(type, length, vertexData, mode);
    }

    @Override
    public void glBindBuffer(int type, int address) {
        GLES30.glBindBuffer(type, address);
    }

    @Override
    public void glEnable(int mode) {
        GLES30.glEnable(mode);
    }

    @Override
    public void glDisable(int mode) {
        GLES30.glDisable(mode);
    }

    @Override
    public void texImage2D(int target, int level, int internalFormat, PImage image, int type, int border) {
        GLUtils.texImage2D(target, level, internalFormat, (Bitmap) image.getBitmap(), type, border);
    }

    @Override
    public void glTexParameteri(int textureType, int filter, int interpolation) {
        GLES30.glTexParameteri(textureType, filter, interpolation);
    }

    @Override
    public void glGenTextures(int number, int[] textureIds, int offset) {
        GLES30.glGenTextures(number, textureIds, offset);
    }

    @Override
    public void glDeleteTextures(int number, int[] ids, int offset) {
        GLES30.glDeleteTextures(number, ids, offset);
    }

    @Override
    public void texImage2D(int target, int level, PImage image, int border) {
        GLUtils.texImage2D(target, level, (Bitmap) image.getBitmap(), border);
    }

    @Override
    public void glDepthMask(boolean on) {
        GLES30.glDepthMask(on);
    }

    @Override
    public void glDeleteFramebuffers(int number, int[] framebuffers, int offset) {
        GLES30.glDeleteFramebuffers(number, framebuffers, offset);
    }

    @Override
    public void glDeleteRenderbuffers(int number, int[] buffers, int offset) {
        GLES30.glDeleteRenderbuffers(number, buffers, offset);
    }

    @Override
    public void glBindFramebuffer(int type, int id) {
        GLES30.glBindFramebuffer(type, id);
    }

    @Override
    public void glClear(int mask) {
        GLES30.glClear(mask);
    }

    @Override
    public void glGenFramebuffers(int num, int[] buffers, int offset) {
        GLES30.glGenFramebuffers(num, buffers, offset);
    }

    @Override
    public void glTexImage2D(int type, int level, int internalFormat, int width, int height, int border, int texType, int localDataType, FloatBuffer pixels) {
        GLES30.glTexImage2D(type, level, internalFormat, width, height, border, texType, localDataType, pixels);
    }

    // --- texture ---
    public void texParameterf(int target, int pname, float param) {
        GLES30.glTexParameterf(target, pname, param);
    }

    public void bindTexture(int target, int texture) {
        GLES30.glBindTexture(target, texture);
    }

    // --- framebuffer ---
    public void bindFramebuffer(int target, int framebuffer) {
        GLES30.glBindFramebuffer(target, framebuffer);
    }

    public void framebufferTexture2D(
            int target, int attachment, int textarget, int texture, int level) {
        GLES30.glFramebufferTexture2D(
                target, attachment, textarget, texture, level
        );
    }

    // --- renderbuffer ---
    public void genRenderbuffers(int n, int[] buffers, int offset) {
        GLES30.glGenRenderbuffers(n, buffers, offset);
    }

    public void bindRenderbuffer(int target, int renderbuffer) {
        GLES30.glBindRenderbuffer(target, renderbuffer);
    }

    public void renderbufferStorage(int target, int internalformat, int width, int height) {
        GLES30.glRenderbufferStorage(target, internalformat, width, height);
    }

    public void framebufferRenderbuffer(
            int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GLES30.glFramebufferRenderbuffer(
                target, attachment, renderbuffertarget, renderbuffer
        );
    }


}
