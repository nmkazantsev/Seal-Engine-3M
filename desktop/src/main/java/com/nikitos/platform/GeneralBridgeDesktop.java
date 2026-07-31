package com.nikitos.platform;

import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import io.github.humbleui.skija.Image;
import io.github.humbleui.skija.Surface;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Path;


public class GeneralBridgeDesktop extends GeneralPlatformBridge {
    @Override
    public byte[] readPixelsRgba(int width, int height) {
        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
        GL33.glReadPixels(0, 0, width, height, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, pixels);
        byte[] result = new byte[pixels.capacity()];
        pixels.rewind();
        pixels.get(result);
        return result;
    }

    @Override
    public void writePng(Path outputFile, int width, int height, byte[] rgbaBottomFirst) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            int sourceRow = (height - 1 - y) * width * 4;
            for (int x = 0; x < width; x++) {
                int pixel = sourceRow + x * 4;
                int r = rgbaBottomFirst[pixel] & 0xFF;
                int g = rgbaBottomFirst[pixel + 1] & 0xFF;
                int b = rgbaBottomFirst[pixel + 2] & 0xFF;
                int a = rgbaBottomFirst[pixel + 3] & 0xFF;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(image, "png", outputFile.toFile());
    }
    @Override
    public void glDrawArrays(int type, int offest, int count) {
        GL30.glDrawArrays(type, offest, count);
    }

    @Override
    public int glGetUniformLocation(int program, String name) {
        /* if(loc == -1){
            System.err.println("glGetUniformLocation: Program " + program + " uniform " + name);
        }*/
        return GL30.glGetUniformLocation(program, name);
    }

    @Override
    public void glUniform3f(int location, float x, float y, float z) {
        if (location == -1) {
            return;
        }
        GL30.glUniform3f(location, x, y, z);
    }

    @Override
    public void glActiveTexture(int texture) {
        GL30.glActiveTexture(texture);
    }

    @Override
    public void glBindTexture(int texture, int location) {
        if (location == -1) {
            return;
        }
        GL30.glBindTexture(texture, location);
    }

    @Override
    public void glUniform1i(int location, int value) {
        if (location == -1) {
            return;
        }
        GL30.glUniform1i(location, value);
    }

    @Override
    public void glGenerateMipmap(int type) {
        GL30.glGenerateMipmap(type);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, FloatBuffer vertexData) {
        if (aPositionLocation == -1) {
            return;
        }
        GL33.glVertexAttribPointer(aPositionLocation, step, type, normalized, size, vertexData);
    }

    @Override
    public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, int vertexData) {
        if (aPositionLocation == -1) {
            return;
        }
        GL33.glVertexAttribPointer(aPositionLocation, step, type, false, size, vertexData);
    }

    @Override
    public void glEnableVertexAttribArray(int aPositionLocation) {
        if (aPositionLocation == -1) {
            return;
        }
        GL33.glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public int glGetAttribLocation(int programId, String name) {
        if (programId == -1) {
            return -1;
        }
        return GL33.glGetAttribLocation(programId, name);
    }

    @Override
    public void glBufferData(int type, int length, FloatBuffer vertexData, int mode) {
        GL33.glBufferData(type, vertexData, mode);
    }

    @Override
    public void glBindBuffer(int type, int address) {
        if (address == -1) {
            return;
        }
        GL33.glBindBuffer(type, address);
    }

    @Override
    public void glEnable(int mode) {
        GL33.glEnable(mode);
    }

    @Override
    public void glDisable(int mode) {
        GL33.glDisable(mode);
    }

    ByteBuffer buffer;

    @Override
    public void texImage2D(int target, int level, int internalFormat, PImage image, int type, int border) {

        // Получаем raster Surface из PImageDesktop
        Surface surface = ((Surface) image.getBitmap());

        // Делаем snapshot immutable Image
        Image snapshot = surface.makeImageSnapshot();

        // Берём ByteBuffer с пикселями (RGBA, PREMUL)
        ByteBuffer pixels = snapshot.peekPixels();
        if (pixels == null) throw new RuntimeException("Failed to peek pixels");

        // Создаём прямой буфер для OpenGL
        buffer = ByteBuffer.allocateDirect(pixels.remaining());
        buffer.put(pixels);
        buffer.flip();

        buffer.position(0);
        // Загружаем в OpenGL
        GL33.glTexImage2D(
                target,
                level,
                GL33.GL_RGBA8,
                snapshot.getWidth(),
                snapshot.getHeight(),
                border,
                GL33.GL_RGBA,
                GL33.GL_UNSIGNED_BYTE,
                buffer
        );
    }

    @Override
    public void glTexParameteri(int textureType, int filter, int interpolation) {
        GL33.glTexParameteri(textureType, filter, interpolation);
    }

    @Override
    public void glGenTextures(int number, int[] textureIds, int offset) {
        GL33.glGenTextures(textureIds);
    }

    @Override
    public void glDeleteTextures(int number, int[] ids, int offset) {
        GL33.glDeleteTextures(ids);
    }

    @Override
    public void texImage2D(int target, int level, PImage bitmap, int border) {
        texImage2D(target, level, GL33.GL_UNSIGNED_BYTE, bitmap, GL33.GL_RGBA, border);
    }


    @Override
    public void glDepthMask(boolean on) {
        GL33.glDepthMask(on);
    }

    @Override
    public void glDeleteFramebuffers(int number, int[] framebuffers, int offset) {
        GL33.glDeleteFramebuffers(framebuffers);
    }

    @Override
    public void glDeleteRenderbuffers(int number, int[] buffers, int offset) {
        GL33.glDeleteRenderbuffers(buffers);
    }

    @Override
    public void glBindFramebuffer(int type, int id) {
        GL33.glBindFramebuffer(type, id);
    }

    @Override
    public void glClear(int mask) {
        GL33.glClear(mask);
    }

    @Override
    public void glGenFramebuffers(int num, int[] buffers, int offset) {
        GL33.glGenFramebuffers(buffers);
    }


    @Override
    public void glTexImage2D(int type, int level, int internalFormat, int width, int height, int border, int texType, int localDataType, FloatBuffer pixels) {
        GL33.glTexImage2D(type, level, internalFormat, width, height, border, texType, localDataType, pixels);
    }

    // --- texture ---
    public void texParameterf(int target, int pname, float param) {
        GL33.glTexParameterf(target, pname, param);
    }

    public void bindTexture(int target, int texture) {
        glBindTexture(target, texture);
    }

    // --- framebuffer ---
    public void bindFramebuffer(int target, int framebuffer) {
        glBindFramebuffer(target, framebuffer);
    }

    public void framebufferTexture2D(
            int target, int attachment, int textarget, int texture, int level) {
        GL33.glFramebufferTexture2D(
                target, attachment, textarget, texture, level
        );
    }

    // --- renderbuffer ---
    public void genRenderbuffers(int n, int[] buffers, int offset) {
        GL33.glGenRenderbuffers(buffers);
    }

    public void bindRenderbuffer(int target, int renderbuffer) {
        GL33.glBindRenderbuffer(target, renderbuffer);
    }

    public void renderbufferStorage(int target, int internalformat, int width, int height) {
        GL33.glRenderbufferStorage(target, internalformat, width, height);
    }

    public void framebufferRenderbuffer(
            int target, int attachment, int renderbuffertarget, int renderbuffer) {
        GL33.glFramebufferRenderbuffer(
                target, attachment, renderbuffertarget, renderbuffer
        );
    }

    @Override
    public void glUniform1f(int location, float val) {
        if (location == -1) {
            return;
        }
        GL33.glUniform1f(location, val);
    }

    @Override
    public void glBlendFunc(int func1, int func2) {
        GL33.glBlendFunc(func1, func2);
    }


}
