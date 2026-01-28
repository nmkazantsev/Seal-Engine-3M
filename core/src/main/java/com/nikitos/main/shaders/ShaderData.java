package com.nikitos.main.shaders;

import com.nikitos.GamePageClass;

public abstract class ShaderData {
    private final GamePageClass gamePageClass;

    protected ShaderData(GamePageClass gamePageClass) {
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
