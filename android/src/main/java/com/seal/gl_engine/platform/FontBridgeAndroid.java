package com.seal.gl_engine.platform;

import com.nikitos.main.images.AbstractFont;
import com.nikitos.platformBridge.FontBridge;
import com.seal.gl_engine.engine.main.images.FontAndroid;

public class FontBridgeAndroid extends FontBridge {
    @Override
    public AbstractFont createEmptyFont() {
        return new FontAndroid();
    }
}
