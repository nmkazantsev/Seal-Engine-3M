package com.nikitos.platform;

import com.nikitos.platformBridge.ShaderBridge;
import org.lwjgl.opengl.GL32;

public class ShaderBridgeDesktop extends ShaderBridge {
    @Override
    public void deleteProgram(int link) {
        GL32.glDeleteProgram(link);
    }

    @Override
    public int glCreateProgram() {
        return GL32.glCreateProgram();
    }

    @Override
    public void glAttachShader(int prog, int shader) {
        GL32.glAttachShader(prog, shader);
    }

    @Override
    public void glLinkProgram(int programId) {
        GL32.glLinkProgram(programId);
    }

    @Override
    public void glGetProgramiv(int program, int type, int[] status, int i) {
        GL32.glGetProgramiv(program, type, status);
    }

    @Override
    public void glShaderSource(int shader, String text) {
        GL32.glShaderSource(shader, text);
    }

    @Override
    public void glCompileShader(int shader) {
        GL32.glCompileShader(shader);
    }

    @Override
    public void glUseProgram(int programId) {
        GL32.glUseProgram(programId);
    }

    @Override
    public String glGetProgramInfoLog(int programId) {
        return GL32.glGetProgramInfoLog(programId);
    }

    @Override
    public void glGetShaderiv(int shaderId, int type, int[] status, int i) {
        GL32.glGetShaderiv(shaderId, type, status);
    }

    @Override
    public int glCreateShader(int type) {
        return GL32.glCreateShader(type);
    }

    @Override
    public void glDeleteShader(int id) {
        GL32.glDeleteShader(id);
    }

    @Override
    public String glGetShaderInfoLog(int shaderId) {
        return GL32.glGetShaderInfoLog(shaderId);
    }

}
