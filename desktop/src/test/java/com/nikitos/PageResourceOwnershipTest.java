package com.nikitos;

import com.nikitos.main.VRAMobject;
import com.nikitos.main.frameBuffers.FrameBuffer;
import com.nikitos.main.keyboard.KeyComboListener;
import com.nikitos.main.keyboard.KeyListener;
import com.nikitos.main.keyboard.KeyReleasedListener;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.light.AmbientLight;
import com.nikitos.main.light.DirectedLight;
import com.nikitos.main.light.ExpouseSettings;
import com.nikitos.main.light.Material;
import com.nikitos.main.light.PointLight;
import com.nikitos.main.light.SourceLight;
import com.nikitos.main.shaders.Adaptor;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.main.touch.MyMotionEvent;
import com.nikitos.main.touch.TouchPoint;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.main.vertices.Face;
import com.nikitos.maths.PVector;
import com.nikitos.platform.DesktopBridge;
import com.nikitos.platform.GeneralBridgeDesktop;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.platformBridge.SealAssetManager;
import com.nikitos.platformBridge.ShaderBridge;
import com.nikitos.platformBridge.VertexBridge;
import com.nikitos.runtime.FrameContext;
import com.nikitos.runtime.RuntimeObserver;
import com.nikitos.runtime.RuntimeResourceSnapshot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        int expectedShaderData = Adaptor.getTrackedShaderDataCount();
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
                () -> assertEquals(expectedMouse, snapshot.getDesktopMouseCallbackRegistrations()),
                () -> assertEquals(expectedShaderData, snapshot.getShaderData())
        );
    }

    @Test
    void framebufferResizeAfterDrawReusesItsTrackedVertexBufferAndDeletesItOnce() {
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        CoreRenderer.engine = engine;
        EmptyPage owner = new EmptyPage();
        engine.startNewPage(owner);
        Shader shader = new Shader("vertex", "fragment", owner, new NoOpAdaptor());
        shader.apply();
        FrameBuffer frameBuffer = new FrameBuffer(640, 360, owner);
        int trackedBeforeFirstDraw = VRAMobject.getTrackedObjectCount();

        frameBuffer.drawTexture(
                new PVector(0, 0, 1),
                new PVector(640, 0, 1),
                new PVector(0, 360, 1)
        );
        int trackedAfterFirstDraw = VRAMobject.getTrackedObjectCount();
        int bufferAllocationsAfterFirstDraw = bridge.vertexBridge().bufferAllocations;
        int arrayAllocationsAfterFirstDraw = bridge.vertexBridge().arrayAllocations;
        int framebufferBeforeInvalidResize = frameBuffer.getFrameBuffer();
        assertEquals(trackedBeforeFirstDraw, trackedAfterFirstDraw);

        assertThrows(
                IllegalArgumentException.class,
                () -> frameBuffer.resize(0, 720)
        );
        assertAll(
                () -> assertEquals(640, frameBuffer.getWidth()),
                () -> assertEquals(360, frameBuffer.getHeight()),
                () -> assertEquals(
                        framebufferBeforeInvalidResize,
                        frameBuffer.getFrameBuffer()
                ),
                () -> assertEquals(trackedAfterFirstDraw, VRAMobject.getTrackedObjectCount())
        );

        frameBuffer.resize(1280, 720);
        frameBuffer.drawTexture(
                new PVector(0, 0, 1),
                new PVector(1280, 0, 1),
                new PVector(0, 720, 1)
        );

        assertAll(
                () -> assertEquals(1280, frameBuffer.getWidth()),
                () -> assertEquals(720, frameBuffer.getHeight()),
                () -> assertEquals(trackedAfterFirstDraw, VRAMobject.getTrackedObjectCount()),
                () -> assertEquals(
                        bufferAllocationsAfterFirstDraw,
                        bridge.vertexBridge().bufferAllocations
                ),
                () -> assertEquals(
                        arrayAllocationsAfterFirstDraw,
                        bridge.vertexBridge().arrayAllocations
                )
        );

        VRAMobject.onRedraw();
        assertAll(
                () -> assertEquals(trackedAfterFirstDraw, VRAMobject.getTrackedObjectCount()),
                () -> assertEquals(
                        bufferAllocationsAfterFirstDraw + 1,
                        bridge.vertexBridge().bufferAllocations
                ),
                () -> assertEquals(
                        arrayAllocationsAfterFirstDraw + 1,
                        bridge.vertexBridge().arrayAllocations
                )
        );
        frameBuffer.drawTexture(
                new PVector(0, 0, 1),
                new PVector(1280, 0, 1),
                new PVector(0, 720, 1)
        );
        assertAll(
                () -> assertEquals(
                        bufferAllocationsAfterFirstDraw + 1,
                        bridge.vertexBridge().bufferAllocations
                ),
                () -> assertEquals(
                        arrayAllocationsAfterFirstDraw + 1,
                        bridge.vertexBridge().arrayAllocations
                )
        );

        frameBuffer.delete();
        engine.startNewPage(new FirstEmptyPage());

        assertAll(
                () -> assertEquals(1, bridge.vertexBridge().bufferDeletions),
                () -> assertEquals(1, bridge.vertexBridge().arrayDeletions)
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
        int expectedShaderData = Adaptor.getTrackedShaderDataCount();

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
                () -> assertEquals(expectedMouse, snapshot.getDesktopMouseCallbackRegistrations()),
                () -> assertEquals(expectedShaderData, snapshot.getShaderData())
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

    @Test
    void sameClassTransitionRemovesOnlyOutgoingShaderDataAndReindexesIncomingLights() {
        ShaderDataTestContext context = prepareShaderDataTest();
        SameClassShaderDataPage outgoing = new SameClassShaderDataPage();
        context.engine.startNewPage(outgoing);
        SameClassShaderDataPage incoming = new SameClassShaderDataPage();

        context.engine.startNewPage(incoming);
        context.bridge.generalBridge.clearCalls();
        incoming.applyShader();

        assertIncomingShaderDataOnly(context, outgoing, incoming);
    }

    @Test
    void differentClassTransitionRemovesOnlyOutgoingShaderDataAndReindexesIncomingLights() {
        ShaderDataTestContext context = prepareShaderDataTest();
        FirstShaderDataPage outgoing = new FirstShaderDataPage();
        context.engine.startNewPage(outgoing);
        SecondShaderDataPage incoming = new SecondShaderDataPage();

        context.engine.startNewPage(incoming);
        context.bridge.generalBridge.clearCalls();
        incoming.applyShader();

        assertIncomingShaderDataOnly(context, outgoing, incoming);
    }

    @Test
    void globalShaderDataSurvivesPageTransitions() {
        ShaderDataTestContext context = prepareShaderDataTest();
        RecordingShaderData global = new RecordingShaderData(null);
        context.engine.startNewPage(new FirstEmptyPage());
        SecondEmptyPage currentPage = new SecondEmptyPage();
        context.engine.startNewPage(currentPage);
        Shader shader = new Shader("vertex", "fragment", currentPage, new NoOpAdaptor());

        shader.apply();

        assertAll(
                () -> assertEquals(context.baselineShaderData + 1, Adaptor.getTrackedShaderDataCount()),
                () -> assertEquals(0, global.deleteCalls),
                () -> assertEquals(1, global.locationCalls),
                () -> assertEquals(1, global.forwardCalls)
        );
    }

    private static ShaderDataTestContext prepareShaderDataTest() {
        RecordingBridge bridge = new RecordingBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        CoreRenderer.engine = engine;
        EmptyPage cleanupPage = new EmptyPage();
        engine.startNewPage(cleanupPage);
        new Shader("vertex", "fragment", cleanupPage, new NoOpAdaptor()).apply();
        return new ShaderDataTestContext(engine, bridge, Adaptor.getTrackedShaderDataCount());
    }

    private static void assertIncomingShaderDataOnly(
            ShaderDataTestContext context,
            ShaderDataOwnedPage outgoing,
            ShaderDataOwnedPage incoming
    ) {
        RecordingGeneralBridge gl = context.bridge.generalBridge;
        assertAll(
                () -> assertEquals(
                        context.baselineShaderData + ShaderDataOwnedPage.OWNED_SHADER_DATA,
                        Adaptor.getTrackedShaderDataCount()
                ),
                () -> assertEquals(1, outgoing.constructorData.deleteCalls),
                () -> assertEquals(1, outgoing.surfaceData.deleteCalls),
                () -> assertEquals(0, outgoing.constructorData.forwardCalls),
                () -> assertEquals(0, outgoing.surfaceData.forwardCalls),
                () -> assertEquals(0, incoming.constructorData.deleteCalls),
                () -> assertEquals(0, incoming.surfaceData.deleteCalls),
                () -> assertEquals(1, incoming.constructorData.forwardCalls),
                () -> assertEquals(1, incoming.surfaceData.forwardCalls),
                () -> assertEquals(1, gl.callCount("aLight.color")),
                () -> assertEquals(1, gl.callCount("exposure")),
                () -> assertEquals(1, gl.callCount("gamma")),
                () -> assertEquals(1, gl.requestCount("material.ambient")),
                () -> assertEquals(1, gl.lastInt("dLightNum")),
                () -> assertEquals(1, gl.lastInt("pLightNum")),
                () -> assertEquals(1, gl.lastInt("sLightNum")),
                () -> assertEquals(1, gl.requestCount("dLights[0].color")),
                () -> assertEquals(0, gl.requestCount("dLights[1].color")),
                () -> assertEquals(1, gl.requestCount("pLights[0].color")),
                () -> assertEquals(0, gl.requestCount("pLights[1].color")),
                () -> assertEquals(1, gl.requestCount("sLights[0].color")),
                () -> assertEquals(0, gl.requestCount("sLights[1].color"))
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

    private abstract static class ShaderDataOwnedPage extends GamePageClass {
        private static final int OWNED_SHADER_DATA = 8;
        private final Shader shader;
        private final RecordingShaderData constructorData;
        private final AmbientLight ambientLight;
        private final DirectedLight directedLight;
        private final PointLight pointLight;
        private final SourceLight sourceLight;
        private final ExpouseSettings expouseSettings;
        private final Material material;
        private RecordingShaderData surfaceData;

        private ShaderDataOwnedPage() {
            shader = new Shader("vertex", "fragment", this, new NoOpAdaptor());
            constructorData = new RecordingShaderData(this);
            ambientLight = new AmbientLight(this);
            directedLight = new DirectedLight(this);
            directedLight.color = vector();
            directedLight.direction = vector();
            pointLight = new PointLight(this);
            pointLight.color = vector();
            pointLight.position = vector();
            sourceLight = new SourceLight(this);
            sourceLight.color = vector();
            sourceLight.position = vector();
            sourceLight.direction = vector();
            expouseSettings = new ExpouseSettings(this);
            material = new Material(this);
            material.ambient = vector();
            material.diffuse = vector();
            material.specular = vector();
        }

        private static PVector vector() {
            return new PVector(1, 2, 3);
        }

        final void applyShader() {
            shader.apply();
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
            if (surfaceData == null) {
                surfaceData = new RecordingShaderData(this);
            }
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

    private static final class SameClassShaderDataPage extends ShaderDataOwnedPage {
    }

    private static final class FirstShaderDataPage extends ShaderDataOwnedPage {
    }

    private static final class SecondShaderDataPage extends ShaderDataOwnedPage {
    }

    private static final class RecordingShaderData extends ShaderData {
        private int locationCalls;
        private int forwardCalls;
        private int deleteCalls;

        private RecordingShaderData(GamePageClass owner) {
            super(owner);
        }

        @Override
        protected void getLocations(int programId) {
            locationCalls++;
        }

        @Override
        protected void forwardData() {
            forwardCalls++;
        }

        @Override
        protected void delete() {
            deleteCalls++;
        }
    }

    private static final class ShaderDataTestContext {
        private final Engine engine;
        private final RecordingBridge bridge;
        private final int baselineShaderData;

        private ShaderDataTestContext(Engine engine, RecordingBridge bridge, int baselineShaderData) {
            this.engine = engine;
            this.bridge = bridge;
            this.baselineShaderData = baselineShaderData;
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
        private final RecordingGeneralBridge generalBridge = new RecordingGeneralBridge();
        private RecordingVertexBridge vertexBridge;
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
        public GeneralPlatformBridge getGeneralPlatformBridge() {
            return generalBridge;
        }

        @Override
        public VertexBridge getVertexBridge() {
            return vertexBridge();
        }

        private RecordingVertexBridge vertexBridge() {
            if (vertexBridge == null) {
                vertexBridge = new RecordingVertexBridge();
            }
            return vertexBridge;
        }

        @Override
        public SealAssetManager getAssetManager() {
            return assetManager;
        }
    }

    private static final class RecordingGeneralBridge extends GeneralBridgeDesktop {
        private int nextObject = 1;
        private int nextLocation = 1;
        private final Map<String, Integer> locations = new HashMap<>();
        private final Map<Integer, String> names = new HashMap<>();
        private final Map<String, Integer> locationRequests = new HashMap<>();
        private final Map<String, Integer> calls = new HashMap<>();
        private final Map<String, Integer> lastInts = new HashMap<>();

        @Override
        public int glGetUniformLocation(int program, String name) {
            locationRequests.merge(name, 1, Integer::sum);
            Integer existing = locations.get(name);
            if (existing != null) {
                return existing;
            }
            int location = nextLocation++;
            locations.put(name, location);
            names.put(location, name);
            return location;
        }

        @Override
        public void glUniform3f(int location, float x, float y, float z) {
            record(location);
        }

        @Override
        public void glUniform1f(int location, float val) {
            record(location);
        }

        @Override
        public void glUniform1i(int location, int value) {
            String name = record(location);
            lastInts.put(name, value);
        }

        private String record(int location) {
            String name = names.get(location);
            calls.merge(name, 1, Integer::sum);
            return name;
        }

        private int requestCount(String name) {
            return locationRequests.getOrDefault(name, 0);
        }

        private int callCount(String name) {
            return calls.getOrDefault(name, 0);
        }

        private int lastInt(String name) {
            return lastInts.getOrDefault(name, -1);
        }

        private void clearCalls() {
            locationRequests.clear();
            calls.clear();
            lastInts.clear();
        }

        @Override
        public void glGenTextures(int number, int[] textureIds, int offset) {
            fillIds(number, textureIds, offset);
        }

        @Override
        public void glGenFramebuffers(int number, int[] buffers, int offset) {
            fillIds(number, buffers, offset);
        }

        @Override
        public void genRenderbuffers(int number, int[] buffers, int offset) {
            fillIds(number, buffers, offset);
        }

        private void fillIds(int number, int[] ids, int offset) {
            for (int index = 0; index < number; index++) {
                ids[offset + index] = nextObject++;
            }
        }

        @Override
        public void glActiveTexture(int texture) {
        }

        @Override
        public void glBindTexture(int texture, int location) {
        }

        @Override
        public void glTexImage2D(
                int type,
                int level,
                int internalFormat,
                int width,
                int height,
                int border,
                int texType,
                int localDataType,
                FloatBuffer pixels
        ) {
        }

        @Override
        public void texParameterf(int target, int pname, float param) {
        }

        @Override
        public void glBindFramebuffer(int type, int id) {
        }

        @Override
        public void framebufferTexture2D(
                int target,
                int attachment,
                int textarget,
                int texture,
                int level
        ) {
        }

        @Override
        public void bindRenderbuffer(int target, int renderbuffer) {
        }

        @Override
        public void renderbufferStorage(int target, int internalformat, int width, int height) {
        }

        @Override
        public void framebufferRenderbuffer(
                int target,
                int attachment,
                int renderbufferTarget,
                int renderbuffer
        ) {
        }

        @Override
        public void glDeleteTextures(int number, int[] ids, int offset) {
        }

        @Override
        public void glDeleteFramebuffers(int number, int[] framebuffers, int offset) {
        }

        @Override
        public void glDeleteRenderbuffers(int number, int[] buffers, int offset) {
        }

        @Override
        public void glDrawArrays(int type, int offset, int count) {
        }
    }

    private static final class RecordingVertexBridge extends VertexBridge {
        private int nextObject = 1;
        private int bufferAllocations;
        private int arrayAllocations;
        private int bufferDeletions;
        private int arrayDeletions;

        @Override
        public void glGenBuffers(int number, int[] buffers, int offset) {
            bufferAllocations++;
            fillIds(number, buffers, offset);
        }

        @Override
        public void glDeleteBuffers(int number, int[] buffers, int offset) {
            bufferDeletions++;
        }

        @Override
        public void glGenVertexArrays(int number, int[] arrays, int offset) {
            arrayAllocations++;
            fillIds(number, arrays, offset);
        }

        @Override
        public void glBindVertexArray(int array) {
        }

        @Override
        public void glDeleteVertexArrays(int number, int[] arrays, int offset) {
            arrayDeletions++;
        }

        private void fillIds(int number, int[] ids, int offset) {
            for (int index = 0; index < number; index++) {
                ids[offset + index] = nextObject++;
            }
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
