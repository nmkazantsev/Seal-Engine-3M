package com.seal.gl_engine.platform;

import android.graphics.Bitmap;
import com.nikitos.platformBridge.ImgBridge;

public class ImgBridgeAndroid extends ImgBridge {
    @Override
    public float getWidth(Object bitmap) {
        return ((Bitmap) bitmap).getWidth();
    }

    @Override
    public float getHeight(Object bitmap) {
        return ((Bitmap) bitmap).getHeight();
    }

    @Override
    public void recycleBitmap(Object bitmap) {
        ((Bitmap) bitmap).recycle();
    }

}
