package com.nikitos.platformBridge;

/**
 * an abstract class to be implemented by platform - dependent implementations
 */
public abstract class PlatformBridge {
    public abstract void onPause();

    public abstract void onResume();

    public abstract MatrixPlatformBridge getMatrixPlatformBridge();

    public abstract ShaderBridge getShaderBridge();

    public abstract VertexBridge getVertexBridge();

    public abstract void glClearColor(float r, float g, float b, float a);

    public abstract int glGetError();

    public abstract void log_e(String tag, String message);
    public abstract void log_i(String tag, String message);

    public abstract void print(String text);
}
