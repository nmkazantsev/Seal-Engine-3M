package com.seal.gl_engine.utils;


import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.nikitos.CoreRenderer;
import com.nikitos.platformBridge.ImgBridge;
import com.seal.gl_engine.engine.main.images.PImageAndroid;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class Utils {
    public static Context context;

    public static PImageAndroid loadImage(String name) {
        try {
            ImgBridge imgBridge = CoreRenderer.engine.getPlatformBridge().getImgBridge();
            PImageAndroid img = new PImageAndroid(getBitmapFromAssets(name, context));
            //this will write width and height to corresponding fileds
            img.getWidth();
            img.getHeight();
            img.setLoaded(true);
            return img;
        } catch (Exception e) {
            Log.e("ERROR LOADING", name + e.getMessage());
        }
        return null;
    }

    public static void loadImageAsync(String name, Function<PImageAndroid, ?> callback) {
        new Thread(() -> {
            PImageAndroid image = loadImage(name);
            callback.apply(image);
        }).start();
    }

    private static Bitmap getBitmapFromAssets(String fileName, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();

        InputStream istr = assetManager.open(fileName);

        return BitmapFactory.decodeStream(istr);
    }


    public static int[] append(int[] a, int b) {
        int[] r = new int[a.length + 1];
        System.arraycopy(a, 0, r, 0, a.length);
        r[a.length] = b;
        return r;
    }

    public static float[] append(float[] a, float b) {
        float[] r = new float[a.length + 1];
        System.arraycopy(a, 0, r, 0, a.length);
        r[a.length] = b;
        return r;
    }


}
