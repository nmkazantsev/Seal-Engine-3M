package com.nikitos.main;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.PageOwnership;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class for all objects, using video memory
 */
public abstract class VRAMobject {
    protected final GeneralPlatformBridge gl;
    protected final GLConstBridge glc;
    protected final Class<?> creator;
    protected final GamePageClass gamePageClass;
    protected final Object ownershipToken;
    private static final List<VRAMobject> allObjects = new ArrayList<>();//links to all objects

    public VRAMobject(GamePageClass creator) {
        this(creator, true);
    }

    /**
     * Creates a GPU resource whose lifecycle may be owned by another tracked
     * resource.
     */
    protected VRAMobject(GamePageClass creator, boolean registerForLifecycle) {
        gl = CoreRenderer.engine.getPlatformBridge().getGeneralPlatformBridge();
        glc = CoreRenderer.engine.getPlatformBridge().getGLConstBridge();
        gamePageClass = creator;
        ownershipToken = PageOwnership.tokenOf(creator);
        if (creator != null) {
            this.creator = creator.getClass();
        } else {
            this.creator = null;
        }
        if (registerForLifecycle) {
            allObjects.add(this);
        }
    }

    public abstract void delete();

    public abstract void reload();

    public static void onPageChange() {
        Object currentOwnershipToken = PageOwnership.currentToken();
        Iterator<VRAMobject> iterator = allObjects.iterator();
        while (iterator.hasNext()) {
            VRAMobject obj = iterator.next();
            if (obj.ownershipToken != null && obj.ownershipToken != currentOwnershipToken) {
                obj.delete();
                iterator.remove();
            }
        }
    }

    public static void onRedraw() {
        for (VRAMobject e : allObjects) {
            e.reload();
        }
    }

    public static int getTrackedObjectCount() {
        return allObjects.size();
    }
}
