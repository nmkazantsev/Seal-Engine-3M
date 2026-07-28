package com.nikitos.main.keyboard;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;

import java.util.HashSet;
import java.util.function.Function;

import static com.nikitos.main.keyboard.KeyboardProcessor.normalizeKeyName;

/**
 * Key released listener.
 *
 * <p>Constructors:
 * <ul>
 *     <li>{@link #KeyReleasedListener(String, Function, GamePageClass)} - bind to one key</li>
 *     <li>{@link #KeyReleasedListener(String[], Function, GamePageClass)} - bind to several keys (any of them)</li>
 * </ul>
 *
 * <p>Any-key option: {@link #anyKey(Function, GamePageClass)}</p>
 */
public class KeyReleasedListener {
    // Токен отличает экземпляры одной страницы и не даёт слушателю пережить переход.
    private final Object ownershipToken;
    private final HashSet<String> keys = new HashSet<>();
    private boolean anyKey = false;

    private Function<String, Void> keyReleasedCallback;
    private boolean blocked = false;

    public KeyReleasedListener(String key, Function<String, Void> keyReleasedCallback, GamePageClass creatorPage) {
        this.ownershipToken = creatorPage == null ? null : creatorPage.getResourceOwnershipToken();
        this.keyReleasedCallback = keyReleasedCallback;
        String n = normalizeKeyName(key);
        if (n != null) {
            keys.add(n);
        }
        KeyboardProcessor.register(this);
    }

    public KeyReleasedListener(String[] keys, Function<String, Void> keyReleasedCallback, GamePageClass creatorPage) {
        this.ownershipToken = creatorPage == null ? null : creatorPage.getResourceOwnershipToken();
        this.keyReleasedCallback = keyReleasedCallback;
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

    public static KeyReleasedListener anyKey(Function<String, Void> keyReleasedCallback, GamePageClass creatorPage) {
        KeyReleasedListener l = new KeyReleasedListener(new String[0], keyReleasedCallback, creatorPage);
        l.anyKey = true;
        return l;
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

    boolean isActiveForCurrentPage() {
        if (ownershipToken == null) return true;
        return ownershipToken == CoreRenderer.engine.getCurrentPageOwnershipToken();
    }

    Function<String, Void> getKeyReleasedCallback() {
        return keyReleasedCallback;
    }

    Object getOwnershipToken() {
        return ownershipToken;
    }
}
