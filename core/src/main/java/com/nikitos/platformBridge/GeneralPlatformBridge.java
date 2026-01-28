package com.nikitos.platformBridge;

public abstract class GeneralPlatformBridge {
    public abstract void glDrawArrays(int type, int offest, int count);
    public abstract int glGetUniformLocation(int program, String name);
    public abstract void glUniform3f(int location, float x, float y, float z);
    public abstract void glActiveTexture(int texture);
    public abstract void glBindTexture(int texture, int location);
    public abstract void glUniform1i(int textureLocation, int slot);
    public abstract void glGenerateMipmap (int type);
}
