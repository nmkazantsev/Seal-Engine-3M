package com.nikitos.main.textures;

import com.nikitos.GamePageClass;

public class CubeMap extends Texture {
    public CubeMap(GamePageClass creator) {
        super(creator);
    }

    @Override
    protected int createTexture() {
        final int[] textureIds = new int[1];
        //создаем пустой массив из одного элемента
        //в этот массив OpenGL ES запишет свободный номер текстуры,
        // получаем свободное имя текстуры, которое будет записано в names[0]
        gl.glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }
        // настройка объекта текстуры
        gl.glActiveTexture(glc.GL_TEXTURE0());
        gl.glBindTexture(glc.GL_TEXTURE_CUBE_MAP(), textureIds[0]);

        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_S(), glc.GL_CLAMP_TO_EDGE());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_T(), glc.GL_CLAMP_TO_EDGE());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_R(), glc.GL_CLAMP_TO_EDGE());
        // сброс target
        gl.glBindTexture(glc.GL_TEXTURE_2D(), 0);

        return textureIds[0];
    }
}
