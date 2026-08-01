package com.nikitos;

public abstract class GamePageClass {

    public abstract void onSurfaceChanged(int x, int y);

    public abstract void update(float dtMillis);

    public abstract void render();

    public abstract void onResume();

    public abstract void onPause();

}
