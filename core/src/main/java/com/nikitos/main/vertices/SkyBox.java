package com.nikitos.main.vertices;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import com.nikitos.main.textures.CubeMap;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import static com.nikitos.utils.FileUtils.loadImage;

public class SkyBox implements VerticesSet {
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;
    private final CubeMap texture;
    private final String textureFileName, res;
    private Face[] faces;
    private final PImage[] images = new PImage[6];

    private boolean postToGlNeeded = true;
    private boolean redrawNeeded = true;

    private final Function<Void, PImage> redrawFunction;

    private final Class<?> context;
    private final String[] names = new String[]{"right", "left", "bottom", "top", "front", "back"};

    public SkyBox(String textureFileName, String res, GamePageClass page) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();

        this.res = res;
        this.context = page.getClass();
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VerticesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        texture = new CubeMap(page);
        try {
            createFaces();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        onRedrawSetup();
        redrawNow();
    }

    //loads a cube.obj file to faces
    private void createFaces() throws IOException {

        ByteArrayInputStream inputStream;
        Obj object;

        //library projects con not have assets
        String cube_file = "# Blender v3.2.2 OBJ File: ''\n" +
                "# www.blender.org\n" +
                "mtllib cube.mtl\n" +
                "o Cube\n" +
                "v 1.000000 1.000000 -1.000000\n" +
                "v 1.000000 -1.000000 -1.000000\n" +
                "v 1.000000 1.000000 1.000000\n" +
                "v 1.000000 -1.000000 1.000000\n" +
                "v -1.000000 1.000000 -1.000000\n" +
                "v -1.000000 -1.000000 -1.000000\n" +
                "v -1.000000 1.000000 1.000000\n" +
                "v -1.000000 -1.000000 1.000000\n" +
                "vt 0.875000 0.500000\n" +
                "vt 0.625000 0.750000\n" +
                "vt 0.625000 0.500000\n" +
                "vt 0.375000 1.000000\n" +
                "vt 0.375000 0.750000\n" +
                "vt 0.625000 0.000000\n" +
                "vt 0.375000 0.250000\n" +
                "vt 0.375000 0.000000\n" +
                "vt 0.375000 0.500000\n" +
                "vt 0.125000 0.750000\n" +
                "vt 0.125000 0.500000\n" +
                "vt 0.625000 0.250000\n" +
                "vt 0.875000 0.750000\n" +
                "vt 0.625000 1.000000\n" +
                "vn 0.0000 1.0000 0.0000\n" +
                "vn 0.0000 0.0000 1.0000\n" +
                "vn -1.0000 0.0000 0.0000\n" +
                "vn 0.0000 -1.0000 0.0000\n" +
                "vn 1.0000 0.0000 0.0000\n" +
                "vn 0.0000 0.0000 -1.0000\n" +
                "usemtl Material\n" +
                "s off\n" +
                "f 5/1/1 3/2/1 1/3/1\n" +
                "f 3/2/2 8/4/2 4/5/2\n" +
                "f 7/6/3 6/7/3 8/8/3\n" +
                "f 2/9/4 8/10/4 6/11/4\n" +
                "f 1/3/5 4/5/5 2/9/5\n" +
                "f 5/12/6 2/9/6 6/7/6\n" +
                "f 5/1/1 7/13/1 3/2/1\n" +
                "f 3/2/2 7/14/2 8/4/2\n" +
                "f 7/6/3 5/12/3 6/7/3\n" +
                "f 2/9/4 4/5/4 8/10/4\n" +
                "f 1/3/5 3/2/5 4/5/5\n" +
                "f 5/12/6 1/3/6 2/9/6\n";
        inputStream = new ByteArrayInputStream(cube_file.getBytes(StandardCharsets.UTF_8));

        object = ObjUtils.convertToRenderable(
                ObjReader.read(inputStream));


        //конвертируем в Face
        this.faces = new Face[object.getNumFaces()];
        Shape.parseObj(faces, object);
    }

    private PImage loadTexture(Void v) {
        for (int i = 0; i < images.length; i++) {
            images[i] = loadImage(textureFileName + names[i] + "." + res, context);
        }
        return null;
    }


    public void bindData() {

        Shader.getActiveShader().getAdaptor().bindData(faces);

        // помещаем текстуру в target 2D юнита 0
        gl.glActiveTexture(glc.GL_TEXTURE0());
        if (!postToGlNeeded) {
            gl.glBindTexture(glc.GL_TEXTURE_CUBE_MAP(), texture.getId());
        }
        if (postToGlNeeded) {
            postToGl();
        }
        // юнит текстуры
        gl.glUniform1i(Shader.getActiveShader().getAdaptor().getTextureLocation(), 0);
        postToGlNeeded = false;
    }

    private void postToGl() {
        gl.glActiveTexture(glc.GL_TEXTURE0());
        gl.glBindTexture(glc.GL_TEXTURE_CUBE_MAP(), texture.getId());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_MIN_FILTER(), glc.GL_LINEAR());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_MAG_FILTER(), glc.GL_LINEAR());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_S(), glc.GL_CLAMP_TO_EDGE());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_T(), glc.GL_CLAMP_TO_EDGE());
        gl.glTexParameteri(glc.GL_TEXTURE_CUBE_MAP(), glc.GL_TEXTURE_WRAP_R(), glc.GL_CLAMP_TO_EDGE());


        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_NEGATIVE_X(), 0, images[0], 0);
        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_POSITIVE_X(), 0, images[1], 0);

        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y(), 0, images[2], 0);
        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_POSITIVE_Y(), 0, images[3], 0);

        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z(), 0, images[4], 0);
        gl.texImage2D(glc.GL_TEXTURE_CUBE_MAP_POSITIVE_Z(), 0, images[5], 0);
        for (PImage i : images) {
            i.delete();
        }
    }

    public void prepareAndDraw() {
        bindData();
        gl.glDepthMask(false);
        gl.glDrawArrays(glc.GL_TRIANGLES(), 0, 12 * 3);
        gl.glDepthMask(true);
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
        if (redrawNeeded) {
            VerticesShapesManager.allShapesToRedraw.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        }
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }


    @Override
    public void onRedraw() {
        redrawFunction.apply(null);
        setRedrawNeeded(false);
    }


    @Override
    public String getCreatorClassName() {
        return null;
    }

    @Override
    public void onFrameBegin() {

    }

    @Override
    public void delete() {
        //everything is already deleted
    }

    public void redrawNow() {
        onRedraw();
    }

}

