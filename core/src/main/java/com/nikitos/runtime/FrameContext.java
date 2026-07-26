package com.nikitos.runtime;

import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.Platform;

public final class FrameContext {
    private final long frameId;
    private final GamePageClass page;
    private final int width;
    private final int height;
    private final Platform platform;
    private final RuntimeResourceSnapshot resourceSnapshot;

    public FrameContext(long frameId, GamePageClass page, int width, int height, Platform platform) {
        this(frameId, page, width, height, platform, null);
    }

    public FrameContext(
            long frameId,
            GamePageClass page,
            int width,
            int height,
            Platform platform,
            RuntimeResourceSnapshot resourceSnapshot
    ) {
        this.frameId = frameId;
        this.page = page;
        this.width = width;
        this.height = height;
        this.platform = platform;
        this.resourceSnapshot = resourceSnapshot;
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

    public RuntimeResourceSnapshot getResourceSnapshot() {
        return resourceSnapshot;
    }
}
