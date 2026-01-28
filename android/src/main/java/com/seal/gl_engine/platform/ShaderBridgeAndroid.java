package com.seal.gl_engine.platform;


import android.opengl.GLES30;
import com.nikitos.platformBridge.ShaderBridge;

public class ShaderBridgeAndroid extends ShaderBridge {
    @Override
    public void deleteProgram(int link) {
        GLES30.glDeleteProgram(link);
    }

    @Override
    public int glCreateProgram() {
        return GLES30.glCreateProgram();
    }

    @Override
    public void glAttachShader(int prog, int shader) {
        GLES30.glAttachShader(prog, shader);
    }

    @Override
    public void glLinkProgram(int programId) {
        GLES30.glLinkProgram(programId);
    }

    @Override
    public void glGetProgramiv(int program, int type, int[] status, int i) {
        GLES30.glGetProgramiv(program, type, status, i);
    }

    @Override
    public void glShaderSource(int shader, String text) {
        GLES30.glShaderSource(shader, text);
    }

    @Override
    public void glCompileShader(int shader) {
        GLES30.glCompileShader(shader);
    }

    @Override
    public void glUseProgram(int programId) {
        GLES30.glUseProgram(programId);
    }

    @Override
    public String glGetProgramInfoLog(int programId) {
        return GLES30.glGetProgramInfoLog(programId);
    }

    @Override
    public void glGetShaderiv(int shaderId, int type, int[] status, int i) {
        GLES30.glGetShaderiv(shaderId, type, status, i);
    }

    @Override
    public int glCreateShader(int type) {
        return GLES30.glCreateShader(type);
    }

    @Override
    public void glDeleteShader(int id) {
        GLES30.glDeleteShader(id);
    }

    @Override
    public String glGetShaderInfoLog(int programId) {
        return GLES30.glGetShaderInfoLog(programId);
    }
}
