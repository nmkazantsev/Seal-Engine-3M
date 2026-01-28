package com.seal.gl_engine.platform;

import android.opengl.GLES30;
import com.nikitos.platformBridge.GeneralPlatformBridge;

public class GeneralBridgeAndroid extends GeneralPlatformBridge {
    @Override
    public void glDrawArrays(int type, int offest, int count){
        GLES30.glDrawArrays( type,  offest,  count);}

    @Override
    public int glGetUniformLocation(int program, String name){return GLES30. glGetUniformLocation( program,  name);}

    @Override
    public void glUniform3f(int location, float x, float y, float z){GLES30. glUniform3f( location,  x,  y,  z);}

    @Override
    public void glActiveTexture(int texture){GLES30. glActiveTexture( texture);}

    @Override
    public void glBindTexture(int texture, int location){GLES30. glBindTexture( texture,  location);}

    @Override
    public void glUniform1i(int textureLocation, int slot){GLES30. glUniform1i( textureLocation,  slot);}

    @Override
    public void glGenerateMipmap (int type){GLES30. glGenerateMipmap ( type);}


}
