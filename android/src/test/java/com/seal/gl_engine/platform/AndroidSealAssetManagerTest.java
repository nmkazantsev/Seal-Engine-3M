package com.seal.gl_engine.platform;

import android.content.Context;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AndroidSealAssetManagerTest {

    @Test
    public void packagedAssetWinsWithoutOpeningClasspathFallback() {
        AtomicInteger fallbackCalls = new AtomicInteger();
        InputStream packaged = new ByteArrayInputStream(new byte[]{1});
        AssetStreamResolver resolver = new AssetStreamResolver(
                path -> packaged,
                path -> {
                    fallbackCalls.incrementAndGet();
                    return new ByteArrayInputStream(new byte[]{2});
                }
        );

        assertSame(packaged, resolver.open("textures/tank.png"));
        assertEquals(0, fallbackCalls.get());
    }

    @Test
    public void missingPackagedAssetFallsBackToClasspath() {
        InputStream fallback = new ByteArrayInputStream(new byte[]{2});
        AssetStreamResolver resolver = new AssetStreamResolver(
                path -> {
                    throw new FileNotFoundException("packaged " + path);
                },
                path -> fallback
        );

        assertSame(fallback, resolver.open("engine/shader.glsl"));
    }

    @Test
    public void bothMissingReportPathAndRetainLookupEvidence() {
        FileNotFoundException packagedMissing =
                new FileNotFoundException("packaged missing");
        FileNotFoundException classpathMissing =
                new FileNotFoundException("classpath missing");
        AssetStreamResolver resolver = new AssetStreamResolver(
                path -> {
                    throw packagedMissing;
                },
                path -> {
                    throw classpathMissing;
                }
        );

        try {
            resolver.open("missing/resource.bin");
            fail("Expected missing asset failure");
        } catch (RuntimeException failure) {
            assertTrue(failure.getMessage().contains("missing/resource.bin"));
            assertSame(packagedMissing, failure.getCause());
            assertArrayEquals(
                    new Throwable[]{classpathMissing},
                    failure.getSuppressed()
            );
        }
    }

    @Test
    public void packagedOpenFailureDoesNotFallBack() {
        IOException packagedFailure = new IOException("asset manager closed");
        AtomicInteger fallbackCalls = new AtomicInteger();
        AssetStreamResolver resolver = new AssetStreamResolver(
                path -> {
                    throw packagedFailure;
                },
                path -> {
                    fallbackCalls.incrementAndGet();
                    return new ByteArrayInputStream(new byte[0]);
                }
        );

        try {
            resolver.open("existing/resource.bin");
            fail("Expected packaged asset failure");
        } catch (RuntimeException failure) {
            assertTrue(failure.getMessage().contains("existing/resource.bin"));
            assertSame(packagedFailure, failure.getCause());
            assertEquals(0, fallbackCalls.get());
        }
    }

    @Test
    public void loadReturnsCallerOwnedLiveStream() throws IOException {
        TrackingInputStream stream = new TrackingInputStream(new byte[]{7});
        AndroidSealAssetManager manager = managerReturning(stream);

        assertSame(stream, manager.load("live.bin"));
        assertFalse(stream.closed);
        assertEquals(7, stream.read());
        assertFalse(stream.closed);

        stream.close();
        assertTrue(stream.closed);
    }

    @Test
    public void loadTextUsesUtf8AndClosesStreamOnEverySupportedApiPath() {
        String text = "Привет, Android";
        TrackingInputStream stream = new TrackingInputStream(
                text.getBytes(StandardCharsets.UTF_8)
        );
        AndroidSealAssetManager manager = managerReturning(stream);

        assertEquals(text, manager.loadText("text.txt"));
        assertTrue(stream.closed);
    }

    @Test
    public void loadBytesClosesStream() {
        byte[] bytes = new byte[]{0, 1, 2, -1};
        TrackingInputStream stream = new TrackingInputStream(bytes);
        AndroidSealAssetManager manager = managerReturning(stream);

        assertArrayEquals(bytes, manager.loadBytes("bytes.bin"));
        assertTrue(stream.closed);
    }

    @Test
    public void packagedReadFailureIsPrimaryAndDoesNotFallBack() {
        IOException readFailure = new IOException("corrupt packaged asset");
        AtomicInteger fallbackCalls = new AtomicInteger();
        AndroidSealAssetManager manager = new AndroidSealAssetManager(
                new AssetStreamResolver(
                        path -> new InputStream() {
                            @Override
                            public int read() throws IOException {
                                throw readFailure;
                            }
                        },
                        path -> {
                            fallbackCalls.incrementAndGet();
                            return new ByteArrayInputStream(new byte[]{9});
                        }
                )
        );

        try {
            manager.loadBytes("corrupt.bin");
            fail("Expected packaged read failure");
        } catch (RuntimeException failure) {
            assertTrue(failure.getMessage().contains("corrupt.bin"));
            assertSame(readFailure, failure.getCause());
            assertEquals(0, fallbackCalls.get());
        }
    }

    @Test
    public void managerDoesNotRetainAnyContext() {
        for (Field field : AndroidSealAssetManager.class.getDeclaredFields()) {
            assertFalse(
                    "Context field retained: " + field.getName(),
                    Context.class.isAssignableFrom(field.getType())
            );
        }
    }

    private static AndroidSealAssetManager managerReturning(InputStream stream) {
        return new AndroidSealAssetManager(
                new AssetStreamResolver(path -> stream, path -> {
                    throw new AssertionError("fallback must not be used");
                })
        );
    }

    private static final class TrackingInputStream extends ByteArrayInputStream {
        private boolean closed;

        private TrackingInputStream(byte[] bytes) {
            super(bytes);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }
    }
}
