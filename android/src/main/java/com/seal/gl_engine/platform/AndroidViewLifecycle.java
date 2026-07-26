package com.seal.gl_engine.platform;

final class AndroidViewLifecycle<T> {
    private final Callbacks<T> callbacks;
    private T current;
    private boolean paused;

    AndroidViewLifecycle(Callbacks<T> callbacks) {
        this.callbacks = callbacks;
    }

    synchronized void attach(T view) {
        if (view == null) {
            throw new IllegalArgumentException("Android view cannot be null");
        }
        current = view;
        callbacks.attach(view, paused);
    }

    synchronized void pause(T expected) {
        if (expected != current || paused) {
            return;
        }
        callbacks.pause(expected);
        paused = true;
    }

    synchronized void resume(T expected) {
        if (expected != current || !paused) {
            return;
        }
        callbacks.resume(expected);
        paused = false;
    }

    synchronized void detach(T expected) {
        if (expected != current) {
            return;
        }
        callbacks.detach(expected);
        current = null;
    }

    synchronized T current() {
        return current;
    }

    interface Callbacks<T> {
        void attach(T view, boolean paused);

        void pause(T view);

        void resume(T view);

        void detach(T view);
    }
}
