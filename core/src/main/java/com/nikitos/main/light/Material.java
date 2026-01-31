package com.nikitos.main.light;


import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.PVector;

public class Material extends ShaderData {
    public PVector ambient;
    public PVector diffuse;
    public PVector specular;
    public float shininess;
    private int ambLoc, diffLoc, specLoc, shininessLoc;

    public Material(GamePageClass gamePageClass) {
        super(gamePageClass);
    }

    @Override
    protected void getLocations(int programId) {
        ambLoc =gl.glGetUniformLocation(programId, "material.ambient");
        diffLoc =gl.glGetUniformLocation(programId, "material.diffuse");
        specLoc =gl.glGetUniformLocation(programId, "material.specular");
        shininessLoc =gl.glGetUniformLocation(programId, "material.shininess");
    }

    @Override
    protected void forwardData() {
        //no automatic application on shader enable
    }

    @Override
    protected void delete() {
    }

    public void apply() {
        gl.glUniform3f(ambLoc, ambient.x, ambient.y, ambient.z);
        gl.glUniform3f(diffLoc, diffuse.x, diffuse.y, diffuse.z);
        gl.glUniform3f(specLoc, specular.x, specular.y, specular.z);
        gl.glUniform1f(shininessLoc, shininess);
    }
}
