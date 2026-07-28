package com.nikitos.main.shaders;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.GeneralPlatformBridge;

public abstract class ShaderData {
    protected final GeneralPlatformBridge gl;
    private final Object ownershipToken;

    protected ShaderData(GamePageClass gamePageClass) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        // ShaderData удаляется вместе с конкретной страницей, а не со всеми страницами её класса.
        ownershipToken = gamePageClass == null ? null : gamePageClass.getResourceOwnershipToken();
        Adaptor.addLightAdaptor(this);
    }

    final Object getOwnershipToken() {
        return ownershipToken;
    }

    protected abstract void getLocations(int programId);

    protected abstract void forwardData();

    public void forwardNow() {
        this.getLocations(Shader.getActiveShader().getAdaptor().programId);
        this.forwardData();
    }

    protected abstract void delete();
}
