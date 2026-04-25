package touch;

import com.nikitos.main.touch.MyMotionEvent;

public class DesktopMotionEventAdapter implements MyMotionEvent {

    private int actionMasked;
    private final int actionIndex;
    private float x;
    private float y;

    public DesktopMotionEventAdapter(int actionMasked, float x, float y) {
        this.actionMasked = actionMasked;
        this.actionIndex = 0;
        this.x = x;
        this.y = y;
    }

    public DesktopMotionEventAdapter set(int actionMasked, float x, float y) {
        this.actionMasked = actionMasked;
        this.x = x;
        this.y = y;
        return this;
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
        return 0; // мышь = один палец
    }

    @Override
    public int getPointerCount() {
        return 1;
    }

    @Override
    public float getX(int index) {
        return x;
    }

    @Override
    public float getY(int index) {
        return y;
    }
}
