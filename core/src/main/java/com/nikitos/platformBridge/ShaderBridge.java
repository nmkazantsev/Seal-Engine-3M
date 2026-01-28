package com.nikitos.platformBridge;

public abstract class ShaderBridge {

    public void glDeleteProgram(int programId) {
        deleteProgram(programId);
    }

    public abstract void deleteProgram(int link);

    public abstract int glCreateProgram();

    public abstract void glAttachShader(int prog, int shader);

    public abstract void glLinkProgram(int programId);

    public abstract void glGetProgramiv(int shaderId, int type, int [] status, int i);

    public abstract void glShaderSource(int shader, String text);

    public abstract void glCompileShader(int shader);

    public abstract void glUseProgram(int programId);

    public abstract String glGetProgramInfoLog(int programId);

    public abstract void glGetShaderiv(int shaderId, int type, int []    status, int i);

    public abstract int glCreateShader(int type);

    public abstract void glDeleteShader(int id);

    public abstract String glGetShaderInfoLog(int programId);

}
