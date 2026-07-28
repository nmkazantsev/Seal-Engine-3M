package com.nikitos.main.keyboard;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;

import java.util.*;
import java.util.function.Function;

import static com.nikitos.main.keyboard.KeyboardProcessor.normalizeKeyName;

/**
 * Key pressed listener.
 *
 * <p>Constructors:
 * <ul>
 *     <li>{@link #KeyListener(String, Function, GamePageClass)} - bind to one key</li>
 *     <li>{@link #KeyListener(String[], Function, GamePageClass)} - bind to several keys (any of them)</li>
 * </ul>
 *
 * <p>Optional hold callback: {@link #setHoldListener(long, Function)}</p>
 * <p>Any-key option: {@link #anyKey(Function, GamePageClass)}</p>
 */
public class KeyListener {
    // Токен отличает экземпляры одной страницы и не даёт слушателю пережить переход.
    private final Object ownershipToken;
    private final HashSet<String> keys = new HashSet<>();
    private boolean anyKey = false;

    private Function<String, Void> keyPressedCallback;

    private Function<String, Void> keyHoldCallback;
    private long holdTimeoutMs = -1;
    private final HashSet<String> holdFired = new HashSet<>();

    private boolean blocked = false;

    public KeyListener(String key, Function<String, Void> keyPressedCallback, GamePageClass creatorPage) {
        this.ownershipToken = creatorPage == null ? null : creatorPage.getResourceOwnershipToken();
        this.keyPressedCallback = keyPressedCallback;
        String n = normalizeKeyName(key);
        if (n != null) {
            keys.add(n);
        }
        KeyboardProcessor.register(this);
    }

    public KeyListener(String[] keys, Function<String, Void> keyPressedCallback, GamePageClass creatorPage) {
        this.ownershipToken = creatorPage == null ? null : creatorPage.getResourceOwnershipToken();
        this.keyPressedCallback = keyPressedCallback;
        if (keys != null) {
            for (String k : keys) {
                String n = normalizeKeyName(k);
                if (n != null) {
                    this.keys.add(n);
                }
            }
        }
        KeyboardProcessor.register(this);
    }

    public static KeyListener anyKey(Function<String, Void> keyPressedCallback, GamePageClass creatorPage) {
        KeyListener l = new KeyListener(new String[0], keyPressedCallback, creatorPage);
        l.anyKey = true;
        return l;
    }

    public KeyListener setHoldListener(long timeoutMs, Function<String, Void> keyHoldCallback) {
        this.holdTimeoutMs = timeoutMs;
        this.keyHoldCallback = keyHoldCallback;
        return this;
    }

    public void block() {
        blocked = true;
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

    boolean matches(String normalizedKeyName) {
        if (anyKey) return true;
        return keys.contains(normalizedKeyName);
    }

    void onKeyReleasedInternal(String normalizedKeyName) {
        holdFired.remove(normalizedKeyName);
    }

    void processHold(long nowMillis, Map<String, Long> pressedKeys) {
        if (keyHoldCallback == null) return;
        if (holdTimeoutMs < 0) return;
        if (pressedKeys.isEmpty()) return;

        if (anyKey) {
            for (Map.Entry<String, Long> e : pressedKeys.entrySet()) {
                String key = e.getKey();
                long start = e.getValue();
                if (nowMillis - start >= holdTimeoutMs && !holdFired.contains(key)) {
                    holdFired.add(key);
                    keyHoldCallback.apply(key);
                }
            }
            return;
        }

        for (String key : keys) {
            Long start = pressedKeys.get(key);
            if (start == null) continue;
            if (nowMillis - start >= holdTimeoutMs && !holdFired.contains(key)) {
                holdFired.add(key);
                keyHoldCallback.apply(key);
            }
        }
    }

    boolean isActiveForCurrentPage() {
        if (ownershipToken == null) return true;
        return ownershipToken == CoreRenderer.engine.getCurrentPageOwnershipToken();
    }

    Function<String, Void> getKeyPressedCallback() {
        return keyPressedCallback;
    }

    Object getOwnershipToken() {
        return ownershipToken;
    }
}
