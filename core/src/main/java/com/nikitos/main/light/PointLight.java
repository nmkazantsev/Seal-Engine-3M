package com.nikitos.main.light;


import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PointLight extends ShaderData {
    public PVector color;
    public PVector position;
    public float diffuse;
    public float specular;
    public float constant, linear, quadratic;

    private int colorLoc, posLoc, diffuseLoc, specLoc, numberLoc, constLoc, linLoc, quadLoc;
    private int index;
    private static final List<WeakReference<PointLight>> pointLights = new ArrayList<>();
    private final WeakReference<PointLight> thisRef;//link to this object for deleting later


    public PointLight(GamePageClass gamePageClass) {
        super(gamePageClass);
        index = pointLights.size();
        thisRef = new WeakReference<>(this);
        pointLights.add(thisRef);
    }

    //always use before apply shader
    public void deleteLight() {
        pointLights.remove(index);
        for (int i = 0; i < pointLights.size(); i++) {
            pointLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    protected void getLocations(int programId) {
        colorLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].color");
        posLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].position");
        diffuseLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].diffuse");
        specLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].specular");
        constLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].constant");
        linLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].linear");
        quadLoc =gl.glGetUniformLocation(programId, "pLights[" + index + "].quadratic");
        numberLoc =gl.glGetUniformLocation(programId, "pLightNum");
    }

    @Override
    protected void forwardData() {
        gl.glUniform3f(posLoc, position.x, position.y, position.z);
        gl.glUniform3f(colorLoc, color.x, color.y, color.z);
        gl.glUniform1f(specLoc, specular);
        gl.glUniform1f(diffuseLoc, diffuse);
        gl.glUniform1f(constLoc, constant);
        gl.glUniform1f(linLoc, linear);
        gl.glUniform1f(quadLoc, quadratic);
        gl.glUniform1i(numberLoc, pointLights.size());
    }

    @Override
    protected void delete() {
        pointLights.remove(thisRef);
    }
}


