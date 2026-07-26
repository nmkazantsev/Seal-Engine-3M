package com.nikitos.main.keyboard;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.PageOwnership;

import java.util.*;
import java.util.function.Function;

import static com.nikitos.main.keyboard.KeyboardProcessor.normalizeKeyName;

/**
 * A listener whose callback is called when all specified keys are pressed together (in any order).
 * The callback is called once per activation, until any key from the combo is released.
 */
public class KeyComboListener {
    private final Class<?> creatorClassName;
    private final Object ownershipToken;
    private final ArrayList<String> keysSorted;
    private final HashSet<String> keysSet;
    private final String comboName;

    private final Function<String, Void> comboPressedCallback;
    private boolean active = false;
    private boolean blocked = false;

    public KeyComboListener(String[] keys, Function<String, Void> comboPressedCallback, GamePageClass creatorPage) {
        this.creatorClassName = creatorPage == null ? null : creatorPage.getClass();
        this.ownershipToken = PageOwnership.tokenOf(creatorPage);
        this.comboPressedCallback = comboPressedCallback;

        ArrayList<String> normalized = new ArrayList<>();
        if (keys != null) {
            for (String k : keys) {
                String n = normalizeKeyName(k);
                if (n != null) {
                    normalized.add(n);
                }
            }
        }
        normalized.sort(String::compareTo);
        this.keysSorted = normalized;
        this.keysSet = new HashSet<>(normalized);
        this.comboName = String.join("+", this.keysSorted);

        KeyboardProcessor.register(this);
    }

    public KeyComboListener(String key1, String key2, Function<String, Void> comboPressedCallback, GamePageClass creatorPage) {
        this(new String[]{key1, key2}, comboPressedCallback, creatorPage);
    }

    public void block() {
        blocked = true;
        active = false;
    }

    public void unblock() {
        blocked = false;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void delete() {
        KeyboardProcessor.unregister(this);
    }

    String getComboName() {
        return comboName;
    }

    boolean isActiveForCurrentPage() {
        if (ownershipToken == null) return true;
        if (CoreRenderer.engine == null) return true;
        try {
            return ownershipToken == PageOwnership.currentToken();
        } catch (Throwable ignored) {
            return true;
        }
    }

    boolean isComboPressed(Set<String> pressedKeys) {
        if (keysSet.isEmpty()) return false;
        return pressedKeys.containsAll(keysSet);
    }

    void updateActive(Set<String> pressedKeys) {
        if (!isComboPressed(pressedKeys)) {
            active = false;
        }
    }

    boolean shouldInvokeCallbackNow(Set<String> pressedKeys) {
        return !active && isComboPressed(pressedKeys);
    }

    void markActive() {
        active = true;
    }

    Function<String, Void> getComboPressedCallback() {
        return comboPressedCallback;
    }

    Class<?> getCreatorClassName() {
        return creatorClassName;
    }

    Object getOwnershipToken() {
        return ownershipToken;
    }
}
