package com.nikitos.main.images;

public abstract class AbstractFont {
    protected boolean loaded = false;
    public abstract void loadFromAsset(String assetPath);   // для Android assets
    public abstract void close();
    public abstract boolean isLoaded();
    public abstract Object getPlatformFont();               // возвращает Typeface (Android) или Font (Skija)
}
