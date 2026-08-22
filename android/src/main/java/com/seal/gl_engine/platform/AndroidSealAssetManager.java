package com.seal.gl_engine.platform;

import android.content.Context;
import android.content.res.AssetManager;

import com.nikitos.platformBridge.SealAssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AndroidSealAssetManager implements SealAssetManager {

    private final AssetManager assets;

    public AndroidSealAssetManager(Context context) {
        this.assets = context.getAssets();
    }

    @Override
    public InputStream load(String path) {
        InputStream is = Objects.requireNonNull(getClass()
                        .getClassLoader())
                .getResourceAsStream(path);

        if (is == null) {
            throw new RuntimeException("Asset not found: " + path);
        }

        return is;
    }

    @Override
    public String loadText(String path) {
        try (InputStream is = load(path)) {
            return readUtf8(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String readUtf8(InputStream input) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return new String(result.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public byte[] loadBytes(String path) {
        try (InputStream is = load(path);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[4096];
            int nRead;

            while ((nRead = is.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
