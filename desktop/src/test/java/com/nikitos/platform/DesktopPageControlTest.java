package com.nikitos.platform;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.runtime.PageTransition;
import com.nikitos.runtime.RuntimeObserver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DesktopPageControlTest {

    @Test
    void activeOwnerThreadPublishesExactSameClassPageTransitions() {
        RecordingObserver observer = new RecordingObserver();
        Engine engine = createEngine(observer);
        DesktopWindowControl windowControl = attachedWindowControl();
        DesktopPageControl pageControl = new DesktopPageControl(windowControl, engine);
        SameClassPage first = new SameClassPage();
        SameClassPage second = new SameClassPage();

        pageControl.requestPage(first);
        pageControl.requestPage(second);

        assertAll(
                () -> assertEquals(2, observer.transitions.size()),
                () -> assertNull(
                        observer.transitions.get(0).getPreviousPage()
                ),
                () -> assertSame(
                        first,
                        observer.transitions.get(0).getNewPage()
                ),
                () -> assertSame(
                        first,
                        observer.transitions.get(1).getPreviousPage()
                ),
                () -> assertSame(
                        second,
                        observer.transitions.get(1).getNewPage()
                )
        );
    }

    @Test
    void pageRequestsFailBeforeAttachAndAfterDetachWithoutTransition() {
        RecordingObserver observer = new RecordingObserver();
        Engine engine = createEngine(observer);
        DesktopWindowControl windowControl = new DesktopWindowControl(
                new NoOpNativeWindow(),
                Thread.currentThread()
        );
        DesktopPageControl pageControl = new DesktopPageControl(windowControl, engine);

        assertThrows(
                IllegalStateException.class,
                () -> pageControl.requestPage(new SameClassPage())
        );
        windowControl.attachWindow(61L);
        windowControl.detachWindow();
        assertThrows(
                IllegalStateException.class,
                () -> pageControl.requestPage(new SameClassPage())
        );

        assertTrue(observer.transitions.isEmpty());
    }

    @Test
    void pageRequestsRejectNonOwnerThreadWithoutTransition()
            throws InterruptedException {
        RecordingObserver observer = new RecordingObserver();
        Engine engine = createEngine(observer);
        DesktopWindowControl windowControl = attachedWindowControl();
        DesktopPageControl pageControl = new DesktopPageControl(windowControl, engine);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread caller = new Thread(() ->
                failure.set(catchFailure(
                        () -> pageControl.requestPage(new SameClassPage())
                ))
        );
        caller.start();
        caller.join();

        assertAll(
                () -> assertTrue(failure.get() instanceof IllegalStateException),
                () -> assertTrue(observer.transitions.isEmpty())
        );
    }

    private static Engine createEngine(RecordingObserver observer) {
        Engine engine = new Engine(
                new DesktopBridge(),
                new LauncherParams().setRuntimeObserver(observer)
        );
        CoreRenderer.engine = engine;
        return engine;
    }

    private static DesktopWindowControl attachedWindowControl() {
        DesktopWindowControl control = new DesktopWindowControl(
                new NoOpNativeWindow(),
                Thread.currentThread()
        );
        control.attachWindow(61L);
        return control;
    }

    private static Throwable catchFailure(Runnable action) {
        try {
            action.run();
            return null;
        } catch (Throwable failure) {
            return failure;
        }
    }

    private static final class RecordingObserver implements RuntimeObserver {
        private final List<PageTransition> transitions = new ArrayList<>();

        @Override
        public void onPageChanged(PageTransition pageTransition) {
            transitions.add(pageTransition);
        }
    }

    private static final class SameClassPage extends GamePageClass {
        @Override
        public void onSurfaceChanged(int width, int height) {
        }

        @Override
        public void draw() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static final class NoOpNativeWindow
            implements DesktopWindowControl.NativeWindowOperations {
        @Override
        public void setWindowShouldClose(long window, boolean shouldClose) {
        }

        @Override
        public void setWindowSize(long window, int width, int height) {
        }
    }
}
