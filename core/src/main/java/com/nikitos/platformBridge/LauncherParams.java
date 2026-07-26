package com.nikitos.platformBridge;

import com.nikitos.GamePageClass;
import com.nikitos.runtime.RuntimeObserver;

import java.util.function.Function;

/**
 * an abstract class to be extended by platform - dependent implementations
 */
public class LauncherParams {
    protected Function<Void, GamePageClass> startPage = null;
    protected boolean debug = false;
    protected boolean MSAA = false;
    protected boolean isDesktop = true;

    protected boolean useBSOD = false;

    protected boolean fullScreen = true;

    protected String windowTitle = "Seal Engine 3-M";

    protected RuntimeObserver runtimeObserver = null;

    protected Integer windowWidth = null;
    protected Integer windowHeight = null;
    protected boolean maximized = true;
    protected boolean vSync = true;

    public LauncherParams setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public LauncherParams setWindowTitle(String windowTitle) {
        this.windowTitle = windowTitle;
        return this;
    }

    public LauncherParams setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
        return this;
    }

    public LauncherParams setMSAA(boolean MSAA) {
        this.MSAA = MSAA;
        return this;
    }

    public LauncherParams setUseBSOD(boolean useBSOD) {
        this.useBSOD = useBSOD;
        return this;
    }

    public boolean getUseBSOD() {
        return useBSOD;
    }

    protected LauncherParams setIsDesktop(boolean isDesktop) {
        this.isDesktop = isDesktop;
        return this;
    }

    public LauncherParams setStartPage(Function<Void, GamePageClass> startPage) {
        this.startPage = startPage;
        return this;
    }

    public LauncherParams setRuntimeObserver(RuntimeObserver runtimeObserver) {
        this.runtimeObserver = runtimeObserver;
        return this;
    }

    public LauncherParams setWindowSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Window dimensions must be positive");
        }
        this.windowWidth = width;
        this.windowHeight = height;
        return this;
    }

    public LauncherParams setMaximized(boolean maximized) {
        this.maximized = maximized;
        return this;
    }

    public LauncherParams setVSync(boolean vSync) {
        this.vSync = vSync;
        return this;
    }

    public Function<Void, GamePageClass> getStartPage() {
        return startPage;
    }

    public RuntimeObserver getRuntimeObserver() {
        return runtimeObserver;
    }

    public boolean hasWindowSize() {
        return windowWidth != null;
    }

    public Integer getWindowWidth() {
        return windowWidth;
    }

    public Integer getWindowHeight() {
        return windowHeight;
    }

    public boolean getMaximized() {
        return maximized;
    }

    public boolean getVSync() {
        return vSync;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean getMSAA() {
        return MSAA;
    }

    public boolean isDesktop() {
        return isDesktop;
    }

    public String getWindowTitle() {
        return windowTitle;
    }

    public boolean getFullScreen() {
        return fullScreen;
    }

}
