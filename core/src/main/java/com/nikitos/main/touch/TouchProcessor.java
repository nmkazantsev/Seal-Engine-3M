package com.nikitos.main.touch;


import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.debugger.BSODScreen;
import com.nikitos.main.debugger.Debugger;

import java.util.*;
import java.util.function.Function;

import static com.nikitos.utils.Utils.millis;

/**
 * A class, created for tracking touch.
 * On every new ouch it's object is being bind (in accordance with defined hitbox) to some touch and object's callbacks will be called for certain touch.
 * All touch events are buffered and then processed in main thread, for it to be possible to call opengl functions
 */
public class TouchProcessor {
    private static final HashMap<Integer, TouchProcessor> activeProcessors = new HashMap<>();
    private static final List<TouchProcessor> allProcessors = new ArrayList<>();
    private static final List<BufferedCommand> commandQueue = new ArrayList<>();
    private static final HashMap<Class<?>, Function<MousePoint, Void>> leftButtonProcessors = new HashMap<>();
    private static final HashMap<Class<?>, Function<MousePoint, Void>> rightButtonProcessors = new HashMap<>();
    private static final HashMap<Class<?>, Function<MousePoint, Void>> mouseMovedProcessors = new HashMap<>();
    private static final HashMap<Class<?>, Function<MouseWheelData, Void>> mouseWheelProcessors = new HashMap<>();
    private static boolean pageChanged = false;
    private final Class<?> creatorClassName;
    private final Function<TouchPoint, Boolean> checkHitboxCallback;
    private final Function<TouchPoint, Void> touchStartedCallback;
    private final Function<TouchPoint, Void> touchMovedCallback;
    private final Function<TouchPoint, Void> touchEndedCallback; //here the last known touch point
    public TouchPoint lastTouchPoint = null;
    private Integer touchId = -1;
    private boolean touchAlive = false;
    private boolean touchEndProcessed = false;
    private boolean blocked = false;
    private long startTime;

    private int priority = 0;
    private static boolean resSortNeeeded = false;

    /**
     * Create a new TouchProcessor
     *
     * @param checkHitboxCallback  called every time new touch starts. Must return true of this touch should init tracking for this object; else return false.
     * @param touchStartedCallback called when this object captures a new touch.
     * @param touchMovedCallback   called when captured touch moves.
     * @param touchEndedCallback   called when captured touch ends.
     * @param creatorPage          not null creator page object.
     */
    public TouchProcessor(Function<TouchPoint, Boolean> checkHitboxCallback,
                          Function<TouchPoint, Void> touchStartedCallback,
                          Function<TouchPoint, Void> touchMovedCallback,

                          Function<TouchPoint, Void> touchEndedCallback, GamePageClass creatorPage) {
        if (creatorPage != null) {
            this.creatorClassName = creatorPage.getClass();
        } else {
            this.creatorClassName = null;
        }
        this.checkHitboxCallback = checkHitboxCallback;
        this.touchStartedCallback = touchStartedCallback;
        this.touchMovedCallback = touchMovedCallback;
        this.touchEndedCallback = touchEndedCallback;
        allProcessors.add(this);
        resSortNeeeded = true;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        resSortNeeeded = true;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * blocks this processor as if it was not created
     */
    public void block() {
        this.blocked = true;
        this.terminate();
    }

    /**
     * resumes touch capturing.
     */
    public void unblock() {
        this.blocked = false;
    }

    public long getDuration() {
        if (!getTouchAlive()) {
            return -1;
        }
        return millis() - startTime;
    }

    /**
     * A function that stops tracking current touch. May be called any time you wish.
     * Also called when touch ends.
     */
    public void terminate() {
        //the same as usual terminate(event), but call callback ot once, because we are already in main thread and editing command queue will crash the app
        touchAlive = false;
        touchEndProcessed = true;
        activeProcessors.remove(touchId);
        touchId = -1;
        if (touchEndedCallback != null) {
            touchEndedCallback.apply(null);
        }
    }

    private void terminate(MyMotionEvent event) {
        touchAlive = false;
        touchEndProcessed = false;
        activeProcessors.remove(touchId);
        touchId = -1;
        if (touchEndedCallback != null) {
            TouchCommand c = new TouchCommand(lastTouchPoint, touchEndedCallback, this);
            c.isTouchEnded = true;
            commandQueue.add(c);
        }
    }

    private boolean checkHitbox(TouchPoint event) {
        return checkHitboxCallback.apply(event);
    }

    /**
     * Get if touch is pressed at the moment of calling this.
     *
     * @return true if finger is pressing, false if finger is released and this touch object is ready to capture new touch.
     */
    public boolean getTouchAlive() {
        return touchAlive;
    }

    //**********STATIC METHODS********************
    public static void onTouch(MyMotionEvent event) {
        synchronized (commandQueue) {
            TouchProcessor t = activeProcessors.getOrDefault(event.getPointerId(event.getActionIndex()), null);
            if (t != null && !t.blocked) {
                if (t.creatorClassName == CoreRenderer.engine.getPageClass() || t.creatorClassName == null) {
                    if (event.getActionMasked() == MyMotionEvent.ACTION_MOVE) {
                        touchMoved(event);
                    }
                    if (event.getActionMasked() == MyMotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MyMotionEvent.ACTION_UP) {
                        touchEnded(event);
                    }
                }
            } else if (event.getActionMasked() == MyMotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MyMotionEvent.ACTION_DOWN) {
                touchStarted(event);
            }
        }
    }

    public static void setLeftButtonProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage) {
        synchronized (commandQueue) {
            setProcessor(leftButtonProcessors, processor, creatorPage);
        }
    }

    public static void setRightButtonProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage) {
        synchronized (commandQueue) {
            setProcessor(rightButtonProcessors, processor, creatorPage);
        }
    }

    public static void setMouseMovedProcessor(Function<MousePoint, Void> processor, GamePageClass creatorPage) {
        synchronized (commandQueue) {
            setProcessor(mouseMovedProcessors, processor, creatorPage);
        }
    }

    public static void setMouseWheelProcessor(Function<MouseWheelData, Void> processor, GamePageClass creatorPage) {
        synchronized (commandQueue) {
            setProcessor(mouseWheelProcessors, processor, creatorPage);
        }
    }

    public static void onLeftButtonPressed(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            Function<MousePoint, Void> processor = getProcessorForCurrentPage(leftButtonProcessors);
            if (processor != null) {
                commandQueue.add(new MousePointCommand(new MousePoint(mouseX, mouseY), processor));
            }
        }
    }

    public static void onRightButtonPressed(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            Function<MousePoint, Void> processor = getProcessorForCurrentPage(rightButtonProcessors);
            if (processor != null) {
                commandQueue.add(new MousePointCommand(new MousePoint(mouseX, mouseY), processor));
            }
        }
    }

    public static void onMouseMoved(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            Function<MousePoint, Void> processor = getProcessorForCurrentPage(mouseMovedProcessors);
            if (processor != null) {
                commandQueue.add(new MousePointCommand(new MousePoint(mouseX, mouseY), processor));
            }
        }
    }

    public static void onMouseWheel(float mouseX, float mouseY, float wheelX, float wheelY) {
        synchronized (commandQueue) {
            Function<MouseWheelData, Void> processor = getProcessorForCurrentPage(mouseWheelProcessors);
            if (processor != null) {
                commandQueue.add(new MouseWheelCommand(new MouseWheelData(mouseX, mouseY, wheelX, wheelY), processor));
            }
        }
    }

    public static void processMotions() {
        synchronized (commandQueue) {
            Iterator<BufferedCommand> iterator = commandQueue.iterator();
            while (iterator.hasNext()) {
                BufferedCommand command = iterator.next();
                //clean events if page changed
                if (pageChanged) {
                    iterator.remove(); //remove all without processing
                    continue;
                }
                if (command.shouldRun()) {
                    command.run();
                }
                iterator.remove();//no need in this event to be buffered anymore
            }
        }
        pageChanged = false;
    }

    public void delete() {
        if (!touchEndProcessed) {
            terminate();
        }
        allProcessors.remove(this);

    }

    private static void touchStarted(MyMotionEvent event) {
        if (resSortNeeeded) {
            resSortNeeeded = false;
            class PrioritySorter implements Comparator<TouchProcessor> {
                @Override
                public int compare(TouchProcessor a, TouchProcessor b) {
                    return b.priority - a.priority;
                }
            }
            allProcessors.sort(new PrioritySorter());
        }
        if (Debugger.getPage() == 0) { //do not process touches when debugger available
            for (TouchProcessor t : allProcessors) {
                if (t.checkHitbox(new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) && (t.creatorClassName == CoreRenderer.engine.getPageClass() || t.creatorClassName == null) && !t.touchAlive && !t.blocked) { //not to start the same processor twice if 2 touches in 1 area
                    activeProcessors.put(event.getPointerId(event.getActionIndex()), t);
                    t.lastTouchPoint = new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
                    t.touchAlive = true;
                    t.touchId = event.getPointerId(event.getActionIndex());
                    t.startTime = millis();
                    if (t.touchStartedCallback != null) {
                        commandQueue.add(new TouchCommand(t.lastTouchPoint, t.touchStartedCallback, t));
                        //t.touchStartedCallback.apply(t.lastTouchPoint);
                    }
                    return;
                }
            }
        } else {
            //process full screen debugger
            //touch moves will not be processed if starts are not processed here (blocked by debugger)
            TouchProcessor t = Debugger.getMainPageTouchProcessor();
            if (t.checkHitbox(new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) && (t.creatorClassName == CoreRenderer.engine.getPageClass() || t.creatorClassName == null) && !t.touchAlive && !t.blocked) { //not to start the same processor twice if 2 touches in 1 area
                activeProcessors.put(event.getPointerId(event.getActionIndex()), t);
                t.lastTouchPoint = new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
                t.touchAlive = true;
                t.touchId = event.getPointerId(event.getActionIndex());
                t.startTime = millis();
                if (t.touchStartedCallback != null) {
                    commandQueue.add(new TouchCommand(t.lastTouchPoint, t.touchStartedCallback, t));
                    //t.touchStartedCallback.apply(t.lastTouchPoint);
                }
            }
        }
    }

    private static void touchMoved(MyMotionEvent event) {
        /*
        No other ways here to get indexes of touch moved are not specified in docs.
        Process all moved touches here.
         */
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getActionMasked() == MyMotionEvent.ACTION_MOVE) {
                TouchProcessor t = activeProcessors.getOrDefault(event.getPointerId(i), null);
                if (t != null && t.touchAlive) {
                    t.lastTouchPoint = new TouchPoint(event.getX(i), event.getY(i));
                    if (t.touchMovedCallback != null) {
                        commandQueue.add(new TouchCommand(t.lastTouchPoint, t.touchMovedCallback, t));
                    }
                }
            }
        }
    }

    private static void touchEnded(MyMotionEvent event) {
        TouchProcessor t = activeProcessors.get(event.getPointerId(event.getActionIndex()));
        t.terminate(event);
    }

    public static void onPageChange() {
        //clearing only through iterator, else concurrent modification error
        synchronized (commandQueue) {
            activeProcessors.clear();
            pageChanged = true;
            //do not call terminate here not to call touch ended
            allProcessors.removeIf(e -> !(e.creatorClassName == CoreRenderer.engine.getPageClass()) && !(e.creatorClassName == null));
            removeInactivePageProcessors(leftButtonProcessors);
            removeInactivePageProcessors(rightButtonProcessors);
            removeInactivePageProcessors(mouseMovedProcessors);
            removeInactivePageProcessors(mouseWheelProcessors);
        }
    }

    //a class for queue of postponed (in nearest frame) callback (not all callbacks are allowed in touch thread, problems with openGL context)
    private interface BufferedCommand {
        boolean shouldRun();

        void run();
    }

    private static class TouchCommand implements BufferedCommand {
        private final TouchPoint touchPoint;
        private final Function<TouchPoint, Void> function;
        private final TouchProcessor parent;
        private boolean isTouchEnded = false;

        private TouchCommand(TouchPoint t, Function<TouchPoint, Void> function, TouchProcessor parent) {
            this.touchPoint = t;
            this.function = function;
            this.parent = parent;
        }

        @Override
        public boolean shouldRun() {
            return parent.touchAlive || (!parent.touchAlive && !parent.touchEndProcessed && isTouchEnded);
        }

        @Override
        public void run() {
            if (CoreRenderer.engine.getBsodAllowed()) {
                try {
                    function.apply(touchPoint);
                } catch (Exception e) {
                    CoreRenderer.engine.startNewPage(new BSODScreen(e));
                }
            } else {
                function.apply(touchPoint);
            }
        }
    }

    private static class MousePointCommand implements BufferedCommand {
        private final MousePoint mousePoint;
        private final Function<MousePoint, Void> function;

        private MousePointCommand(MousePoint mousePoint, Function<MousePoint, Void> function) {
            this.mousePoint = mousePoint;
            this.function = function;
        }

        @Override
        public boolean shouldRun() {
            return true;
        }

        @Override
        public void run() {
            if (CoreRenderer.engine.getBsodAllowed()) {
                try {
                    function.apply(mousePoint);
                } catch (Exception e) {
                    CoreRenderer.engine.startNewPage(new BSODScreen(e));
                }
            } else {
                function.apply(mousePoint);
            }
        }
    }

    private static class MouseWheelCommand implements BufferedCommand {
        private final MouseWheelData mouseWheelData;
        private final Function<MouseWheelData, Void> function;

        private MouseWheelCommand(MouseWheelData mouseWheelData, Function<MouseWheelData, Void> function) {
            this.mouseWheelData = mouseWheelData;
            this.function = function;
        }

        @Override
        public boolean shouldRun() {
            return true;
        }

        @Override
        public void run() {
            if (CoreRenderer.engine.getBsodAllowed()) {
                try {
                    function.apply(mouseWheelData);
                } catch (Exception e) {
                    CoreRenderer.engine.startNewPage(new BSODScreen(e));
                }
            } else {
                function.apply(mouseWheelData);
            }
        }
    }

    private static <T> void setProcessor(HashMap<Class<?>, Function<T, Void>> processors,
                                         Function<T, Void> processor,
                                         GamePageClass creatorPage) {
        Class<?> key = creatorPage == null ? null : creatorPage.getClass();
        if (processor == null) {
            processors.remove(key);
        } else {
            processors.put(key, processor);
        }
    }

    private static <T> Function<T, Void> getProcessorForCurrentPage(HashMap<Class<?>, Function<T, Void>> processors) {
        Class<?> currentPage = safeGetCurrentPageClass();
        if (currentPage != null && processors.containsKey(currentPage)) {
            return processors.get(currentPage);
        }
        return processors.get(null);
    }

    private static <T> void removeInactivePageProcessors(HashMap<Class<?>, Function<T, Void>> processors) {
        Class<?> currentPage = safeGetCurrentPageClass();
        processors.entrySet().removeIf(e -> e.getKey() != null && e.getKey() != currentPage);
    }

    private static Class<?> safeGetCurrentPageClass() {
        try {
            if (CoreRenderer.engine == null) {
                return null;
            }
            return CoreRenderer.engine.getPageClass();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
