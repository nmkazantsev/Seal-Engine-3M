package com.nikitos.runtime;

import com.nikitos.platformBridge.LauncherParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameCaptureContractTest {

    @Test
    void capturedFrameValidatesDimensionsAndRgbaLength() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CapturedFrame(0, 1, new byte[0])
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CapturedFrame(1, 0, new byte[0])
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CapturedFrame(2, 2, new byte[15])
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> new CapturedFrame(1, 1, null)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> new CapturedFrame(
                                Integer.MAX_VALUE,
                                Integer.MAX_VALUE,
                                new byte[4]
                        )
                )
        );
    }

    @Test
    void capturedFrameDefensivelyCopiesRgbaBytes() {
        byte[] input = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        CapturedFrame frame = new CapturedFrame(2, 1, input);

        input[0] = 99;
        byte[] firstRead = frame.getRgba();
        firstRead[1] = 88;

        assertAll(
                () -> assertEquals(2, frame.getWidth()),
                () -> assertEquals(1, frame.getHeight()),
                () -> assertArrayEquals(
                        new byte[]{1, 2, 3, 4, 5, 6, 7, 8},
                        frame.getRgba()
                )
        );
    }

    @Test
    void unavailableCaptureSourceIsStableAndFailsExplicitly() {
        FrameCaptureSource first = FrameCaptureSource.unavailable();
        FrameCaptureSource second = FrameCaptureSource.unavailable();

        assertAll(
                () -> assertSame(first, second),
                () -> assertFalse(first.isAvailable()),
                () -> assertThrows(UnsupportedOperationException.class, first::capture)
        );
    }

    @Test
    void launcherParamsPreserveLegacyWindowDefaults() {
        LauncherParams params = new LauncherParams();

        assertAll(
                () -> assertFalse(params.hasWindowSize()),
                () -> assertNull(params.getWindowWidth()),
                () -> assertNull(params.getWindowHeight()),
                () -> assertTrue(params.getMaximized()),
                () -> assertTrue(params.getVSync())
        );
    }

    @Test
    void launcherParamsExposeDeterministicWindowModeFluently() {
        LauncherParams params = new LauncherParams();

        assertAll(
                () -> assertSame(params, params.setWindowSize(1280, 720)),
                () -> assertSame(params, params.setMaximized(false)),
                () -> assertSame(params, params.setVSync(false)),
                () -> assertTrue(params.hasWindowSize()),
                () -> assertEquals(1280, params.getWindowWidth()),
                () -> assertEquals(720, params.getWindowHeight()),
                () -> assertFalse(params.getMaximized()),
                () -> assertFalse(params.getVSync())
        );
    }

    @Test
    void launcherParamsRejectInvalidWindowSizeWithoutPartialMutation() {
        LauncherParams params = new LauncherParams().setWindowSize(640, 480);

        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> params.setWindowSize(0, 480)
                ),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> params.setWindowSize(640, -1)
                ),
                () -> assertEquals(640, params.getWindowWidth()),
                () -> assertEquals(480, params.getWindowHeight())
        );
    }
}
