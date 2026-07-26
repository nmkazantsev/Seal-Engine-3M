package com.seal.gl_engine.platform;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
interface AssetStreamSource {
    InputStream open(String path) throws IOException;
}
