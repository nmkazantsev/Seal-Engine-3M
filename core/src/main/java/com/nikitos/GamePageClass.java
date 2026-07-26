package com.nikitos;

public abstract class GamePageClass {
    private final Object resourceOwnershipToken = new Object();

    final Object getResourceOwnershipToken() {
        return resourceOwnershipToken;
    }

    public abstract void onSurfaceChanged(int x, int y);

    public abstract void draw();

    public abstract void onResume();

    public abstract void onPause();

}
