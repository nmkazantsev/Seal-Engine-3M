package com.nikitos;

public abstract class GamePageClass {
    private final Object resourceOwnershipToken = new Object();

    /**
     * Уникальный ключ экземпляра страницы для реестров ресурсов.
     * Он не зависит от класса страницы, поэтому переход на новый экземпляр того же класса очищает старые ресурсы.
     */
    public final Object getResourceOwnershipToken() {
        return resourceOwnershipToken;
    }

    public abstract void onSurfaceChanged(int x, int y);

    public abstract void draw();

    public abstract void onResume();

    public abstract void onPause();

}
