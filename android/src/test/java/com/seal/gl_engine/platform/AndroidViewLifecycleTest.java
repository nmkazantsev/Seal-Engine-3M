package com.seal.gl_engine.platform;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class AndroidViewLifecycleTest {

    @Test
    public void onlyCurrentViewCanPauseResumeOrDetachAndCallbacksAreIdempotent() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        Object currentView = new Object();

        lifecycle.replace(() -> oldView);
        lifecycle.replace(() -> currentView);
        lifecycle.pause(oldView);
        lifecycle.pause(currentView);
        lifecycle.pause(currentView);
        lifecycle.resume(oldView);
        lifecycle.resume(currentView);
        lifecycle.resume(currentView);
        lifecycle.detach(oldView);
        lifecycle.detach(currentView);
        lifecycle.detach(currentView);

        assertEquals(
                Arrays.asList(
                        "attach-old",
                        "quiesce-old",
                        "attach-current",
                        "pause-current",
                        "resume-current",
                        "detach-current"
                ),
                callbacks.events
        );
    }

    @Test
    public void pausedStateSurvivesViewReplacementUntilCurrentViewResumes() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        Object currentView = new Object();
        callbacks.oldView = oldView;
        callbacks.currentView = currentView;

        lifecycle.replace(() -> oldView);
        lifecycle.pause(oldView);
        lifecycle.replace(() -> currentView);
        lifecycle.resume(currentView);

        assertSame(currentView, lifecycle.current());
        assertEquals(
                Arrays.asList(
                        "attach-old",
                        "pause-old",
                        "attach-current",
                        "pause-current",
                        "resume-current"
                ),
                callbacks.events
        );
    }

    @Test
    public void replacementQuiescesOldViewBeforeRendererActivation() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        Object currentView = new Object();
        callbacks.oldView = oldView;
        callbacks.currentView = currentView;
        lifecycle.replace(() -> oldView);

        Object result = lifecycle.replace(() -> {
            callbacks.events.add("activate-current");
            return currentView;
        });
        lifecycle.pause(oldView);

        assertSame(currentView, result);
        assertSame(currentView, lifecycle.current());
        assertEquals(
                Arrays.asList(
                        "attach-old",
                        "quiesce-old",
                        "activate-current",
                        "attach-current"
                ),
                callbacks.events
        );
    }

    @Test
    public void failedReplacementRestoresRunningOldViewAndKeepsItCurrent() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        callbacks.oldView = oldView;
        lifecycle.replace(() -> oldView);
        RuntimeException activationFailure =
                new RuntimeException("activation failed");

        try {
            lifecycle.replace(failingReplacement(
                    callbacks,
                    activationFailure
            ));
            fail("Expected activation failure");
        } catch (RuntimeException thrown) {
            assertSame(activationFailure, thrown);
        }

        assertSame(oldView, lifecycle.current());
        assertEquals(
                Arrays.asList(
                        "attach-old",
                        "quiesce-old",
                        "activate-failed",
                        "restore-old"
                ),
                callbacks.events
        );
    }

    @Test
    public void nullReplacementRestoresRunningOldView() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        callbacks.oldView = oldView;
        lifecycle.replace(() -> oldView);

        Object result = lifecycle.replace(() -> {
            callbacks.events.add("activate-null");
            return null;
        });

        assertEquals(null, result);
        assertSame(oldView, lifecycle.current());
        assertEquals(
                Arrays.asList(
                        "attach-old",
                        "quiesce-old",
                        "activate-null",
                        "restore-old"
                ),
                callbacks.events
        );
    }

    private static Supplier<Object> failingReplacement(
            RecordingCallbacks callbacks,
            RuntimeException failure
    ) {
        return () -> {
            callbacks.events.add("activate-failed");
            throw failure;
        };
    }

    private static final class RecordingCallbacks
            implements AndroidViewLifecycle.Callbacks<Object> {
        private final List<String> events = new ArrayList<>();
        private Object oldView;
        private Object currentView;

        @Override
        public void attach(Object view, boolean paused) {
            if (oldView == null) {
                oldView = view;
            }
            if (currentView == null && view != oldView) {
                currentView = view;
            }
            events.add("attach-" + name(view));
            if (paused) {
                events.add("pause-" + name(view));
            }
        }

        @Override
        public void pause(Object view) {
            events.add("pause-" + name(view));
        }

        @Override
        public void resume(Object view) {
            events.add("resume-" + name(view));
        }

        @Override
        public void detach(Object view) {
            events.add("detach-" + name(view));
        }

        @Override
        public void quiesce(Object view) {
            events.add("quiesce-" + name(view));
        }

        @Override
        public void restore(Object view, boolean paused) {
            events.add("restore-" + name(view));
        }

        private String name(Object view) {
            return view == oldView ? "old" : "current";
        }
    }
}
