package com.nikitos.main.shaders;


import com.nikitos.CoreRenderer;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.platformBridge.ShaderBridge;

public class ShaderUtils {
    private final ShaderBridge shaderBridge;
    private final PlatformBridge platformBridge;

    public ShaderUtils() {
        platformBridge = CoreRenderer.engine.getPlatformBridge();
        shaderBridge = platformBridge.getShaderBridge();
    }

    protected int createProgram(int vertexShaderId, int fragmentShaderId) {

        final int programId = shaderBridge.glCreateProgram();
        if (programId == 0) {
            return 0;
        }
        shaderBridge.glAttachShader(programId, vertexShaderId);
        shaderBridge.glAttachShader(programId, fragmentShaderId);

        shaderBridge.glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        shaderBridge.glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            platformBridge.log_e("error compiling shaders", String.valueOf(platformBridge.glGetError()));
            platformBridge.log_e("Load Shader Failed", "loading\n" + shaderBridge.glGetProgramInfoLog(programId));
            shaderBridge.glDeleteProgram(programId);
            return 0;
        }
        return programId;

    }

    protected int createProgram(int vertexShaderId, int fragmentShaderId, int geomShaderId) {

        final int programId = shaderBridge.glCreateProgram();
        if (programId == 0) {
            return 0;
        }
        shaderBridge.glAttachShader(programId, vertexShaderId);
        shaderBridge.glAttachShader(programId, fragmentShaderId);
        shaderBridge.glAttachShader(programId, geomShaderId);
        shaderBridge.glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        shaderBridge.glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            platformBridge.log_e("error compiling shaders", String.valueOf(platformBridge.glGetError()));
            platformBridge.log_e("Load Shader Failed", "loading\n" + shaderBridge.glGetProgramInfoLog(programId));
            shaderBridge.glDeleteProgram(programId);
            return 0;
        }
        return programId;

    }

    protected int createShader(int type, String shaderText) {
        final int shaderId = shaderBridge.glCreateShader(type);
        if (shaderId == 0) {
            platformBridge.log_e("Load Shader Failed" + shaderText, "loading\n" + shaderBridge.glGetShaderInfoLog(shaderId));

            return 0;
        }
        shaderBridge.glShaderSource(shaderId, shaderText);
        shaderBridge.glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        shaderBridge.glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            platformBridge.log_e("Load Shader Failed" + shaderText, "Compilation\n" + shaderBridge.glGetShaderInfoLog(shaderId));
            shaderBridge.glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }

    protected int createShaderProgram(String vertexShader, String fragmentShader) {
        int programId;
        int vertexShaderId = createShader(GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = createShader(GL_FRAGMENT_SHADER, fragmentShader);
        programId = createProgram(vertexShaderId, fragmentShaderId);
        return programId;
    }

    protected int createShaderProgram(String vertexShader, String fragmentShader, String geomShader) {
        int programId;
        int vertexShaderId = createShader(GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = createShader(GL_FRAGMENT_SHADER, fragmentShader);
        int geomShaderId = createShader(GL_GEOMETRY_SHADER, geomShader);
        programId = createProgram(vertexShaderId, fragmentShaderId, geomShaderId);
        return programId;
    }

    static int prevProgramId;

    protected void applyShader(int programId) {
        if (programId != prevProgramId) {
            shaderBridge.glUseProgram(programId);
            prevProgramId = programId;
        }
    }

}
