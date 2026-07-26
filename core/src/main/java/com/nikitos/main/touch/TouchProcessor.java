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
    private static final MousePoint mousePointState = new MousePoint();
    private static final MouseWheelData mouseWheelState = new MouseWheelData();
    private static boolean mouseMovedDirty = false;
    private static boolean leftButtonPressedPending = false;
    private static boolean rightButtonPressedPending = false;
    private static boolean leftButtonDown = false;
    private static boolean rightButtonDown = false;
    private static float accumulatedWheelX = 0;
    private static float accumulatedWheelY = 0;
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

    /**
     * disables all touch processors
     */
    public static void disableAll() {
        for (TouchProcessor processor : activeProcessors.values()) {
            processor.block();
        }
    }

    /**
     * enables all touch processors. Also switches off effect from disablePriorities(int, int)
     */
    public static void enableAll() {
        for (TouchProcessor processor : activeProcessors.values()) {
            processor.unblock();
        }
    }

    /**
     * disable all touch processors with priorities [min:max] (including borders)
     *
     * @param min - start of interval
     * @param max - stop of interval
     */
    public static void disablePriorities(int min, int max) {
        for (TouchProcessor processor : activeProcessors.values()) {
            if (processor.priority <= max && processor.priority >= min) {
                processor.block();
            }
        }
    }

    /**
     * disable all touch processors with priorities [min:max] (including borders)
     *
     * @param min - start of interval
     * @param max - stop of interval
     */
    public static void enablePriorities(int min, int max) {
        for (TouchProcessor processor : activeProcessors.values()) {
            if (processor.priority <= max && processor.priority >= min) {
                processor.unblock();
            }
        }
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
            if (t != null && !t.blocked && !CoreRenderer.engine.switchingNewGamePage()) {
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
            mousePointState.set(mouseX, mouseY);
            leftButtonDown = true;
            leftButtonPressedPending = true;
        }
    }

    public static void onRightButtonPressed(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            mousePointState.set(mouseX, mouseY);
            rightButtonDown = true;
            rightButtonPressedPending = true;
        }
    }

    public static void onLeftButtonReleased(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            mousePointState.set(mouseX, mouseY);
            leftButtonDown = false;
        }
    }

    public static void onRightButtonReleased(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            mousePointState.set(mouseX, mouseY);
            rightButtonDown = false;
        }
    }

    public static void onMouseMoved(float mouseX, float mouseY) {
        synchronized (commandQueue) {
            mousePointState.set(mouseX, mouseY);
            mouseMovedDirty = true;
        }
    }

    public static boolean getLeftButtonDown() {
        return leftButtonDown;
    }

    public static boolean getRightButtonDown() {
        return rightButtonDown;
    }

    public static void onMouseWheel(float mouseX, float mouseY, float wheelX, float wheelY) {
        synchronized (commandQueue) {
            mousePointState.set(mouseX, mouseY);
            accumulatedWheelX += wheelX;
            accumulatedWheelY += wheelY;
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
            if (pageChanged) {
                clearPendingMouseSignals();
                pageChanged = false;
                return;
            }
            dispatchMouseCallbacks();
        }
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
                if (!CoreRenderer.engine.switchingNewGamePage()) {
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
            }
        } else {
            //process full screen debugger
            //touch moves will not be processed if starts are not processed here (blocked by debugger)
            TouchProcessor t = Debugger.getMainPageTouchProcessor();
            if (!CoreRenderer.engine.switchingNewGamePage()) {
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
            clearPendingMouseSignals();
            leftButtonDown = false;
            rightButtonDown = false;
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

    private static <T> void setProcessor(HashMap<Class<?>, Function<T, Void>> processors,
                                         Function<T, Void> processor,
                                         GamePageClass creatorPage) {
        if (creatorPage == null) {
            throw new IllegalArgumentException("creatorPage can not be null for mouse processors");
        }
        Class<?> key = creatorPage.getClass();
        if (processor == null) {
            processors.remove(key);
        } else {
            processors.put(key, processor);
        }
    }

    private static <T> Function<T, Void> getProcessorForCurrentPage(HashMap<Class<?>, Function<T, Void>> processors) {
        Class<?> currentPage = safeGetCurrentPageClass();
        if (currentPage == null) {
            return null;
        }
        return processors.get(currentPage);
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

    private static void dispatchMouseCallbacks() {
        dispatchMousePointProcessor(leftButtonProcessors, leftButtonPressedPending);
        dispatchMousePointProcessor(rightButtonProcessors, rightButtonPressedPending);
        dispatchMousePointProcessor(mouseMovedProcessors, mouseMovedDirty);

        if (accumulatedWheelX != 0 || accumulatedWheelY != 0) {
            Function<MouseWheelData, Void> processor = getProcessorForCurrentPage(mouseWheelProcessors);
            if (processor != null) {
                mouseWheelState.set(mousePointState.mouseX, mousePointState.mouseY, accumulatedWheelX, accumulatedWheelY);
                invokeMouseWheelProcessor(processor);
            }
        }

        mouseMovedDirty = false;
        leftButtonPressedPending = false;
        rightButtonPressedPending = false;
        accumulatedWheelX = 0;
        accumulatedWheelY = 0;
    }

    private static void clearPendingMouseSignals() {
        mouseMovedDirty = false;
        leftButtonPressedPending = false;
        rightButtonPressedPending = false;
        accumulatedWheelX = 0;
        accumulatedWheelY = 0;
    }

    private static void dispatchMousePointProcessor(HashMap<Class<?>, Function<MousePoint, Void>> processors, boolean shouldDispatch) {
        if (!shouldDispatch) {
            return;
        }
        Function<MousePoint, Void> processor = getProcessorForCurrentPage(processors);
        if (processor != null) {
            invokeMousePointProcessor(processor);
        }
    }

    private static void invokeMousePointProcessor(Function<MousePoint, Void> processor) {
        if (CoreRenderer.engine.getBsodAllowed()) {
            try {
                processor.apply(mousePointState);
            } catch (Exception e) {
                CoreRenderer.engine.startNewPage(new BSODScreen(e));
            }
        } else {
            processor.apply(mousePointState);
        }
    }

    private static void invokeMouseWheelProcessor(Function<MouseWheelData, Void> processor) {
        if (CoreRenderer.engine.getBsodAllowed()) {
            try {
                processor.apply(mouseWheelState);
            } catch (Exception e) {
                CoreRenderer.engine.startNewPage(new BSODScreen(e));
            }
        } else {
            processor.apply(mouseWheelState);
        }
    }
}
