package com.seal.gl_engine.touch;

import com.nikitos.main.touch.MyMotionEvent;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AndroidMotionEventAdapterTest {

    @Test
    public void queuedAdapterKeepsCompletePointerSnapshotAfterSourceChanges() {
        MutableMotionEvent source = new MutableMotionEvent(
                MyMotionEvent.ACTION_POINTER_UP,
                1,
                new int[]{7, 11},
                new float[]{12.5f, 25.5f},
                new float[]{30.5f, 40.5f}
        );
        AndroidMotionEventAdapter snapshot =
                new AndroidMotionEventAdapter(source);

        source.actionMasked = MyMotionEvent.ACTION_MOVE;
        source.actionIndex = 0;
        source.pointerIds[0] = 99;
        source.pointerIds[1] = 100;
        source.xs[0] = -1.0f;
        source.xs[1] = -2.0f;
        source.ys[0] = -3.0f;
        source.ys[1] = -4.0f;

        assertEquals(MyMotionEvent.ACTION_POINTER_UP, snapshot.getActionMasked());
        assertEquals(1, snapshot.getActionIndex());
        assertEquals(2, snapshot.getPointerCount());
        assertEquals(7, snapshot.getPointerId(0));
        assertEquals(11, snapshot.getPointerId(1));
        assertEquals(12.5f, snapshot.getX(0), 0.0f);
        assertEquals(25.5f, snapshot.getX(1), 0.0f);
        assertEquals(30.5f, snapshot.getY(0), 0.0f);
        assertEquals(40.5f, snapshot.getY(1), 0.0f);
    }

    private static final class MutableMotionEvent implements MyMotionEvent {
        private int actionMasked;
        private int actionIndex;
        private final int[] pointerIds;
        private final float[] xs;
        private final float[] ys;

        private MutableMotionEvent(
                int actionMasked,
                int actionIndex,
                int[] pointerIds,
                float[] xs,
                float[] ys
        ) {
            this.actionMasked = actionMasked;
            this.actionIndex = actionIndex;
            this.pointerIds = pointerIds;
            this.xs = xs;
            this.ys = ys;
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
    }
}
