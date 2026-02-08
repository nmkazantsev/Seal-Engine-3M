package com.nikitos.main.touch;

public interface MyMotionEvent {

    int ACTION_DOWN = 0;
    int ACTION_UP = 1;
    int ACTION_MOVE = 2;
    int ACTION_POINTER_DOWN = 5;
    int ACTION_POINTER_UP = 6;

    int getActionMasked();

    int getActionIndex();

    int getPointerId(int index);

    int getPointerCount();

    float getX(int index);

    float getY(int index);
}
