package com.nikitos.main.light;


import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SourceLight extends ShaderData {
    public PVector color;
    public PVector position;
    public PVector direction;
    public float diffuse;
    public float specular;
    public float constant, linear, quadratic, cutOff, outerCutOff;

    private int colorLoc, posLoc, diffuseLoc, specLoc, numberLoc, constLoc, linLoc, quadLoc, cutOffLoc, outerCutOffLoc, directionLoc;
    private int index;
    private static final List<WeakReference<SourceLight>> sourceLights = new ArrayList<>();
    private final WeakReference<SourceLight> thisRef;//link to this object for deleting later


    public SourceLight(GamePageClass gamePageClass) {
        super(gamePageClass);
        index = sourceLights.size();
        thisRef = new WeakReference<>(this);
        sourceLights.add(thisRef);
    }

    //always use before apply shader
    public void deleteLight() {
        sourceLights.remove(index);
        for (int i = 0; i < sourceLights.size(); i++) {
            sourceLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    protected void getLocations(int programId) {
        colorLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].color");
        posLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].position");
        diffuseLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].diffuse");
        specLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].specular");
        constLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].constant");
        linLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].linear");
        quadLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].quadratic");
        numberLoc = gl.glGetUniformLocation(programId, "sLightNum");
        directionLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].direction");
        cutOffLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].cutOff");
        outerCutOffLoc = gl.glGetUniformLocation(programId, "sLights[" + index + "].outerCutOff");

    }

    @Override
    protected void forwardData() {
        gl.glUniform3f(posLoc, position.x, position.y, position.z);
        gl.glUniform3f(colorLoc, color.x, color.y, color.z);
        gl.glUniform3f(directionLoc, direction.x, direction.y, direction.z);
        gl.glUniform1f(specLoc, specular);
        gl.glUniform1f(diffuseLoc, diffuse);
        gl.glUniform1f(constLoc, constant);
        gl.glUniform1f(linLoc, linear);
        gl.glUniform1f(quadLoc, quadratic);
        gl.glUniform1f(cutOffLoc, cutOff);
        gl.glUniform1f(outerCutOffLoc, outerCutOff);
        gl.glUniform1i(numberLoc, sourceLights.size());
    }

    @Override
    protected void delete() {
        sourceLights.remove(thisRef);
    }
}
