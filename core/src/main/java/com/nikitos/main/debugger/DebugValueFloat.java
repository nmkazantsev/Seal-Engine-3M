package com.nikitos.main.debugger;

/**
 * Create object of this class to create a debuggable from engine value
 * public value is your source value. Use it like a usual value, but it can be changed with debugger.
 */
public class DebugValueFloat {
    public float value;
    protected float min;
    protected float max;
    protected String name;

    protected DebugValueFloat(float min, float max,  String name) {
        this.max = max;
        this.min = min;
        this.name = name;
    }
}
