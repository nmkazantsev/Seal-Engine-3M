package com.nikitos.platform;

import com.nikitos.platformBridge.GeneralPlatformBridge;
import org.lwjgl.opengl.GL30;
public class GeneralPlatformBridgeDesktop extends GeneralPlatformBridge {
    @Override
    public void glDrawArrays(int type, int offest, int count){GL30. glDrawArrays( type,  offest,  count);}

    @Override
    public int glGetUniformLocation(int program, String name){return GL30. glGetUniformLocation( program,  name);}

    @Override
    public void glUniform3f(int location, float x, float y, float z){GL30. glUniform3f( location,  x,  y,  z);}

    @Override
    public void glActiveTexture(int texture){GL30. glActiveTexture( texture);}

    @Override
    public void glBindTexture(int texture, int location){GL30. glBindTexture( texture,  location);}

    @Override
    public void glUniform1i(int textureLocation, int slot){GL30. glUniform1i( textureLocation,  slot);}

    @Override
    public void glGenerateMipmap (int type){GL30. glGenerateMipmap ( type);}


}
