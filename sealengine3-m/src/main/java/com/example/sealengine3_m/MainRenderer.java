package com.example.sealengine3_m;

import android.opengl.GLES10;

import com.nikitos.GamePageClass;

public class MainRenderer extends GamePageClass {
    @Override
    public void draw() {
        GLES10.glClearColor(1, 0, 0, 0);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
