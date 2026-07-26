package com.nikitos;

import com.nikitos.platform.DesktopBridge;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.runtime.CapturedFrame;
import com.nikitos.runtime.FrameCaptureSource;
import com.nikitos.runtime.FrameContext;
import com.nikitos.runtime.RuntimeObserver;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

class RuntimeObserverCaptureTest {

    @Test
    void oldOneArgumentAfterFrameCallbackRemainsCompatibleWithoutCapturing() {
        RecordingCaptureSource source = new RecordingCaptureSource();
        CaptureBridge bridge = new CaptureBridge(source);
        AtomicInteger afterFrames = new AtomicInteger();
        RuntimeObserver observer = new RuntimeObserver() {
            @Override
            public void afterFrame(FrameContext frameContext) {
                afterFrames.incrementAndGet();
            }
        };

        renderer(bridge, observer).draw();

        assertAll(
                () -> assertEquals(1, afterFrames.get()),
                () -> assertEquals(1, bridge.captureSourceRequests),
                () -> assertEquals(0, source.captureCalls)
        );
    }

    @Test
    void nullObserverPathDoesNotEvenObtainCaptureSource() {
        RecordingCaptureSource source = new RecordingCaptureSource();
        CaptureBridge bridge = new CaptureBridge(source);

        renderer(bridge, null).draw();

        assertAll(
                () -> assertEquals(0, bridge.captureSourceRequests),
                () -> assertEquals(0, source.captureCalls)
        );
    }

    @Test
    void observerRequestedCaptureRunsExactlyOnceAndReturnsTopLeftRgba() {
        byte[] expected = new byte[]{
                9, 10, 11, 12, 13, 14, 15, 16,
                1, 2, 3, 4, 5, 6, 7, 8
        };
        RecordingCaptureSource source = new RecordingCaptureSource(
                new CapturedFrame(2, 2, expected)
        );
        CaptureBridge bridge = new CaptureBridge(source);
        AtomicReference<CapturedFrame> captured = new AtomicReference<>();
        RuntimeObserver observer = new RuntimeObserver() {
            @Override
            public void afterFrame(
                    FrameContext frameContext,
                    FrameCaptureSource captureSource
            ) {
                captured.set(captureSource.capture());
            }
        };

        renderer(bridge, observer).draw();

        assertAll(
                () -> assertEquals(1, bridge.captureSourceRequests),
                () -> assertEquals(1, source.captureCalls),
                () -> assertSame(source.frame, captured.get()),
                () -> assertArrayEquals(expected, captured.get().getRgba())
        );
    }

    @Test
    void desktopBridgeKeepsOneCaptureSourceAndStartsUnavailable() {
        DesktopBridge bridge = new DesktopBridge();

        FrameCaptureSource first = bridge.getFrameCaptureSource();
        FrameCaptureSource second = bridge.getFrameCaptureSource();

        assertAll(
                () -> assertSame(first, second),
                () -> assertFalse(first.isAvailable())
        );
    }

    private static CoreRenderer renderer(
            CaptureBridge bridge,
            RuntimeObserver observer
    ) {
        TestPage page = new TestPage();
        CaptureEngine engine = new CaptureEngine(bridge, observer, page);
        return new CoreRenderer(engine, ignored -> page);
    }

    private static final class CaptureBridge extends DesktopBridge {
        private final FrameCaptureSource source;
        private int captureSourceRequests;

        private CaptureBridge(FrameCaptureSource source) {
            this.source = source;
        }

        @Override
        public FrameCaptureSource getFrameCaptureSource() {
            captureSourceRequests++;
            return source;
        }
    }

    private static final class RecordingCaptureSource implements FrameCaptureSource {
        private final CapturedFrame frame;
        private int captureCalls;

        private RecordingCaptureSource() {
            this(new CapturedFrame(1, 1, new byte[]{0, 0, 0, 0}));
        }

        private RecordingCaptureSource(CapturedFrame frame) {
            this.frame = frame;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public CapturedFrame capture() {
            captureCalls++;
            return frame;
        }
    }

    private static final class CaptureEngine extends Engine {
        private final GamePageClass page;

        private CaptureEngine(
                DesktopBridge bridge,
                RuntimeObserver observer,
                GamePageClass page
        ) {
            super(
                    bridge,
                    observer == null
                            ? new LauncherParams()
                            : new LauncherParams().setRuntimeObserver(observer)
            );
            this.page = page;
        }

        @Override
        public void calculateFps() {
        }

        @Override
        GamePageClass getGamePage() {
            return page;
        }

        @Override
        public Class<?> getPageClass() {
            return page.getClass();
        }

        @Override
        public boolean getBsodAllowed() {
            return false;
        }
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
