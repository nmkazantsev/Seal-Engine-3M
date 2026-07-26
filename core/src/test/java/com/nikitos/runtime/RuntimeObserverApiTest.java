package com.nikitos.runtime;

import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.platformBridge.Platform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RuntimeObserverApiTest {

    @Test
    void launcherParamsKeepsOptionalRuntimeObserverAndRemainsFluent() {
        LauncherParams launcherParams = new LauncherParams();
        RuntimeObserver observer = new RuntimeObserver() {
        };

        assertNull(launcherParams.getRuntimeObserver());
        assertSame(launcherParams, launcherParams.setRuntimeObserver(observer));
        assertSame(observer, launcherParams.getRuntimeObserver());
    }

    @Test
    void runtimeEventsExposeTheirOriginalPayloads() {
        GamePageClass previousPage = new TestPage();
        GamePageClass currentPage = new TestPage();
        FrameContext frameContext = new FrameContext(42L, currentPage, 1920, 1080, Platform.DESKTOP);
        IllegalStateException cause = new IllegalStateException("frame failed");
        PageTransition transition = new PageTransition(previousPage, currentPage);
        RuntimeFailure failure = new RuntimeFailure(
                RuntimeFailure.Stage.PAGE_DRAW,
                cause,
                frameContext,
                currentPage
        );

        assertAll(
                () -> assertEquals(42L, frameContext.getFrameId()),
                () -> assertSame(currentPage, frameContext.getPage()),
                () -> assertEquals(1920, frameContext.getWidth()),
                () -> assertEquals(1080, frameContext.getHeight()),
                () -> assertSame(Platform.DESKTOP, frameContext.getPlatform()),
                () -> assertSame(previousPage, transition.getPreviousPage()),
                () -> assertSame(currentPage, transition.getNewPage()),
                () -> assertSame(RuntimeFailure.Stage.PAGE_DRAW, failure.getStage()),
                () -> assertSame(cause, failure.getCause()),
                () -> assertSame(frameContext, failure.getFrameContext()),
                () -> assertSame(currentPage, failure.getPage()),
                () -> assertArrayEquals(
                        new RuntimeFailure.Stage[]{
                                RuntimeFailure.Stage.FRAME_SETUP,
                                RuntimeFailure.Stage.PAGE_DRAW,
                                RuntimeFailure.Stage.DEBUGGER_DRAW,
                                RuntimeFailure.Stage.VERTICES_REDRAW,
                                RuntimeFailure.Stage.TOUCH_PROCESS,
                                RuntimeFailure.Stage.KEYBOARD_PROCESS,
                                RuntimeFailure.Stage.PAGE_TRANSITION
                        },
                        RuntimeFailure.Stage.values()
                )
        );
    }

    @Test
    void frameContextKeepsLegacyConstructorAndCanExposeResourceCounts() {
        FrameContext legacy = new FrameContext(1L, null, 640, 480, Platform.DESKTOP);
        RuntimeResourceSnapshot legacyResources = new RuntimeResourceSnapshot(
                2,
                3,
                5,
                7,
                11,
                13,
                17
        );
        RuntimeResourceSnapshot resources = new RuntimeResourceSnapshot(
                2,
                3,
                5,
                7,
                11,
                13,
                17,
                19
        );
        FrameContext observed = new FrameContext(
                2L,
                null,
                1280,
                720,
                Platform.DESKTOP,
                resources
        );

        assertAll(
                () -> assertNull(legacy.getResourceSnapshot()),
                () -> assertEquals(0, legacyResources.getShaderData()),
                () -> assertSame(resources, observed.getResourceSnapshot()),
                () -> assertEquals(2, resources.getTrackedVramObjects()),
                () -> assertEquals(3, resources.getShaders()),
                () -> assertEquals(5, resources.getTouchProcessors()),
                () -> assertEquals(7, resources.getKeyboardPressListeners()),
                () -> assertEquals(11, resources.getKeyboardReleaseListeners()),
                () -> assertEquals(13, resources.getKeyboardComboListeners()),
                () -> assertEquals(17, resources.getDesktopMouseCallbackRegistrations()),
                () -> assertEquals(19, resources.getShaderData())
        );
    }

    @Test
    void defaultRuntimeObserverMethodsAreSafeNoOps() {
        RuntimeObserver observer = new RuntimeObserver() {
        };
        FrameContext frameContext = new FrameContext(1L, null, 0, 0, Platform.MOBILE);

        observer.beforeFrame(frameContext);
        observer.afterFrame(frameContext);
        observer.onPageChanged(new PageTransition(null, null));
        observer.onFailure(new RuntimeFailure(RuntimeFailure.Stage.FRAME_SETUP, null, null, null));
    }

    private static final class TestPage extends GamePageClass {
        @Override
        public void onSurfaceChanged(int x, int y) {
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
}
