package com.nikitos.runtime;

import java.util.Arrays;
import java.util.Objects;

/**
 * Неизменяемый снимок RGBA: первый ряд массива соответствует верхнему ряду изображения.
 * Конструктор и геттер копируют байты, чтобы снимок не менялся из пользовательского кода.
 */
public final class CapturedFrame {
    private final int width;
    private final int height;
    private final byte[] rgba;

    public CapturedFrame(int width, int height, byte[] rgba) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Captured frame dimensions must be positive");
        }
        Objects.requireNonNull(rgba, "rgba");
        long expectedLength;
        try {
            expectedLength = Math.multiplyExact(
                    Math.multiplyExact((long) width, (long) height),
                    4L
            );
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException(
                    "Captured frame dimensions are too large",
                    overflow
            );
        }
        if (expectedLength > Integer.MAX_VALUE || rgba.length != (int) expectedLength) {
            throw new IllegalArgumentException(
                    "RGBA length must equal width * height * 4"
            );
        }
        this.width = width;
        this.height = height;
        this.rgba = Arrays.copyOf(rgba, rgba.length);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public byte[] getRgba() {
        return Arrays.copyOf(rgba, rgba.length);
    }
}
