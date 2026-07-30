package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.debugger.BSODScreen;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.touch.TouchProcessor;
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
    public static final float MIN_SIMULATION_FPS = 30.0f;
    public static final float MAX_DT_MILLIS = 1000.0f / MIN_SIMULATION_FPS;

    private boolean firstStart = true;
    public static Engine engine;
    private final PlatformBridge pf;
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;
    private long previousFrameNanos;
    private float dtMillis;

    public CoreRenderer(float width, float height, Engine engine) {
        CoreRenderer.engine = engine;
        pf = engine.getPlatformBridge();
        gl = pf.getGeneralPlatformBridge();
        glc = pf.getGLConstBridge();
        float x = Utils.getX();
        float y = Utils.getY();
        float kx;
        float ky;
        pf.print("init core renderer " + x + " " + y);
        x = width;
        y = height;
        ky = y / 1280.0f;
        kx = x / 720.0f;
        if (x > y) {
            kx = x / 1280.0f;
            ky = y / 720.0f;
        }
        Utils.setDim(x, y, kx, ky);
        pf.log_i("engine", "x, y: " + x + " " + y);
        pf.log_i("engine", "kx, ky: " + kx + " " + ky);
        graphicsSetup(); //not sure if necessary but formally we have updated opengl context
        engine.onSurfaceChanged((int) width, (int) height);
    }

    public void onSurfaceCreated() {
        graphicsSetup();
        //glClearColor(0f, 0f, 0f, 1f);
        gl.glEnable(glc.GL_DEPTH_TEST());
        if (firstStart) {
            firstStart = false;
        }
    }

    private void graphicsSetup() {
        Shader.updateAllLocations();
        VRAMobject.onRedraw();
        VerticesShapesManager.onRedrawSetup();
    }

    public void draw() {
        if (engine.getRunState() == EngineRunState.RENDERING_SUSPENDED || engine.getRunState() == EngineRunState.CLOSED) {
            return;
        }
        //calculate fps:
        engine.calculateFps();

        if (engine.getGamePage() == null) {
            engine.startDefaultPage();
        }
        VerticesShapesManager.onFrameBegin();
        float frameDtMillis = updateDt();
        if (engine.getBsodAllowed()) {
            try {
                GamePageClass framePage = engine.getGamePage();
                if (engine.getRunState() == EngineRunState.RUNNING) framePage.update(frameDtMillis);
                if (engine.getGamePage() == framePage) {
                    framePage.render();
                }
            } catch (Exception ex) {
                engine.startNewPage(new BSODScreen(ex));
            }
        } else {
            GamePageClass framePage = engine.getGamePage();
            if (engine.getRunState() == EngineRunState.RUNNING) framePage.update(frameDtMillis);
            if (engine.getGamePage() == framePage) {
                framePage.render();
            }
        }
        Debugger.draw();

        VerticesShapesManager.redrawAll();
        TouchProcessor.processMotions();
        KeyboardProcessor.processKeys();
    }

    public float dt() {
        return dtMillis;
    }

    private float updateDt() {
        long now = System.nanoTime();
        float calculatedDtMillis;
        if (previousFrameNanos == 0L) {
            calculatedDtMillis = 1000.0f / Math.max(engine.fps, MIN_SIMULATION_FPS);
        } else {
            float rawDtMillis = (now - previousFrameNanos) / 1_000_000.0f;
            calculatedDtMillis = Math.min(Math.max(rawDtMillis, 0.0f), MAX_DT_MILLIS);
        }
        previousFrameNanos = now;
        dtMillis = calculatedDtMillis;
        return dtMillis;
    }
}
