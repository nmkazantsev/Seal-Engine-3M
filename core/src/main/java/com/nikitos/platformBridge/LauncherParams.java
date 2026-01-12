package com.nikitos.platformBridge;

import com.nikitos.GamePageClass;

import java.util.function.Function;

/**
 * an abstract class to be extended by platform - dependent implementations
 */
public class LauncherParams {
    protected Function<Void, GamePageClass> startPage = null;
    protected boolean landscape = false;
    protected boolean debug = false;
    protected boolean MSAA = false;
    protected boolean isDesktop = true;

    public LauncherParams setLandscape(boolean landscape) {
        this.landscape = landscape;
        return this;
    }

    public LauncherParams setDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public LauncherParams setMSAA(boolean MSAA) {
        this.MSAA = MSAA;
        return this;
    }

    protected LauncherParams setIsDesktop(boolean isDesktop) {
        this.isDesktop = isDesktop;
        return this;
    }

    public LauncherParams setStartPage(Function<Void, GamePageClass> startPage) {
        this.startPage = startPage;
        return this;
    }

    public Function<Void, GamePageClass> getStartPage() {
        return startPage;
    }

    public boolean isLandscape() {
        return landscape;
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

}
