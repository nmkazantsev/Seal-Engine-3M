package com.nikitos.platform;

import com.nikitos.platformBridge.GLConstBridge;
import org.lwjgl.opengl.GL33;

import java.util.concurrent.RecursiveTask;

public class GLConstBridgeDesktop extends GLConstBridge {

    // 1. Состояния
    @Override
    public int GL_DEPTH_TEST() {
        return GL33.GL_DEPTH_TEST;
    }

    @Override
    public int GL_BLEND() {
        return GL33.GL_BLEND;
    }

    @Override
    public int GL_CULL_FACE() {
        return GL33.GL_CULL_FACE;
    }

    @Override
    public int GL_SCISSOR_TEST() {
        return GL33.GL_SCISSOR_TEST;
    }

    @Override
    public int GL_STENCIL_TEST() {
        return GL33.GL_STENCIL_TEST;
    }

    @Override
    public int GL_TRIANGLES() {
        return GL33.GL_TRIANGLES;
    }

    @Override
    public int GL_LINES() {
        return GL33.GL_LINES;
    }

    // 2. Шейдеры
    @Override
    public int GL_VERTEX_SHADER() {
        return GL33.GL_VERTEX_SHADER;
    }

    @Override
    public int GL_FRAGMENT_SHADER() {
        return GL33.GL_FRAGMENT_SHADER;
    }

    @Override
    public int GL_GEOMETRY_SHADER() {
        return GL33.GL_GEOMETRY_SHADER;
    }

    @Override
    public int GL_COMPILE_STATUS() {
        return GL33.GL_COMPILE_STATUS;
    }

    @Override
    public int GL_LINK_STATUS() {
        return GL33.GL_LINK_STATUS;
    }

    @Override
    public int GL_INFO_LOG_LENGTH() {
        return GL33.GL_INFO_LOG_LENGTH;
    }

    @Override
    public int GL_CURRENT_PROGRAM() {
        return GL33.GL_CURRENT_PROGRAM;
    }

    // 3. Буферы
    @Override
    public int GL_ARRAY_BUFFER() {
        return GL33.GL_ARRAY_BUFFER;
    }

    @Override
    public int GL_ELEMENT_ARRAY_BUFFER() {
        return GL33.GL_ELEMENT_ARRAY_BUFFER;
    }

    @Override
    public int GL_STATIC_DRAW() {
        return GL33.GL_STATIC_DRAW;
    }

    @Override
    public int GL_DYNAMIC_DRAW() {
        return GL33.GL_DYNAMIC_DRAW;
    }

    @Override
    public int GL_STREAM_DRAW() {
        return GL33.GL_STREAM_DRAW;
    }

    @Override
    public int GL_BUFFER_SIZE() {
        return GL33.GL_BUFFER_SIZE;
    }

    @Override
    public int GL_BUFFER_USAGE() {
        return GL33.GL_BUFFER_USAGE;
    }

    // 4. Текстуры
    @Override
    public int GL_TEXTURE_2D() {
        return GL33.GL_TEXTURE_2D;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP() {
        return GL33.GL_TEXTURE_CUBE_MAP;
    }

    @Override
    public int GL_TEXTURE0() {
        return GL33.GL_TEXTURE0;
    }

    @Override
    public int GL_TEXTURE1() {
        return GL33.GL_TEXTURE1;
    }

    @Override
    public int GL_TEXTURE_WRAP_S() {
        return GL33.GL_TEXTURE_WRAP_S;
    }

    @Override
    public int GL_TEXTURE_WRAP_T() {
        return GL33.GL_TEXTURE_WRAP_T;
    }

    @Override
    public int GL_TEXTURE_MIN_FILTER() {
        return GL33.GL_TEXTURE_MIN_FILTER;
    }

    @Override
    public int GL_TEXTURE_MAG_FILTER() {
        return GL33.GL_TEXTURE_MAG_FILTER;
    }

    @Override
    public int GL_NEAREST() {
        return GL33.GL_NEAREST;
    }

    @Override
    public int GL_LINEAR() {
        return GL33.GL_LINEAR;
    }

    @Override
    public int GL_LINEAR_MIPMAP_LINEAR() {
        return GL33.GL_LINEAR_MIPMAP_LINEAR;
    }

    @Override
    public int GL_RGBA() {
        return GL33.GL_RGBA;
    }

    @Override
    public int GL_RGB() {
        return GL33.GL_RGB;
    }

    @Override
    public int GL_UNSIGNED_BYTE() {
        return GL33.GL_UNSIGNED_BYTE;
    }

    @Override
    public int GL_FLOAT() {
        return GL33.GL_FLOAT;
    }

    @Override
    public int GL_TEXTURE_MAX_LEVEL() {
        return GL33.GL_TEXTURE_MAX_LEVEL;
    }

    // 5. Blending
    @Override
    public int GL_SRC_ALPHA() {
        return GL33.GL_SRC_ALPHA;
    }

    @Override
    public int GL_ONE_MINUS_SRC_ALPHA() {
        return GL33.GL_ONE_MINUS_SRC_ALPHA;
    }

    @Override
    public int GL_ONE() {
        return GL33.GL_ONE;
    }

    @Override
    public int GL_ZERO() {
        return GL33.GL_ZERO;
    }

    @Override
    public int GL_FUNC_ADD() {
        return GL33.GL_FUNC_ADD;
    }

    // 6. Очистка
    @Override
    public int GL_COLOR_BUFFER_BIT() {
        return GL33.GL_COLOR_BUFFER_BIT;
    }

    @Override
    public int GL_DEPTH_BUFFER_BIT() {
        return GL33.GL_DEPTH_BUFFER_BIT;
    }

    @Override
    public int GL_STENCIL_BUFFER_BIT() {
        return GL33.GL_STENCIL_BUFFER_BIT;
    }

    // 7. Форматы
    @Override
    public int GL_FLOAT_VEC2() {
        return GL33.GL_FLOAT_VEC2;
    }

    @Override
    public int GL_FLOAT_VEC3() {
        return GL33.GL_FLOAT_VEC3;
    }

    @Override
    public int GL_FLOAT_VEC4() {
        return GL33.GL_FLOAT_VEC4;
    }

    @Override
    public int GL_FLOAT_MAT3() {
        return GL33.GL_FLOAT_MAT3;
    }

    @Override
    public int GL_FLOAT_MAT4() {
        return GL33.GL_FLOAT_MAT4;
    }

    @Override
    public int GL_INT() {
        return GL33.GL_INT;
    }

    @Override
    public int GL_BOOL() {
        return GL33.GL_BOOL;
    }

    // 8. glGet
    @Override
    public int GL_MAX_TEXTURE_SIZE() {
        return GL33.GL_MAX_TEXTURE_SIZE;
    }

    @Override
    public int GL_MAX_VERTEX_ATTRIBS() {
        return GL33.GL_MAX_VERTEX_ATTRIBS;
    }

    @Override
    public int GL_MAX_VERTEX_UNIFORM_COMPONENTS() {
        return GL33.GL_MAX_VERTEX_UNIFORM_COMPONENTS;
    }

    @Override
    public int GL_MAX_FRAGMENT_UNIFORM_COMPONENTS() {
        return GL33.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS;
    }

    @Override
    public int GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS() {
        return GL33.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS;
    }

    @Override
    public int GL_MAX_TEXTURE_IMAGE_UNITS() {
        return GL33.GL_MAX_TEXTURE_IMAGE_UNITS;
    }

    @Override
    public int GL_MAX_RENDERBUFFER_SIZE() {
        return GL33.GL_MAX_RENDERBUFFER_SIZE;
    }

    @Override
    public int GL_MAX_VIEWPORT_DIMS() {
        return GL33.GL_MAX_VIEWPORT_DIMS;
    }

    @Override
    public int GL_MAX_CUBE_MAP_TEXTURE_SIZE() {
        return GL33.GL_MAX_CUBE_MAP_TEXTURE_SIZE;
    }

    @Override
    public int GL_MAX_VARYING_COMPONENTS() {
        return GL33.GL_MAX_VARYING_COMPONENTS;
    }

    // 9. Строки
    @Override
    public int GL_VENDOR() {
        return GL33.GL_VENDOR;
    }

    @Override
    public int GL_RENDERER() {
        return GL33.GL_RENDERER;
    }

    @Override
    public int GL_VERSION() {
        return GL33.GL_VERSION;
    }

    @Override
    public int GL_SHADING_LANGUAGE_VERSION() {
        return GL33.GL_SHADING_LANGUAGE_VERSION;
    }

    @Override
    public int GL_EXTENSIONS() {
        return GL33.GL_EXTENSIONS;
    }

    @Override
    public int GL_RASTERIZER_DISCARD() {
        return GL33.GL_RASTERIZER_DISCARD;
    }

    @Override
    public int GL_DITHER() {
        return GL33.GL_DITHER;
    }

    @Override
    public int GL_POLYGON_OFFSET_FILL() {
        return GL33.GL_POLYGON_OFFSET_FILL;
    }

    @Override
    public int GL_MIRRORED_REPEAT() {
        return GL33.GL_MIRRORED_REPEAT;
    }

    @Override
    public int GL_CLAMP_TO_EDGE() {
        return GL33.GL_CLAMP_TO_EDGE;
    }

    @Override
    public int GL_TEXTURE_WRAP_R() {
        return GL33.GL_TEXTURE_WRAP_R;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_NEGATIVE_X() {
        return GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_X;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_POSITIVE_X() {
        return GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_NEGATIVE_Y() {
        return GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_POSITIVE_Y() {
        return GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Y;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_NEGATIVE_Z() {
        return GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z;
    }

    @Override
    public int GL_TEXTURE_CUBE_MAP_POSITIVE_Z() {
        return GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Z;
    }

    @Override
    public int GL_FRAMEBUFFER() {
        return GL33.GL_FRAMEBUFFER;
    }

    @Override
    public int GL_RENDERBUFFER() {
        return GL33.GL_RENDERBUFFER;
    }

    @Override
    public int GL_COLOR_ATTACHMENT0() {
        return GL33.GL_COLOR_ATTACHMENT0;
    }

    @Override
    public int GL_DEPTH_ATTACHMENT() {
        return GL33.GL_DEPTH_ATTACHMENT;
    }

    @Override
    public int GL_DEPTH_COMPONENT16() {
        return GL33.GL_DEPTH_COMPONENT16;
    }

    @Override
    public int GL_RGBA16F() {
        return GL33.GL_RGBA16F;
    }

    @Override
    public int GL_SRGB() {
        return GL33.GL_SRGB;
    }

}
