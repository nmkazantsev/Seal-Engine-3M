package com.nikitos.runtime;

public interface RuntimeObserver {
    /**
     * Вызывается в render-потоке после полной отрисовки и до обмена буферов.
     * Вызов {@link FrameCaptureSource#capture()} создаёт снимок только по необходимости.
     */
    default void afterFrame(FrameCaptureSource frameCaptureSource) {
    }
}
