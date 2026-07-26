package com.seal.gl_engine.platform;

import java.util.function.Supplier;

final class AndroidViewLifecycle<T> {
    private final Callbacks<T> callbacks;
    private T current;
    private boolean paused;

    AndroidViewLifecycle(Callbacks<T> callbacks) {
        this.callbacks = callbacks;
    }

    synchronized T replace(Supplier<T> replacementFactory) {
        T previous = current;
        if (previous != null && !paused) {
            callbacks.quiesce(previous);
        }

        T replacement;
        try {
            replacement = replacementFactory.get();
        } catch (RuntimeException | Error activationFailure) {
            restore(previous, activationFailure);
            throw activationFailure;
        }
        if (replacement == null) {
            restore(previous, null);
            return null;
        }

        current = replacement;
        try {
            callbacks.attach(replacement, paused);
        } catch (RuntimeException | Error attachFailure) {
            current = previous;
            restore(previous, attachFailure);
            throw attachFailure;
        }
        return replacement;
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

    private void restore(T previous, Throwable primaryFailure) {
        if (previous == null) {
            return;
        }
        try {
            callbacks.restore(previous, paused);
        } catch (RuntimeException | Error restoreFailure) {
            if (primaryFailure == null) {
                throw restoreFailure;
            }
            if (primaryFailure != restoreFailure) {
                primaryFailure.addSuppressed(restoreFailure);
            }
        }
    }

    interface Callbacks<T> {
        void attach(T view, boolean paused);

        void pause(T view);

        void resume(T view);

        void detach(T view);

        void quiesce(T view);

        void restore(T view, boolean paused);
    }
}
