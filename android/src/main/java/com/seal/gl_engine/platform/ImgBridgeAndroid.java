package com.seal.gl_engine.platform;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.ImgBridge;
import com.seal.gl_engine.engine.main.images.PImageAndroid;

import java.io.BufferedInputStream;
import java.io.InputStream;

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

    @Override
    public PImage loadImage(InputStream stream) {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);
        return new PImageAndroid(bmp);
    }

}
