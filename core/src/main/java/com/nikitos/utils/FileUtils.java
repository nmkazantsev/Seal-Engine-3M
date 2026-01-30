package com.nikitos.utils;

import com.nikitos.CoreRenderer;
import com.nikitos.main.images.PImage;
import com.nikitos.platformBridge.ImgBridge;
import com.nikitos.platformBridge.PlatformBridge;

import java.awt.*;
import java.io.*;
import java.util.stream.Collectors;

public class FileUtils {
    private final PlatformBridge platformBridge;

    /*public static String readTextFromRaw(Context context, int resourceId) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream =
                        context.getResources().openRawResource(resourceId);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\r\n");
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (Resources.NotFoundException nfex) {
            nfex.printStackTrace();
        }
        return stringBuilder.toString();
    }*/
    public FileUtils() {
        platformBridge = CoreRenderer.engine.getPlatformBridge();
    }


    public String readFileFromAssets(Class<?> cls, String fileName) {
        String result;
        try (InputStream is = cls.getResourceAsStream(fileName)) {
            assert is != null;
            result = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            return result;
        } catch (Exception e) {
            platformBridge.log_e("file utils", "ERROR opening " + fileName);
            throw new RuntimeException(e);
        }
    }

    public String readFile(InputStream is) {
        String result;
        assert is != null;
        result = new BufferedReader(new InputStreamReader(is))
                .lines().collect(Collectors.joining("\n"));
        return result;

    }

    public static PImage loadImage(InputStream inputStream) {
        ImgBridge imgBridge = CoreRenderer.engine.getPlatformBridge().getImgBridge();
        return imgBridge.loadImage(inputStream);
    }

    public static PImage loadImage(String fileName, Class<?> cls) {
        InputStream inputStream = cls.getResourceAsStream(fileName);
        return loadImage(inputStream);
    }

  /*  public static Bitmap getBitmapFromAssets(String fileName, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();

        InputStream istr = assetManager.open(fileName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }
*/
}