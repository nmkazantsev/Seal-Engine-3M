package com.nikitos.main.textures;

import com.nikitos.GamePageClass;

public class NormalMap extends Texture {
    public NormalMap(GamePageClass creator) {
        super(creator);
    }

    @Override
    protected int createTexture() {
        final int[] textureIds = new int[1];
        /*create an empty array of one element
        OpenGL ES will write a free texture number to this array,
        get a free texture name, which will be written to names[0]*/
        gl.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }
        // настройка объекта текстуры
        gl.glActiveTexture(glc.GL_TEXTURE0());
        gl.glBindTexture(glc.GL_TEXTURE_2D(), textureIds[0]);

        gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR());
        gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());

        gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_WRAP_S(), glc.GL_CLAMP_TO_EDGE());
        gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_WRAP_T(), glc.GL_CLAMP_TO_EDGE());

        // сброс target
        gl.glBindTexture(glc.GL_TEXTURE_2D(), 0);

        return textureIds[0];
    }

}
