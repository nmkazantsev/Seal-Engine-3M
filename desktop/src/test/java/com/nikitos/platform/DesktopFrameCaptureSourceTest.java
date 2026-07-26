package com.nikitos.platform;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DesktopFrameCaptureSourceTest {

    @Test
    void convertsOpenGlBottomLeftRowsToTopLeftRgba() {
        ByteBuffer bottomLeft = ByteBuffer.wrap(new byte[]{
                1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16
        });

        byte[] topLeft = DesktopFrameCaptureSource.toTopLeftRgba(
                bottomLeft,
                2,
                2
        );

        assertArrayEquals(
                new byte[]{
                        9, 10, 11, 12, 13, 14, 15, 16,
                        1, 2, 3, 4, 5, 6, 7, 8
                },
                topLeft
        );
    }

    @Test
    void rejectsFramebufferByteCountsThatOverflowLongOrJavaArrays() {
        assertThrows(
                IllegalStateException.class,
                () -> DesktopFrameCaptureSource.checkedRgbaByteCount(
                        Integer.MAX_VALUE,
                        Integer.MAX_VALUE
                )
        );
    }
}
