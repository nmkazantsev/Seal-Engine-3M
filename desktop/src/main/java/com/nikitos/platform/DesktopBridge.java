package com.nikitos.platform;

import com.nikitos.main.camera.CameraSettings;
import com.nikitos.main.camera.ProjectionMatrixSettings;
import com.nikitos.platformBridge.MatrixPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL32;

import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;


public class DesktopBridge extends PlatformBridge {
    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public MatrixPlatformBridge getMatrixPlatformBridge() {
        return new MatrixPlatformBridgeDesktop();
    }
    @Override
    public void glClearColor(float r, float g, float b, float a) {
        // Установка цвета очистки экрана
        GL32.glClearColor(r, g, b, a);
    }
}
