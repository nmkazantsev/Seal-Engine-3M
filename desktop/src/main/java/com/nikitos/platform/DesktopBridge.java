package com.nikitos.platform;

import com.nikitos.platformBridge.MatrixPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.platformBridge.ShaderBridge;
import com.nikitos.platformBridge.VertexBridge;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;


public class DesktopBridge extends PlatformBridge {
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
}
