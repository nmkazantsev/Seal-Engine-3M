package com.nikitos.main.touch;

public class MouseWheelData {
    public float mouseX;
    public float mouseY;
    public float wheelX;
    public float wheelY;

    public MouseWheelData() {
        this(0, 0, 0, 0);
    }

    public MouseWheelData(float mouseX, float mouseY, float wheelX, float wheelY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.wheelX = wheelX;
        this.wheelY = wheelY;
    }

    public MouseWheelData set(float mouseX, float mouseY, float wheelX, float wheelY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.wheelX = wheelX;
        this.wheelY = wheelY;
        return this;
    }
}
