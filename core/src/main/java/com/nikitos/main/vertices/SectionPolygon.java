package com.nikitos.main.vertices;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.ShaderData;
import com.nikitos.maths.Section;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.lang.ref.WeakReference;

//in fact implementation of interface, that automatically redraws textures needed for bind data to be called when needed
public class SectionPolygon implements VerticesSet {
    private GamePageClass page;
    private final String creatorClassName;
    private PVector color = new PVector(1);
    ShaderData lineColorData;

    private final GeneralPlatformBridge gl;
    private final GLConstBridge glc;

    public SectionPolygon(GamePageClass page) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        VerticesShapesManager.allShapes.add(new WeakReference<>(this));//add link to this object

        creatorClassName = page.getClass().getName();

        lineColorData = new ShaderData(page) {
            private int colorLoc;

            @Override
            protected void getLocations(int programId) {
                colorLoc = gl.glGetUniformLocation(programId, "vColor");
            }

            @Override
            protected void forwardData() {
                gl.glUniform3f(colorLoc, color.x, color.y, color.z);
            }

            @Override
            protected void delete() {

            }
        };
    }

    private void bindData(Section section) {
        Shader.getActiveShader().getAdaptor().bindDataLine(section.getBaseVector(), section.getBaseVector().add(section.getDirectionVector()), color);

    }

    public void draw(Section section) {
        lineColorData.forwardNow();
        bindData(section);
        gl.glDrawArrays(glc.GL_LINES(), 0, 2);
    }

    public void setColor(PVector color) {
        this.color = color;
    }

    //do not need these methods as we are going to pass color each time draw function is called
    @Override
    public void onRedrawSetup() {

    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {

    }

    @Override
    public boolean isRedrawNeeded() {
        return false;
    }

    @Override
    public void onRedraw() {

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

    }
}
