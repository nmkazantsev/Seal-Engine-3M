package com.seal.gl_engine.platform;

import android.opengl.GLES30;
import android.util.Log;
import com.nikitos.platformBridge.ErrorPrinter;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class ErrorPrinterAndroid extends ErrorPrinter {
    /**
     * Вывод ключевых параметров текущего состояния OpenGL.
     */
    public void printOpenGLState() {
        // Информация о реализации
        String version = GLES30.glGetString(GLES30.GL_VERSION);
        String vendor = GLES30.glGetString(GLES30.GL_VENDOR);
        String renderer = GLES30.glGetString(GLES30.GL_RENDERER);
        String glslVersion = GLES30.glGetString(GLES30.GL_SHADING_LANGUAGE_VERSION);

        System.out.println("=== OpenGL State ===");
        System.out.println("Версия OpenGL: " + version);
        System.out.println("Производитель: " + vendor);
        System.out.println("Рендерер: " + renderer);
        System.out.println("Версия GLSL: " + glslVersion);

        // Viewport
        int[] viewport = new int[4];
        GLES30.glGetIntegerv(GLES30.GL_VIEWPORT, IntBuffer.wrap(viewport));
        System.out.println("Viewport: x=" + viewport[0] + ", y=" + viewport[1] +
                ", ширина=" + viewport[2] + ", высота=" + viewport[3]);

        // Цвет очистки
        float[] clearColor = new float[4];
        GLES30.glGetFloatv(GLES30.GL_COLOR_CLEAR_VALUE, FloatBuffer.wrap(clearColor));
        System.out.printf("Цвет очистки: r=%.2f, g=%.2f, b=%.2f, a=%.2f%n",
                clearColor[0], clearColor[1], clearColor[2], clearColor[3]);

        // Проверка включённых состояний
        boolean depthTest = GLES30.glIsEnabled(GLES30.GL_DEPTH_TEST);
        boolean blend = GLES30.glIsEnabled(GLES30.GL_BLEND);
        boolean cullFace = GLES30.glIsEnabled(GLES30.GL_CULL_FACE);
        boolean stencilTest = GLES30.glIsEnabled(GLES30.GL_STENCIL_TEST);
        boolean scissorTest = GLES30.glIsEnabled(GLES30.GL_SCISSOR_TEST);

        System.out.println("Тест глубины: " + depthTest);
        System.out.println("Смешивание: " + blend);
        System.out.println("Отсечение граней: " + cullFace);
        System.out.println("Трафаретный тест: " + stencilTest);
        System.out.println("Тест отсечения (scissor): " + scissorTest);

        int[] tmp = new int[1];
        // Текущая шейдерная программа
        GLES30.glGetIntegerv(GLES30.GL_CURRENT_PROGRAM, tmp,0);
        System.out.println("Текущая шейдерная программа: " + tmp[0]);

        // Активный текстурный блок
        GLES30.glGetIntegerv(GLES30.GL_ACTIVE_TEXTURE, tmp,0);
        System.out.println("Активный текстурный блок: GL_TEXTURE" + (tmp[0] - GLES30.GL_TEXTURE0));

        // Текущий массив вершинных атрибутов (VAO)
        GLES30.glGetIntegerv(GLES30.GL_VERTEX_ARRAY_BINDING, tmp,0);
        System.out.println("Текущий VAO: " + tmp[0]);

        // Параметры упаковки пикселей (для чтения текселей)
        int[] packAlignment = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_PACK_ALIGNMENT, IntBuffer.wrap(packAlignment));
        System.out.println("Выравнивание упаковки (pack alignment): " + packAlignment[0]);

        // Дополнительно можно получить многие другие параметры,
        // например, текущий цвет, полигональный режим, настройки смешивания и т.д.
    }

    /**
     * Проверка наличия ошибок OpenGL с помощью glGetError.
     *
     * @param location строка, описывающая место вызова (для контекста)
     */
    public void checkGLErrors(String location) {
        int error;
        boolean foundError = false;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            foundError = true;
            String errorMsg = getGLErrorString(error);
            Log.e("engine", "Ошибка OpenGL в " + location + ": " + errorMsg +
                    " (код 0x" + Integer.toHexString(error) + ")");
        }
    }

    /**
     * Преобразование кода ошибки в строку.
     */
    private static String getGLErrorString(int errorCode) {
        if (errorCode == GLES30.GL_INVALID_ENUM) {
            return "GL_INVALID_ENUM (недопустимое перечисление)";
        } else if (errorCode == GLES30.GL_INVALID_VALUE) {
            return "GL_INVALID_VALUE (недопустимое значение)";
        } else if (errorCode == GLES30.GL_INVALID_OPERATION) {
            return "GL_INVALID_OPERATION (недопустимая операция)";
        } else if (errorCode == GLES30.GL_OUT_OF_MEMORY) {
            return "GL_OUT_OF_MEMORY (недостаточно памяти)";
        } else if (errorCode == GLES30.GL_INVALID_FRAMEBUFFER_OPERATION) {
            return "GL_INVALID_FRAMEBUFFER_OPERATION (некорректная операция с буфером кадра)";
        } else {
            return "Неизвестная ошибка (0x" + Integer.toHexString(errorCode) + ")";
        }
    }


}
