package com.nikitos.main.frameBuffers;



import com.nikitos.CoreRenderer;
import com.nikitos.main.VRAMobject;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.vertices.Face;
import com.nikitos.GamePageClass;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

public class FrameBuffer extends VRAMobject {
    protected int texture;
    protected int depth;
    protected int frameBuffer;
    protected int w;
    protected int h;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;

    // https://www.programcreek.com/java-api-examples/?class=android.opengl.glc.method=glBindFramebuffer
    public FrameBuffer(int width, int height, GamePageClass page) {
        super(page);
        this.w = width;
        this.h = height;
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        onRedrawSetup();
    }

    public void onRedrawSetup() {
        int[] frameBuffers = new int[1];
        int[] frameBufferTextures = new int[1];
        gl.glGenFramebuffers(1, frameBuffers, 0);

        gl.glGenTextures(1, frameBufferTextures, 0);
        gl.glBindTexture(glc.GL_TEXTURE_2D(), frameBufferTextures[0]);
        gl.glTexImage2D(glc.GL_TEXTURE_2D(), 0, glc.GL_RGBA16F(),
                w, h, 0,
                gl.GL_RGBA(), glc.GL_FLOAT(), null);
        gl.texParameterf(glc.GL_TEXTURE_2D(),
                gl.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());
        gl.texParameterf(glc.GL_TEXTURE_2D(),
                gl.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR());
        gl.glBindFramebuffer(glc.GL_FRAMEBUFFER(), frameBuffers[0]);
        gl.framebufferTexture2D(glc.GL_FRAMEBUFFER(), glc.GL_COLOR_ATTACHMENT0(),
                glc.GL_TEXTURE_2D(), frameBufferTextures[0], 0);

        gl.glBindTexture(glc.GL_TEXTURE_2D(), 0);

        int[] depthBuffer = new int[1];
        gl.genRenderbuffers(1, depthBuffer, 0);
        gl.bindRenderbuffer(glc.GL_RENDERBUFFER(), depthBuffer[0]);
        gl.renderbufferStorage(glc.GL_RENDERBUFFER(), glc.GL_DEPTH_COMPONENT16(), w, h);
        gl.framebufferRenderbuffer(glc.GL_FRAMEBUFFER(), glc.GL_DEPTH_ATTACHMENT(), glc.GL_RENDERBUFFER(), depthBuffer[0]);

        gl.glBindFramebuffer(glc.GL_FRAMEBUFFER(), 0);

        frameBuffer = frameBuffers[0];
        depth = depthBuffer[0];
        texture = frameBufferTextures[0];
    }

    public void drawTexture(PVector a, PVector b, PVector d) {
        PVector c = new PVector(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[][] vertices = new float[][]{
                {a.x, a.y, a.z},
                {d.x, d.y, d.z},
                {b.x, b.y, b.z},
                {c.x, c.y, c.z}
        };

        float[][] textCoords = new float[][]{
                {1, 1},
                {1, 0},
                {0, 1},
                {0, 0},
        };
        Face face1 = new Face(
                new PVector[]{
                        new PVector(vertices[0][0], vertices[0][1], vertices[0][2]),
                        new PVector(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new PVector(vertices[2][0], vertices[2][1], vertices[2][2]),
                },
                new PVector[]{
                        new PVector(textCoords[0][0], textCoords[0][1]),
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                },
                new PVector(0, 0, 1));
        Face face2 = new Face(
                new PVector[]{
                        new PVector(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new PVector(vertices[2][0], vertices[2][1], vertices[2][2]),
                        new PVector(vertices[3][0], vertices[3][1], vertices[3][2]),
                },
                new PVector[]{
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                        new PVector(textCoords[3][0], textCoords[3][1]),
                },
                new PVector(0, 0, 1));
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2});
        //place texture to target 2D of unit 0
        gl.glActiveTexture(glc.GL_TEXTURE0());
        gl.glBindTexture(glc.GL_TEXTURE_2D(), texture);
        gl.glDrawArrays(glc.GL_TRIANGLES(), 0, 6);
    }

    public int getFrameBuffer() {
        return frameBuffer;
    }

    public int getDepth() {
        return depth;
    }

    public int getTexture() {
        return texture;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public void delete() {
        gl.glDeleteFramebuffers(1, new int[]{getFrameBuffer()}, 0);
        gl.glDeleteRenderbuffers(1, new int[]{getDepth()}, 0);
        gl.glDeleteTextures(1, new int[]{getTexture()}, 0);
    }

    @Override
    public void reload() {
        onRedrawSetup();
    }

    public void apply() {
        gl.glBindFramebuffer(glc.GL_FRAMEBUFFER(), frameBuffer);
        gl.glClear(glc.GL_COLOR_BUFFER_BIT() | glc.GL_DEPTH_BUFFER_BIT());
    }

    public void connectDefaultFrameBuffer() {
        // switch to the buffer
        gl.glBindFramebuffer(glc.GL_FRAMEBUFFER(), 0);
        gl.glClear(glc.GL_COLOR_BUFFER_BIT() | glc.GL_DEPTH_BUFFER_BIT());
    }
}
