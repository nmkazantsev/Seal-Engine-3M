package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.keyboard.KeyComboListener;
import com.nikitos.main.keyboard.KeyListener;
import com.nikitos.main.keyboard.KeyReleasedListener;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.shaders.Adaptor;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.touch.MyMotionEvent;
import com.nikitos.main.touch.TouchPoint;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;
import com.nikitos.platform.DesktopBridge;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.platformBridge.SealAssetManager;
import com.nikitos.platformBridge.ShaderBridge;
import com.nikitos.runtime.FrameContext;
import com.nikitos.runtime.RuntimeObserver;
import com.nikitos.runtime.RuntimeResourceSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResourceOwnershipTest {
    private static final String PRESS_KEY = "PAGE_OWNER_PRESS";
    private static final String COMBO_KEY_1 = "PAGE_OWNER_COMBO_1";
    private static final String COMBO_KEY_2 = "PAGE_OWNER_COMBO_2";

    @AfterEach
    void releaseInputState() {
        KeyboardProcessor.onKeyReleased(PRESS_KEY);
        KeyboardProcessor.onKeyReleased(COMBO_KEY_1);
        KeyboardProcessor.onKeyReleased(COMBO_KEY_2);
        if (CoreRenderer.engine != null) {
            TouchProcessor.onPageChange();
            TouchProcessor.processMotions();
            KeyboardProcessor.onPageChange();
            KeyboardProcessor.processKeys();
        }
    }

    @Test
    void sameClassTransitionDeletesOutgoingOwnershipAndKeepsIncomingConstructorAndSurfaceOwnership() {
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        CoreRenderer.engine = engine;
        RegistryPage outgoing = new RegistryPage("outgoing");

        engine.startNewPage(outgoing);
        outgoing.surfaceTouch.setPriority(100);
        RegistryPage incoming = new RegistryPage("incoming");
        outgoing.installMouseCallback();

        engine.startNewPage(incoming);

        assertAll(
                () -> assertTrue(outgoing.constructorVram.deleted),
                () -> assertTrue(outgoing.surfaceVram.deleted),
                () -> assertTrue(bridge.shaderBridge.wasDeleted(outgoing.constructorShaderProgram)),
                () -> assertTrue(bridge.shaderBridge.wasDeleted(outgoing.surfaceShaderProgram)),
                () -> assertEquals(1, bridge.shaderBridge.deleteCount(outgoing.constructorShaderProgram)),
                () -> assertEquals(1, bridge.shaderBridge.deleteCount(outgoing.surfaceShaderProgram)),
                () -> assertFalse(incoming.constructorVram.deleted),
                () -> assertFalse(incoming.surfaceVram.deleted),
                () -> assertFalse(bridge.shaderBridge.wasDeleted(incoming.constructorShaderProgram)),
                () -> assertFalse(bridge.shaderBridge.wasDeleted(incoming.surfaceShaderProgram))
        );

        clearPageChangeSignals();
        dispatchTouch();
        dispatchKeyboard();
        dispatchMouse();

        assertAll(
                () -> assertEquals(0, outgoing.touchStarts),
                () -> assertEquals(0, outgoing.keyPresses),
                () -> assertEquals(0, outgoing.keyReleases),
                () -> assertEquals(0, outgoing.keyCombos),
                () -> assertEquals(0, outgoing.mouseMoves),
                () -> assertEquals(1, incoming.touchStarts),
                () -> assertEquals(1, incoming.keyPresses),
                () -> assertEquals(1, incoming.keyReleases),
                () -> assertEquals(1, incoming.keyCombos),
                () -> assertEquals(1, incoming.mouseMoves)
        );

        VRAMobject.onRedraw();
        assertAll(
                () -> assertEquals(0, outgoing.constructorVram.reloads),
                () -> assertEquals(0, outgoing.surfaceVram.reloads),
                () -> assertEquals(1, incoming.constructorVram.reloads),
                () -> assertEquals(1, incoming.surfaceVram.reloads)
        );
    }

    @Test
    void observedFrameExposesExactRegistryCounts() {
        RecordingObserver observer = new RecordingObserver();
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(
                bridge,
                new LauncherParams().setRuntimeObserver(observer)
        );
        CoreRenderer.engine = engine;
        RegistryPage page = new RegistryPage("observed");
        engine.startNewPage(page);
        clearPageChangeSignals();
        int expectedVram = VRAMobject.getTrackedObjectCount();
        int expectedShaders = Shader.getTrackedShaderCount();
        int expectedTouch = TouchProcessor.getTrackedProcessorCount();
        int expectedPress = KeyboardProcessor.getPressListenerCount();
        int expectedRelease = KeyboardProcessor.getReleaseListenerCount();
        int expectedCombo = KeyboardProcessor.getComboListenerCount();
        int expectedMouse = TouchProcessor.getDesktopMouseCallbackRegistrationCount();
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> new EmptyPage());

        renderer.draw();

        RuntimeResourceSnapshot snapshot = observer.frameContext.getResourceSnapshot();
        assertNotNull(snapshot);
        assertAll(
                () -> assertEquals(expectedVram, snapshot.getTrackedVramObjects()),
                () -> assertEquals(expectedShaders, snapshot.getShaders()),
                () -> assertEquals(expectedTouch, snapshot.getTouchProcessors()),
                () -> assertEquals(expectedPress, snapshot.getKeyboardPressListeners()),
                () -> assertEquals(expectedRelease, snapshot.getKeyboardReleaseListeners()),
                () -> assertEquals(expectedCombo, snapshot.getKeyboardComboListeners()),
                () -> assertEquals(expectedMouse, snapshot.getDesktopMouseCallbackRegistrations())
        );
    }

    @Test
    void firstObservedFrameSamplesResourcesBeforeDefaultPageCreation() {
        RecordingObserver observer = new RecordingObserver();
        RecordingBridge bridge = new RecordingBridge();
        RegistryPage[] createdPage = new RegistryPage[1];
        Engine engine = new Engine(
                bridge,
                new LauncherParams()
                        .setRuntimeObserver(observer)
                        .setStartPage(ignored -> {
                            createdPage[0] = new RegistryPage("default");
                            return createdPage[0];
                        })
        );
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> new EmptyPage());
        int expectedVram = VRAMobject.getTrackedObjectCount();
        int expectedShaders = Shader.getTrackedShaderCount();
        int expectedTouch = TouchProcessor.getTrackedProcessorCount();
        int expectedPress = KeyboardProcessor.getPressListenerCount();
        int expectedRelease = KeyboardProcessor.getReleaseListenerCount();
        int expectedCombo = KeyboardProcessor.getComboListenerCount();
        int expectedMouse = TouchProcessor.getDesktopMouseCallbackRegistrationCount();

        renderer.draw();

        RuntimeResourceSnapshot snapshot = observer.frameContext.getResourceSnapshot();
        assertAll(
                () -> assertNull(observer.frameContext.getPage()),
                () -> assertNotNull(createdPage[0]),
                () -> assertSame(createdPage[0], engine.getGamePage()),
                () -> assertEquals(expectedVram, snapshot.getTrackedVramObjects()),
                () -> assertEquals(expectedShaders, snapshot.getShaders()),
                () -> assertEquals(expectedTouch, snapshot.getTouchProcessors()),
                () -> assertEquals(expectedPress, snapshot.getKeyboardPressListeners()),
                () -> assertEquals(expectedRelease, snapshot.getKeyboardReleaseListeners()),
                () -> assertEquals(expectedCombo, snapshot.getKeyboardComboListeners()),
                () -> assertEquals(expectedMouse, snapshot.getDesktopMouseCallbackRegistrations())
        );
    }

    @Test
    void globalOwnershipSurvivesPageTransitionsAndContextRedraw() {
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        CoreRenderer.engine = engine;
        int[] callbacks = new int[4];
        TestVram globalVram = new TestVram(null);
        new Shader("vertex", "fragment", null, new NoOpAdaptor());
        int globalShaderProgram = bridge.shaderBridge.lastCreatedProgram();
        TouchProcessor globalTouch = new TouchProcessor(
                point -> true,
                point -> {
                    callbacks[0]++;
                    return null;
                },
                null,
                null,
                null
        );
        KeyListener globalPress = new KeyListener(PRESS_KEY, key -> {
            callbacks[1]++;
            return null;
        }, null);
        KeyReleasedListener globalRelease = new KeyReleasedListener(PRESS_KEY, key -> {
            callbacks[2]++;
            return null;
        }, null);
        KeyComboListener globalCombo = new KeyComboListener(
                COMBO_KEY_1,
                COMBO_KEY_2,
                key -> {
                    callbacks[3]++;
                    return null;
                },
                null
        );
        TouchProcessor.setMouseMovedProcessor(point -> {
            callbacks[0]++;
            return null;
        }, null);

        engine.startNewPage(new FirstEmptyPage());
        engine.startNewPage(new SecondEmptyPage());
        clearPageChangeSignals();
        dispatchTouch();
        dispatchKeyboard();
        dispatchMouse();
        VRAMobject.onRedraw();

        assertAll(
                () -> assertFalse(globalVram.deleted),
                () -> assertFalse(bridge.shaderBridge.wasDeleted(globalShaderProgram)),
                () -> assertEquals(1, globalVram.reloads),
                () -> assertEquals(2, callbacks[0]),
                () -> assertEquals(1, callbacks[1]),
                () -> assertEquals(1, callbacks[2]),
                () -> assertEquals(1, callbacks[3])
        );

        globalTouch.delete();
        globalPress.delete();
        globalRelease.delete();
        globalCombo.delete();
        TouchProcessor.setMouseMovedProcessor(null, null);
    }

    @Test
    void differentClassTransitionKeepsExistingCleanupAndIncomingDispatchSemantics() {
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        CoreRenderer.engine = engine;
        FirstOwnedPage outgoing = new FirstOwnedPage();
        engine.startNewPage(outgoing);
        SecondOwnedPage incoming = new SecondOwnedPage();

        engine.startNewPage(incoming);
        clearPageChangeSignals();
        dispatchTouch();
        dispatchKeyboard();
        dispatchMouse();

        assertAll(
                () -> assertTrue(outgoing.vram.deleted),
                () -> assertTrue(bridge.shaderBridge.wasDeleted(outgoing.shaderProgram)),
                () -> assertFalse(incoming.vram.deleted),
                () -> assertFalse(bridge.shaderBridge.wasDeleted(incoming.shaderProgram)),
                () -> assertEquals(0, outgoing.callbacks),
                () -> assertEquals(5, incoming.callbacks)
        );
    }

    private static void clearPageChangeSignals() {
        TouchProcessor.processMotions();
        KeyboardProcessor.processKeys();
    }

    private static void dispatchTouch() {
        TouchProcessor.onTouch(new SinglePointerEvent(MyMotionEvent.ACTION_DOWN));
        TouchProcessor.processMotions();
        TouchProcessor.onTouch(new SinglePointerEvent(MyMotionEvent.ACTION_UP));
        TouchProcessor.processMotions();
    }

    private static void dispatchKeyboard() {
        KeyboardProcessor.onKeyPressed(PRESS_KEY);
        KeyboardProcessor.processKeys();
        KeyboardProcessor.onKeyReleased(PRESS_KEY);
        KeyboardProcessor.processKeys();

        KeyboardProcessor.onKeyPressed(COMBO_KEY_1);
        KeyboardProcessor.onKeyPressed(COMBO_KEY_2);
        KeyboardProcessor.processKeys();
        KeyboardProcessor.onKeyReleased(COMBO_KEY_1);
        KeyboardProcessor.onKeyReleased(COMBO_KEY_2);
        KeyboardProcessor.processKeys();
    }

    private static void dispatchMouse() {
        TouchProcessor.onMouseMoved(10, 20);
        TouchProcessor.processMotions();
    }

    private static final class RegistryPage extends GamePageClass {
        private final String name;
        private final TestVram constructorVram;
        private final int constructorShaderProgram;
        private final TouchProcessor constructorTouch;
        private final KeyListener keyListener;
        private final KeyReleasedListener keyReleasedListener;
        private final KeyComboListener keyComboListener;
        private TestVram surfaceVram;
        private int surfaceShaderProgram;
        private TouchProcessor surfaceTouch;
        private int touchStarts;
        private int keyPresses;
        private int keyReleases;
        private int keyCombos;
        private int mouseMoves;

        private RegistryPage(String name) {
            this.name = name;
            constructorVram = new TestVram(this);
            new Shader("vertex", "fragment", this, new NoOpAdaptor());
            constructorShaderProgram = shaderBridge().lastCreatedProgram();
            constructorTouch = touchProcessor(10);
            keyListener = new KeyListener(PRESS_KEY, key -> {
                keyPresses++;
                return null;
            }, this);
            keyReleasedListener = new KeyReleasedListener(PRESS_KEY, key -> {
                keyReleases++;
                return null;
            }, this);
            keyComboListener = new KeyComboListener(
                    COMBO_KEY_1,
                    COMBO_KEY_2,
                    key -> {
                        keyCombos++;
                        return null;
                    },
                    this
            );
            installMouseCallback();
        }

        private TouchProcessor touchProcessor(int priority) {
            TouchProcessor processor = new TouchProcessor(
                    point -> true,
                    point -> {
                        touchStarts++;
                        return null;
                    },
                    null,
                    null,
                    this
            );
            processor.setPriority(priority);
            return processor;
        }

        private void installMouseCallback() {
            TouchProcessor.setMouseMovedProcessor(point -> {
                mouseMoves++;
                return null;
            }, this);
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
            if (surfaceVram == null) {
                surfaceVram = new TestVram(this);
                new Shader("vertex", "fragment", this, new NoOpAdaptor());
                surfaceShaderProgram = shaderBridge().lastCreatedProgram();
                surfaceTouch = touchProcessor(20);
            }
        }

        private static RecordingShaderBridge shaderBridge() {
            return ((RecordingBridge) CoreRenderer.engine.getPlatformBridge()).shaderBridge;
        }

        @Override
        public void draw() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static class EmptyPage extends GamePageClass {
        @Override
        public void onSurfaceChanged(int x, int y) {
        }

        @Override
        public void draw() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static final class FirstEmptyPage extends EmptyPage {
    }

    private static final class SecondEmptyPage extends EmptyPage {
    }

    private abstract static class BasicOwnedPage extends GamePageClass {
        final TestVram vram;
        final int shaderProgram;
        int callbacks;

        private BasicOwnedPage() {
            vram = new TestVram(this);
            new Shader("vertex", "fragment", this, new NoOpAdaptor());
            shaderProgram = RegistryPage.shaderBridge().lastCreatedProgram();
            new TouchProcessor(
                    point -> true,
                    point -> {
                        callbacks++;
                        return null;
                    },
                    null,
                    null,
                    this
            );
            new KeyListener(PRESS_KEY, key -> {
                callbacks++;
                return null;
            }, this);
            new KeyReleasedListener(PRESS_KEY, key -> {
                callbacks++;
                return null;
            }, this);
            new KeyComboListener(COMBO_KEY_1, COMBO_KEY_2, key -> {
                callbacks++;
                return null;
            }, this);
            TouchProcessor.setMouseMovedProcessor(point -> {
                callbacks++;
                return null;
            }, this);
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
        }

        @Override
        public void draw() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static final class FirstOwnedPage extends BasicOwnedPage {
    }

    private static final class SecondOwnedPage extends BasicOwnedPage {
    }

    private static final class RecordingObserver implements RuntimeObserver {
        private FrameContext frameContext;

        @Override
        public void beforeFrame(FrameContext frameContext) {
            this.frameContext = frameContext;
        }
    }

    private static final class TestVram extends VRAMobject {
        private boolean deleted;
        private int reloads;

        private TestVram(GamePageClass owner) {
            super(owner);
        }

        @Override
        public void delete() {
            deleted = true;
        }

        @Override
        public void reload() {
            reloads++;
        }
    }

    private static final class SinglePointerEvent implements MyMotionEvent {
        private final int action;

        private SinglePointerEvent(int action) {
            this.action = action;
        }

        @Override
        public int getActionMasked() {
            return action;
        }

        @Override
        public int getActionIndex() {
            return 0;
        }

        @Override
        public int getPointerId(int index) {
            return 1;
        }

        @Override
        public int getPointerCount() {
            return 1;
        }

        @Override
        public float getX(int index) {
            return 10;
        }

        @Override
        public float getY(int index) {
            return 20;
        }
    }

    private static final class RecordingBridge extends DesktopBridge {
        private final RecordingShaderBridge shaderBridge = new RecordingShaderBridge();
        private final SealAssetManager assetManager = new SealAssetManager() {
            @Override
            public InputStream load(String path) {
                return new ByteArrayInputStream(path.getBytes());
            }

            @Override
            public String loadText(String path) {
                return path;
            }

            @Override
            public byte[] loadBytes(String path) {
                return path.getBytes();
            }
        };

        @Override
        public ShaderBridge getShaderBridge() {
            return shaderBridge;
        }

        @Override
        public SealAssetManager getAssetManager() {
            return assetManager;
        }
    }

    private static final class RecordingShaderBridge extends ShaderBridge {
        private int nextId = 1;
        private final List<Integer> deletedPrograms = new ArrayList<>();
        private final List<Integer> createdPrograms = new ArrayList<>();

        private int lastCreatedProgram() {
            return createdPrograms.get(createdPrograms.size() - 1);
        }

        private boolean wasDeleted(int program) {
            return deletedPrograms.contains(program);
        }

        private int deleteCount(int program) {
            int count = 0;
            for (int deletedProgram : deletedPrograms) {
                if (deletedProgram == program) {
                    count++;
                }
            }
            return count;
        }

        @Override
        public void deleteProgram(int link) {
            deletedPrograms.add(link);
        }

        @Override
        public int glCreateProgram() {
            int program = nextId++;
            createdPrograms.add(program);
            return program;
        }

        @Override
        public void glAttachShader(int prog, int shader) {
        }

        @Override
        public void glLinkProgram(int programId) {
        }

        @Override
        public void glGetProgramiv(int shaderId, int type, int[] status, int i) {
            status[i] = 1;
        }

        @Override
        public void glShaderSource(int shader, String text) {
        }

        @Override
        public void glCompileShader(int shader) {
        }

        @Override
        public void glUseProgram(int programId) {
        }

        @Override
        public String glGetProgramInfoLog(int programId) {
            return "";
        }

        @Override
        public void glGetShaderiv(int shaderId, int type, int[] status, int i) {
            status[i] = 1;
        }

        @Override
        public int glCreateShader(int type) {
            return nextId++;
        }

        @Override
        public void glDeleteShader(int id) {
        }

        @Override
        public String glGetShaderInfoLog(int programId) {
            return "";
        }
    }

    private static final class NoOpAdaptor extends Adaptor {
        @Override
        public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
            return 0;
        }

        @Override
        public void bindDataLine(PVector a, PVector b, PVector color) {
        }

        @Override
        public void updateLocations() {
        }

        @Override
        public int getTransformMatrixLocation() {
            return 0;
        }

        @Override
        public int getCameraLocation() {
            return 0;
        }

        @Override
        public int getProjectionLocation() {
            return 0;
        }

        @Override
        public int getTextureLocation() {
            return 0;
        }

        @Override
        public int getNormalTextureLocation() {
            return 0;
        }

        @Override
        public int getNormalMapEnableLocation() {
            return 0;
        }

        @Override
        public int getCameraPosLlocation() {
            return 0;
        }
    }
}
