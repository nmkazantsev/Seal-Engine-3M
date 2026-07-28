package com.seal.gl_engine.touch;


import com.nikitos.main.touch.MyMotionEvent;

public class AndroidMotionEventAdapter implements MyMotionEvent {

    private final android.view.MotionEvent e;

    public AndroidMotionEventAdapter(android.view.MotionEvent e) {
        this.e = e;
    }

    @Override
    public int getActionMasked() {
        return e.getActionMasked();
    }

    @Override
    public int getActionIndex() {
        return e.getActionIndex();
    }

    @Override
    public int getPointerId(int index) {
        return e.getPointerId(index);
    }

    @Override
    public int getPointerCount() {
        return e.getPointerCount();
    }

    @Override
    public float getX(int index) {
        return e.getX(index);
    }

    @Override
    public float getY(int index) {
        return e.getY(index);
    }
}

