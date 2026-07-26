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
import com.nikitos.runtime.FrameCaptureSource;
import com.nikitos.runtime.FrameContext;
import com.nikitos.runtime.RuntimeFailure;
import com.nikitos.runtime.RuntimeObserver;
import com.nikitos.utils.Utils;

import java.util.function.Function;

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
    private final RuntimeObserver runtimeObserver;
    private final FrameCaptureSource frameCaptureSource;
    private final Function<Exception, GamePageClass> bsodPageFactory;
    private long observedFrameId;

    public CoreRenderer(float width, float height, Engine engine) {
        CoreRenderer.engine = engine;
        pf = engine.getPlatformBridge();
        gl = pf.getGeneralPlatformBridge();
        glc = pf.getGLConstBridge();
        runtimeObserver = engine.getRuntimeObserver();
        frameCaptureSource = runtimeObserver == null
                ? null
                : pf.getFrameCaptureSource();
        bsodPageFactory = null;
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

    CoreRenderer(Engine engine, Function<Exception, GamePageClass> bsodPageFactory) {
        CoreRenderer.engine = engine;
        pf = engine.getPlatformBridge();
        gl = pf.getGeneralPlatformBridge();
        glc = pf.getGLConstBridge();
        runtimeObserver = engine.getRuntimeObserver();
        frameCaptureSource = runtimeObserver == null
                ? null
                : pf.getFrameCaptureSource();
        this.bsodPageFactory = bsodPageFactory;
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
        if (runtimeObserver == null) {
            //calculate fps:
            engine.calculateFps();

            if (engine.getGamePage() == null) {
                engine.startDefaultPage();
            }
            VerticesShapesManager.onFrameBegin();
            if (engine.getBsodAllowed()) {
                try {
                    engine.getGamePage().draw();
                } catch (Exception ex) {
                    engine.startNewPage(new BSODScreen(ex));
                }
            } else {
                engine.getGamePage().draw();
            }
            Debugger.draw();

            VerticesShapesManager.redrawAll();
            TouchProcessor.processMotions();
            KeyboardProcessor.processKeys();
            return;
        }

        FrameContext frameContext = new FrameContext(
                ++observedFrameId,
                engine.getGamePage(),
                (int) Utils.getX(),
                (int) Utils.getY(),
                engine.getPlatform()
        );
        engine.beginObservedFrame();
        try {
            drawObserved(frameContext);
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure.propagate();
        } finally {
            engine.endObservedFrame();
        }
    }

    private void drawObserved(FrameContext frameContext) {
        runtimeObserver.beforeFrame(frameContext);

        try {
            engine.calculateFps();
            if (engine.getGamePage() == null) {
                engine.startDefaultPage();
            }
            VerticesShapesManager.onFrameBegin();
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure;
        } catch (Exception ex) {
            notifyFailure(RuntimeFailure.Stage.FRAME_SETUP, ex, frameContext);
            throw ex;
        } catch (Error error) {
            notifyFailure(RuntimeFailure.Stage.FRAME_SETUP, error, frameContext);
            throw error;
        }

        if (engine.getBsodAllowed()) {
            try {
                engine.getGamePage().draw();
            } catch (Engine.ObservedLifecycleException lifecycleFailure) {
                throw lifecycleFailure;
            } catch (Exception ex) {
                notifyFailure(RuntimeFailure.Stage.PAGE_DRAW, ex, frameContext);
                engine.startNewPage(createBsodPage(ex));
            } catch (Error error) {
                notifyFailure(RuntimeFailure.Stage.PAGE_DRAW, error, frameContext);
                throw error;
            }
        } else {
            try {
                engine.getGamePage().draw();
            } catch (Engine.ObservedLifecycleException lifecycleFailure) {
                throw lifecycleFailure;
            } catch (Exception ex) {
                notifyFailure(RuntimeFailure.Stage.PAGE_DRAW, ex, frameContext);
                throw ex;
            } catch (Error error) {
                notifyFailure(RuntimeFailure.Stage.PAGE_DRAW, error, frameContext);
                throw error;
            }
        }

        try {
            Debugger.draw();
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure;
        } catch (Exception ex) {
            notifyFailure(RuntimeFailure.Stage.DEBUGGER_DRAW, ex, frameContext);
            throw ex;
        } catch (Error error) {
            notifyFailure(RuntimeFailure.Stage.DEBUGGER_DRAW, error, frameContext);
            throw error;
        }

        try {
            VerticesShapesManager.redrawAll();
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure;
        } catch (Exception ex) {
            notifyFailure(RuntimeFailure.Stage.VERTICES_REDRAW, ex, frameContext);
            throw ex;
        } catch (Error error) {
            notifyFailure(RuntimeFailure.Stage.VERTICES_REDRAW, error, frameContext);
            throw error;
        }

        try {
            TouchProcessor.processMotions();
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure;
        } catch (Exception ex) {
            notifyFailure(RuntimeFailure.Stage.TOUCH_PROCESS, ex, frameContext);
            throw ex;
        } catch (Error error) {
            notifyFailure(RuntimeFailure.Stage.TOUCH_PROCESS, error, frameContext);
            throw error;
        }

        try {
            KeyboardProcessor.processKeys();
        } catch (Engine.ObservedLifecycleException lifecycleFailure) {
            throw lifecycleFailure;
        } catch (Exception ex) {
            notifyFailure(RuntimeFailure.Stage.KEYBOARD_PROCESS, ex, frameContext);
            throw ex;
        } catch (Error error) {
            notifyFailure(RuntimeFailure.Stage.KEYBOARD_PROCESS, error, frameContext);
            throw error;
        }

        runtimeObserver.afterFrame(frameContext, frameCaptureSource);
    }

    private GamePageClass createBsodPage(Exception cause) {
        if (bsodPageFactory != null) {
            return bsodPageFactory.apply(cause);
        }
        return new BSODScreen(cause);
    }

    private void notifyFailure(
            RuntimeFailure.Stage stage,
            Throwable cause,
            FrameContext frameContext
    ) {
        try {
            runtimeObserver.onFailure(new RuntimeFailure(
                    stage,
                    cause,
                    frameContext,
                    engine.getGamePage()
            ));
        } catch (Throwable observerFailure) {
            if (observerFailure != cause) {
                cause.addSuppressed(observerFailure);
            }
        }
    }
}
