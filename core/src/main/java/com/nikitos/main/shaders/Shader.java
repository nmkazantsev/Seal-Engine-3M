package com.nikitos.main.shaders;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.platformBridge.Platform;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.platformBridge.SealAssetManager;
import com.nikitos.platformBridge.ShaderBridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Shader { //means shader program
    private static final List<Shader> allShaders = new ArrayList<>();
    private int link;
    private final String vertex;
    private final String fragment;
    private String geom = null;
    private Class<?> page;
    private boolean reloadNeeded = false;
    private final Adaptor adaptor;
    private static Shader activeShader;

    private final ShaderBridge shaderBridge;
    private final ShaderUtils shaderUtils;

    public Shader(String vertex, String fragment, GamePageClass page, Adaptor adaptor) {
        shaderUtils = new ShaderUtils();
        PlatformBridge platformBridge = CoreRenderer.engine.getPlatformBridge();
        SealAssetManager assetManager = platformBridge.getAssetManager();
        Platform platform = platformBridge.getPlatform();
        this.vertex = assetManager.loadText(resolveAssetPath(vertex, platform));
        this.fragment = assetManager.loadText(resolveAssetPath(fragment, platform));
        link = shaderUtils.createShaderProgram(this.vertex, this.fragment);
        if (page != null) {
            this.page = page.getClass();
        }
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
        shaderBridge = CoreRenderer.engine.getPlatformBridge().getShaderBridge();
    }

    public Shader(String vertex, String fragment, String geom, GamePageClass page, Adaptor adaptor) {
        shaderUtils = new ShaderUtils();
        PlatformBridge platformBridge = CoreRenderer.engine.getPlatformBridge();
        SealAssetManager assetManager = platformBridge.getAssetManager();
        Platform platform = platformBridge.getPlatform();
        this.vertex = assetManager.loadText(resolveAssetPath(vertex, platform));
        this.fragment = assetManager.loadText(resolveAssetPath(fragment, platform));
        this.geom = assetManager.loadText(resolveAssetPath(geom, platform));
        link = shaderUtils.createShaderProgram(this.vertex, this.fragment, this.geom);
        if (page != null) {
            this.page = page.getClass();
        }
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
        shaderBridge = CoreRenderer.engine.getPlatformBridge().getShaderBridge();
    }

    static String resolveAssetPath(String logicalPath, Platform platform) {
        if (logicalPath == null || platform == null || logicalPath.isBlank()
                || !logicalPath.equals(logicalPath.trim())
                || logicalPath.startsWith("/") || logicalPath.indexOf('\\') >= 0
                || logicalPath.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Invalid logical shader path: " + logicalPath);
        }
        String[] segments = logicalPath.split("/", -1);
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IllegalArgumentException("Invalid logical shader path: " + logicalPath);
            }
        }
        if (segments[0].equals("shaders") || segments[0].equals("android") || segments[0].equals("desktop")) {
            throw new IllegalArgumentException("Shader path must not contain a platform prefix: " + logicalPath);
        }
        return "shaders/" + (platform == Platform.DESKTOP ? "desktop/" : "android/") + logicalPath;
    }

    private void reload() {
        //this.delete();
        if (geom == null) {
            link = shaderUtils.createShaderProgram(vertex, fragment);
        } else {
            link = shaderUtils.createShaderProgram(vertex, fragment, geom);
        }
    }

    public static void updateAllLocations() {
        ShaderUtils.prevProgramId = -1;
        for (Shader allShader : allShaders) {
            if (allShader != null) {
                allShader.reloadNeeded = true;
            }
        }
    }

    private boolean unneeded() {
        if (this.page == null) {
            return false;
        }
        if (!(this.page == CoreRenderer.engine.getPageClass())) {
            this.delete();
            return true;
        }
        return false;
    }

    public void delete() {
        shaderBridge.deleteProgram(link);
    }

    public void apply() {
        applyShader(this);
    }

    public void applyShader(Shader s) {
        if (s.reloadNeeded) {
            s.reload();
            s.reloadNeeded = false;
        }
        shaderUtils.applyShader(s.link);
        activeShader = s;
        s.adaptor.programId = s.link;

        s.adaptor.updateLocations();
        Adaptor.updateShaderDataLocations();
        Adaptor.forwardData();

    }


    public static void onPageChange() {
        Iterator<Shader> iterator = allShaders.iterator();
        while (iterator.hasNext()) {
            Shader e = iterator.next();
            if (e == null) {
                iterator.remove();
            } else if (e.unneeded()) {
                e.delete();
                iterator.remove();
            }
        }
    }

    public Adaptor getAdaptor() {
        return adaptor;
    }

    public static Shader getActiveShader() {
        return activeShader;
    }
}
