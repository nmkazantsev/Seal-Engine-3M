package com.nikitos.platform;

import com.nikitos.platformBridge.ImgBridge;

public class ImgBridgeDesktop extends ImgBridge {
    @Override
    public float getWidth(Object bitmap) {
        return 0;
    }

    @Override
    public float getHeight(Object bitmap) {
        return 0;
    }

    @Override
    public void recycleBitmap(Object bitmap) {

    }
}
