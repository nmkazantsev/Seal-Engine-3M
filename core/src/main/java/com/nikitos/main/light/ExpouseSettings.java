package com.nikitos.main.light;
import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;

public class ExpouseSettings extends ShaderData {
    private int expouseLoc, gammaLoc;
    public float expouse, gamma;

    public ExpouseSettings(GamePageClass gamePageClass) {
        super(gamePageClass);
        expouse = 1;
        gamma = 1;
    }

    @Override
    protected void getLocations(int programId) {
        expouseLoc = gl.glGetUniformLocation(programId, "exposure");
        gammaLoc = gl.glGetUniformLocation(programId, "gamma");
    }

    @Override
    protected void forwardData() {
        gl.glUniform1f(expouseLoc, expouse);
        gl.glUniform1f(gammaLoc, gamma);
    }

    @Override
    protected void delete() {

    }
}
