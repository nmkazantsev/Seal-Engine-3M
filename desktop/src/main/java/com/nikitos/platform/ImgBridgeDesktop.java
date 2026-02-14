package com.nikitos.platform;

import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.ImgBridge;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import io.github.humbleui.skija.*;
import main.images.PImageDesktop;

public class ImgBridgeDesktop extends ImgBridge {
    @Override
    public float getWidth(Object bitmap) {
        return 0;
    }

    @Override
    public float getHeight(Object bitmap) {
        return 0;
    }

    @Override
    public void recycleBitmap(Object bitmap) {

    }

    @Override
    public PImage loadImage(InputStream stream) {
        try {
            byte[] bytes = stream.readAllBytes();
            Image image = Image.makeFromEncoded(bytes);
            if (image == null) {
                throw new RuntimeException("Failed to decode image");
            }

            int width = image.getWidth();
            int height = image.getHeight();

            // 4. Создаём твой PImage
            PImage result = new PImage(new PImageDesktop(image));
            image.close();
            return  result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load image", e);
        }
    }

}
