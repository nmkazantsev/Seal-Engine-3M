package com.nikitos.main.shaders;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.PageOwnership;
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
    private final Object ownershipToken;
    private boolean reloadNeeded = false;
    private final Adaptor adaptor;
    private static Shader activeShader;

    private final ShaderBridge shaderBridge;
    private final ShaderUtils shaderUtils;

    public Shader(String vertex, String fragment, GamePageClass page, Adaptor adaptor) {
        shaderUtils = new ShaderUtils();
        PlatformBridge platformBridge = CoreRenderer.engine.getPlatformBridge();
        SealAssetManager assetManager = platformBridge.getAssetManager();
        this.vertex = assetManager.loadText(vertex);
        this.fragment = assetManager.loadText(fragment);
        link = shaderUtils.createShaderProgram(this.vertex, this.fragment);
        if (page != null) {
            this.page = page.getClass();
        }
        ownershipToken = PageOwnership.tokenOf(page);
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
        shaderBridge = CoreRenderer.engine.getPlatformBridge().getShaderBridge();
    }

    public Shader(String vertex, String fragment, String geom, GamePageClass page, Adaptor adaptor) {
        shaderUtils = new ShaderUtils();
        PlatformBridge platformBridge = CoreRenderer.engine.getPlatformBridge();
        SealAssetManager assetManager = platformBridge.getAssetManager();
        this.vertex = assetManager.loadText(vertex);
        this.fragment = assetManager.loadText(fragment);
        this.geom = assetManager.loadText(geom);
        link = shaderUtils.createShaderProgram(this.vertex, this.fragment, this.geom);
        if (page != null) {
            this.page = page.getClass();
        }
        ownershipToken = PageOwnership.tokenOf(page);
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
        shaderBridge = CoreRenderer.engine.getPlatformBridge().getShaderBridge();
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
        if (ownershipToken == null) {
            return false;
        }
        if (ownershipToken != PageOwnership.currentToken()) {
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

    public static int getTrackedShaderCount() {
        return allShaders.size();
    }
}
