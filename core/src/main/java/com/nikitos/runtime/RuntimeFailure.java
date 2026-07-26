package com.nikitos.runtime;

import com.nikitos.GamePageClass;

public final class RuntimeFailure {
    public enum Stage {
        FRAME_SETUP,
        PAGE_DRAW,
        DEBUGGER_DRAW,
        VERTICES_REDRAW,
        TOUCH_PROCESS,
        KEYBOARD_PROCESS,
        PAGE_TRANSITION
    }

    private final Stage stage;
    private final Throwable cause;
    private final FrameContext frameContext;
    private final GamePageClass page;

    public RuntimeFailure(Stage stage, Throwable cause, FrameContext frameContext, GamePageClass page) {
        this.stage = stage;
        this.cause = cause;
        this.frameContext = frameContext;
        this.page = page;
    }

    public Stage getStage() {
        return stage;
    }

    public Throwable getCause() {
        return cause;
    }

    public FrameContext getFrameContext() {
        return frameContext;
    }

    public GamePageClass getPage() {
        return page;
    }
}
