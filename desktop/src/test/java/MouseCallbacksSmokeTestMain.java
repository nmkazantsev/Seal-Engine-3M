import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.touch.MousePoint;
import com.nikitos.main.touch.MouseWheelData;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.maths.Matrix;
import com.nikitos.platform.DesktopLauncher;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.utils.Utils;

import java.util.List;
import java.util.function.Function;

/**
 * Minimal desktop-only visual smoke test for mouse callback verification.
 *
 * Polygon A:
 * - fixed X
 * - wheel changes Y only
 *
 * Polygon B:
 * - follows mouse position using TouchProcessor mouse-move callbacks
 */
public final class MouseCallbacksSmokeTestMain {
    public static void main(String[] args) {
        LauncherParams params = new LauncherParams()
                .setWindowTitle("Seal Engine Mouse Callback Smoke Test")
                .setFullScreen(false)
                .setDebug(false)
                .setStartPage(unused -> new MouseCallbacksTestPage());

        new DesktopLauncher(params).run();
    }

    private static final class MouseCallbacksTestPage extends GamePageClass {
        private final Shader shader;
        private final SimplePolygon wheelPolygon;
        private final SimplePolygon mousePolygon;

        private Camera camera;
        private float screenWidth;
        private float screenHeight;

        private float wheelPolygonX;
        private float wheelPolygonY;
        private float wheelPolygonWidth;
        private float wheelPolygonHeight;

        private float mousePolygonX;
        private float mousePolygonY;
        private float mousePolygonSize;

        private MouseCallbacksTestPage() {
            shader = new Shader(
                    "vertex_shader_engine.glsl",
                    "fragment_shader_engine.glsl",
                    this,
                    new MainShaderAdaptor()
            );
            wheelPolygon = new SimplePolygon(drawWheelPolygon, false, 0, this);
            mousePolygon = new SimplePolygon(drawMousePolygon, false, 0, this);

            TouchProcessor.setMouseWheelProcessor(this::onMouseWheel, this);
            TouchProcessor.setMouseMovedProcessor(this::onMouseMoved, this);
            TouchProcessor.setLeftButtonProcessor(new Function<MousePoint, Void>() {
                @Override
                public Void apply(MousePoint mousePoint) {
                    CoreRenderer.engine.disableMouseCursor();
                    return null;
                }
            }, this);
            TouchProcessor.setRightButtonProcessor(new Function<MousePoint, Void>() {
                @Override
                public Void apply(MousePoint mousePoint) {
                    CoreRenderer.engine.enableMouseCursor();
                    return null;
                }
            }, this);
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
            screenWidth = x;
            screenHeight = y;

            camera = new Camera(x, y);
            camera.resetFor2d();

            wheelPolygonWidth = Math.max(96f, x * 0.12f);
            wheelPolygonHeight = Math.max(96f, y * 0.18f);
            wheelPolygonX = Math.max(40f, x * 0.12f);
            wheelPolygonY = clamp(y * 0.35f, 0f, Math.max(0f, y - wheelPolygonHeight));

            mousePolygonSize = Math.max(48f, Math.min(x, y) * 0.08f);
            mousePolygonX = x * 0.5f;
            mousePolygonY = y * 0.5f;

            wheelPolygon.redrawNow();
            mousePolygon.redrawNow();
        }

        @Override
        public void update(float dtMillis) {
        }

        @Override
        public void render() {
            Utils.background(18, 24, 28);
            CoreRenderer.engine.glClear();

            shader.apply();
            camera.resetFor2d();
            camera.apply();
            Matrix.applyMatrix(Matrix.resetTranslateMatrix(new float[16]));

            wheelPolygon.prepareAndDraw(wheelPolygonX, wheelPolygonY, wheelPolygonWidth, wheelPolygonHeight, 0.1f);
            mousePolygon.prepareAndDraw(
                    mousePolygonX - mousePolygonSize * 0.5f,
                    mousePolygonY - mousePolygonSize * 0.5f,
                    mousePolygonSize,
                    mousePolygonSize,
                    0.1f
            );
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }

        private Void onMouseMoved(MousePoint mousePoint) {
            mousePolygonX = mousePoint.mouseX;
            mousePolygonY = mousePoint.mouseY;
            return null;
        }

        private Void onMouseWheel(MouseWheelData wheelData) {
            float step = Math.max(20f, screenHeight * 0.04f);
            wheelPolygonY = clamp(
                    wheelPolygonY - wheelData.wheelY * step,
                    0f,
                    Math.max(0f, screenHeight - wheelPolygonHeight)
            );
            return null;
        }

        private final Function<List<Object>, PImage> drawWheelPolygon = objects -> {
            float width = Math.max(8f, wheelPolygonWidth);
            float height = Math.max(8f, wheelPolygonHeight);
            PImage image = new PImage(width, height);
            image.setAntiAlias(true);
            image.background(36, 180, 108, 255);
            image.stroke(245, 250, 245, 255);
            image.strokeWeight(6);
            image.rect(6, 6, Math.max(0, width - 12), Math.max(0, height - 12));
            return image;
        };

        private final Function<List<Object>, PImage> drawMousePolygon = objects -> {
            float size = Math.max(8f, mousePolygonSize);
            PImage image = new PImage(size, size);
            image.setAntiAlias(true);
            image.background(220, 72, 72, 255);
            image.stroke(255, 246, 214, 255);
            image.strokeWeight(5);
            image.ellipse(size * 0.5f, size * 0.5f, size * 0.34f, size * 0.34f);
            return image;
        };

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
