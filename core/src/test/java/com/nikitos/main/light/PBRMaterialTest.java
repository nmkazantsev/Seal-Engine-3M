package com.nikitos.main.light;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class PBRMaterialTest {
    @Test
    void defaultsUseNeutralFallbacks() {
        PBRMaterial material = new PBRMaterial();

        assertEquals(1f, material.baseColor.x);
        assertEquals(1f, material.baseColor.y);
        assertEquals(1f, material.baseColor.z);
        assertFalse(material.isNormalMapEnabled());
        assertEquals(0f, material.metallic);
        assertEquals(0.7f, material.roughness);
        assertEquals(1f, material.ambientOcclusion);
        assertNull(material.baseColorMapPath);
        assertNull(material.normalMapPath);
        assertNull(material.metallicMapPath);
        assertNull(material.roughnessMapPath);
        assertNull(material.ambientOcclusionMapPath);
    }
}
