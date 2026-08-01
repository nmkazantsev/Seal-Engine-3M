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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private long frameNumber;

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
        frameNumber++;

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
        writeRequestedFrameCapture();
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

    private void writeRequestedFrameCapture() {
        Engine.FrameCaptureRequest request = engine.consumeFrameCaptureRequest();
        if (request == null) return;
        int width = (int) Utils.getX();
        int height = (int) Utils.getY();
        if (width <= 0 || height <= 0) return;
        String basename = "capture-" + frameNumber;
        String metadata = captureMetadata(basename, width, height);
        try {
            Path outputDirectory = request.outputDirectory == null ? Paths.get("captures") : request.outputDirectory;
            Files.createDirectories(outputDirectory);
            Path pngFile = outputDirectory.resolve(basename + ".png");
            engine.getPlatformBridge().getGeneralPlatformBridge().writePng(pngFile, width, height, gl.readPixelsRgba(width, height));
            engine.saveTextFile(outputDirectory.resolve(basename + ".json").toString(), metadata);
        } catch (IOException | RuntimeException exception) {
            pf.log_e("engine", "frame capture failed: " + exception.getMessage());
        }
    }

    private String captureMetadata(String captureId, int width, int height) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("captureId", captureId);
        metadata.put("frameNumber", frameNumber);
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("dtMillis", dtMillis);
        metadata.put("fps", engine.fps);
        metadata.put("runState", engine.getRunState().name());
        metadata.put("viewport", Map.of("width", width, "height", height));
        GamePageClass page = engine.getGamePage();
        metadata.put("pageClassName", page == null ? null : page.getClass().getName());
        metadata.put("fullscreen", engine.getFullScreen());
        metadata.put("mouse", Map.of(
                "x", TouchProcessor.getMouseX(),
                "y", TouchProcessor.getMouseY(),
                "leftButtonDown", TouchProcessor.getLeftButtonDown(),
                "rightButtonDown", TouchProcessor.getRightButtonDown()
        ));
        metadata.put("platform", engine.getPlatform().name().toLowerCase());
        FrameCaptureDataProvider provider = engine.getFrameCaptureDataProvider();
        metadata.put("game", provider == null ? null : provider.getFrameCaptureData());
        return toJson(metadata);
    }

    static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return '"' + escapeJsonString((String) value) + '"';
        }
        if (value instanceof Boolean || value instanceof Number) {
            return value.toString();
        }
        if (value instanceof Map) {
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!first) json.append(',');
                json.append(toJson(entry.getKey().toString())).append(':').append(toJson(entry.getValue()));
                first = false;
            }
            return json.append('}').toString();
        }
        if (value instanceof List) {
            StringBuilder json = new StringBuilder("[");
            for (int index = 0; index < ((List<?>) value).size(); index++) {
                if (index > 0) json.append(',');
                json.append(toJson(((List<?>) value).get(index)));
            }
            return json.append(']').toString();
        }
        return toJson(value.toString());
    }

    static String escapeJsonString(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\': escaped.append("\\\\"); break;
                case '"': escaped.append("\\\""); break;
                case '\n': escaped.append("\\n"); break;
                case '\r': escaped.append("\\r"); break;
                case '\t': escaped.append("\\t"); break;
                case '\b': escaped.append("\\b"); break;
                case '\f': escaped.append("\\f"); break;
                default:
                    if (character <= 0x1F) {
                        escaped.append(String.format("\\u%04X", (int) character));
                    } else {
                        escaped.append(character);
                    }
            }
        }
        return escaped.toString();
    }
}
