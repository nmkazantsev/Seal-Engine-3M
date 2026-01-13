package com.nikitos;

import com.nikitos.GamePageClass;
import com.nikitos.utils.Utils;

public class MainRenderer extends GamePageClass {
    @Override
    public void draw() {
        // GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //GLES20.glEnable(GLES10.GL_DEPTH_TEST);
        //GLES30.glClearColor(1, 0, 0, 0.0f);
        Utils.background(255, 255, 0);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }
}
