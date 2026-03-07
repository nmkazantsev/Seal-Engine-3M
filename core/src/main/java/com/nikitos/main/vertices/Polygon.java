package com.nikitos.main.vertices;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.textures.Texture;
import com.nikitos.main.vertex_bueffer.VertexBuffer;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * this is 3D glShape (glShape deleted in version 3.0.0)
 */
public class Polygon implements VerticesSet {
    private final GeneralPlatformBridge gl;
    private final GLConstBridge glConst;
    private Face face1;
    private Face face2;
    private final boolean saveMemory;
    private final String creatorClassName;
    float[] vertexes, textCoords;
    private final Texture texture;
    protected boolean postToGlNeeded = true;
    protected boolean redrawNeeded = true;
    public PImage image;
    public List<Object> redrawParams = new ArrayList<>();//change it in the way you like

    private final Function<List<Object>, PImage> redrawFunction;
    private VertexBuffer vertexBuffer;
    private boolean vboCreated = false;

    private final GamePageClass gamePageClass;

    public Polygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page) {
        this.gamePageClass = page;
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glConst = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        this.redrawFunction = redrawFunction;
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page);
        for (int i = 0; i < paramSize; i++) {
            redrawParams.add("");
        }
        this.saveMemory = saveMemory;
        redrawNow();
        if (page == null) {
            creatorClassName = null;
        } else {
            creatorClassName = page.getClass().getName();
        }
    }

    public Polygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page, boolean mipMap) {
        this.gamePageClass = page;
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glConst = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        this.redrawFunction = redrawFunction;
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page, mipMap);
        for (int i = 0; i < paramSize; i++) {
            redrawParams.add("");
        }
        this.saveMemory = saveMemory;
        redrawNow();
        if (page == null) {
            creatorClassName = null;
        } else {
            creatorClassName = page.getClass().getName();
        }
    }

    public void newParamsSize(int paramSize) {
        redrawParams = new ArrayList<>();
        for (int i = 0; i < paramSize; i++) {
            redrawParams.add("");
        }
    }

    public void prepareData(PVector a, PVector b, PVector d) {
        /*
        a-----b
        |     |
        |     |
        d-----c
         */

        PVector c = new PVector(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[][] vertexes = new float[][]{
                {a.x, a.y, a.z},
                {d.x, d.y, d.z},
                {b.x, b.y, b.z},
                {c.x, c.y, c.z}
        };

        //вообще хз откуда эти координаты вылезли
        float[][] textCoords = new float[][]{
                {0, 1},
                {0, 0},
                {1, 1},
                {1, 0}
        };
        createFaces(vertexes, textCoords);
    }

    public void prepareData(PVector a, PVector b, PVector d, float texx, float texy, float texa, float texb) {
        /*
        a-----b
        |     |
        |     |
        d-----c
         */

        PVector c = new PVector(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[][] vertexes = new float[][]{
                {a.x, a.y, a.z},
                {d.x, d.y, d.z},
                {b.x, b.y, b.z},
                {c.x, c.y, c.z}
        };

        float u2 = texx + texa;
        float v2 = texy + texb;

        float[][] textCoords = new float[][]{
                {1.0f - u2, 1.0f - v2},  // правый нижний -> левый верхний
                {1.0f - u2, 1.0f - texy},  // правый верхний -> левый нижний
                {1.0f - texx, 1.0f - v2},  // левый нижний -> правый верхний
                {1.0f - texx, 1.0f - texy}   // левый верхний -> правый нижний
        };
        createFaces(vertexes, textCoords);
    }

    private void createFaces(float[][] vertexes, float[][] textCoords) {
        face1 = new Face(
                new PVector[]{
                        new PVector(vertexes[0][0], vertexes[0][1], vertexes[0][2]),
                        new PVector(vertexes[1][0], vertexes[1][1], vertexes[1][2]),
                        new PVector(vertexes[2][0], vertexes[2][1], vertexes[2][2]),
                },
                new PVector[]{
                        new PVector(textCoords[0][0], textCoords[0][1]),
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                },
                new PVector[]{
                        new PVector(0, 0, 1), new PVector(0, 0, 1), new PVector(0, 0, 1)
                });
        face2 = new Face(
                new PVector[]{
                        new PVector(vertexes[1][0], vertexes[1][1], vertexes[1][2]),
                        new PVector(vertexes[2][0], vertexes[2][1], vertexes[2][2]),
                        new PVector(vertexes[3][0], vertexes[3][1], vertexes[3][2]),
                },
                new PVector[]{
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                        new PVector(textCoords[3][0], textCoords[3][1]),
                },
                new PVector[]{
                        new PVector(0, 0, 1), new PVector(0, 0, 1), new PVector(0, 0, 1)
                });
    }

    protected void prepareData(PVector A, PVector B, float texx, float texy, float texa, float texb) {
        /*
        a-----b
        |     |
        |     |
        d-----c
         */
        float x = A.x;
        float y = A.y;
        float a = A.x - B.x;
        float b = A.y - B.y;
        float z = A.z;
        vertexes = new float[]{
                x, y, z,
                x + a, y, z,
                x, y + b, z,

                x, y + b, z,
                x + a, y + b, z,
                x + a, y, z
        };

        float u2 = texx + texa;
        float v2 = texy + texb;

        // После поворота на 180°
        float u1_rot = 1.0f - u2;  // инвертируем и меняем порядок
        float u2_rot = 1.0f - texx;
        float v1_rot = 1.0f - v2;
        float v2_rot = 1.0f - texy;

        textCoords = new float[]{

                // Первый треугольник
                u1_rot, v1_rot,
                u2_rot, v1_rot,
                u1_rot, v2_rot,

                // Второй треугольник
                u1_rot, v2_rot,
                u2_rot, v2_rot,
                u2_rot, v1_rot
        };
    }

    private void bindData() {
        if (!vboCreated) {
            vertexBuffer = new VertexBuffer(5, gamePageClass); //5 because 5 types of coordinates so we need 5 buffers
            vertexBuffer.setDynamicDraw(true);
        }
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2}, vertexBuffer, false); //reload always
        vboCreated = true;
        // помещаем текстуру в target 2D юнита 0
        gl.glActiveTexture(glConst.GL_TEXTURE0());
        if (!postToGlNeeded) {
            gl.glBindTexture(glConst.GL_TEXTURE_2D(), texture.getId());
        }
        if (postToGlNeeded) {
            postToGl();
        }
        // юнит текстуры
        gl.glUniform1i(Shader.getActiveShader().getAdaptor().getTextureLocation(), 0);

    }

    private void postToGl() {
        if (redrawNeeded || !image.isLoaded()) {
            redrawNow();
        }
        postToGlNeeded = false;
        gl.glActiveTexture(glConst.GL_TEXTURE0());
        gl.glBindTexture(glConst.GL_TEXTURE_2D(), texture.getId());
        gl.texImage2D(glConst.GL_TEXTURE_2D(), 0, image, 0);
        if (texture.hasMinMaps()) {
            gl.glGenerateMipmap(glConst.GL_TEXTURE_2D());
        }
        if (saveMemory) {
            image.delete();
        }
        // glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void prepareAndDraw(PVector a, PVector b, PVector c) {
        prepareData(a, b, c);
        bindData();
        vertexBuffer.bindVao();
        gl.glDrawArrays(glConst.GL_TRIANGLES(), 0, 6);
        vertexBuffer.bindDefaultVao();
    }

    public void prepareAndDraw(PVector a, PVector b, float texx, float texy, float teexa, float texb) {
        prepareData(a, b, texx, texy, teexa, texb);
        bindData();
        vertexBuffer.bindVao();
        gl.glDrawArrays(glConst.GL_TRIANGLES(), 0, 6);
        vertexBuffer.bindDefaultVao();
    }

    public void prepareAndDraw(PVector a, PVector b, PVector c, float texx, float texy, float teexa, float texb) {
        prepareData(a, b, c, texx, texy, teexa, texb);
        bindData();
        vertexBuffer.bindVao();
        gl.glDrawArrays(glConst.GL_TRIANGLES(), 0, 6);
        vertexBuffer.bindDefaultVao();
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
        VerticesShapesManager.allShapesToRedraw.add(new WeakReference<>(this));//добавить ссылку на Poligon
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }

    @Override
    public void onRedraw() {
        if (image != null) {
            image.delete();
        }
        this.image = redrawFunction.apply(redrawParams);
        image.setLoaded(true);
        setRedrawNeeded(false);
    }

    @Override
    public String getCreatorClassName() {
        return creatorClassName;
    }

    @Override
    public void onFrameBegin() {

    }

    @Override
    public void delete() {
        image.delete();
    }

    public void redrawNow() {
        onRedraw();
    }
}
