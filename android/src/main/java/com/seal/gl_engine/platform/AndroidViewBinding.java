package com.seal.gl_engine.platform;

final class AndroidViewBinding<T> {
    private final Operations<T> operations;
    private T current;
    private T paused;

    AndroidViewBinding(Operations<T> operations) {
        this.operations = operations;
    }

    synchronized void attach(T view, boolean attachPaused) {
        T previous = current;
        if (previous != null && previous != view && paused != previous) {
            operations.pause(previous);
        }
        current = view;
        if (previous != view) {
            paused = null;
        }
        if (attachPaused && paused != view) {
            operations.pause(view);
            paused = view;
        }
    }

    synchronized void pause(T expected) {
        if (current == expected && paused != expected) {
            operations.pause(expected);
            paused = expected;
        }
    }

    synchronized void pauseCurrent() {
        pause(current);
    }

    synchronized void resumeCurrent() {
        T view = current;
        if (view != null && paused == view) {
            operations.resume(view);
            paused = null;
        }
    }

    synchronized void restore(T view, boolean restorePaused) {
        T displaced = current;
        if (displaced != null
                && displaced != view
                && paused != displaced) {
            operations.pause(displaced);
        }
        boolean viewAlreadyPaused = paused == view;
        current = view;
        paused = null;
        if (restorePaused) {
            if (!viewAlreadyPaused) {
                operations.pause(view);
            }
            paused = view;
        } else {
            operations.resume(view);
        }
    }

    synchronized void detach(T expected) {
        if (current != expected) {
            return;
        }
        if (paused != expected) {
            operations.pause(expected);
        }
        current = null;
        paused = null;
    }

    interface Operations<T> {
        void pause(T view);

        void resume(T view);
    }
}
