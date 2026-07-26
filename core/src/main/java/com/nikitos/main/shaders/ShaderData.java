package com.nikitos.main.shaders;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.PageOwnership;
import com.nikitos.platformBridge.GeneralPlatformBridge;

public abstract class ShaderData {
    protected final GeneralPlatformBridge gl;
    private final GamePageClass gamePageClass;
    private final Object ownershipToken;

    protected ShaderData(GamePageClass gamePageClass) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        this.gamePageClass = gamePageClass;
        ownershipToken = PageOwnership.tokenOf(gamePageClass);
        Adaptor.addLightAdaptor(this);
    }

    final Object getOwnershipToken() {
        return ownershipToken;
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
