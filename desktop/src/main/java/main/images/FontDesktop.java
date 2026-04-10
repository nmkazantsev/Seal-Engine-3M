package main.images;

import com.nikitos.CoreRenderer;
import com.nikitos.main.images.AbstractFont;
import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.Typeface;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.URL;

public class FontDesktop extends AbstractFont {
    private Typeface typeface;
    private Font font;
    private float defaultSize = 16f; // размер по умолчанию, будет переопределён при textSize()

    @Override
    public void loadFromAsset(String assetPath) {
        String resourcePath = assetPath.startsWith("/") ? assetPath.substring(1) : assetPath;
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (resourceUrl == null) {
            throw new RuntimeException("Resource not found: " + resourcePath);
        }

        String filePath;
        try (InputStream is = resourceUrl.openStream()) {
            File tempFile = File.createTempFile("font_", ".ttf");
            tempFile.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
            }
            filePath = tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract font from JAR: " + assetPath, e);
        }

        FontMgr fontMgr = FontMgr.getDefault();
        this.typeface = fontMgr.makeFromFile(filePath);
        if (this.typeface == null) {
            throw new RuntimeException("Failed to load font from file: " + filePath);
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