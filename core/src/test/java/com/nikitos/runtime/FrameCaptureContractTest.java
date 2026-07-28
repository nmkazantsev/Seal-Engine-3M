package com.nikitos.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FrameCaptureContractTest {

    @Test
    void capturedFrameRejectsInvalidRgbaShape() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CapturedFrame(2, 2, new byte[15])
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CapturedFrame(Integer.MAX_VALUE, Integer.MAX_VALUE, new byte[4])
        );
    }

    @Test
    void capturedFrameDoesNotExposeMutablePixels() {
        byte[] pixels = new byte[]{1, 2, 3, 4};
        CapturedFrame frame = new CapturedFrame(1, 1, pixels);

        pixels[0] = 9;
        byte[] returned = frame.getRgba();
        returned[1] = 9;

        assertArrayEquals(new byte[]{1, 2, 3, 4}, frame.getRgba());
    }
}
