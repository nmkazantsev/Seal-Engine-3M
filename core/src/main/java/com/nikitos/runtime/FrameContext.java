package com.nikitos.runtime;

import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.Platform;

public final class FrameContext {
    private final long frameId;
    private final GamePageClass page;
    private final int width;
    private final int height;
    private final Platform platform;

    public FrameContext(long frameId, GamePageClass page, int width, int height, Platform platform) {
        this.frameId = frameId;
        this.page = page;
        this.width = width;
        this.height = height;
        this.platform = platform;
    }

    public long getFrameId() {
        return frameId;
    }

    public GamePageClass getPage() {
        return page;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Platform getPlatform() {
        return platform;
    }
}
