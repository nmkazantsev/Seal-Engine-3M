package com.seal.gl_engine.touch;

import com.nikitos.main.touch.MyMotionEvent;

import java.util.Objects;

public final class AndroidMotionEventAdapter implements MyMotionEvent {
    private final int actionMasked;
    private final int actionIndex;
    private final int[] pointerIds;
    private final float[] xs;
    private final float[] ys;

    public AndroidMotionEventAdapter(android.view.MotionEvent event) {
        this(new MotionEventView(event));
    }

    AndroidMotionEventAdapter(MyMotionEvent event) {
        Objects.requireNonNull(event, "event");
        actionMasked = event.getActionMasked();
        actionIndex = event.getActionIndex();
        int pointerCount = event.getPointerCount();
        pointerIds = new int[pointerCount];
        xs = new float[pointerCount];
        ys = new float[pointerCount];
        for (int index = 0; index < pointerCount; index++) {
            pointerIds[index] = event.getPointerId(index);
            xs[index] = event.getX(index);
            ys[index] = event.getY(index);
        }
    }

    @Override
    public int getActionMasked() {
        return actionMasked;
    }

    @Override
    public int getActionIndex() {
        return actionIndex;
    }

    @Override
    public int getPointerId(int index) {
        return pointerIds[index];
    }

    @Override
    public int getPointerCount() {
        return pointerIds.length;
    }

    @Override
    public float getX(int index) {
        return xs[index];
    }

    @Override
    public float getY(int index) {
        return ys[index];
    }

    private static final class MotionEventView implements MyMotionEvent {
        private final android.view.MotionEvent event;

        private MotionEventView(android.view.MotionEvent event) {
            this.event = Objects.requireNonNull(event, "event");
        }

        @Override
        public int getActionMasked() {
            return event.getActionMasked();
        }

        @Override
        public int getActionIndex() {
            return event.getActionIndex();
        }

        @Override
        public int getPointerId(int index) {
            return event.getPointerId(index);
        }

        @Override
        public int getPointerCount() {
            return event.getPointerCount();
        }

        @Override
        public float getX(int index) {
            return event.getX(index);
        }

        @Override
        public float getY(int index) {
            return event.getY(index);
        }
    }
}
