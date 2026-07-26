package com.nikitos.platform;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesktopWindowControlTest {

    @Test
    void activeControlRequestsCleanWindowClose() {
        RecordingNativeWindow nativeWindow = new RecordingNativeWindow();
        DesktopWindowControl control = new DesktopWindowControl(
                nativeWindow,
                Thread.currentThread()
        );
        control.attachWindow(37L);

        control.requestStop();

        assertAll(
                () -> assertEquals(1, nativeWindow.closeCalls),
                () -> assertEquals(37L, nativeWindow.closeWindow),
                () -> assertTrue(nativeWindow.shouldClose)
        );
    }

    @Test
    void activeControlRequestsExactPositiveWindowSize() {
        RecordingNativeWindow nativeWindow = new RecordingNativeWindow();
        DesktopWindowControl control = new DesktopWindowControl(
                nativeWindow,
                Thread.currentThread()
        );
        control.attachWindow(91L);

        control.requestWindowSize(1280, 720);

        assertAll(
                () -> assertEquals(1, nativeWindow.resizeCalls),
                () -> assertEquals(91L, nativeWindow.resizeWindow),
                () -> assertEquals(1280, nativeWindow.width),
                () -> assertEquals(720, nativeWindow.height)
        );
    }

    @Test
    void windowRequestsFailOutsideAttachedLifecycle() {
        RecordingNativeWindow nativeWindow = new RecordingNativeWindow();
        DesktopWindowControl control = new DesktopWindowControl(
                nativeWindow,
                Thread.currentThread()
        );

        assertThrows(IllegalStateException.class, control::requestStop);
        assertThrows(
                IllegalStateException.class,
                () -> control.requestWindowSize(640, 480)
        );
        assertThrows(
                IllegalStateException.class,
                () -> control.requestWindowSize(0, 480)
        );

        control.attachWindow(52L);
        control.detachWindow();

        assertAll(
                () -> assertThrows(
                        IllegalStateException.class,
                        control::requestStop
                ),
                () -> assertThrows(
                        IllegalStateException.class,
                        () -> control.requestWindowSize(0, 480)
                ),
                () -> assertEquals(0, nativeWindow.closeCalls),
                () -> assertEquals(0, nativeWindow.resizeCalls)
        );
    }

    @Test
    void resizeRejectsNonPositiveDimensionsWithoutNativeCall() {
        RecordingNativeWindow nativeWindow = new RecordingNativeWindow();
        DesktopWindowControl control = new DesktopWindowControl(
                nativeWindow,
                Thread.currentThread()
        );
        control.attachWindow(73L);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> control.requestWindowSize(0, 480)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> control.requestWindowSize(640, -1)
                ),
                () -> assertEquals(0, nativeWindow.resizeCalls)
        );
    }

    @Test
    void windowRequestsRejectNonOwnerThreadWithoutNativeCall()
            throws InterruptedException {
        RecordingNativeWindow nativeWindow = new RecordingNativeWindow();
        DesktopWindowControl control = new DesktopWindowControl(
                nativeWindow,
                Thread.currentThread()
        );
        control.attachWindow(84L);
        AtomicReference<Throwable> stopFailure = new AtomicReference<>();
        AtomicReference<Throwable> resizeFailure = new AtomicReference<>();

        Thread caller = new Thread(() -> {
            stopFailure.set(catchFailure(control::requestStop));
            resizeFailure.set(catchFailure(
                    () -> control.requestWindowSize(800, 600)
            ));
        });
        caller.start();
        caller.join();

        assertAll(
                () -> assertTrue(
                        stopFailure.get() instanceof IllegalStateException
                ),
                () -> assertTrue(
                        resizeFailure.get() instanceof IllegalStateException
                ),
                () -> assertEquals(0, nativeWindow.closeCalls),
                () -> assertEquals(0, nativeWindow.resizeCalls)
        );
    }

    private static Throwable catchFailure(Runnable action) {
        try {
            action.run();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    private static final class RecordingNativeWindow
            implements DesktopWindowControl.NativeWindowOperations {
        private int closeCalls;
        private long closeWindow;
        private boolean shouldClose;
        private int resizeCalls;
        private long resizeWindow;
        private int width;
        private int height;

        @Override
        public void setWindowShouldClose(long window, boolean shouldClose) {
            closeCalls++;
            closeWindow = window;
            this.shouldClose = shouldClose;
        }

        @Override
        public void setWindowSize(long window, int width, int height) {
            resizeCalls++;
            resizeWindow = window;
            this.width = width;
            this.height = height;
        }
    }
}
