package com.nikitos.runtime;

/**
 * On-demand access to the current platform framebuffer.
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
