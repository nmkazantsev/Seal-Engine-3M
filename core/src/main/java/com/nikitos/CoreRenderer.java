package com.nikitos;

import com.nikitos.utils.Utils;

/**
 * platform - independent realization of renderer
 * the heart of the engine
 */
public class CoreRenderer {
    private boolean firstStart = true;
    public static Engine engine;

    public CoreRenderer(float width, float height, Engine engine) {
        CoreRenderer.engine = engine;
        Utils.x = width;
        Utils.y = height;
        Utils.ky = Utils.y / 1280.0f;
        Utils.kx = Utils.x / 720.0f;
        if (Utils.x > Utils.y) {
            Utils.kx = Utils.x / 1280.0f;
            Utils.ky = Utils.y / 720.0f;
        }
    }

    public void onSurfaceCreated() {
        graphicsSetup();
        //glClearColor(0f, 0f, 0f, 1f);
        // glEnable(GL_DEPTH_TEST);
        if (firstStart) {
            Utils.programStartTime = System.currentTimeMillis();
            setup();
            firstStart = false;
        }
        if (Utils.millis() > 60 * 60 * 1000) {
            //smth went wrong...
            Utils.programStartTime = System.currentTimeMillis();
            engine.resetPrevPageChangeTime();
        }
    }

    private void graphicsSetup() {
        //Shader.updateAllLocations();
       // VRAMobject.onRedraw();
       // VerticesShapesManager.onRedrawSetup();
        //VerticesShapesManager.redrawAllSetup();
    }


    private void setup() {
        engine.resetPrevPageChangeTime();
    }

    public void draw() {
        //calculate fps:
        engine.calculateFps();

        if (engine.getGamePage() == null) {
            engine.startDefaultPage();
        }
        //VerticesShapesManager.onFrameBegin();

        engine.getGamePage().draw();
        //Debugger.draw();

        //VerticesShapesManager.redrawAll();
        //TouchProcessor.processMotions();
    }
}
