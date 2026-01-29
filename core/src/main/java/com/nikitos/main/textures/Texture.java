package com.nikitos.main.textures;

import com.nikitos.GamePageClass;
import com.nikitos.main.VRAMobject;


public class Texture extends VRAMobject {
    private int id;
    private boolean mipMap = false;

    public Texture(GamePageClass creator) {
        super(creator);
        id = createTexture();
    }

    public Texture(GamePageClass creator, boolean mipMap) {
        super(creator);
        id = createTexture();
        this.mipMap = mipMap;
    }

    @Override
    public void delete() {
        gl.glDeleteTextures(1, new int[]{id}, 0);//delete texture with id texture, offset zero, array length 1
    }

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
        if (!mipMap) {
            gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR());
            gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());
        } else {
            gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR_MIPMAP_LINEAR());
            gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());
        }
        gl.glTexParameteri(glc.GL_TEXTURE_2D(), glc.GL_TEXTURE_WRAP_S(), glc.GL_MIRRORED_REPEAT());
        gl.glTexParameteri(glc.GL_TEXTURE_2D(),glc. GL_TEXTURE_WRAP_T(), glc.GL_MIRRORED_REPEAT());

        // сброс target
        gl.glBindTexture(glc.GL_TEXTURE_2D(), 0);

        return textureIds[0];
    }

    public void reload() {
        id = createTexture();//create it once again. Do not delete it, you may delete a previously created in this loop texture
    }

    public int getId() {
        return id;
    }

    public boolean hasMinMaps() {
        return mipMap;
    }
}
