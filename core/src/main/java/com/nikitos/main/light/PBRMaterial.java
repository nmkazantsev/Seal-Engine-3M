package com.nikitos.main.light;

import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.textures.Texture;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.util.HashMap;
import java.util.Map;

import static com.nikitos.utils.FileUtils.loadImage;

/**
 * Material values and optional image paths for the engine's PBR-style shader.
 * Map images are optional; absent maps use these scalar defaults.
 */
public class PBRMaterial {
    public PVector baseColor = new PVector(1f);
    public String baseColorMapPath;
    public String normalMapPath;
    public String metallicMapPath;
    public String roughnessMapPath;
    public String ambientOcclusionMapPath;

    public float metallic = 0f;
    public float roughness = 0.7f;
    public float ambientOcclusion = 1f;

    public boolean isNormalMapEnabled() {
        return normalMapPath != null && !normalMapPath.isBlank();
    }

    public boolean[] bindTextures(GamePageClass page, GeneralPlatformBridge gl, GLConstBridge glc,
                                  boolean shapeHasNormalMap) {
        String[] paths = {baseColorMapPath, normalMapPath, metallicMapPath, roughnessMapPath, ambientOcclusionMapPath};
        boolean[] enabled = new boolean[paths.length];
        for (String path : paths) {
            if (path != null && !path.isBlank()) {
                maps.computeIfAbsent(path, key -> new MapTexture(page, key));
            }
        }
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            if (path == null || path.isBlank()) {
                enabled[i] = i == 0 || (i == 1 && shapeHasNormalMap);
                continue;
            }
            MapTexture map = maps.computeIfAbsent(path, key -> new MapTexture(page, key));
            map.bind(gl, glc, i);
            enabled[i] = true;
        }
        return enabled;
    }

    private final Map<String, MapTexture> maps = new HashMap<>();

    private static class MapTexture extends Texture {
        private final PImage image;
        private int uploadedId = -1;

        private MapTexture(GamePageClass page, String path) {
            super(page, true);
            image = loadImage(path);
        }

        private void bind(GeneralPlatformBridge gl, GLConstBridge glc, int unit) {
            gl.glActiveTexture(glc.GL_TEXTURE0() + unit);
            gl.glBindTexture(glc.GL_TEXTURE_2D(), getId());
            if (uploadedId != getId() && image != null && image.isLoaded()) {
                gl.texImage2D(glc.GL_TEXTURE_2D(), 0, glc.GL_RGBA(), image, glc.GL_UNSIGNED_BYTE(), 0);
                gl.glGenerateMipmap(glc.GL_TEXTURE_2D());
                uploadedId = getId();
            }
        }

        @Override
        public void reload() {
            super.reload();
            uploadedId = -1;
        }

        @Override
        public void delete() {
            super.delete();
            if (image != null) {
                image.delete();
            }
        }
    }
}
