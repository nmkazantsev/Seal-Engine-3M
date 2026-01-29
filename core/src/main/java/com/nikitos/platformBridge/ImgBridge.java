package com.nikitos.platformBridge;

public abstract class ImgBridge {
    public abstract float getWidth(Object bitmap);
    public abstract float getHeight(Object bitmap);

    public abstract void recycleBitmap(Object bitmap);
}
