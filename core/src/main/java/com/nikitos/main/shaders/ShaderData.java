package com.nikitos.main.shaders;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.GeneralPlatformBridge;

public abstract class ShaderData {
    protected final GeneralPlatformBridge gl;
    private final GamePageClass gamePageClass;

    protected ShaderData(GamePageClass gamePageClass) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        this.gamePageClass = gamePageClass;
        Adaptor.addLightAdaptor(this);
    }


    protected Class<?> getCreatorClass() {
        if (gamePageClass != null) {
            return gamePageClass.getClass();
        }
        return null;
    }

    protected abstract void getLocations(int programId);

    protected abstract void forwardData();

    public void forwardNow() {
        this.getLocations(Shader.getActiveShader().getAdaptor().programId);
        this.forwardData();
    }

    protected abstract void delete();
}
