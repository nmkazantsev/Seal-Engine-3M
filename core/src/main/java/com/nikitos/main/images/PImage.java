package com.nikitos.main.images;

import com.nikitos.CoreRenderer;
import com.nikitos.platformBridge.ImgBridge;

public class PImage {
    protected final ImgBridge imgBridge;
    private boolean isLoaded = false;

    public PImage() {
        imgBridge = CoreRenderer.engine.getPlatformBridge().getImgBridge();
    }


    // Общие поля и свойства
    public abstract float getWidth();

    public abstract float getHeight();

    public abstract boolean isLoaded();

    // Основные методы рисования
    public abstract void background(int r, int g, int b);

    public abstract void background(float r, float g, float b, float a);

    public abstract void rect(float x, float y, float width, float height);

    public abstract void ellipse(float centerX, float centerY, float radiusX, float radiusY);

    public abstract void line(float x1, float y1, float x2, float y2);

    public abstract void text(String text, float x, float y);

    // Методы стилей
    public abstract void fill(int r, int g, int b);

    public abstract void fill(int r, int g, int b, int a);

    public abstract void stroke(int r, int g, int b);

    public abstract void strokeWeight(float weight);

    public abstract void noStroke();

    public abstract void textSize(float size);

    // Работа с изображениями
    public abstract void image(PImage img, float x, float y);

    public abstract void image(PImage img, float x, float y, float width, float height);

    public abstract void image(PImage img, float x, float y, float scale);

    public abstract void rotImage(PImage img, float x, float y, float scale, float rotation);

    // Утилиты
    public abstract void delete();

    public boolean getLoaded(){
        return isLoaded;
    }

    public void setLoaded(boolean loaded){
        isLoaded = loaded;
    }
}
