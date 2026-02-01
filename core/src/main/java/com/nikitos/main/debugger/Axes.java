package com.nikitos.main.debugger;

import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.SectionShaderAdaptor;
import com.nikitos.main.vertices.SectionPolygon;
import com.nikitos.maths.PVector;
import com.nikitos.maths.Section;
import com.nikitos.utils.FileUtils;

import static com.nikitos.maths.Matrix.applyMatrix;
import static com.nikitos.maths.Matrix.resetTranslateMatrix;

public class Axes {
    private final SectionPolygon line;
    private static Shader shader = null;

    public Axes(GamePageClass gamePageClass) {
        FileUtils fileUtils = new FileUtils();
        line = new SectionPolygon(gamePageClass);
        shader = new Shader(
                fileUtils.readFileFromAssets(this.getClass(), "/line_vertex.glsl"),
                fileUtils.readFileFromAssets(this.getClass(), "/line_fragment.glsl"),
                gamePageClass,
                new SectionShaderAdaptor()); //compile only once

    }

    public void drawAxes(float limit, float step, float tickSize, float[] matrix, Camera camera) {
        Shader prevShader = Shader.getActiveShader();
        shader.apply();
        if (matrix == null) {
            matrix = resetTranslateMatrix(new float[16]);
        }
        applyMatrix(matrix);
        camera.apply();
        //x
        line.setColor(new PVector(1, 0, 0));
        line.draw(new Section(new PVector(0), new PVector(limit, 0, 0)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(i, -tickSize, 0), new PVector(i, tickSize, 0)));
            line.draw(new Section(new PVector(i, 0, -tickSize), new PVector(i, 0, tickSize)));
        }

        line.setColor(new PVector(0.5f, 0, 0));
        line.draw(new Section(new PVector(0), new PVector(-limit, 0, 0)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(i, -tickSize, 0), new PVector(i, tickSize, 0)));
            line.draw(new Section(new PVector(i, 0, -tickSize), new PVector(i, 0, tickSize)));
        }

        //y
        line.setColor(new PVector(0, 1, 0));
        line.draw(new Section(new PVector(0), new PVector(0, limit, 0)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(-tickSize, i, 0), new PVector(tickSize, i, 0)));
            line.draw(new Section(new PVector(0, i, -tickSize), new PVector(0, i, tickSize)));
        }

        line.setColor(new PVector(0, 0.5f, 0));
        line.draw(new Section(new PVector(0), new PVector(0, -limit, 0)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(-tickSize, i, 0), new PVector(tickSize, i, 0)));
            line.draw(new Section(new PVector(0, i, -tickSize), new PVector(0, i, tickSize)));
        }
        //z
        line.setColor(new PVector(0, 0, 1));
        line.draw(new Section(new PVector(0), new PVector(0, 0, limit)));
        for (float i = 0; i < limit; i += step) {
            line.draw(new Section(new PVector(-tickSize, 0, i), new PVector(tickSize, 0, i)));
            line.draw(new Section(new PVector(0, -tickSize, i), new PVector(0, tickSize, i)));
        }

        line.setColor(new PVector(0, 0, 0.5f));
        line.draw(new Section(new PVector(0), new PVector(0, 0, -limit)));
        for (float i = -limit; i < 0; i += step) {
            line.draw(new Section(new PVector(-tickSize, 0, i), new PVector(tickSize, 0, i)));
            line.draw(new Section(new PVector(0, -tickSize, i), new PVector(0, tickSize, i)));
        }


        prevShader.apply();
        camera.apply();
        applyMatrix(matrix);
    }
}
