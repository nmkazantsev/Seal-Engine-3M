package com.nikitos.main.shaders.default_adaptors;

import com.nikitos.main.light.PBRMaterial;
import com.nikitos.main.vertices.Shape;

public class PBRShaderAdaptor extends LightShaderAdaptor {
    private final int[] mapLocations = new int[5];
    private final int[] mapEnabledLocations = new int[5];
    private int baseColorLocation;
    private int metallicLocation;
    private int roughnessLocation;
    private int aoLocation;

    @Override
    public void updateLocations() {
        super.updateLocations();
        int program = programId;
        String[] maps = {"u_TextureUnit", "normalMap", "metallicMap", "roughnessMap", "aoMap"};
        String[] enabled = {"baseColorMapEnable", "normalMapEnable", "metallicMapEnable", "roughnessMapEnable", "aoMapEnable"};
        for (int i = 0; i < maps.length; i++) {
            mapLocations[i] = gl.glGetUniformLocation(program, maps[i]);
            mapEnabledLocations[i] = gl.glGetUniformLocation(program, enabled[i]);
        }
        baseColorLocation = gl.glGetUniformLocation(program, "baseColor");
        metallicLocation = gl.glGetUniformLocation(program, "metallic");
        roughnessLocation = gl.glGetUniformLocation(program, "roughness");
        aoLocation = gl.glGetUniformLocation(program, "ambientOcclusion");
        for (int i = 0; i < mapLocations.length; i++) {
            gl.glUniform1i(mapLocations[i], i);
        }
    }

    public void bindMaterial(PBRMaterial material, Shape shape) {
        boolean[] enabled = material.bindTextures(shape.getCreator(), gl, glConstBridge, shape.hasNormalMap());
        for (int i = 0; i < enabled.length; i++) {
            gl.glUniform1i(mapEnabledLocations[i], enabled[i] ? 1 : 0);
        }
        gl.glUniform3f(baseColorLocation, material.baseColor.x, material.baseColor.y, material.baseColor.z);
        gl.glUniform1f(metallicLocation, material.metallic);
        gl.glUniform1f(roughnessLocation, material.roughness);
        gl.glUniform1f(aoLocation, material.ambientOcclusion);
        gl.glActiveTexture(glConstBridge.GL_TEXTURE0());
    }
}
