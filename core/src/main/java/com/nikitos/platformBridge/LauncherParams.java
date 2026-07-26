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

    public Function<Void, GamePageClass> getStartPage() {
        return startPage;
    }

    public RuntimeObserver getRuntimeObserver() {
        return runtimeObserver;
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
