package com.nikitos.main.touch;

public abstract class MotionEvent {

    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_POINTER_DOWN = 5;
    public static final int ACTION_POINTER_UP = 6;

    public abstract int getActionMasked();

    public abstract int getActionIndex();

    public abstract int getPointerId(int index);

    public abstract int getPointerCount();

    public abstract float getX(int index);

    public abstract float getY(int index);
}
