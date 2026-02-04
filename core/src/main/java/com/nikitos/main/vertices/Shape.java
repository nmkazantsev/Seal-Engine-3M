package com.nikitos.main.vertices;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.textures.NormalMap;
import com.nikitos.main.textures.Texture;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.function.Function;

import static com.nikitos.utils.FileUtils.loadImage;


public class Shape implements VerticesSet {
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;
    private final PlatformBridge platformBridge;

    private final Class<?> contextClass;
    private boolean isVertexLoaded = false, globalLoaded = false;

    private final Texture texture;
    private NormalMap normalTexture;
    private final String textureFileName;
    private String normalMapFileName;
    private Obj object;
    private Face[] faces;
    private final GamePageClass creator;

    private boolean postToGlNeeded = true;
    private boolean redrawNeeded = true;
    private PImage image, normalImage;

    private final Function<Void, PImage> redrawFunction;

    private VertexBuffer vertexBuffer;

    private boolean vboLoaded = false;

    public Shape(PreLoadedMesh preLoadedMesh, String textureFileName, GamePageClass page) {
        platformBridge = CoreRenderer.engine.getPlatformBridge();
        gl = platformBridge.getGeneralPlatformBridge();
        glc = platformBridge.getGLConstBridge();
        contextClass = page.getClass();

        creator = page;
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page);

        faces = preLoadedMesh.facesArr;
        object = (Obj) preLoadedMesh.object;

        onRedrawSetup();
        redrawNow();
    }

    public Shape(String fileName, String textureFileName, GamePageClass page, Class<?> context) {
        platformBridge = CoreRenderer.engine.getPlatformBridge();
        gl = platformBridge.getGeneralPlatformBridge();
        glc = platformBridge.getGLConstBridge();
        contextClass = page.getClass();

        creator = page;
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page);
        loadFacesAsync(fileName, facesAndObject -> {
            isVertexLoaded = true;
            faces = facesAndObject.facesArr;
            object = (Obj) facesAndObject.object;
            return null;
        }, context);
        onRedrawSetup();
        redrawNow();
    }

    public static class PreLoadedMesh {
        private Face[] facesArr;
        private Object object;
    }

    //тут оправдан статик чтобы не городить еще класс на preloader
    //и нужен свой platform bridge
    public static void loadFacesAsync(String fileName, Function<PreLoadedMesh, Void> callback, Class<?> cls) {
        PlatformBridge platformBridge = CoreRenderer.engine.getPlatformBridge();
        new Thread(() -> {
            Face[] faces1;
            InputStream inputStream;
            Obj object = null;
            try {
                inputStream = cls.getResourceAsStream(fileName);
                assert inputStream != null;
                object = ObjUtils.convertToRenderable(
                        ObjReader.read(inputStream));
            } catch (IOException e) {
                platformBridge.log_e("ERROR LOADING", fileName);
            }
            if (object == null) {
                return;
            }
            //convert to Face
            faces1 = new Face[object.getNumFaces()];
            parseObj(faces1, object);
            PreLoadedMesh preLoadedMesh = new PreLoadedMesh();
            preLoadedMesh.facesArr = faces1;
            preLoadedMesh.object = object;
            callback.apply(preLoadedMesh);
        }).start();
    }

    static void parseObj(Face[] faces1, Obj object) {
        for (int i = 0; i < object.getNumFaces(); i++) {
            faces1[i] = new Face(new PVector[]{
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(0)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(0)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(0)).getZ()),
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(1)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(1)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(1)).getZ()),
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(2)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(2)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(2)).getZ())},
                    new PVector[]{
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getY()),
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getY()),
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getY())},
                    new PVector[]{
                            new PVector(
                                    object.getNormal(object.getFace(i).getNormalIndex(0)).getX(),
                                    object.getNormal(object.getFace(i).getNormalIndex(0)).getY(),
                                    object.getNormal(object.getFace(i).getNormalIndex(0)).getZ()
                            ),
                            new PVector(
                                    object.getNormal(object.getFace(i).getNormalIndex(1)).getX(),
                                    object.getNormal(object.getFace(i).getNormalIndex(1)).getY(),
                                    object.getNormal(object.getFace(i).getNormalIndex(1)).getZ()
                            ),
                            new PVector(
                                    object.getNormal(object.getFace(i).getNormalIndex(2)).getX(),
                                    object.getNormal(object.getFace(i).getNormalIndex(2)).getY(),
                                    object.getNormal(object.getFace(i).getNormalIndex(2)).getZ()
                            )
                    });
        }
    }

    public void onFrameBegin() {
        if (isVertexLoaded) {
            globalLoaded = true;
        }
    }

    public void addNormalMap(String normalMapFileName) {
        this.normalMapFileName = normalMapFileName;
        normalImage = loadImage(normalMapFileName, contextClass);
        normalTexture = new NormalMap(creator);
    }

    private PImage loadTexture(Void v) {
        if (normalMapFileName != null) {
            normalImage = loadImage(normalMapFileName, contextClass);
        }
        return loadImage(textureFileName, contextClass);
    }


    public void bindData() {
        if (!vboLoaded) {
            vertexBuffer = new VertexBuffer(5, creator); //5 because 5 types of coordinates so we need 5 buffers
        }
        Shader.getActiveShader().getAdaptor().bindData(faces, vertexBuffer, vboLoaded);
        vboLoaded = true;
        // place texture in target 2D unit 0
        gl.glActiveTexture(glc.GL_TEXTURE0());
        if (!postToGlNeeded) {
            gl.glBindTexture(glc.GL_TEXTURE_2D(), texture.getId());
        }
        if (postToGlNeeded) {
            postToGl();
        }
        // texture unit
        gl.glUniform1i(Shader.getActiveShader().getAdaptor().getTextureLocation(), 0);

        //  place texture in target 2D unit 0
        gl.glActiveTexture(glc.GL_TEXTURE1());
        if (!postToGlNeeded && normalTexture != null) {
            gl.glBindTexture(glc.GL_TEXTURE_2D(), normalTexture.getId());
        }
        if (postToGlNeeded) {
            postToGlNormals();
        }
        //texture unit
        gl.glUniform1i(Shader.getActiveShader().getAdaptor().getNormalTextureLocation(), 1);

        //enable or disable normal map in shader
        if (normalTexture != null) {
            gl.glUniform1i(Shader.getActiveShader().getAdaptor().getNormalMapEnableLocation(), 1);
        } else {
            gl.glUniform1i(Shader.getActiveShader().getAdaptor().getNormalMapEnableLocation(), 0);
        }
        postToGlNeeded = false;
    }

    private void postToGl() {
        gl.glActiveTexture(glc.GL_TEXTURE0());
        gl.glBindTexture(glc.GL_TEXTURE_2D(), texture.getId());
        gl.texImage2D(glc.GL_TEXTURE_2D(), 0, glc.GL_RGBA(), image, glc.GL_UNSIGNED_BYTE(), 0);
    }

    private void postToGlNormals() {
        if (normalImage != null && normalImage.isLoaded()) {
            gl.glActiveTexture(glc.GL_TEXTURE1());
            gl.glBindTexture(glc.GL_TEXTURE_2D(), normalTexture.getId());
            gl.texImage2D(glc.GL_TEXTURE_2D(), 0, glc.GL_RGBA(), normalImage, glc.GL_UNSIGNED_BYTE(), 0);
            normalImage.delete();
            gl.glActiveTexture(glc.GL_TEXTURE0());
        }
    }


    public void prepareAndDraw() {
        if (globalLoaded) {
            bindData();
            vertexBuffer.bindVao();
            gl.glEnable(glc.GL_CULL_FACE()); //i dont know what is it, it should be optimization
            gl.glDrawArrays(glc.GL_TRIANGLES(), 0, object.getNumFaces() * 3);
            gl.glDisable(glc.GL_CULL_FACE());
            vertexBuffer.bindDefaultVao();
        }
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
        vboLoaded = false;
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
        if (redrawNeeded) {
            VerticesShapesManager.allShapesToRedraw.add(new WeakReference<>(this));//добавить ссылку на Poligon
            vboLoaded = false;
            postToGlNeeded = true;
            VerticesShapesManager.allShapesToRedraw.add(new WeakReference<>(this));//добавить ссылку на Poligon
            vboLoaded = false;
        }
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }


    @Override
    public void onRedraw() {
        vboLoaded = false;
        if (image != null) {
            image.delete();
        }
        this.image = redrawFunction.apply(null);
        setRedrawNeeded(false);
    }


    @Override
    public String getCreatorClassName() {
        return null;
    }

    @Override
    public void delete() {
        image.delete();
        normalImage.delete();
        vertexBuffer.delete();
    }

    public void redrawNow() {
        onRedraw();
    }
}
