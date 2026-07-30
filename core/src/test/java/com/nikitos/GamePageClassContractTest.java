package com.nikitos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class GamePageClassContractTest {
    @Test
    void exposesUpdateAndRenderContract() {
        assertDoesNotThrow(() -> GamePageClass.class.getDeclaredMethod("update", float.class));
        assertDoesNotThrow(() -> GamePageClass.class.getDeclaredMethod("render"));
    }

    @Test
    void noLongerExposesDrawContract() {
        assertDoesNotThrow(() -> {
            try {
                GamePageClass.class.getDeclaredMethod("draw");
                throw new AssertionError("draw must be replaced by update/render");
            } catch (NoSuchMethodException ignored) {
            }
        });
    }
}
