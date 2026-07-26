package com.seal.gl_engine.platform;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AndroidViewBindingTest {

    @Test
    public void replacementQuiescesOldViewAndAllOperationsAreIdentityAware() {
        List<String> events = new ArrayList<>();
        Object oldView = new Object();
        Object currentView = new Object();
        Object pausedReplacement = new Object();
        AndroidViewBinding<Object> binding = new AndroidViewBinding<>(
                new AndroidViewBinding.Operations<>() {
                    @Override
                    public void pause(Object view) {
                        events.add("pause-" + name(
                                view,
                                oldView,
                                currentView
                        ));
                    }

                    @Override
                    public void resume(Object view) {
                        events.add("resume-" + name(
                                view,
                                oldView,
                                currentView
                        ));
                    }
                }
        );

        binding.attach(oldView, false);
        binding.attach(currentView, false);
        binding.pause(oldView);
        binding.pause(currentView);
        binding.pauseCurrent();
        binding.resumeCurrent();
        binding.resumeCurrent();
        binding.attach(pausedReplacement, true);
        binding.detach(currentView);
        binding.detach(pausedReplacement);

        assertEquals(
                Arrays.asList(
                        "pause-old",
                        "pause-current",
                        "resume-current",
                        "pause-current",
                        "pause-replacement"
                ),
                events
        );
    }

    @Test
    public void rollbackQuiescesReplacementAndRestoresOldRunningView() {
        List<String> events = new ArrayList<>();
        Object oldView = new Object();
        Object replacement = new Object();
        AndroidViewBinding<Object> binding = new AndroidViewBinding<>(
                new AndroidViewBinding.Operations<>() {
                    @Override
                    public void pause(Object view) {
                        events.add(view == oldView
                                ? "pause-old"
                                : "pause-replacement");
                    }

                    @Override
                    public void resume(Object view) {
                        events.add(view == oldView
                                ? "resume-old"
                                : "resume-replacement");
                    }
                }
        );
        binding.attach(oldView, false);
        binding.pause(oldView);
        binding.attach(replacement, false);

        binding.restore(oldView, false);

        assertEquals(
                Arrays.asList(
                        "pause-old",
                        "pause-replacement",
                        "resume-old"
                ),
                events
        );
    }

    private static String name(
            Object view,
            Object oldView,
            Object currentView
    ) {
        if (view == oldView) {
            return "old";
        }
        if (view == currentView) {
            return "current";
        }
        return "replacement";
    }
}
