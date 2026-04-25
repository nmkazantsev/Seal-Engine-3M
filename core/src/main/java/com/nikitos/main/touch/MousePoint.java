package com.nikitos.main.touch;

public class MousePoint {
    public float mouseX;
    public float mouseY;

    public MousePoint() {
        this(0, 0);
    }

    public MousePoint(float mouseX, float mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public MousePoint set(float mouseX, float mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        return this;
    }
}
