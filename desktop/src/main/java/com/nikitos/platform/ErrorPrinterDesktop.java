package com.nikitos.platform;

import com.nikitos.platformBridge.ErrorPrinter;
import org.lwjgl.opengl.GL30;

public class ErrorPrinterDesktop extends ErrorPrinter {
    /**
     * Вывод ключевых параметров текущего состояния OpenGL.
     */
    public void printOpenGLState() {
        // Информация о реализации
        String version = GL30.glGetString(GL30.GL_VERSION);
        String vendor = GL30.glGetString(GL30.GL_VENDOR);
        String renderer = GL30.glGetString(GL30.GL_RENDERER);
        String glslVersion = GL30.glGetString(GL30.GL_SHADING_LANGUAGE_VERSION);

        System.out.println("=== OpenGL State ===");
        System.out.println("Версия OpenGL: " + version);
        System.out.println("Производитель: " + vendor);
        System.out.println("Рендерер: " + renderer);
        System.out.println("Версия GLSL: " + glslVersion);

        // Viewport
        int[] viewport = new int[4];
        GL30.glGetIntegerv(GL30.GL_VIEWPORT, viewport);
        System.out.println("Viewport: x=" + viewport[0] + ", y=" + viewport[1] +
                ", ширина=" + viewport[2] + ", высота=" + viewport[3]);

        // Цвет очистки
        float[] clearColor = new float[4];
        GL30.glGetFloatv(GL30.GL_COLOR_CLEAR_VALUE, clearColor);
        System.out.printf("Цвет очистки: r=%.2f, g=%.2f, b=%.2f, a=%.2f%n",
                clearColor[0], clearColor[1], clearColor[2], clearColor[3]);

        // Проверка включённых состояний
        boolean depthTest = GL30.glIsEnabled(GL30.GL_DEPTH_TEST);
        boolean blend = GL30.glIsEnabled(GL30.GL_BLEND);
        boolean cullFace = GL30.glIsEnabled(GL30.GL_CULL_FACE);
        boolean stencilTest = GL30.glIsEnabled(GL30.GL_STENCIL_TEST);
        boolean scissorTest = GL30.glIsEnabled(GL30.GL_SCISSOR_TEST);

        System.out.println("Тест глубины: " + depthTest);
        System.out.println("Смешивание: " + blend);
        System.out.println("Отсечение граней: " + cullFace);
        System.out.println("Трафаретный тест: " + stencilTest);
        System.out.println("Тест отсечения (scissor): " + scissorTest);

        // Текущая шейдерная программа
        int currentProgram = GL30.glGetInteger(GL30.GL_CURRENT_PROGRAM);
        System.out.println("Текущая шейдерная программа: " + currentProgram);

        // Активный текстурный блок
        int activeTexture = GL30.glGetInteger(GL30.GL_ACTIVE_TEXTURE);
        System.out.println("Активный текстурный блок: GL_TEXTURE" + (activeTexture - GL30.GL_TEXTURE0));

        // Текущий массив вершинных атрибутов (VAO)
        int currentVAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        System.out.println("Текущий VAO: " + currentVAO);

        // Параметры упаковки пикселей (для чтения текселей)
        int[] packAlignment = new int[1];
        GL30.glGetIntegerv(GL30.GL_PACK_ALIGNMENT, packAlignment);
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
        while ((error = GL30.glGetError()) != GL30.GL_NO_ERROR) {
            foundError = true;
            String errorMsg = getGLErrorString(error);
            System.err.println("Ошибка OpenGL в " + location + ": " + errorMsg +
                    " (код 0x" + Integer.toHexString(error) + ")");
        }
    }

    /**
     * Преобразование кода ошибки в строку.
     */
    private static String getGLErrorString(int errorCode) {
        return switch (errorCode) {
            case GL30.GL_INVALID_ENUM -> "GL_INVALID_ENUM (недопустимое перечисление)";
            case GL30.GL_INVALID_VALUE -> "GL_INVALID_VALUE (недопустимое значение)";
            case GL30.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION (недопустимая операция)";
            case GL30.GL_STACK_OVERFLOW -> "GL_STACK_OVERFLOW (переполнение стека)";
            case GL30.GL_STACK_UNDERFLOW -> "GL_STACK_UNDERFLOW (исчерпание стека)";
            case GL30.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY (недостаточно памяти)";
            case GL30.GL_INVALID_FRAMEBUFFER_OPERATION ->
                    "GL_INVALID_FRAMEBUFFER_OPERATION (некорректная операция с буфером кадра)";
            default -> "Неизвестная ошибка (0x" + Integer.toHexString(errorCode) + ")";
        };
    }
}

