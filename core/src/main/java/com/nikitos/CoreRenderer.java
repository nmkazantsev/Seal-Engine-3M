package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.vertices.VerticesShapesManager;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.Utils;

/**
 * platform - independent realization of renderer
 * the heart of the engine
 */
public class CoreRenderer {
    private boolean firstStart = true;
    public static Engine engine;
    private final PlatformBridge pf;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;

    public CoreRenderer(float width, float height, Engine engine) {
        CoreRenderer.engine = engine;
        pf = engine.getPlatformBridge();
        gl = pf.getGeneralPlatformBridge();
        glc = pf.getGLConstBridge();

        pf.print("init core renderer " + Utils.x + " " + Utils.y);
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
        gl.glEnable(glc.GL_DEPTH_TEST());
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
        Shader.updateAllLocations();
        VRAMobject.onRedraw();
        VerticesShapesManager.onRedrawSetup();
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
        VerticesShapesManager.onFrameBegin();

        engine.getGamePage().draw();
        //Debugger.draw();

        VerticesShapesManager.redrawAll();
        //TouchProcessor.processMotions();
    }
}
