package com.nikitos;

import com.nikitos.main.keyboard.KeyListener;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.platform.DesktopBridge;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.runtime.FrameContext;
import com.nikitos.runtime.PageTransition;
import com.nikitos.runtime.RuntimeFailure;
import com.nikitos.runtime.RuntimeObserver;
import com.nikitos.utils.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeObserverLifecycleTest {
    private static final String TEST_KEY = "RUNTIME_OBSERVER_TEST";

    private final List<KeyListener> keyListeners = new ArrayList<>();
    private final List<GamePageClass> touchPages = new ArrayList<>();

    @AfterEach
    void clearInputState() {
        for (KeyListener listener : keyListeners) {
            listener.delete();
        }
        keyListeners.clear();
        for (GamePageClass page : touchPages) {
            TouchProcessor.setMouseMovedProcessor(null, page);
        }
        touchPages.clear();
        KeyboardProcessor.onKeyReleased(TEST_KEY);
        if (CoreRenderer.engine != null) {
            TouchProcessor.onPageChange();
            TouchProcessor.processMotions();
            KeyboardProcessor.onPageChange();
            KeyboardProcessor.processKeys();
        }
    }

    @Test
    void engineReportsInitialAndSubsequentTransitionsAfterCompletion() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        LauncherParams params = new LauncherParams().setRuntimeObserver(observer);
        Engine engine = new Engine(new DesktopBridge(), params);
        CoreRenderer.engine = engine;
        TestPage first = new TestPage("first", events, null);
        TestPage second = new TestPage("second", events, null);

        engine.startNewPage(first);
        engine.startNewPage(second);

        assertAll(
                () -> assertSame(observer, engine.getRuntimeObserver()),
                () -> assertEquals(
                        List.of(
                                "surface:first",
                                "changed:null->first",
                                "surface:second",
                                "changed:first->second"
                        ),
                        events
                ),
                () -> assertEquals(2, observer.transitions.size()),
                () -> assertSame(null, observer.transitions.get(0).getPreviousPage()),
                () -> assertSame(first, observer.transitions.get(0).getNewPage()),
                () -> assertSame(first, observer.transitions.get(1).getPreviousPage()),
                () -> assertSame(second, observer.transitions.get(1).getNewPage())
        );
    }

    @Test
    void engineReportsTransitionFailureAndPreservesOriginalPropagation() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        Engine engine = new Engine(
                new DesktopBridge(),
                new LauncherParams().setRuntimeObserver(observer)
        );
        CoreRenderer.engine = engine;
        IllegalStateException cause = new IllegalStateException("surface failed");
        TestPage page = new TestPage("failing", events, cause);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> engine.startNewPage(page)
        );

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(List.of("surface:failing", "failure:PAGE_TRANSITION"), events),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_TRANSITION,
                        cause,
                        null,
                        page
                )
        );
    }

    @Test
    void pageChangedCallbackFailurePropagatesWithoutRecursiveFailureNotification() {
        List<String> events = new ArrayList<>();
        IllegalStateException callbackFailure = new IllegalStateException("observer failed");
        RecordingObserver observer = new RecordingObserver(events) {
            @Override
            public void onPageChanged(PageTransition transition) {
                super.onPageChanged(transition);
                throw callbackFailure;
            }
        };
        Engine engine = new Engine(
                new DesktopBridge(),
                new LauncherParams().setRuntimeObserver(observer)
        );
        CoreRenderer.engine = engine;

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> engine.startNewPage(new TestPage("page", events, null))
        );

        assertAll(
                () -> assertSame(callbackFailure, thrown),
                () -> assertTrue(observer.failures.isEmpty()),
                () -> assertEquals(List.of("surface:page", "changed:null->page"), events)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void initialPageCallbackFailurePropagatesWithoutReclassification(boolean bsodAllowed) {
        List<String> events = new ArrayList<>();
        IllegalStateException callbackFailure = new IllegalStateException("observer failed");
        RecordingObserver observer = new RecordingObserver(events) {
            @Override
            public void onPageChanged(PageTransition transition) {
                super.onPageChanged(transition);
                throw callbackFailure;
            }
        };
        TestPage page = new TestPage("page", events, null);
        TestPage bsodPage = new TestPage("bsod", events, null);
        LauncherParams params = new LauncherParams()
                .setRuntimeObserver(observer)
                .setUseBSOD(bsodAllowed)
                .setStartPage(ignored -> page);
        Engine engine = new Engine(new DesktopBridge(), params, ignored -> bsodPage);
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> bsodPage);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                renderer::draw
        );

        assertAll(
                () -> assertSame(callbackFailure, thrown),
                () -> assertTrue(observer.failures.isEmpty()),
                () -> assertEquals(
                        List.of("before:1", "surface:page", "changed:null->page"),
                        events
                ),
                () -> assertSame(page, engine.getGamePage())
        );
    }

    @Test
    void initialTransitionFailureIsReportedOnceAsPageTransition() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("surface failed");
        TestPage page = new TestPage("page", events, cause);
        LauncherParams params = new LauncherParams()
                .setRuntimeObserver(observer)
                .setStartPage(ignored -> page);
        Engine engine = new Engine(new DesktopBridge(), params);
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> page);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                renderer::draw
        );

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(
                        List.of("before:1", "surface:page", "failure:PAGE_TRANSITION"),
                        events
                ),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_TRANSITION,
                        cause,
                        null,
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void inFrameTransitionFailureIsReportedOnceAsPageTransition(boolean bsodAllowed) {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("surface failed");
        TestPage targetPage = new TestPage("target", events, cause);
        TestPage bsodPage = new TestPage("bsod", events, null);
        Engine[] engineRef = new Engine[1];
        TestPage currentPage = new TestPage(
                "current",
                events,
                null,
                () -> engineRef[0].startNewPage(targetPage)
        );
        LauncherParams params = new LauncherParams()
                .setRuntimeObserver(observer)
                .setUseBSOD(bsodAllowed);
        Engine engine = new Engine(new DesktopBridge(), params, ignored -> bsodPage);
        engineRef[0] = engine;
        CoreRenderer.engine = engine;
        engine.startNewPage(currentPage);
        observer.clear();
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> bsodPage);

        if (bsodAllowed) {
            renderer.draw();
        } else {
            assertSame(cause, assertThrows(IllegalStateException.class, renderer::draw));
        }

        assertAll(
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_TRANSITION,
                        cause,
                        null,
                        targetPage
                ),
                () -> assertFalse(observer.failures.stream().anyMatch(
                        failure -> failure.getStage() == RuntimeFailure.Stage.PAGE_DRAW
                )),
                () -> assertSame(bsodAllowed ? bsodPage : targetPage, engine.getGamePage()),
                () -> assertEquals(bsodAllowed ? 1 : 0, observer.afterContexts.size())
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void inFramePageChangedFailurePropagatesWithoutFailureOrBsod(boolean bsodAllowed) {
        List<String> events = new ArrayList<>();
        IllegalStateException callbackFailure = new IllegalStateException("observer failed");
        TestPage targetPage = new TestPage("target", events, null);
        RecordingObserver observer = new RecordingObserver(events) {
            @Override
            public void onPageChanged(PageTransition transition) {
                super.onPageChanged(transition);
                if (transition.getNewPage() == targetPage) {
                    throw callbackFailure;
                }
            }
        };
        TestPage bsodPage = new TestPage("bsod", events, null);
        Engine[] engineRef = new Engine[1];
        TestPage currentPage = new TestPage(
                "current",
                events,
                null,
                () -> engineRef[0].startNewPage(targetPage)
        );
        LauncherParams params = new LauncherParams()
                .setRuntimeObserver(observer)
                .setUseBSOD(bsodAllowed);
        Engine engine = new Engine(new DesktopBridge(), params, ignored -> bsodPage);
        engineRef[0] = engine;
        CoreRenderer.engine = engine;
        engine.startNewPage(currentPage);
        observer.clear();
        CoreRenderer renderer = new CoreRenderer(engine, ignored -> bsodPage);

        IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                renderer::draw
        );

        assertAll(
                () -> assertSame(callbackFailure, thrown),
                () -> assertTrue(observer.failures.isEmpty()),
                () -> assertSame(targetPage, engine.getGamePage()),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void transitionErrorIsReportedAndNeverConvertedToBsod(boolean bsodAllowed) {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        AssertionError cause = new AssertionError("surface error");
        TestPage targetPage = new TestPage("target", events, cause);
        TestPage bsodPage = new TestPage("bsod", events, null);
        LauncherParams params = new LauncherParams()
                .setRuntimeObserver(observer)
                .setUseBSOD(bsodAllowed);
        Engine engine = new Engine(new DesktopBridge(), params, ignored -> bsodPage);
        CoreRenderer.engine = engine;

        AssertionError thrown = assertThrows(
                AssertionError.class,
                () -> engine.startNewPage(targetPage)
        );

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_TRANSITION,
                        cause,
                        null,
                        targetPage
                ),
                () -> assertSame(targetPage, engine.getGamePage())
        );
    }

    @Test
    void observedFramesHaveMonotonicIdsContextAndCallbackOrder() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        Utils.setDim(1024, 768, 1, 1);
        CoreRenderer renderer = renderer(engine);
        registerInput(page, events, null);

        queueInput();
        renderer.draw();
        KeyboardProcessor.onKeyReleased(TEST_KEY);
        queueInput();
        renderer.draw();

        assertAll(
                () -> assertEquals(
                        List.of(
                                "before:1", "fps", "draw:page", "touch", "keyboard", "after:1",
                                "before:2", "fps", "draw:page", "touch", "keyboard", "after:2"
                        ),
                        events
                ),
                () -> assertEquals(2, observer.beforeContexts.size()),
                () -> assertEquals(2, observer.afterContexts.size()),
                () -> assertFrameContext(observer.beforeContexts.get(0), 1, page, 1024, 768),
                () -> assertFrameContext(observer.beforeContexts.get(1), 2, page, 1024, 768),
                () -> assertSame(observer.beforeContexts.get(0), observer.afterContexts.get(0)),
                () -> assertSame(observer.beforeContexts.get(1), observer.afterContexts.get(1))
        );
    }

    @Test
    void noObserverUsesLegacyStageOrderWithoutCallbacks() {
        List<String> events = new ArrayList<>();
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(null, false, page, events);
        CoreRenderer renderer = renderer(engine);
        registerInput(page, events, null);
        queueInput();

        renderer.draw();

        assertEquals(List.of("fps", "draw:page", "touch", "keyboard"), events);
    }

    @Test
    void bsodPageFailureIsReportedAndPostPageStagesContinue() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("page failed");
        TestPage page = new TestPage("page", events, cause);
        LifecycleEngine engine = new LifecycleEngine(observer, true, page, events);
        CoreRenderer renderer = renderer(engine);
        registerInput(page, events, null);
        queueInput();

        renderer.draw();

        RuntimeFailure failure = observer.failures.get(0);
        // The test engine swaps the current instance without registry cleanup;
        // an outgoing page callback must still not dispatch for the BSOD instance.
        assertAll(
                () -> assertEquals(
                        List.of(
                                "before:1", "fps", "draw:page", "failure:PAGE_DRAW",
                                "transition:bsod", "keyboard", "after:1"
                        ),
                        events
                ),
                () -> assertFailure(
                        failure,
                        RuntimeFailure.Stage.PAGE_DRAW,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertSame(engine.getGamePage(), engine.bsodPage)
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void pageDrawErrorIsReportedAndNeverConvertedToBsod(boolean bsodAllowed) {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        AssertionError cause = new AssertionError("page error");
        TestPage page = new TestPage("page", events, cause);
        LifecycleEngine engine = new LifecycleEngine(observer, bsodAllowed, page, events);
        CoreRenderer renderer = renderer(engine);

        AssertionError thrown = assertThrows(AssertionError.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_DRAW,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertSame(page, engine.getGamePage()),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void nonBsodPageFailureIsReportedAndOriginalCausePropagates() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("page failed");
        TestPage page = new TestPage("page", events, cause);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        CoreRenderer renderer = renderer(engine);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(
                        List.of("before:1", "fps", "draw:page", "failure:PAGE_DRAW"),
                        events
                ),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.PAGE_DRAW,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void postPageFailureReportsExactStageAndPropagates() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("keyboard failed");
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        CoreRenderer renderer = renderer(engine);
        registerInput(page, events, cause);
        queueInput();

        IllegalStateException thrown = assertThrows(IllegalStateException.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(
                        List.of(
                                "before:1", "fps", "draw:page", "touch", "keyboard",
                                "failure:KEYBOARD_PROCESS"
                        ),
                        events
                ),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.KEYBOARD_PROCESS,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void postPageErrorReportsExactStageAndPropagates() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        AssertionError cause = new AssertionError("keyboard error");
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        CoreRenderer renderer = renderer(engine);
        registerInput(page, events, cause);
        queueInput();

        AssertionError thrown = assertThrows(AssertionError.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.KEYBOARD_PROCESS,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void observerFailureIsSuppressedOnOriginalFrameFailure() {
        List<String> events = new ArrayList<>();
        IllegalStateException observerFailure = new IllegalStateException("observer failed");
        RecordingObserver observer = new RecordingObserver(events) {
            @Override
            public void onFailure(RuntimeFailure failure) {
                super.onFailure(failure);
                throw observerFailure;
            }
        };
        IllegalStateException cause = new IllegalStateException("page failed");
        TestPage page = new TestPage("page", events, cause);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        CoreRenderer renderer = renderer(engine);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(1, thrown.getSuppressed().length),
                () -> assertSame(observerFailure, thrown.getSuppressed()[0]),
                () -> assertEquals(
                        List.of("before:1", "fps", "draw:page", "failure:PAGE_DRAW"),
                        events
                )
        );
    }

    @Test
    void frameSetupFailureReportsItsStageAndPropagates() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        IllegalStateException cause = new IllegalStateException("fps failed");
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        engine.setupFailure = cause;
        CoreRenderer renderer = renderer(engine);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(
                        List.of("before:1", "fps", "failure:FRAME_SETUP"),
                        events
                ),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.FRAME_SETUP,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void frameSetupErrorReportsItsStageAndPropagates() {
        List<String> events = new ArrayList<>();
        RecordingObserver observer = new RecordingObserver(events);
        AssertionError cause = new AssertionError("fps error");
        TestPage page = new TestPage("page", events, null);
        LifecycleEngine engine = new LifecycleEngine(observer, false, page, events);
        engine.setupFailure = cause;
        CoreRenderer renderer = renderer(engine);

        AssertionError thrown = assertThrows(AssertionError.class, renderer::draw);

        assertAll(
                () -> assertSame(cause, thrown),
                () -> assertEquals(1, observer.failures.size()),
                () -> assertFailure(
                        observer.failures.get(0),
                        RuntimeFailure.Stage.FRAME_SETUP,
                        cause,
                        observer.beforeContexts.get(0),
                        page
                ),
                () -> assertTrue(observer.afterContexts.isEmpty())
        );
    }

    @Test
    void frameBoundaryCallbackFailuresPropagateWithoutFailureNotification() {
        List<String> beforeEvents = new ArrayList<>();
        IllegalStateException beforeFailure = new IllegalStateException("before failed");
        RecordingObserver beforeObserver = new RecordingObserver(beforeEvents) {
            @Override
            public void beforeFrame(FrameContext context) {
                super.beforeFrame(context);
                throw beforeFailure;
            }
        };
        TestPage beforePage = new TestPage("before-page", beforeEvents, null);
        CoreRenderer beforeRenderer = renderer(
                new LifecycleEngine(beforeObserver, false, beforePage, beforeEvents)
        );

        IllegalStateException beforeThrown = assertThrows(
                IllegalStateException.class,
                beforeRenderer::draw
        );

        List<String> afterEvents = new ArrayList<>();
        IllegalStateException afterFailure = new IllegalStateException("after failed");
        RecordingObserver afterObserver = new RecordingObserver(afterEvents) {
            @Override
            public void afterFrame(FrameContext context) {
                super.afterFrame(context);
                throw afterFailure;
            }
        };
        TestPage afterPage = new TestPage("after-page", afterEvents, null);
        CoreRenderer afterRenderer = renderer(
                new LifecycleEngine(afterObserver, false, afterPage, afterEvents)
        );

        IllegalStateException afterThrown = assertThrows(
                IllegalStateException.class,
                afterRenderer::draw
        );

        assertAll(
                () -> assertSame(beforeFailure, beforeThrown),
                () -> assertEquals(List.of("before:1"), beforeEvents),
                () -> assertTrue(beforeObserver.failures.isEmpty()),
                () -> assertSame(afterFailure, afterThrown),
                () -> assertEquals(
                        List.of("before:1", "fps", "draw:after-page", "after:1"),
                        afterEvents
                ),
                () -> assertTrue(afterObserver.failures.isEmpty())
        );
    }

    private CoreRenderer renderer(LifecycleEngine engine) {
        TestPage bsodPage = new TestPage("bsod", engine.events, null);
        engine.bsodPage = bsodPage;
        return new CoreRenderer(engine, ignored -> bsodPage);
    }

    private void registerInput(
            GamePageClass page,
            List<String> events,
            Throwable keyboardFailure
    ) {
        TouchProcessor.processMotions();
        KeyboardProcessor.processKeys();
        TouchProcessor.setMouseMovedProcessor(point -> {
            events.add("touch");
            return null;
        }, page);
        touchPages.add(page);
        KeyListener keyListener = new KeyListener(TEST_KEY, key -> {
            events.add("keyboard");
            if (keyboardFailure != null) {
                throwUnchecked(keyboardFailure);
            }
            return null;
        }, page);
        keyListeners.add(keyListener);
    }

    private static void queueInput() {
        TouchProcessor.onMouseMoved(10, 20);
        KeyboardProcessor.onKeyPressed(TEST_KEY);
    }

    private static void assertFrameContext(
            FrameContext context,
            long id,
            GamePageClass page,
            int width,
            int height
    ) {
        assertAll(
                () -> assertEquals(id, context.getFrameId()),
                () -> assertSame(page, context.getPage()),
                () -> assertEquals(width, context.getWidth()),
                () -> assertEquals(height, context.getHeight()),
                () -> assertSame(com.nikitos.platformBridge.Platform.DESKTOP, context.getPlatform())
        );
    }

    private static void assertFailure(
            RuntimeFailure failure,
            RuntimeFailure.Stage stage,
            Throwable cause,
            FrameContext context,
            GamePageClass page
    ) {
        assertAll(
                () -> assertSame(stage, failure.getStage()),
                () -> assertSame(cause, failure.getCause()),
                () -> assertSame(context, failure.getFrameContext()),
                () -> assertSame(page, failure.getPage())
        );
    }

    private static class RecordingObserver implements RuntimeObserver {
        final List<String> events;
        final List<FrameContext> beforeContexts = new ArrayList<>();
        final List<FrameContext> afterContexts = new ArrayList<>();
        final List<PageTransition> transitions = new ArrayList<>();
        final List<RuntimeFailure> failures = new ArrayList<>();

        RecordingObserver(List<String> events) {
            this.events = events;
        }

        @Override
        public void beforeFrame(FrameContext context) {
            beforeContexts.add(context);
            events.add("before:" + context.getFrameId());
        }

        @Override
        public void afterFrame(FrameContext context) {
            afterContexts.add(context);
            events.add("after:" + context.getFrameId());
        }

        @Override
        public void onPageChanged(PageTransition transition) {
            transitions.add(transition);
            events.add(
                    "changed:"
                            + pageName(transition.getPreviousPage())
                            + "->"
                            + pageName(transition.getNewPage())
            );
        }

        @Override
        public void onFailure(RuntimeFailure failure) {
            failures.add(failure);
            events.add("failure:" + failure.getStage());
        }

        void clear() {
            events.clear();
            beforeContexts.clear();
            afterContexts.clear();
            transitions.clear();
            failures.clear();
        }
    }

    private static final class LifecycleEngine extends Engine {
        final List<String> events;
        private final boolean bsodAllowed;
        private GamePageClass currentPage;
        private Throwable setupFailure;
        private GamePageClass bsodPage;

        LifecycleEngine(
                RuntimeObserver observer,
                boolean bsodAllowed,
                GamePageClass currentPage,
                List<String> events
        ) {
            super(
                    new DesktopBridge(),
                    observer == null
                            ? new LauncherParams()
                            : new LauncherParams().setRuntimeObserver(observer)
            );
            this.bsodAllowed = bsodAllowed;
            this.currentPage = currentPage;
            this.events = events;
        }

        @Override
        public void calculateFps() {
            events.add("fps");
            if (setupFailure != null) {
                throwUnchecked(setupFailure);
            }
        }

        @Override
        public boolean getBsodAllowed() {
            return bsodAllowed;
        }

        @Override
        GamePageClass getGamePage() {
            return currentPage;
        }

        @Override
        public Class<?> getPageClass() {
            return currentPage.getClass();
        }

        @Override
        public void startNewPage(GamePageClass newPage) {
            currentPage = newPage;
            events.add("transition:" + pageName(newPage));
        }
    }

    private static final class TestPage extends GamePageClass {
        private final String name;
        private final List<String> events;
        private final Throwable failure;
        private final Runnable drawAction;

        TestPage(String name, List<String> events, Throwable failure) {
            this(name, events, failure, null);
        }

        TestPage(
                String name,
                List<String> events,
                Throwable failure,
                Runnable drawAction
        ) {
            this.name = name;
            this.events = events;
            this.failure = failure;
            this.drawAction = drawAction;
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
            events.add("surface:" + name);
            if (failure != null) {
                throwUnchecked(failure);
            }
        }

        @Override
        public void draw() {
            events.add("draw:" + name);
            if (failure != null) {
                throwUnchecked(failure);
            }
            if (drawAction != null) {
                drawAction.run();
            }
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }
    }

    private static String pageName(GamePageClass page) {
        return page == null ? "null" : ((TestPage) page).name;
    }

    private static void throwUnchecked(Throwable failure) {
        if (failure instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        throw (Error) failure;
    }
}
