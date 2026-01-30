package com.nikitos.main.images;


import com.nikitos.CoreRenderer;
import com.nikitos.maths.Section;

public class PImage {
    private boolean upperText = false;
    private final AbstractImage impl;

    public PImage(AbstractImage impl) {
        this.impl = impl;
    }

    public PImage() {
        this.impl = CoreRenderer.engine.getPlatformBridge().getAbstractImage();
    }

    public PImage(int width, int height) {
        this.impl = CoreRenderer.engine.getPlatformBridge().getAbstractImage();
        impl.createBitmap(width, height);
    }

    public void setUpperText(boolean upperText) {
        this.upperText = upperText;
    }

    public boolean getUpperText() {
        return upperText;
    }

    public void delete() {
        impl.delete();
    }

    public boolean isLoaded() {
        return impl.isLoaded();
    }

    public void setLoaded(boolean v) {
        impl.setLoaded(v);
    }

    public void background(float r, float g, float b, float a) {
        impl.background((int) (r), (int) (g), (int) (b), (int) (a));
    }

    public void rect(float x, float y, float w, float h) {
        impl.rect(x, y, w, h);
    }

    public void roundRect(float x, float y, float w, float h, float rx, float ry) {
        impl.roundRect(x, y, w, h, rx, ry);
    }

    public void ellipse(float x, float y, float rx, float ry) {
        impl.ellipse(x, y, rx, ry);
    }

    public void fill(float r, float g, float b, float a) {
        impl.fill((int) (r), (int) (g), (int) (b), (int) (a));
    }

    public void stroke(float r, float g, float b, float a) {
        impl.stroke((int) (r), (int) (g), (int) (b), (int) (a));
    }

    public void strokeWeight(float w) {
        impl.strokeWeight(w);
    }

    public void noStroke() {
        impl.noStroke();
    }

    public void line(float x1, float y1, float x2, float y2) {
        impl.line(x1, y1, x2, y2);
    }

    public void line(Section s) {
        impl.line(s);
    }

    public void textSize(float s) {
        impl.textSize(s);
    }

    public void text(String s, float x, float y) {
        impl.text(s, x, y, upperText);
    }

    public void image(PImage img, float x, float y) {
        impl.image(img.impl, x, y);
    }

    public void image(PImage img, float x, float y, float w, float h) {
        impl.image(img.impl, x, y, w, h);
    }

    public void image(PImage img, float x, float y, float scale) {
        impl.image(img.impl, x, y, scale);
    }

    public void rotImage(PImage img, float x, float y, float scale, float rot) {
        impl.rotImage(img.impl, x, y, scale, rot);
    }

    public int getWidth() {
        return impl.getWidth();
    }


    public int getHeight() {
        return impl.getHeight();
    }

    public Object getBitmap() {
        return impl.getBitmap();
    }

}
