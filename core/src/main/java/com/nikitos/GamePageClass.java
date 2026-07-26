package com.nikitos;

public abstract class GamePageClass {
    private final Object resourceOwnershipToken = new Object();

    final Object getResourceOwnershipToken() {
        return resourceOwnershipToken;
    }

    /**
     * Called whenever this exact page instance is installed by
     * {@link Engine#startNewPage(GamePageClass)}, before its initial surface
     * callback and before outgoing page registries are cleaned.
     *
     * <p>The default implementation preserves existing page behavior.</p>
     */
    public void onInstalled() {
    }

    public abstract void onSurfaceChanged(int x, int y);

    public abstract void draw();

    public abstract void onResume();

    public abstract void onPause();

}
