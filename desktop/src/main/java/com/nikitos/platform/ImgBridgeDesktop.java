package com.nikitos.platform;

import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.ImgBridge;
import main.images.PImageDesktop;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

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
            BufferedImage imBuff = ImageIO.read(stream);
            return new PImage(new PImageDesktop(imBuff));
        } catch (IOException e) {
            System.err.println("Failed to load image from " + stream);
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
