package com.nikitos;

import com.nikitos.main.debugger.Debugger;
import com.nikitos.platform.DesktopBridge;
import com.nikitos.platformBridge.LauncherParams;
import com.sun.management.ThreadMXBean;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Repeatable allocation check for the literal no-observer frame path.
 */
public final class NoObserverAllocationBenchmark {
    private static final int DEFAULT_WARMUP_FRAMES = 50_000;
    private static final int DEFAULT_MEASURED_FRAMES = 250_000;
    private static final int DEFAULT_SAMPLES = 5;

    private NoObserverAllocationBenchmark() {
    }

    public static void main(String[] args) throws Exception {
        int warmupFrames = intArgument(args, 0, DEFAULT_WARMUP_FRAMES);
        int measuredFrames = intArgument(args, 1, DEFAULT_MEASURED_FRAMES);
        int samples = intArgument(args, 2, DEFAULT_SAMPLES);
        ThreadMXBean allocationBean = allocationBean();
        long threadId = Thread.currentThread().getId();
        CoreRenderer renderer = rendererWithoutInitialization();
        Debugger.setEnabled(false);

        runFrames(renderer, warmupFrames);
        long[] allocatedBytes = new long[samples];
        for (int sample = 0; sample < samples; sample++) {
            long before = allocationBean.getThreadAllocatedBytes(threadId);
            runFrames(renderer, measuredFrames);
            allocatedBytes[sample] =
                    allocationBean.getThreadAllocatedBytes(threadId) - before;
        }

        Arrays.sort(allocatedBytes);
        long medianBytes = allocatedBytes[allocatedBytes.length / 2];
        double medianBytesPerFrame = medianBytes / (double) measuredFrames;
        System.out.printf(
                "NO_OBSERVER_ALLOCATION warmup=%d frames=%d samples=%d "
                        + "medianBytes=%d medianBytesPerFrame=%.6f raw=%s%n",
                warmupFrames,
                measuredFrames,
                samples,
                medianBytes,
                medianBytesPerFrame,
                Arrays.toString(allocatedBytes)
        );
    }

    private static CoreRenderer rendererWithoutInitialization() throws Exception {
        Engine engine = new Engine(new DesktopBridge(), new LauncherParams());
        CoreRenderer.engine = engine;
        engine.startNewPage(new NoOpPage());
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return (CoreRenderer) ((Unsafe) unsafeField.get(null))
                .allocateInstance(CoreRenderer.class);
    }

    private static ThreadMXBean allocationBean() {
        ThreadMXBean bean =
                (ThreadMXBean) ManagementFactory.getThreadMXBean();
        if (!bean.isThreadAllocatedMemorySupported()) {
            throw new IllegalStateException(
                    "Thread allocation measurement is not supported by this JVM"
            );
        }
        if (!bean.isThreadAllocatedMemoryEnabled()) {
            bean.setThreadAllocatedMemoryEnabled(true);
        }
        return bean;
    }

    private static int intArgument(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    private static void runFrames(CoreRenderer renderer, int frames) {
        for (int frame = 0; frame < frames; frame++) {
            renderer.draw();
        }
    }

    private static final class NoOpPage extends GamePageClass {
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
}
