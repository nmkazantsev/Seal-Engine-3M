package com.nikitos.platform;

import com.nikitos.main.images.AbstractImage;
import com.nikitos.platformBridge.*;
import com.nikitos.runtime.FrameCaptureSource;
import main.images.PImageDesktop;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;


public class DesktopBridge extends PlatformBridge {
    private final SealAssetManager assetManager = new DesktopSealAssetManager();
    private final AudioPlayer audioPlayer = new AudioPlayerDesktop(assetManager);
    private final RuntimeFileBridge runtimeFileBridge = new DesktopRuntimeFileBridge();
    private final DesktopMouseControlBridge mouseControlBridge = new DesktopMouseControlBridge();
    private final DesktopFrameCaptureSource frameCaptureSource =
            new DesktopFrameCaptureSource();
    // Может быть установлен из любого потока, а читается GLFW-loop в DesktopLauncher.
    private volatile boolean shutdownRequested;

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public MatrixPlatformBridge getMatrixPlatformBridge() {
        return new MatrixBridgeDesktop();
    }

    @Override
    public ShaderBridge getShaderBridge() {
        return new ShaderBridgeDesktop();
    }

    @Override
    public VertexBridge getVertexBridge() {
        return new VertexBridgeDesktop();
    }

    @Override
    public GeneralPlatformBridge getGeneralPlatformBridge() {
        return new GeneralBridgeDesktop();
    }

    @Override
    public GLConstBridge getGLConstBridge() {
        return new GLConstBridgeDesktop();
    }

    @Override
    public ImgBridge getImgBridge() {
        return new ImgBridgeDesktop();
    }

    @Override
    public AbstractImage getAbstractImage() {
        return new PImageDesktop();
    }

    @Override
    public void glClearColor(float r, float g, float b, float a) {
        // Установка цвета очистки экрана
        GL32.glClearColor(r, g, b, a);
    }

    @Override
    public int glGetError() {
        return GL30.glGetError();
    }

    @Override
    public void log_e(String tag, String message) {
        System.err.println(tag + ": " + message);
    }

    @Override
    public void log_i(String tag, String message) {
        System.out.println(tag + ": " + message);
    }

    @Override
    public void print(String text) {
        System.out.println(text);
    }

    @Override
    public Platform getPlatform() {
        return Platform.DESKTOP;
    }

    @Override
    public ErrorPrinter getErrorPrinter() {
        return new ErrorPrinterDesktop();
    }

    @Override
    public SealAssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    @Override
    public FontBridge getFontBridge() {
        return new FontBridgeDesktop();
    }

    @Override
    public RuntimeFileBridge getRuntimeFileBridge() {
        return runtimeFileBridge;
    }

    @Override
    public MouseControlBridge getMouseControlBridge() {
        return mouseControlBridge;
    }

    @Override
    public void shutdownApplication() {
        // GLFW остаётся в потоке launcher: другой поток только запрашивает выход из цикла.
        shutdownRequested = true;
    }

    /** Возвращает запрос на выход; сам GLFW вызов остаётся в потоке launcher. */
    public boolean isShutdownRequested() {
        return shutdownRequested;
    }

    @Override
    public FrameCaptureSource getFrameCaptureSource() {
        return frameCaptureSource;
    }

    public void attachWindow(long window) {
        mouseControlBridge.attachWindow(window);
        // Capture использует то же GLFW-окно и не создаёт вторую поверхность.
        frameCaptureSource.attachWindow(window);
    }
}
