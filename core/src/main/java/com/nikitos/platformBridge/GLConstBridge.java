package com.nikitos.platformBridge;

import javax.swing.plaf.PanelUI;

public abstract class GLConstBridge {
    // 1. Константы состояния и режимов
    public abstract int GL_DEPTH_TEST();

    public abstract int GL_BLEND();

    public abstract int GL_CULL_FACE();

    public abstract int GL_SCISSOR_TEST();

    public abstract int GL_STENCIL_TEST();

    public abstract int GL_TRIANGLES();

    public abstract int GL_LINES();


    // 2. Константы шейдеров
    public abstract int GL_VERTEX_SHADER();

    public abstract int GL_FRAGMENT_SHADER();

    public abstract int GL_GEOMETRY_SHADER();

    public abstract int GL_COMPILE_STATUS();

    public abstract int GL_LINK_STATUS();

    public abstract int GL_INFO_LOG_LENGTH();

    public abstract int GL_CURRENT_PROGRAM();

    // 3. Константы буферов
    public abstract int GL_ARRAY_BUFFER();

    public abstract int GL_ELEMENT_ARRAY_BUFFER();

    public abstract int GL_STATIC_DRAW();

    public abstract int GL_DYNAMIC_DRAW();

    public abstract int GL_STREAM_DRAW();

    public abstract int GL_BUFFER_SIZE();

    public abstract int GL_BUFFER_USAGE();

    // 4. Константы текстур
    public abstract int GL_TEXTURE_2D();

    public abstract int GL_TEXTURE_CUBE_MAP();

    public abstract int GL_TEXTURE0();

    public abstract int GL_TEXTURE1();

    public abstract int GL_TEXTURE_WRAP_S();

    public abstract int GL_TEXTURE_WRAP_T();

    public abstract int GL_TEXTURE_MIN_FILTER();

    public abstract int GL_TEXTURE_MAG_FILTER();

    public abstract int GL_NEAREST();

    public abstract int GL_LINEAR();

    public abstract int GL_LINEAR_MIPMAP_LINEAR();

    public abstract int GL_RGBA();

    public abstract int GL_RGB();

    public abstract int GL_UNSIGNED_BYTE();

    public abstract int GL_FLOAT();

    public abstract int GL_TEXTURE_MAX_LEVEL();

    // 5. Константы blending
    public abstract int GL_SRC_ALPHA();

    public abstract int GL_ONE_MINUS_SRC_ALPHA();

    public abstract int GL_ONE();

    public abstract int GL_ZERO();

    public abstract int GL_FUNC_ADD();

    // 6. Константы очистки
    public abstract int GL_COLOR_BUFFER_BIT();

    public abstract int GL_DEPTH_BUFFER_BIT();

    public abstract int GL_STENCIL_BUFFER_BIT();

    // 7. Константы форматов данных
    public abstract int GL_FLOAT_VEC2();

    public abstract int GL_FLOAT_VEC3();

    public abstract int GL_FLOAT_VEC4();

    public abstract int GL_FLOAT_MAT3();

    public abstract int GL_FLOAT_MAT4();

    public abstract int GL_INT();

    public abstract int GL_BOOL();

    // 8. Константы запросов (glGet)
    public abstract int GL_MAX_TEXTURE_SIZE();

    public abstract int GL_MAX_VERTEX_ATTRIBS();

    public abstract int GL_MAX_VERTEX_UNIFORM_COMPONENTS();

    public abstract int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS();

    public abstract int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS();

    public abstract int GL_MAX_TEXTURE_IMAGE_UNITS();

    public abstract int GL_MAX_RENDERBUFFER_SIZE();

    public abstract int GL_MAX_VIEWPORT_DIMS();

    public abstract int GL_MAX_CUBE_MAP_TEXTURE_SIZE();

    public abstract int GL_MAX_VARYING_COMPONENTS();

    // 9. Строковые константы (glGetString)
    public abstract int GL_VENDOR();

    public abstract int GL_RENDERER();

    public abstract int GL_VERSION();

    public abstract int GL_SHADING_LANGUAGE_VERSION();

    public abstract int GL_EXTENSIONS();

    public abstract int GL_RASTERIZER_DISCARD();

    public abstract int GL_DITHER();

    public abstract int GL_POLYGON_OFFSET_FILL();

    public abstract int GL_MIRRORED_REPEAT();

    public abstract int GL_CLAMP_TO_EDGE();

    public abstract int GL_TEXTURE_WRAP_R();

    public abstract int GL_TEXTURE_CUBE_MAP_NEGATIVE_X();
    public abstract int GL_TEXTURE_CUBE_MAP_POSITIVE_X();
    public abstract int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y();
    public abstract int GL_TEXTURE_CUBE_MAP_POSITIVE_Y();
    public abstract int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z();
    public abstract int GL_TEXTURE_CUBE_MAP_POSITIVE_Z();

    public abstract int GL_FRAMEBUFFER();
    public abstract int GL_RENDERBUFFER();
    public abstract int GL_COLOR_ATTACHMENT0();
    public abstract int GL_DEPTH_ATTACHMENT();
    public abstract int GL_DEPTH_COMPONENT16();
    public abstract int GL_RGBA16F();
    public abstract int GL_SRGB();
}
