package com.nikitos.platformBridge;

import java.io.InputStream;

public interface SealAssetManager {
    InputStream load(String path);
    String loadText(String path);
    byte[] loadBytes(String path);
}
