package com.seal.gl_engine.engine.main.images;

import android.graphics.Typeface;
import android.os.Build;
import com.nikitos.CoreRenderer;
import com.nikitos.main.images.AbstractFont;


import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.io.File;

public class FontAndroid extends AbstractFont {
    private Typeface typeface;

    @Override
    public void loadFromAsset(String assetPath) {
        // Используем SealAssetManager через движок
        byte[] fontData = CoreRenderer.engine.getPlatformBridge().getAssetManager().loadBytes(assetPath);
        if (fontData == null) {
            throw new RuntimeException("Failed to load font data from: " + assetPath);
        }
        typeface = createTypefaceFromBytes(fontData);
        loaded = (typeface != null);
        if (!loaded) {
            throw new RuntimeException("Failed to create Typeface from: " + assetPath);
        }
    }


    // --- ВСПОМОГАТЕЛЬНЫЙ МЕТОД ---
    // Создает Typeface из массива байт, используя временный файл.
    // Это универсальное решение, работающее на всех версиях Android.
    private Typeface createTypefaceFromBytes(byte[] data) {
        File tempFontFile = null;
        try {
            // Создаем временный файл с расширением .ttf
            tempFontFile = File.createTempFile("temp_font_", ".ttf");
            tempFontFile.deleteOnExit(); // Помечаем на удаление при завершении JVM

            // Записываем байты шрифта во временный файл
            try (FileOutputStream fos = new FileOutputStream(tempFontFile)) {
                fos.write(data);
            }

            // Создаем Typeface из временного файла
            // Используем createFromFile, который гарантированно существует во всех версиях API
            return Typeface.createFromFile(tempFontFile);

        } catch (IOException e) {
            throw new RuntimeException("Failed to create temporary font file", e);
        } finally {
            // Безопасно удаляем временный файл после загрузки шрифта в память
            if (tempFontFile != null && tempFontFile.exists()) {
                if (!tempFontFile.delete()) {
                    // Можно добавить логирование ошибки удаления, если нужно
                    // Например: Log.w("FontAndroid", "Failed to delete temp font file");
                }
            }
        }
    }

    @Override
    public void close() {
        // Typeface в Android не требует явного закрытия
        typeface = null;
        loaded = false;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public Object getPlatformFont() {
        return typeface; // возвращаем Android Typeface
    }
}