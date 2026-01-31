package com.nikitos.main.light;

import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.PVector;

public class AmbientLight extends ShaderData {
    private int aLightLocation; //link to color data
    public PVector color = new PVector(0, 0, 0);

    public AmbientLight(GamePageClass gamePageClass) {
        super(gamePageClass);
    }

    @Override
    protected void getLocations(int programId) {
        aLightLocation = gl.glGetUniformLocation(programId, "aLight.color");
    }

    @Override
    protected void forwardData() {
        gl.glUniform3f(aLightLocation, color.x, color.y, color.z);
    }

    @Override
    protected void delete() {

    }

}

