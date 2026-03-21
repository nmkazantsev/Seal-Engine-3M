package com.seal.gl_engine.platform;

import android.content.Context;
import android.content.res.AssetManager;
import com.nikitos.platformBridge.SealAssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AndroidSealAssetManager implements SealAssetManager {

    private final AssetManager assets;

    public AndroidSealAssetManager(Context context) {
        this.assets = context.getAssets();
    }

    @Override
    public InputStream load(String path) {
        try {
            return assets.open(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String loadText(String path) {
        try (InputStream is = load(path);
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int length;

            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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