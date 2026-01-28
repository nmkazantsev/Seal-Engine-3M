package com.nikitos.main;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An abstract class for all objects, using video memory
 */
public abstract class VRAMobject {
    private final Class<?> creator;
    private static final List<VRAMobject> allObjects = new ArrayList<>();//links to all objects

    public VRAMobject(GamePageClass creator) {
        if (creator != null) {
            this.creator = creator.getClass();
        } else {
            this.creator = null;
        }
        allObjects.add(this);
    }

    public abstract void delete();

    public abstract void reload();

    public static void onPageChange() {
        Iterator<VRAMobject> iterator = allObjects.iterator();
        while (iterator.hasNext()) {
            VRAMobject obj = iterator.next();
            if (!(obj.creator == CoreRenderer.engine.getPageClass()) && !(obj.creator == null)) {
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
}
