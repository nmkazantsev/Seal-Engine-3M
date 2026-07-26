package com.nikitos.runtime;

public interface RuntimeObserver {
    default void beforeFrame(FrameContext frameContext) {
    }

    default void afterFrame(FrameContext frameContext) {
    }

    default void afterFrame(
            FrameContext frameContext,
            FrameCaptureSource frameCaptureSource
    ) {
        afterFrame(frameContext);
    }

    default void onPageChanged(PageTransition pageTransition) {
    }

    default void onFailure(RuntimeFailure runtimeFailure) {
    }
}
