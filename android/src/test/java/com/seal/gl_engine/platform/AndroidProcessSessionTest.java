package com.seal.gl_engine.platform;

import android.content.Context;
import android.content.ContextWrapper;

import com.nikitos.Engine;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.runtime.FrameCaptureSource;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AndroidProcessSessionTest {

    @Test
    public void settingsSnapshotRetainsApplicationContextButNotActivityOrParams() {
        Context application = new ContextWrapper(null);
        Context activity = new ApplicationContextStub(application);
        AndroidLauncherParams original = new AndroidLauncherParams(activity)
                .setDebug(true)
                .setMSAA(true)
                .setUseBSOD(true);

        AndroidLaunchSettings snapshot =
                AndroidLaunchSettings.snapshot(original);

        assertSame(application, snapshot.getContext());
        assertNotSame(activity, snapshot.getContext());
        assertNotSame(original, snapshot);
        assertSame(original.getStartPage(), snapshot.getStartPage());
    }

    @Test
    public void registryReusesExactEngineBridgeAndCaptureSource() {
        AndroidBridge bridge = new SilentAndroidBridge();
        Engine engine = new Engine(bridge, new LauncherParams());
        AndroidProcessSession expected =
                new AndroidProcessSession(null, bridge, engine);
        AndroidSessionRegistry registry = new AndroidSessionRegistry();

        AndroidProcessSession first =
                registry.acquire(null, ignored -> expected);
        AndroidProcessSession second = registry.acquire(
                null,
                ignored -> {
                    fail("A second process session must not be created");
                    return null;
                }
        );
        FrameCaptureSource firstCapture =
                first.getBridge().getFrameCaptureSource();

        assertSame(first, second);
        assertSame(engine, second.getEngine());
        assertSame(bridge, second.getBridge());
        assertSame(
                firstCapture,
                second.getBridge().getFrameCaptureSource()
        );
    }

    @Test
    public void noArgBridgeBindsOnlyApplicationContextOnce() {
        Context firstApplication = new ContextWrapper(null);
        Context secondApplication = new ContextWrapper(null);
        AndroidBridge bridge = new AndroidBridge();

        bridge.bindApplicationContext(
                new ApplicationContextStub(firstApplication)
        );
        bridge.bindApplicationContext(
                new ApplicationContextStub(secondApplication)
        );

        assertSame(firstApplication, bridge.getContext());
    }

    @Test
    public void androidBridgeRetainsProtectedStartPageCompatibility()
            throws Exception {
        Field field = AndroidBridge.class.getDeclaredField("startPage");

        assertTrue(Modifier.isProtected(field.getModifiers()));
        assertSame(Function.class, field.getType());
        assertTrue(field.isAnnotationPresent(Deprecated.class));
    }

    @Test
    public void bridgeStartPageMatchesTheProcessSettingsSnapshot() {
        Context application = new ContextWrapper(null);
        AndroidLauncherParams params = new AndroidLauncherParams(
                new ApplicationContextStub(application)
        );
        Function<Void, com.nikitos.GamePageClass> startPage =
                ignored -> null;
        params.setStartPage(startPage);
        AndroidLaunchSettings settings =
                AndroidLaunchSettings.snapshot(params);
        AndroidBridge bridge = new AndroidBridge(application);

        bridge.bindLaunchSettings(settings);

        assertSame(startPage, bridge.startPage);
    }

    private static final class ApplicationContextStub extends ContextWrapper {
        private final Context application;

        private ApplicationContextStub(Context application) {
            super(null);
            this.application = application;
        }

        @Override
        public Context getApplicationContext() {
            return application;
        }
    }

    private static final class SilentAndroidBridge extends AndroidBridge {
        private SilentAndroidBridge() {
            super(null);
        }

        @Override
        public void log_i(String tag, String message) {
        }
    }
}
