package com.nikitos.platform;

import com.nikitos.platformBridge.RuntimeFileBridge;

import java.io.File;

public class DesktopRuntimeFileBridge extends RuntimeFileBridge {
    @Override
    protected File getRelativeRoot() {
        return new File(System.getProperty("user.dir", "."));
    }
}
