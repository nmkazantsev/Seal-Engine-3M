package com.nikitos;

public abstract class GamePageClass {
    public GamePageClass() {
        resetPageMillis();
    }

    public abstract void draw();

    public abstract void onResume();

    public abstract void onPause();
}
