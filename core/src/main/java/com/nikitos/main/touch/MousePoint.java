package com.nikitos.main.touch;

/**
 * Simple immutable mouse position snapshot.
 */
public class MousePoint {
    public final float mouseX;
    public final float mouseY;

    public MousePoint(float mouseX, float mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
