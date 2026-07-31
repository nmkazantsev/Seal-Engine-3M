package com.nikitos;

import com.nikitos.main.images.AbstractImage;
import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;
import com.nikitos.platformBridge.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class CoreRendererCaptureTest {
    @TempDir
    Path tempDir;

    @Test
    void drawWritesPngAndJsonCapture() throws Exception {
        TestPlatformBridge platformBridge = new TestPlatformBridge(tempDir);
        LauncherParams launcherParams = new LauncherParams().setStartPage(ignored -> new TestPage());
        Engine engine = new Engine(platformBridge, launcherParams);
        CoreRenderer renderer = new CoreRenderer(2, 2, engine);
        engine.requestFrameCapture(tempDir);

        renderer.draw();

        Path png = tempDir.resolve("capture-1.png");
        Path json = tempDir.resolve("capture-1.json");
        assertTrue(Files.isRegularFile(png));
        assertTrue(Files.isRegularFile(json));
        BufferedImage image = ImageIO.read(png.toFile());
        assertEquals(2, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(0xFFFF0000, image.getRGB(0, 0));
        assertEquals(0xFF00FF00, image.getRGB(1, 0));
        assertEquals(0xFF0000FF, image.getRGB(0, 1));
        assertTrue(Files.readString(json).contains("\"captureId\":\"capture-1\""));
    }

    @Test
    void runStateUsesEnumStateMachine() {
        TestPlatformBridge platformBridge = new TestPlatformBridge(tempDir);
        Engine engine = new Engine(platformBridge, new LauncherParams().setStartPage(ignored -> new TestPage()));

        assertEquals(EngineRunState.RUNNING, engine.getRunState());
        engine.pauseSimulation();
        assertEquals(EngineRunState.SIMULATION_PAUSED, engine.getRunState());
        engine.suspendRendering();
        assertEquals(EngineRunState.RENDERING_SUSPENDED, engine.getRunState());
        engine.close();
        assertEquals(EngineRunState.CLOSED, engine.getRunState());
    }

    private static final class TestPage extends GamePageClass {
        @Override
        public void onSurfaceChanged(int x, int y) {
        }

        @Override
        public void update(float dtMillis) {
        }

        @Override
        public void render() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static final class TestPlatformBridge extends PlatformBridge {
        private final GeneralPlatformBridge generalPlatformBridge = new TestGeneralPlatformBridge();
        private final GLConstBridge glConstBridge = new TestGLConstBridge();
        private final MatrixPlatformBridge matrixPlatformBridge = new TestMatrixPlatformBridge();
        private final RuntimeFileBridge runtimeFileBridge;

        private TestPlatformBridge(Path root) {
            runtimeFileBridge = new TestRuntimeFileBridge(root);
        }

        @Override public void onPause() {}
        @Override public void onResume() {}
        @Override public MatrixPlatformBridge getMatrixPlatformBridge() { return matrixPlatformBridge; }
        @Override public ShaderBridge getShaderBridge() { return null; }
        @Override public VertexBridge getVertexBridge() { return null; }
        @Override public GeneralPlatformBridge getGeneralPlatformBridge() { return generalPlatformBridge; }
        @Override public GLConstBridge getGLConstBridge() { return glConstBridge; }
        @Override public ImgBridge getImgBridge() { return null; }
        @Override public AbstractImage getAbstractImage() { return null; }
        @Override public void glClearColor(float r, float g, float b, float a) {}
        @Override public int glGetError() { return 0; }
        @Override public void log_e(String tag, String message) {}
        @Override public void log_i(String tag, String message) {}
        @Override public void print(String text) {}
        @Override public Platform getPlatform() { return Platform.DESKTOP; }
        @Override public ErrorPrinter getErrorPrinter() { return null; }
        @Override public SealAssetManager getAssetManager() { return null; }
        @Override public AudioPlayer getAudioPlayer() { return null; }
        @Override public FontBridge getFontBridge() { return null; }
        @Override public RuntimeFileBridge getRuntimeFileBridge() { return runtimeFileBridge; }
        @Override public MouseControlBridge getMouseControlBridge() { return null; }
    }

    private static final class TestRuntimeFileBridge extends RuntimeFileBridge {
        private final File root;

        private TestRuntimeFileBridge(Path root) {
            this.root = root.toFile();
        }

        @Override
        protected File getRelativeRoot() {
            return root;
        }
    }

    private static final class TestGeneralPlatformBridge extends GeneralPlatformBridge {
        @Override
        public byte[] readPixelsRgba(int width, int height) {
            return new byte[]{
                    0, 0, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, (byte) 255,
                    (byte) 255, 0, 0, (byte) 255, 0, (byte) 255, 0, (byte) 255
            };
        }

        @Override
        public void writePng(Path outputFile, int width, int height, byte[] rgbaBottomFirst) throws IOException {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                int sourceRow = (height - 1 - y) * width * 4;
                for (int x = 0; x < width; x++) {
                    int pixel = sourceRow + x * 4;
                    image.setRGB(x, y, ((rgbaBottomFirst[pixel + 3] & 0xFF) << 24)
                            | ((rgbaBottomFirst[pixel] & 0xFF) << 16)
                            | ((rgbaBottomFirst[pixel + 1] & 0xFF) << 8)
                            | (rgbaBottomFirst[pixel + 2] & 0xFF));
                }
            }
            ImageIO.write(image, "png", outputFile.toFile());
        }

        @Override public void glDrawArrays(int type, int offest, int count) {}
        @Override public int glGetUniformLocation(int program, String name) { return 0; }
        @Override public void glUniform3f(int location, float x, float y, float z) {}
        @Override public void glActiveTexture(int texture) {}
        @Override public void glBindTexture(int texture, int location) {}
        @Override public void glUniform1i(int location, int value) {}
        @Override public void glGenerateMipmap(int type) {}
        @Override public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, java.nio.FloatBuffer vertexData) {}
        @Override public void glVertexAttribPointer(int aPositionLocation, int step, int type, boolean normalized, int size, int vertexData) {}
        @Override public void glEnableVertexAttribArray(int aPositionLocation) {}
        @Override public int glGetAttribLocation(int programId, String name) { return 0; }
        @Override public void glBufferData(int type, int length, java.nio.FloatBuffer vertexData, int mode) {}
        @Override public void glBindBuffer(int type, int address) {}
        @Override public void glEnable(int mode) {}
        @Override public void glDisable(int mode) {}
        @Override public void texImage2D(int mode, int level, int type, com.nikitos.main.images.PImage bitmap, int memSize, int i) {}
        @Override public void glTexParameteri(int textureType, int filter, int interpolation) {}
        @Override public void glGenTextures(int number, int[] textureIds, int offset) {}
        @Override public void glDeleteTextures(int number, int[] ids, int offset) {}
        @Override public void texImage2D(int target, int level, com.nikitos.main.images.PImage image, int border) {}
        @Override public void glDepthMask(boolean on) {}
        @Override public void glDeleteFramebuffers(int number, int[] framebuffers, int offset) {}
        @Override public void glDeleteRenderbuffers(int number, int[] buffers, int offset) {}
        @Override public void glClear(int mask) {}
        @Override public void glGenFramebuffers(int num, int[] buffers, int offset) {}
        @Override public void glBindFramebuffer(int type, int id) {}
        @Override public void glTexImage2D(int type, int level, int internalFormat, int width, int height, int border, int texType, int localDataType, java.nio.FloatBuffer pixels) {}
        @Override public void texParameterf(int target, int pname, float param) {}
        @Override public void bindTexture(int target, int texture) {}
        @Override public void bindFramebuffer(int target, int framebuffer) {}
        @Override public void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {}
        @Override public void genRenderbuffers(int n, int[] buffers, int offset) {}
        @Override public void bindRenderbuffer(int target, int renderbuffer) {}
        @Override public void renderbufferStorage(int target, int internalformat, int width, int height) {}
        @Override public void framebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {}
        @Override public void glUniform1f(int location, float val) {}
        @Override public void glBlendFunc(int func1, int func2) {}
    }

    private static final class TestGLConstBridge extends GLConstBridge {
        @Override public int GL_DEPTH_TEST() { return 0; }
        @Override public int GL_BLEND() { return 0; }
        @Override public int GL_CULL_FACE() { return 0; }
        @Override public int GL_SCISSOR_TEST() { return 0; }
        @Override public int GL_STENCIL_TEST() { return 0; }
        @Override public int GL_TRIANGLES() { return 0; }
        @Override public int GL_LINES() { return 0; }
        @Override public int GL_VERTEX_SHADER() { return 0; }
        @Override public int GL_FRAGMENT_SHADER() { return 0; }
        @Override public int GL_GEOMETRY_SHADER() { return 0; }
        @Override public int GL_COMPILE_STATUS() { return 0; }
        @Override public int GL_LINK_STATUS() { return 0; }
        @Override public int GL_INFO_LOG_LENGTH() { return 0; }
        @Override public int GL_CURRENT_PROGRAM() { return 0; }
        @Override public int GL_ARRAY_BUFFER() { return 0; }
        @Override public int GL_ELEMENT_ARRAY_BUFFER() { return 0; }
        @Override public int GL_STATIC_DRAW() { return 0; }
        @Override public int GL_DYNAMIC_DRAW() { return 0; }
        @Override public int GL_STREAM_DRAW() { return 0; }
        @Override public int GL_BUFFER_SIZE() { return 0; }
        @Override public int GL_BUFFER_USAGE() { return 0; }
        @Override public int GL_TEXTURE_2D() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP() { return 0; }
        @Override public int GL_TEXTURE0() { return 0; }
        @Override public int GL_TEXTURE1() { return 0; }
        @Override public int GL_TEXTURE_WRAP_S() { return 0; }
        @Override public int GL_TEXTURE_WRAP_T() { return 0; }
        @Override public int GL_TEXTURE_MIN_FILTER() { return 0; }
        @Override public int GL_TEXTURE_MAG_FILTER() { return 0; }
        @Override public int GL_NEAREST() { return 0; }
        @Override public int GL_LINEAR() { return 0; }
        @Override public int GL_LINEAR_MIPMAP_LINEAR() { return 0; }
        @Override public int GL_RGBA() { return 0; }
        @Override public int GL_RGB() { return 0; }
        @Override public int GL_UNSIGNED_BYTE() { return 0; }
        @Override public int GL_FLOAT() { return 0; }
        @Override public int GL_TEXTURE_MAX_LEVEL() { return 0; }
        @Override public int GL_SRC_ALPHA() { return 0; }
        @Override public int GL_ONE_MINUS_SRC_ALPHA() { return 0; }
        @Override public int GL_ONE() { return 0; }
        @Override public int GL_ZERO() { return 0; }
        @Override public int GL_FUNC_ADD() { return 0; }
        @Override public int GL_COLOR_BUFFER_BIT() { return 0; }
        @Override public int GL_DEPTH_BUFFER_BIT() { return 0; }
        @Override public int GL_STENCIL_BUFFER_BIT() { return 0; }
        @Override public int GL_FLOAT_VEC2() { return 0; }
        @Override public int GL_FLOAT_VEC3() { return 0; }
        @Override public int GL_FLOAT_VEC4() { return 0; }
        @Override public int GL_FLOAT_MAT3() { return 0; }
        @Override public int GL_FLOAT_MAT4() { return 0; }
        @Override public int GL_INT() { return 0; }
        @Override public int GL_BOOL() { return 0; }
        @Override public int GL_MAX_TEXTURE_SIZE() { return 0; }
        @Override public int GL_MAX_VERTEX_ATTRIBS() { return 0; }
        @Override public int GL_MAX_VERTEX_UNIFORM_COMPONENTS() { return 0; }
        @Override public int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS() { return 0; }
        @Override public int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS() { return 0; }
        @Override public int GL_MAX_TEXTURE_IMAGE_UNITS() { return 0; }
        @Override public int GL_MAX_RENDERBUFFER_SIZE() { return 0; }
        @Override public int GL_MAX_VIEWPORT_DIMS() { return 0; }
        @Override public int GL_MAX_CUBE_MAP_TEXTURE_SIZE() { return 0; }
        @Override public int GL_MAX_VARYING_COMPONENTS() { return 0; }
        @Override public int GL_VENDOR() { return 0; }
        @Override public int GL_RENDERER() { return 0; }
        @Override public int GL_VERSION() { return 0; }
        @Override public int GL_SHADING_LANGUAGE_VERSION() { return 0; }
        @Override public int GL_EXTENSIONS() { return 0; }
        @Override public int GL_RASTERIZER_DISCARD() { return 0; }
        @Override public int GL_DITHER() { return 0; }
        @Override public int GL_POLYGON_OFFSET_FILL() { return 0; }
        @Override public int GL_MIRRORED_REPEAT() { return 0; }
        @Override public int GL_CLAMP_TO_EDGE() { return 0; }
        @Override public int GL_TEXTURE_WRAP_R() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_NEGATIVE_X() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_POSITIVE_X() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_POSITIVE_Y() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z() { return 0; }
        @Override public int GL_TEXTURE_CUBE_MAP_POSITIVE_Z() { return 0; }
        @Override public int GL_FRAMEBUFFER() { return 0; }
        @Override public int GL_RENDERBUFFER() { return 0; }
        @Override public int GL_COLOR_ATTACHMENT0() { return 0; }
        @Override public int GL_DEPTH_ATTACHMENT() { return 0; }
        @Override public int GL_DEPTH_COMPONENT16() { return 0; }
        @Override public int GL_DEPTH_COMPONENT32() { return 0; }
        @Override public int GL_RGBA16F() { return 0; }
        @Override public int GL_SRGB() { return 0; }
    }

    private static final class TestMatrixPlatformBridge extends MatrixPlatformBridge {
        @Override public void bindAllMatrix(CameraSettings c, ProjectionMatrixSettings p, float[] mMatrix) {}
        @Override public void applyProjectionMatrix(ProjectionMatrixSettings p, boolean perspectiveEnabled) {}
        @Override public void applyProjectionMatrix(ProjectionMatrixSettings p) {}
        @Override public void applyCameraSettings(CameraSettings cam) {}
        @Override public void applyMatrix(float[] mMatrix) {}
        @Override public void frustumM(float[] result, int offset, float left, float right, float bottom, float top, float near, float far) {}
        @Override public void orthoM(float[] result, int offset, float left, float right, float bottom, float top, float near, float far) {}
        @Override public void setLookAtM(float[] result, int offset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ) {}
        @Override public void multiplyMM(float[] result, int resultOffset, float[] lhs, int lhsOffset, float[] rhs, int rhsOffset) {}
        @Override public void translateM(float[] m, int mOffset, float x, float y, float z) {}
        @Override public void rotateM(float[] m, int mOffset, float a, float x, float y, float z) {}
        @Override public void scaleM(float[] m, int mOffset, float x, float y, float z) {}
        @Override public void setIdentityM(float[] sm, int smOffset) {}
        @Override public boolean invertM(float[] mInv, int mInvOffset, float[] m, int mOffset) { return true; }
        @Override public void transposeM(float[] mTrans, int mTransOffset, float[] m, int mOffset) {}
        @Override public void multiplyMV(float[] resultVec, int resultVecOffset, float[] lhsMat, int lhsMatOffset, float[] rhsVec, int rhsVecOffset) {}
    }
}
