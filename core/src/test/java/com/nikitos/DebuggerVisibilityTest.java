package com.nikitos;

import com.nikitos.main.debugger.Debugger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebuggerVisibilityTest {
    @Test
    void fpsVisibilityIsIndependentFromDebuggerEnablement() {
        try {
            Debugger.setEnabled(false);
            Debugger.setFpsVisible(false);
            assertFalse(Debugger.isFpsVisible());

            Debugger.setEnabled(true);
            assertFalse(Debugger.isFpsVisible());

            Debugger.setFpsVisible(true);
            assertTrue(Debugger.isFpsVisible());
        } finally {
            Debugger.setFpsVisible(true);
            Debugger.setEnabled(false);
        }
    }
}
