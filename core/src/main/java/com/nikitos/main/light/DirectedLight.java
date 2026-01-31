package com.nikitos.main.light;


import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DirectedLight extends ShaderData {

    public PVector color;
    public PVector direction;
    public float diffuse;
    public float specular;

    private int colorLoc, directionLoc, diffuseLoc, specLoc, numberLoc;
    private int index;
    private static final List<WeakReference<DirectedLight>> directLights = new ArrayList<>();
    private final WeakReference<DirectedLight> thisRef;//link to this object for deleting later

    public DirectedLight(GamePageClass gamePageClass) {
        super(gamePageClass);
        index = directLights.size();
        thisRef = new WeakReference<>(this);
        directLights.add(thisRef);
    }

    //always use before apply shader
    public void deleteLight() {
        directLights.remove(index);
        for (int i = 0; i < directLights.size(); i++) {
            directLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    protected void getLocations(int programId) {
        colorLoc = gl.glGetUniformLocation(programId, "dLights[" + index + "].color");
        directionLoc = gl.glGetUniformLocation(programId, "dLights[" + index + "].direction");
        diffuseLoc = gl.glGetUniformLocation(programId, "dLights[" + index + "].diffuse");
        specLoc = gl.glGetUniformLocation(programId, "dLights[" + index + "].specular");
        numberLoc = gl.glGetUniformLocation(programId, "dLightNum");
    }

    @Override
    protected void forwardData() {
        gl.glUniform3f(directionLoc, direction.x, direction.y, direction.z);
        gl.glUniform3f(colorLoc, color.x, color.y, color.z);
        gl.glUniform1f(specLoc, specular);
        gl.glUniform1f(diffuseLoc, diffuse);
        gl.glUniform1i(numberLoc, directLights.size());
    }

    @Override
    protected void delete() {
        directLights.remove(thisRef);
    }
}
