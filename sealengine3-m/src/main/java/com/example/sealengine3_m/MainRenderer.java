package com.example.sealengine3_m;

import static android.opengl.GLES10.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glEnable;

import android.opengl.GLES30;

import com.nikitos.GamePageClass;

public class MainRenderer extends GamePageClass {
    @Override
    public void draw() {
        glClear(GL_COLOR_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        GLES30.glClearColor(1, 0, 0, 0.0f);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
