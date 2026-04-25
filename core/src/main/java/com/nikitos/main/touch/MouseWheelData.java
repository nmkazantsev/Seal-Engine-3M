package com.nikitos.main.touch;

/**
 * Immutable mouse wheel event snapshot.
 */
public class MouseWheelData {
    public final float mouseX;
    public final float mouseY;
    public final float wheelX;
    public final float wheelY;

    public MouseWheelData(float mouseX, float mouseY, float wheelX, float wheelY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.wheelX = wheelX;
        this.wheelY = wheelY;
    }
}
