package com.seal.gl_engine.platform;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class AndroidViewLifecycleTest {

    @Test
    public void onlyCurrentViewCanPauseResumeOrDetachAndCallbacksAreIdempotent() {
        RecordingCallbacks callbacks = new RecordingCallbacks();
        AndroidViewLifecycle<Object> lifecycle =
                new AndroidViewLifecycle<>(callbacks);
        Object oldView = new Object();
        Object currentView = new Object();

        lifecycle.attach(oldView);
        lifecycle.attach(currentView);
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

        lifecycle.attach(oldView);
        lifecycle.pause(oldView);
        lifecycle.attach(currentView);
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

        private String name(Object view) {
            return view == oldView ? "old" : "current";
        }
    }
}
