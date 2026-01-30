package com.nikitos.main.images;

import com.nikitos.maths.Section;

public abstract class AbstractImage {
    protected int width;
    protected int height;

    /* lifecycle */
    public abstract void delete();
    public abstract boolean isLoaded();
    public abstract void setLoaded(boolean loaded);

    /* background */
    public abstract void background(int r, int g, int b, int a);

    /* shapes */
    public abstract void rect(float x, float y, float w, float h);
    public abstract void roundRect(float x, float y, float w, float h, float rx, float ry);
    public abstract void ellipse(float cx, float cy, float rx, float ry);
    public abstract void line(float x1, float y1, float x2, float y2);
    public abstract void line(Section section);

    /* paint */
    public abstract void fill(int r, int g, int b, int a);
    public abstract void stroke(int r, int g, int b, int a);
    public abstract void strokeWeight(float w);
    public abstract void noStroke();

    /* text */
    public abstract void textSize(float size);
    public abstract void textAlign(int align);
    public abstract void text(String text, float x, float y, boolean upperText);
    public abstract void setFont(String font);

    /* images */
    public abstract void image(AbstractImage img, float x, float y);
    public abstract void image(AbstractImage img, float x, float y, float w, float h);
    public abstract void image(AbstractImage img, float x, float y, float scale);
    public abstract void rotImage(AbstractImage img, float x, float y, float scale, float rotRad);

    public abstract int getWidth();
    public abstract int getHeight();

    public abstract Object getBitmap();

    /* utils */
    protected float degrees(float rad) {
        return (float) Math.toDegrees(rad);
    }

    protected float radians(float deg) {
        return (float) Math.toRadians(deg);
    }

    public abstract void createBitmap(int width, int height) ;
}
