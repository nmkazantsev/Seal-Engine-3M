package com.nikitos;

import com.nikitos.platform.DesktopBridge;
import com.nikitos.platformBridge.LauncherParams;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EngineShutdownTest {

    @Test
    void shutdownFromAnotherThreadRequestsDesktopLoopExit() throws InterruptedException {
        DesktopBridge bridge = new DesktopBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                engine.shutdown();
            } catch (Throwable error) {
                failure.set(error);
            }
        });

        worker.start();
        worker.join();

        assertNull(failure.get());
        assertTrue(bridge.isShutdownRequested());
    }
}
