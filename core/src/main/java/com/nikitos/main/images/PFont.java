package com.nikitos.main.images;

import com.nikitos.CoreRenderer;

public class PFont {
    private final AbstractFont impl;

    // Создаёт пустой шрифт (нужно будет вызвать load)
    public PFont() {
        this.impl = CoreRenderer.engine.getPlatformBridge().getFontBridge().createEmptyFont();
    }

    // Если нужен конструктор с готовой реализацией (для внутреннего использования)
    public PFont(AbstractFont impl) {
        this.impl = impl;
    }

    // Загрузка из assets (Android) или из файловой системы (Desktop)
    public void loadAsset(String assetPath) {
        impl.loadFromAsset(assetPath);
    }

    public void close() {
        impl.close();
    }

    public boolean isLoaded() {
        return impl.isLoaded();
    }

    public Object getPlatformFont() {
        return impl.getPlatformFont();
    }

    // Для удобства: статический метод быстрой загрузки из assets
    public static PFont fromAsset(String assetPath) {
        PFont font = new PFont();
        font.loadAsset(assetPath);
        return font;
    }

}