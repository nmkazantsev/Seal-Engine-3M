package com.nikitos.runtime;

public final class RuntimeResourceSnapshot {
    private final int trackedVramObjects;
    private final int shaders;
    private final int touchProcessors;
    private final int keyboardPressListeners;
    private final int keyboardReleaseListeners;
    private final int keyboardComboListeners;
    private final int desktopMouseCallbackRegistrations;

    public RuntimeResourceSnapshot(
            int trackedVramObjects,
            int shaders,
            int touchProcessors,
            int keyboardPressListeners,
            int keyboardReleaseListeners,
            int keyboardComboListeners,
            int desktopMouseCallbackRegistrations
    ) {
        this.trackedVramObjects = trackedVramObjects;
        this.shaders = shaders;
        this.touchProcessors = touchProcessors;
        this.keyboardPressListeners = keyboardPressListeners;
        this.keyboardReleaseListeners = keyboardReleaseListeners;
        this.keyboardComboListeners = keyboardComboListeners;
        this.desktopMouseCallbackRegistrations = desktopMouseCallbackRegistrations;
    }

    public int getTrackedVramObjects() {
        return trackedVramObjects;
    }

    public int getShaders() {
        return shaders;
    }

    public int getTouchProcessors() {
        return touchProcessors;
    }

    public int getKeyboardPressListeners() {
        return keyboardPressListeners;
    }

    public int getKeyboardReleaseListeners() {
        return keyboardReleaseListeners;
    }

    public int getKeyboardComboListeners() {
        return keyboardComboListeners;
    }

    public int getDesktopMouseCallbackRegistrations() {
        return desktopMouseCallbackRegistrations;
    }
}
