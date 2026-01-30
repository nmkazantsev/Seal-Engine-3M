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

}
