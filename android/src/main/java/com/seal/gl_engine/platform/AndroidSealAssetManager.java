package com.seal.gl_engine.platform;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;

import com.nikitos.platformBridge.SealAssetManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AndroidSealAssetManager implements SealAssetManager {

    private final AssetStreamResolver streams;

    public AndroidSealAssetManager(Context context) {
        Context applicationContext =
                Objects.requireNonNull(context, "context").getApplicationContext();
        if (applicationContext == null) {
            if (context instanceof Application) {
                applicationContext = context;
            } else {
                throw new IllegalArgumentException(
                        "Android Context must expose an application context"
                );
            }
        }

        AssetManager assets = applicationContext.getAssets();
        ClassLoader classLoader = Objects.requireNonNull(
                AndroidSealAssetManager.class.getClassLoader(),
                "classLoader"
        );
        streams = new AssetStreamResolver(
                assets::open,
                path -> {
                    InputStream stream = classLoader.getResourceAsStream(path);
                    if (stream == null) {
                        throw new FileNotFoundException(
                                "Classpath resource not found: " + path
                        );
                    }
                    return stream;
                }
        );
    }

    AndroidSealAssetManager(AssetStreamResolver streams) {
        this.streams = Objects.requireNonNull(streams, "streams");
    }

    @Override
    public InputStream load(String path) {
        return streams.open(path);
    }

    @Override
    public String loadText(String path) {
        try (InputStream stream = load(path)) {
            return new String(readAllBytes(stream), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read asset text: " + path, e);
        }
    }

    @Override
    public byte[] loadBytes(String path) {
        try (InputStream stream = load(path)) {
            return readAllBytes(stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read asset bytes: " + path, e);
        }
    }

    private static byte[] readAllBytes(InputStream stream) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[4096];
            int count;
            while ((count = stream.read(chunk)) != -1) {
                bytes.write(chunk, 0, count);
            }
            return bytes.toByteArray();
        }
    }
}
