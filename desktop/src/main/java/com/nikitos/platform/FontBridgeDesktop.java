package com.nikitos.platform;

import com.nikitos.main.images.AbstractFont;
import com.nikitos.platformBridge.FontBridge;
import main.images.FontDesktop;

public class FontBridgeDesktop  extends FontBridge {

    @Override
    public AbstractFont createEmptyFont() {
        return new FontDesktop();
    }
}
