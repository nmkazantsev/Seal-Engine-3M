package main.images;

import com.nikitos.CoreRenderer;
import com.nikitos.main.images.AbstractFont;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.Typeface;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;

public class FontDesktop extends AbstractFont {
    private Typeface typeface;
    private Font font;
    private float defaultSize = 16f; // размер по умолчанию, будет переопределён при textSize()

    @Override
    public void loadFromAsset(String assetPath) {
        // Используем FontMgr — стандартный способ загрузки шрифтов в Skija
        FontMgr fontMgr = FontMgr.getDefault();
        this.typeface = fontMgr.makeFromFile(assetPath);

        if (this.typeface == null) {
            throw new RuntimeException("Failed to load font from file: " + assetPath);
        }
        this.font = new Font(this.typeface, this.defaultSize);
        this.loaded = true;
    }

    @Override
    public void close() {
        if (font != null) font.close();
        if (typeface != null) typeface.close();
        loaded = false;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public Object getPlatformFont() {
        return font; // возвращаем Skija Font
    }

    // Вспомогательный метод для изменения размера (будет вызван из PImageDesktop)
    public void setSize(float size) {
        if (font != null) {
            font.setSize(size);
        }
    }
}