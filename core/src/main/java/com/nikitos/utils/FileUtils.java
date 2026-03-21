package com.nikitos.utils;

import com.nikitos.CoreRenderer;
import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.ImgBridge;
import com.nikitos.platformBridge.PlatformBridge;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class FileUtils {
    private final PlatformBridge platformBridge;

    public FileUtils() {
        platformBridge = CoreRenderer.engine.getPlatformBridge();
    }

    public String readFileFromAssets( String fileName) {
        String result;
        try (InputStream is = platformBridge.getAssetManager().load(fileName)) {
            assert is != null;
            result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            return result;
        } catch (Exception e) {
            platformBridge.log_e("file utils", "ERROR opening " + fileName);
            throw new RuntimeException(e);
        }
    }

    public static PImage loadImage(InputStream inputStream) {
        ImgBridge imgBridge = CoreRenderer.engine.getPlatformBridge().getImgBridge();
        PImage img = imgBridge.loadImage(inputStream);
        img.setLoaded(true);
        return img;
    }

    public static PImage loadImage(String fileName) {
        InputStream inputStream = CoreRenderer.engine.getPlatformBridge().getAssetManager().load(fileName);
        return loadImage(inputStream);
    }
    
}