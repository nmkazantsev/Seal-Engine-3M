package com.seal.gl_engine.platform;

import android.opengl.GLES32;
import com.nikitos.platformBridge.GLConstBridge;

import android.opengl.GLES30;

public class GLConstBridgeAndroid extends GLConstBridge {
    // 1. Состояния
    @Override
    public int GL_DEPTH_TEST() {
        return GLES30.GL_DEPTH_TEST;
    }

    @Override
    public int GL_BLEND() {
        return GLES30.GL_BLEND;
    }

    @Override
    public int GL_CULL_FACE() {
        return GLES30.GL_CULL_FACE;
    }

    @Override
    public int GL_SCISSOR_TEST() {
        return GLES30.GL_SCISSOR_TEST;
    }

    @Override
    public int GL_STENCIL_TEST() {
        return GLES30.GL_STENCIL_TEST;
    }

    @Override
    public int GL_TRIANGLES() {
        return GLES30.GL_TRIANGLES;
    }

    @Override
    public int GL_LINES() {
        return GLES30.GL_LINES;
    }


    // 2. Шейдеры
    @Override
    public int GL_VERTEX_SHADER() {
        return GLES30.GL_VERTEX_SHADER;
    }

    @Override
    public int GL_FRAGMENT_SHADER() {
        return GLES30.GL_FRAGMENT_SHADER;
    }

    @Override
    public int GL_GEOMETRY_SHADER() {
        return GLES32.GL_GEOMETRY_SHADER;
    }

    @Override
    public int GL_COMPILE_STATUS() {
        return GLES30.GL_COMPILE_STATUS;
    }

    @Override
    public int GL_LINK_STATUS() {
        return GLES30.GL_LINK_STATUS;
    }

    @Override
    public int GL_INFO_LOG_LENGTH() {
        return GLES30.GL_INFO_LOG_LENGTH;
    }

    @Override
    public int GL_CURRENT_PROGRAM() {
        return GLES30.GL_CURRENT_PROGRAM;
    }

    // 3. Буферы
    @Override
    public int GL_ARRAY_BUFFER() {
        return GLES30.GL_ARRAY_BUFFER;
    }

    @Override
    public int GL_ELEMENT_ARRAY_BUFFER() {
        return GLES30.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    public int GL_STATIC_DRAW() {
        return GLES30.GL_STATIC_DRAW;
    }

    @Override
    public int GL_DYNAMIC_DRAW() {
        return GLES30.GL_DYNAMIC_DRAW;
    }

    @Override
    public int GL_STREAM_DRAW() {
        return GLES30.GL_STREAM_DRAW;
    }

    @Override
    public int GL_BUFFER_SIZE() {
        return GLES30.GL_BUFFER_SIZE;
    }

    @Override
    public int GL_BUFFER_USAGE() {
        return GLES30.GL_BUFFER_USAGE;
    }

    // 4. Текстуры
    @Override
    public int GL_TEXTURE_2D() {
        return GLES30.GL_TEXTURE_2D;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP() {
        return GLES30.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    public int GL_TEXTURE0() {
        return GLES30.GL_TEXTURE0;
    }

    @Override
    public int GL_TEXTURE1() {
        return GLES30.GL_TEXTURE1;
    }

    @Override
    public int GL_TEXTURE_WRAP_S() {
        return GLES30.GL_TEXTURE_WRAP_S;
    }

    @Override
    public int GL_TEXTURE_WRAP_T() {
        return GLES30.GL_TEXTURE_WRAP_T;
    }

    @Override
    public int GL_TEXTURE_MIN_FILTER() {
        return GLES30.GL_TEXTURE_MIN_FILTER;
    }

    @Override
    public int GL_TEXTURE_MAG_FILTER() {
        return GLES30.GL_TEXTURE_MAG_FILTER;
    }

    @Override
    public int GL_NEAREST() {
        return GLES30.GL_NEAREST;
    }

    @Override
    public int GL_LINEAR() {
        return GLES30.GL_LINEAR;
    }

    @Override
    public int GL_LINEAR_MIPMAP_LINEAR() {
        return GLES30.GL_LINEAR_MIPMAP_LINEAR;
    }

    @Override
    public int GL_RGBA() {
        return GLES30.GL_RGBA;
    }

    @Override
    public int GL_RGB() {
        return GLES30.GL_RGB;
    }

    @Override
    public int GL_UNSIGNED_BYTE() {
        return GLES30.GL_UNSIGNED_BYTE;
    }

    @Override
    public int GL_FLOAT() {
        return GLES30.GL_FLOAT;
    }

    @Override
    public int GL_TEXTURE_MAX_LEVEL() {
        return GLES30.GL_TEXTURE_MAX_LEVEL;
    }

    // 5. Blending
    @Override
    public int GL_SRC_ALPHA() {
        return GLES30.GL_SRC_ALPHA;
    }

    @Override
    public int GL_ONE_MINUS_SRC_ALPHA() {
        return GLES30.GL_ONE_MINUS_SRC_ALPHA;
    }

    @Override
    public int GL_ONE() {
        return GLES30.GL_ONE;
    }

    @Override
    public int GL_ZERO() {
        return GLES30.GL_ZERO;
    }

    @Override
    public int GL_FUNC_ADD() {
        return GLES30.GL_FUNC_ADD;
    }

    // 6. Очистка
    @Override
    public int GL_COLOR_BUFFER_BIT() {
        return GLES30.GL_COLOR_BUFFER_BIT;
    }

    @Override
    public int GL_DEPTH_BUFFER_BIT() {
        return GLES30.GL_DEPTH_BUFFER_BIT;
    }

    @Override
    public int GL_STENCIL_BUFFER_BIT() {
        return GLES30.GL_STENCIL_BUFFER_BIT;
    }

    // 7. Форматы
    @Override
    public int GL_FLOAT_VEC2() {
        return GLES30.GL_FLOAT_VEC2;
    }

    @Override
    public int GL_FLOAT_VEC3() {
        return GLES30.GL_FLOAT_VEC3;
    }

    @Override
    public int GL_FLOAT_VEC4() {
        return GLES30.GL_FLOAT_VEC4;
    }

    @Override
    public int GL_FLOAT_MAT3() {
        return GLES30.GL_FLOAT_MAT3;
    }

    @Override
    public int GL_FLOAT_MAT4() {
        return GLES30.GL_FLOAT_MAT4;
    }

    @Override
    public int GL_INT() {
        return GLES30.GL_INT;
    }

    @Override
    public int GL_BOOL() {
        return GLES30.GL_BOOL;
    }

    // 8. glGet
    @Override
    public int GL_MAX_TEXTURE_SIZE() {
        return GLES30.GL_MAX_TEXTURE_SIZE;
    }

    @Override
    public int GL_MAX_VERTEX_ATTRIBS() {
        return GLES30.GL_MAX_VERTEX_ATTRIBS;
    }

    @Override
    public int GL_MAX_VERTEX_UNIFORM_COMPONENTS() {
        return GLES30.GL_MAX_VERTEX_UNIFORM_COMPONENTS;
    }

    @Override
    public int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS() {
        return GLES30.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS;
    }

    @Override
    public int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS() {
        return GLES30.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
    }

    @Override
    public int GL_MAX_TEXTURE_IMAGE_UNITS() {
        return GLES30.GL_MAX_TEXTURE_IMAGE_UNITS;
    }

    @Override
    public int GL_MAX_RENDERBUFFER_SIZE() {
        return GLES30.GL_MAX_RENDERBUFFER_SIZE;
    }

    @Override
    public int GL_MAX_VIEWPORT_DIMS() {
        return GLES30.GL_MAX_VIEWPORT_DIMS;
    }

    @Override
    public int GL_MAX_CUBE_MAP_TEXTURE_SIZE() {
        return GLES30.GL_MAX_CUBE_MAP_TEXTURE_SIZE;
    }

    @Override
    public int GL_MAX_VARYING_COMPONENTS() {
        return GLES30.GL_MAX_VARYING_COMPONENTS;
    }

    // 9. Строки
    @Override
    public int GL_VENDOR() {
        return GLES30.GL_VENDOR;
    }

    @Override
    public int GL_RENDERER() {
        return GLES30.GL_RENDERER;
    }

    @Override
    public int GL_VERSION() {
        return GLES30.GL_VERSION;
    }

    @Override
    public int GL_SHADING_LANGUAGE_VERSION() {
        return GLES30.GL_SHADING_LANGUAGE_VERSION;
    }

    @Override
    public int GL_EXTENSIONS() {
        return GLES30.GL_EXTENSIONS;
    }

    @Override
    public int GL_RASTERIZER_DISCARD() {
        return GLES30.GL_RASTERIZER_DISCARD;
    }

    @Override
    public int GL_DITHER() {
        return GLES30.GL_DITHER;
    }

    @Override
    public int GL_POLYGON_OFFSET_FILL() {
        return GLES30.GL_POLYGON_OFFSET_FILL;
    }

    @Override
    public int GL_MIRRORED_REPEAT() {
        return GLES30.GL_MIRRORED_REPEAT;
    }

    @Override
    public int GL_CLAMP_TO_EDGE() {
        return GLES30.GL_CLAMP_TO_EDGE;
    }

    @Override
    public int GL_TEXTURE_WRAP_R() {
        return GLES30.GL_TEXTURE_WRAP_R;
    }
}
