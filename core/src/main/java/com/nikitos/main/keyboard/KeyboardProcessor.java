package com.nikitos.main.keyboard;

import com.nikitos.CoreRenderer;
import com.nikitos.main.debugger.BSODScreen;

import java.util.*;
import java.util.function.Function;

import static com.nikitos.utils.Utils.millis;

/**
 * Keyboard input dispatcher.
 * <p>
 * Platform code must forward key events here. Callbacks are buffered and executed later from the render thread
 * via {@link #processKeys()} (similar to {@code TouchProcessor}).
 */
public class KeyboardProcessor {
    private static final List<KeyListener> allKeyListeners = new ArrayList<>();
    private static final List<KeyReleasedListener> allKeyReleasedListeners = new ArrayList<>();
    private static final List<KeyComboListener> allKeyComboListeners = new ArrayList<>();
    private static final List<Command> commandQueue = new ArrayList<>();

    // key -> press start time (millis)
    private static final HashMap<String, Long> pressedKeys = new HashMap<>();
    private static boolean pageChanged = false;

    static void register(KeyListener listener) {
        synchronized (commandQueue) {
            allKeyListeners.add(listener);
        }
    }

    static void unregister(KeyListener listener) {
        synchronized (commandQueue) {
            allKeyListeners.remove(listener);
        }
    }

    static void register(KeyReleasedListener listener) {
        synchronized (commandQueue) {
            allKeyReleasedListeners.add(listener);
        }
    }

    static void unregister(KeyReleasedListener listener) {
        synchronized (commandQueue) {
            allKeyReleasedListeners.remove(listener);
        }
    }

    static void register(KeyComboListener listener) {
        synchronized (commandQueue) {
            allKeyComboListeners.add(listener);
        }
    }

    static void unregister(KeyComboListener listener) {
        synchronized (commandQueue) {
            allKeyComboListeners.remove(listener);
        }
    }

    public static void onKeyPressed(String rawKeyName) {
        String keyName = normalizeKeyName(rawKeyName);
        if (keyName == null) return;

        synchronized (commandQueue) {
            if (!pressedKeys.containsKey(keyName)) {
                pressedKeys.put(keyName, millis());
            }

            for (KeyListener listener : allKeyListeners) {
                if (!listener.isActiveForCurrentPage()) continue;
                if (listener.isBlocked()) continue;
                if (!listener.matches(keyName)) continue;

                Function<String, Void> callback = listener.getKeyPressedCallback();
                if (callback != null) {
                    commandQueue.add(new Command(keyName, callback, listener));
                }
            }

            if (!allKeyComboListeners.isEmpty()) {
                HashSet<String> pressedNow = new HashSet<>(pressedKeys.keySet());
                for (KeyComboListener listener : allKeyComboListeners) {
                    if (!listener.isActiveForCurrentPage()) continue;
                    if (listener.isBlocked()) continue;
                    listener.updateActive(pressedNow);
                    if (!listener.shouldInvokeCallbackNow(pressedNow)) continue;
                    Function<String, Void> callback = listener.getComboPressedCallback();
                    if (callback != null) {
                        listener.markActive();
                        commandQueue.add(new Command(listener.getComboName(), callback, listener));
                    }
                }
            }
        }
    }

    public static void onKeyReleased(String rawKeyName) {
        String keyName = normalizeKeyName(rawKeyName);
        if (keyName == null) return;

        synchronized (commandQueue) {
            pressedKeys.remove(keyName);
            for (KeyListener listener : allKeyListeners) {
                listener.onKeyReleasedInternal(keyName);
            }

            if (!allKeyComboListeners.isEmpty()) {
                HashSet<String> pressedNow = new HashSet<>(pressedKeys.keySet());
                for (KeyComboListener listener : allKeyComboListeners) {
                    listener.updateActive(pressedNow);
                }
            }

            for (KeyReleasedListener listener : allKeyReleasedListeners) {
                if (!listener.isActiveForCurrentPage()) continue;
                if (listener.isBlocked()) continue;
                if (!listener.matches(keyName)) continue;

                Function<String, Void> callback = listener.getKeyReleasedCallback();
                if (callback != null) {
                    commandQueue.add(new Command(keyName, callback, listener));
                }
            }
        }
    }

    /**
     * Must be called from the render thread once per frame.
     */
    public static void processKeys() {
        synchronized (commandQueue) {
            Iterator<Command> iterator = commandQueue.iterator();
            while (iterator.hasNext()) {
                Command command = iterator.next();
                if (pageChanged) {
                    iterator.remove();
                    continue;
                }
                if (command.parent instanceof KeyListener) {
                    if (!((KeyListener) command.parent).isBlocked()) {
                        command.run();
                    }
                } else if (command.parent instanceof KeyReleasedListener) {
                    if (!((KeyReleasedListener) command.parent).isBlocked()) {
                        command.run();
                    }
                } else {
                    command.run();
                }
                iterator.remove();
            }

            if (!pageChanged) {
                long now = millis();
                for (KeyListener listener : allKeyListeners) {
                    if (!listener.isActiveForCurrentPage()) continue;
                    if (listener.isBlocked()) continue;
                    listener.processHold(now, pressedKeys);
                }
            }

            pageChanged = false;
        }
    }

    public static boolean isKeyPressed(String rawKeyName) {
        String keyName = normalizeKeyName(rawKeyName);
        if (keyName == null) return false;
        synchronized (commandQueue) {
            return pressedKeys.containsKey(keyName);
        }
    }

    /**
     * @return how many keys are pressed at this moment.
     */
    public static int getKeysPressedNumber() {
        synchronized (commandQueue) {
            return pressedKeys.size();
        }
    }

    /**
     * @return list of pressed key names at this moment (normalized, uppercase).
     */
    public static List<String> getKeyPresedList() {
        synchronized (commandQueue) {
            ArrayList<String> out = new ArrayList<>(pressedKeys.keySet());
            Collections.sort(out);
            return out;
        }
    }

    public static void onPageChange() {
        synchronized (commandQueue) {
            pressedKeys.clear();
            pageChanged = true;
            if (CoreRenderer.engine == null) {
                return;
            }
            // Удаляем слушатели ушедшего экземпляра, даже если класс страницы не изменился.
            Object currentPage = CoreRenderer.engine.getCurrentPageOwnershipToken();
            allKeyListeners.removeIf(e -> e.getOwnershipToken() != null && e.getOwnershipToken() != currentPage);
            allKeyReleasedListeners.removeIf(e -> e.getOwnershipToken() != null && e.getOwnershipToken() != currentPage);
            allKeyComboListeners.removeIf(e -> e.getOwnershipToken() != null && e.getOwnershipToken() != currentPage);
        }
    }

    static String normalizeKeyName(String keyName) {
        if (keyName == null) return null;
        String t = keyName.trim();
        if (t.isEmpty()) return null;
        return t.toUpperCase(Locale.ROOT);
    }

    private static class Command {
        private final String keyName;
        private final Function<String, Void> function;
        private final Object parent;

        private Command(String keyName, Function<String, Void> function, Object parent) {
            this.keyName = keyName;
            this.function = function;
            this.parent = parent;
        }

        private void run() {
            if(CoreRenderer.engine.getBsodAllowed()){
                try {
                    function.apply(keyName);
                }catch (Exception e){
                    CoreRenderer.engine.startNewPage(new BSODScreen(e));
                }
            }else {
                function.apply(keyName);
            }
        }
    }
}
