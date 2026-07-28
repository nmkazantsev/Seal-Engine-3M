package com.nikitos.runtime;

/**
 * Доступ к текущему framebuffer по требованию. Реализация платформы читает его только
 * из render-потока, поэтому обычный кадр не получает лишнего чтения памяти.
 */
public interface FrameCaptureSource {
    boolean isAvailable();

    CapturedFrame capture();

    static FrameCaptureSource unavailable() {
        return UnavailableFrameCaptureSource.INSTANCE;
    }
}

final class UnavailableFrameCaptureSource implements FrameCaptureSource {
    static final UnavailableFrameCaptureSource INSTANCE =
            new UnavailableFrameCaptureSource();

    private UnavailableFrameCaptureSource() {
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public CapturedFrame capture() {
        throw new UnsupportedOperationException(
                "Framebuffer capture is unavailable on this platform"
        );
    }
}
