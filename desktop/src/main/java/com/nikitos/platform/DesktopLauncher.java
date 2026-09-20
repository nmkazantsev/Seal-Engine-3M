package com.nikitos.platform;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.nikitos.main.debugger.Debugger;
import com.nikitos.main.keyboard.KeyboardProcessor;
import com.nikitos.main.touch.MyMotionEvent;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.platformBridge.LauncherParams;
import com.nikitos.utils.Utils;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import touch.DesktopMotionEventAdapter;

import java.nio.IntBuffer;
import java.util.Locale;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;


public class DesktopLauncher {

    /*
     * GLFW window handle.
     */
    private long window;

    private final CoreRenderer coreRenderer;
    private final LauncherParams launcherParams;
    private final Engine engine;
    private final DesktopBridge desktopBridge;

    private GLFWVidMode vidmode;

    private boolean fullScreenOpened = false;

    /*
     * ---------------------------------------------------------------------
     * Window coordinates
     * ---------------------------------------------------------------------
     *
     * These are GLFW logical/screen coordinates.
     *
     * Mouse cursor coordinates use this coordinate system.
     */
    private int windowWidth;
    private int windowHeight;


    /*
     * ---------------------------------------------------------------------
     * OpenGL framebuffer coordinates
     * ---------------------------------------------------------------------
     *
     * These are the actual pixel dimensions used by OpenGL.
     *
     * On HiDPI displays these may be larger than windowWidth/windowHeight.
     *
     * Example:
     *
     *     window:      1512 x 982
     *     framebuffer: 3024 x 1964
     */
    private int framebufferWidth;
    private int framebufferHeight;


    /*
     * Mouse coordinates are stored in GLFW window coordinates.
     */
    private boolean mousePressed = false;
    private double mouseX = 0;
    private double mouseY = 0;

    private final DesktopMotionEventAdapter mouseTouchEvent =
            new DesktopMotionEventAdapter(
                    MyMotionEvent.ACTION_MOVE,
                    0,
                    0
            );


    public DesktopLauncher(LauncherParams launcherParams) {

        this.launcherParams = launcherParams;

        desktopBridge = new DesktopBridge();

        engine = new Engine(
                desktopBridge,
                launcherParams
        );

        init();

        /*
         * CoreRenderer should receive the actual framebuffer size,
         * not GLFWVidMode.
         *
         * At this point the framebuffer callback has normally already
         * initialized these values.
         */
        coreRenderer = new CoreRenderer(
                framebufferWidth,
                framebufferHeight,
                engine
        );
    }


    private static String glfwKeyToName(
            int key,
            int scancode
    ) {

        String name = glfwGetKeyName(
                key,
                scancode
        );

        if (name != null && !name.isEmpty()) {
            return name.toUpperCase(Locale.ROOT);
        }

        switch (key) {

            case GLFW_KEY_SPACE:
                return "SPACE";

            case GLFW_KEY_ENTER:
                return "ENTER";

            case GLFW_KEY_TAB:
                return "TAB";

            case GLFW_KEY_BACKSPACE:
                return "BACKSPACE";

            case GLFW_KEY_ESCAPE:
                return "ESCAPE";

            case GLFW_KEY_LEFT:
                return "LEFT";

            case GLFW_KEY_RIGHT:
                return "RIGHT";

            case GLFW_KEY_UP:
                return "UP";

            case GLFW_KEY_DOWN:
                return "DOWN";

            case GLFW_KEY_LEFT_SHIFT:
                return "LSHIFT";

            case GLFW_KEY_RIGHT_SHIFT:
                return "RSHIFT";

            case GLFW_KEY_LEFT_CONTROL:
                return "LCTRL";

            case GLFW_KEY_RIGHT_CONTROL:
                return "RCTRL";

            case GLFW_KEY_LEFT_ALT:
                return "LALT";

            case GLFW_KEY_RIGHT_ALT:
                return "RALT";

            default:
                return "KEY_" + key;
        }
    }


    public void run() {

        System.out.println(
                "version of LWJGL " + Version.getVersion() + "!"
        );

        loop();

        glfwFreeCallbacks(window);

        glfwDestroyWindow(window);

        glfwTerminate();

        Objects.requireNonNull(
                glfwSetErrorCallback(null)
        ).free();
    }


    private void init() {

        GLFWErrorCallback.createPrint(
                System.err
        ).set();


        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Unable to initialize GLFW"
            );
        }


        glfwSetErrorCallback(
                (error, description) ->
                        System.err.println(
                                "GLFW error "
                                        + error
                                        + ": "
                                        + GLFWErrorCallback.getDescription(
                                        description
                                )
                        )
        );


        /*
         * -----------------------------------------------------------------
         * GLFW configuration
         * -----------------------------------------------------------------
         */

        glfwDefaultWindowHints();

        glfwWindowHint(
                GLFW_VISIBLE,
                GLFW_FALSE
        );

        glfwWindowHint(
                GLFW_RESIZABLE,
                GLFW_TRUE
        );


        if (launcherParams.getMSAA()) {

            glfwWindowHint(
                    GLFW_SAMPLES,
                    4
            );
        }


        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MAJOR,
                3
        );

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MINOR,
                3
        );

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );


        String osName =
                System.getProperty(
                        "os.name"
                ).toLowerCase(Locale.ROOT);


        /*
         * macOS requires the forward-compatible flag for a core
         * OpenGL context.
         */
        if (osName.contains("mac")) {

            glfwWindowHint(
                    GLFW_OPENGL_FORWARD_COMPAT,
                    GLFW_TRUE
            );
        }


        /*
         * Primary monitor.
         */
        long monitor =
                glfwGetPrimaryMonitor();

        if (monitor == NULL) {

            throw new RuntimeException(
                    "Failed to find primary monitor"
            );
        }


        vidmode =
                glfwGetVideoMode(
                        monitor
                );

        if (vidmode == null) {

            throw new RuntimeException(
                    "Failed to get primary monitor video mode"
            );
        }


        /*
         * -----------------------------------------------------------------
         * Window creation
         * -----------------------------------------------------------------
         */


        if (launcherParams.getFullScreen()) {

            fullScreenOpened = true;


            if (osName.contains("mac")) {

                System.out.println(
                        "engine: macOS borderless fullscreen"
                );

                glfwWindowHint(
                        GLFW_DECORATED,
                        GLFW_FALSE
                );

                glfwWindowHint(
                        GLFW_RESIZABLE,
                        GLFW_FALSE
                );


                try (MemoryStack stack = stackPush()) {

                    IntBuffer x = stack.mallocInt(1);
                    IntBuffer y = stack.mallocInt(1);
                    IntBuffer width = stack.mallocInt(1);
                    IntBuffer height = stack.mallocInt(1);


                    glfwGetMonitorWorkarea(
                            monitor,
                            x,
                            y,
                            width,
                            height
                    );


                    window = glfwCreateWindow(
                            width.get(0),
                            height.get(0),
                            launcherParams.getWindowTitle(),
                            NULL,
                            NULL
                    );


                    if (window == NULL) {
                        throw new RuntimeException(
                                "Failed to create the GLFW window"
                        );
                    }


                    glfwSetWindowPos(
                            window,
                            x.get(0),
                            y.get(0)
                    );
                }

            } else {

                System.out.println(
                        "engine: using general fullscreen mode. Press ESC to exit."
                );


                window = glfwCreateWindow(
                        vidmode.width(),
                        vidmode.height(),
                        launcherParams.getWindowTitle(),
                        monitor,
                        NULL
                );
            }

        } else {

            final int initialWidth = 1280;
            final int initialHeight = 720;


            window = glfwCreateWindow(
                    initialWidth,
                    initialHeight,
                    launcherParams.getWindowTitle(),
                    NULL,
                    NULL
            );


            if (window == NULL) {
                throw new RuntimeException(
                        "Failed to create the GLFW window"
                );
            }


            glfwMaximizeWindow(window);
        }


        if (window == NULL) {

            throw new RuntimeException(
                    "Failed to create the GLFW window"
            );
        }


        desktopBridge.attachWindow(window);

        /*
         * Make OpenGL context current and initialize GL capabilities FIRST
         * before making any OpenGL calls (like glViewport).
         */
        glfwMakeContextCurrent(window);
        GL.createCapabilities();


        /*
         * -----------------------------------------------------------------
         * Read the initial window/framebuffer sizes.
         * -----------------------------------------------------------------
         */
        updateWindowSize();

        updateFramebufferSize();


        /*
         * -----------------------------------------------------------------
         * Keyboard
         * -----------------------------------------------------------------
         */

        glfwSetKeyCallback(
                window,
                (window, key, scancode, action, mods) -> {

                    if (action == GLFW_PRESS) {

                        KeyboardProcessor.onKeyPressed(
                                glfwKeyToName(
                                        key,
                                        scancode
                                )
                        );

                    } else if (action == GLFW_RELEASE) {

                        KeyboardProcessor.onKeyReleased(
                                glfwKeyToName(
                                        key,
                                        scancode
                                )
                        );
                    }


                    if (launcherParams.getFullScreen()) {

                        if (
                                key == GLFW_KEY_ESCAPE
                                        && action == GLFW_RELEASE
                        ) {

                            if (fullScreenOpened) {

                                fullScreenOpened = false;

                                goBoardLessMode(window);

                            } else {

                                fullScreenOpened = true;


                                String currentOs =
                                        System.getProperty(
                                                "os.name"
                                        ).toLowerCase(
                                                Locale.ROOT
                                        );


                                if (currentOs.contains("mac")) {

                                    goBoardLessMode(window);

                                } else {

                                    GLFWVidMode vid =
                                            glfwGetVideoMode(
                                                    glfwGetPrimaryMonitor()
                                            );

                                    if (vid == null) {
                                        return;
                                    }


                                    glfwSetWindowMonitor(
                                            window,
                                            glfwGetPrimaryMonitor(),
                                            0,
                                            0,
                                            vid.width(),
                                            vid.height(),
                                            vid.refreshRate()
                                    );
                                }
                            }
                        }
                    }
                }
        );


        /*
         * -----------------------------------------------------------------
         * Framebuffer resize
         * -----------------------------------------------------------------
         */
        glfwSetFramebufferSizeCallback(
                window,
                (win, width, height) -> {

                    framebufferWidth = width;
                    framebufferHeight = height;


                    if (
                            width <= 0
                                    || height <= 0
                    ) {
                        return;
                    }


                    glViewport(
                            0,
                            0,
                            width,
                            height
                    );


                    updateUtilsDimensions();

                    engine.onSurfaceChanged(
                            width,
                            height
                    );
                }
        );


        /*
         * -----------------------------------------------------------------
         * Window resize
         * -----------------------------------------------------------------
         */
        glfwSetWindowSizeCallback(
                window,
                (win, width, height) -> {

                    windowWidth = width;
                    windowHeight = height;

                    System.out.println(
                            "Window size: "
                                    + width
                                    + " x "
                                    + height
                    );
                }
        );


        /*
         * Enable v-sync.
         */
        glfwSwapInterval(1);


        /*
         * Show window.
         */
        glfwShowWindow(window);


        /*
         * -----------------------------------------------------------------
         * Mouse buttons
         * -----------------------------------------------------------------
         */

        glfwSetMouseButtonCallback(
                window,
                (w, button, action, mods) -> {

                    float touchX =
                            getFramebufferMouseX();

                    float touchY =
                            getFramebufferMouseY();


                    if (
                            button
                                    == GLFW_MOUSE_BUTTON_LEFT
                    ) {

                        int motionAction;


                        if (action == GLFW_PRESS) {

                            mousePressed = true;

                            TouchProcessor.onLeftButtonPressed(
                                    touchX,
                                    touchY
                            );

                            motionAction =
                                    MyMotionEvent.ACTION_DOWN;


                        } else if (action == GLFW_RELEASE) {

                            mousePressed = false;

                            TouchProcessor.onLeftButtonReleased(
                                    touchX,
                                    touchY
                            );

                            motionAction =
                                    MyMotionEvent.ACTION_UP;


                        } else {

                            return;
                        }


                        TouchProcessor.onTouch(
                                mouseTouchEvent.set(
                                        motionAction,
                                        touchX,
                                        touchY
                                )
                        );

                        return;
                    }


                    if (
                            button
                                    == GLFW_MOUSE_BUTTON_RIGHT
                                    && action == GLFW_PRESS
                    ) {

                        TouchProcessor.onRightButtonPressed(
                                touchX,
                                touchY
                        );

                    } else if (
                            button
                                    == GLFW_MOUSE_BUTTON_RIGHT
                                    && action == GLFW_RELEASE
                    ) {

                        TouchProcessor.onRightButtonReleased(
                                touchX,
                                touchY
                        );
                    }
                }
        );


        /*
         * -----------------------------------------------------------------
         * Mouse movement
         * -----------------------------------------------------------------
         */

        glfwSetCursorPosCallback(
                window,
                (w, x, y) -> {

                    mouseX = x;
                    mouseY = y;


                    float touchX =
                            getFramebufferMouseX();

                    float touchY =
                            getFramebufferMouseY();


                    TouchProcessor.onMouseMoved(
                            touchX,
                            touchY
                    );


                    if (!mousePressed) {
                        return;
                    }


                    TouchProcessor.onTouch(
                            mouseTouchEvent.set(
                                    MyMotionEvent.ACTION_MOVE,
                                    touchX,
                                    touchY
                            )
                    );
                }
        );


        /*
         * -----------------------------------------------------------------
         * Mouse wheel
         * -----------------------------------------------------------------
         */

        glfwSetScrollCallback(
                window,
                (w, xoffset, yoffset) -> {

                    TouchProcessor.onMouseWheel(
                            getFramebufferMouseX(),
                            getFramebufferMouseY(),
                            (float) xoffset,
                            (float) yoffset
                    );
                }
        );
    }


    /*
     * ---------------------------------------------------------------------
     * Window size
     * ---------------------------------------------------------------------
     */

    private void updateWindowSize() {

        try (MemoryStack stack = stackPush()) {

            IntBuffer width =
                    stack.mallocInt(1);

            IntBuffer height =
                    stack.mallocInt(1);


            glfwGetWindowSize(
                    window,
                    width,
                    height
            );


            windowWidth =
                    width.get(0);

            windowHeight =
                    height.get(0);
        }
    }


    /*
     * ---------------------------------------------------------------------
     * Framebuffer size
     * ---------------------------------------------------------------------
     */

    private void updateFramebufferSize() {

        try (MemoryStack stack = stackPush()) {

            IntBuffer width =
                    stack.mallocInt(1);

            IntBuffer height =
                    stack.mallocInt(1);


            glfwGetFramebufferSize(
                    window,
                    width,
                    height
            );


            framebufferWidth =
                    width.get(0);

            framebufferHeight =
                    height.get(0);


            if (
                    framebufferWidth > 0
                            && framebufferHeight > 0
            ) {

                glViewport(
                        0,
                        0,
                        framebufferWidth,
                        framebufferHeight
                );

                updateUtilsDimensions();
            }
        }
    }


    /*
     * ---------------------------------------------------------------------
     * Utils dimensions
     * ---------------------------------------------------------------------
     */
    private void updateUtilsDimensions() {

        if (
                framebufferWidth <= 0
                        || framebufferHeight <= 0
        ) {
            return;
        }


        float kx =
                (float) framebufferWidth
                        / 720.0f;

        float ky =
                (float) framebufferHeight
                        / 1280.0f;


        if (
                (float) framebufferWidth
                        > (float) framebufferHeight
        ) {

            kx =
                    (float) framebufferWidth
                            / 1280.0f;

            ky =
                    (float) framebufferHeight
                            / 720.0f;
        }


        Utils.setDim(
                (float) framebufferWidth,
                (float) framebufferHeight,
                kx,
                ky
        );
    }


    /*
     * ---------------------------------------------------------------------
     * Mouse -> framebuffer conversion
     * ---------------------------------------------------------------------
     */
    private float getFramebufferMouseX() {

        if (windowWidth <= 0) {
            return 0;
        }

        return (float)
                (
                        mouseX
                                * framebufferWidth
                                / (double) windowWidth
                );
    }


    private float getFramebufferMouseY() {

        if (windowHeight <= 0) {
            return 0;
        }

        return (float)
                (
                        mouseY
                                * framebufferHeight
                                / (double) windowHeight
                );
    }


    /*
     * ---------------------------------------------------------------------
     * Borderless fullscreen
     * ---------------------------------------------------------------------
     */
    private void goBoardLessMode(long window) {

        long monitor =
                glfwGetPrimaryMonitor();


        if (monitor == NULL) {
            return;
        }


        try (MemoryStack stack = stackPush()) {

            IntBuffer x =
                    stack.mallocInt(1);

            IntBuffer y =
                    stack.mallocInt(1);

            IntBuffer width =
                    stack.mallocInt(1);

            IntBuffer height =
                    stack.mallocInt(1);


            glfwGetMonitorWorkarea(
                    monitor,
                    x,
                    y,
                    width,
                    height
            );


            glfwSetWindowMonitor(
                    window,
                    NULL,
                    x.get(0),
                    y.get(0),
                    width.get(0),
                    height.get(0),
                    0
            );


            glfwSetWindowAttrib(
                    window,
                    GLFW_DECORATED,
                    GLFW_FALSE
            );


            glfwSetWindowAttrib(
                    window,
                    GLFW_RESIZABLE,
                    GLFW_FALSE
            );


            glfwSetWindowPos(
                    window,
                    x.get(0),
                    y.get(0)
            );


            glfwSetWindowSize(
                    window,
                    width.get(0),
                    height.get(0)
            );
        }
    }


    private void loop() {

        coreRenderer.onSurfaceCreated();


        if (launcherParams.isDebug()) {
            Debugger.debuggerInit();
        }


        glClearColor(
                0.0f,
                1.0f,
                0.0f,
                0.0f
        );


        while (
                !glfwWindowShouldClose(window)
                        && !engine.isShutdownRequested()
        ) {

            glClear(
                    GL_COLOR_BUFFER_BIT
                            | GL_DEPTH_BUFFER_BIT
            );


            coreRenderer.draw();


            glfwSwapBuffers(window);

            glfwPollEvents();
        }


        try {

            com.nikitos.CoreRenderer.engine
                    .getPlatformBridge()
                    .getAudioPlayer()
                    .stopMusic();

        } catch (Exception ignored) {
        }


        engine.close();
    }
}