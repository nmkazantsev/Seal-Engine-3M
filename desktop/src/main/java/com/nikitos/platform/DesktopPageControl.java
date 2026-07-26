package com.nikitos.platform;

import com.nikitos.Engine;
import com.nikitos.GamePageClass;

import java.util.Objects;

final class DesktopPageControl {
    private final DesktopWindowControl windowControl;
    private final Engine engine;

    DesktopPageControl(
            DesktopWindowControl windowControl,
            Engine engine
    ) {
        this.windowControl = Objects.requireNonNull(windowControl);
        this.engine = Objects.requireNonNull(engine);
    }

    void requestPage(GamePageClass page) {
        windowControl.requireActiveOwnerThread();
        engine.startNewPage(Objects.requireNonNull(page));
    }
}
