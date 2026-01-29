package com.nikitos.platformBridge;

import com.nikitos.main.images.PImage;

import java.io.InputStream;

public abstract class ImgBridge {
    public abstract float getWidth(Object bitmap);

    public abstract float getHeight(Object bitmap);

    public abstract void recycleBitmap(Object bitmap);

    public abstract PImage loadImage(InputStream stream);
}
