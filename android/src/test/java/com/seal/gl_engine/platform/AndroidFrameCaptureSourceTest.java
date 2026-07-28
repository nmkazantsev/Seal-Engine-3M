package com.seal.gl_engine.platform;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class AndroidFrameCaptureSourceTest {

    @Test
    public void convertsOpenGlRowsToPublicTopLeftOrder() {
        byte[] result = AndroidFrameCaptureSource.toTopLeftRgba(
                ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}), 1, 2
        );

        assertArrayEquals(new byte[]{5, 6, 7, 8, 1, 2, 3, 4}, result);
    }

    @Test
    public void rejectsEmptyAndTooLargeFramebufferBeforeAllocation() {
        assertIllegalState(() -> AndroidFrameCaptureSource.checkedRgbaByteCount(0, 1));
        assertIllegalState(() -> AndroidFrameCaptureSource.checkedRgbaByteCount(
                Integer.MAX_VALUE, Integer.MAX_VALUE
        ));
    }

    private static void assertIllegalState(Runnable action) {
        try {
            action.run();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // Ожидаемая защита публичного контракта.
        }
    }
}
