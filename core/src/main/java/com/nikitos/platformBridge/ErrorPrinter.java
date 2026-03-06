package com.nikitos.platformBridge;

public abstract class ErrorPrinter {
    public abstract void printOpenGLState() ;
    public abstract void checkGLErrors(String context) ;
}
