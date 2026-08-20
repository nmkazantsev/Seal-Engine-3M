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

    // The window handle
    private long window;

    private final CoreRenderer coreRenderer;

    private final LauncherParams launcherParams;

    private final Engine engine;

    private final DesktopBridge desktopBridge;

    private GLFWVidMode vidmode;

    private boolean fullScreenOpened = false;

    //приколхохим сюда обработку мыши
    private boolean mousePressed = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private final DesktopMotionEventAdapter mouseTouchEvent = new DesktopMotionEventAdapter(MyMotionEvent.ACTION_MOVE, 0, 0);

    public DesktopLauncher(LauncherParams launcherParams) {
        this.launcherParams = launcherParams;
        desktopBridge = new DesktopBridge();
        engine = new Engine(desktopBridge, launcherParams);
        init();
        coreRenderer = new CoreRenderer(vidmode.width(), vidmode.height(), engine);
    }

    private static String glfwKeyToName(int key, int scancode) {
        String name = glfwGetKeyName(key, scancode);
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
        System.out.println("version of LWJGL " + Version.getVersion() + "!");
        loop();
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwSetErrorCallback((error, description) -> {
            System.err.println("GLFW error " + error + ": " + GLFWErrorCallback.getDescription(description));
        });

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        if (launcherParams.getMSAA()) {
            glfwWindowHint(GLFW_SAMPLES, 4);
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        String osName = System.getProperty("os.name").toLowerCase();
       // if (osName.contains("mac")) {
            //glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
       // }

        // Get the resolution of the primary monitor
        vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Create the window
        assert vidmode != null;

        if (launcherParams.getFullScreen()) {
            fullScreenOpened = true;
            if (osName.contains("mac")) {
                System.out.println("engine: This is a Mac operating system. Using custom full screen. Press ESC to exit.");
                glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
                glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
                window = glfwCreateWindow(
                        vidmode.width(),
                        vidmode.height(),
                        launcherParams.getWindowTitle(),
                        NULL,
                        NULL);
            } else {
                System.out.println("engine: using general full screen mode. Press ESC to exit.");
                window = glfwCreateWindow(
                        vidmode.width(),
                        vidmode.height(),
                        launcherParams.getWindowTitle(),
                        glfwGetPrimaryMonitor(),
                        NULL);
            }

        } else {
            window = glfwCreateWindow(
                    vidmode.width(),
                    vidmode.height(),
                    launcherParams.getWindowTitle(),
                    NULL,
                    NULL);
            glfwMaximizeWindow(window);
        }

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        desktopBridge.attachWindow(window);
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                KeyboardProcessor.onKeyPressed(glfwKeyToName(key, scancode));
            } else if (action == GLFW_RELEASE) {
                KeyboardProcessor.onKeyReleased(glfwKeyToName(key, scancode));
            }

            if (launcherParams.getFullScreen()) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    if (fullScreenOpened) {
                        fullScreenOpened = false;
                        //переход в border less режим
                        goBoardLessMode(window);

                        //выход в оконный режим

                        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_TRUE);
                        glfwSetWindowSize(window, 800, 800);
                        glfwSetWindowPos(window, 100, 100);

                        //это костыль, который не дает схлопываться окну когда его берет юзер
                        glfwHideWindow(window);
                        glfwShowWindow(window);
                        glfwMaximizeWindow(window);
                    } else {
                        fullScreenOpened = true;
                        if (osName.contains("mac")) {
                            System.out.println("engine: This is a Mac operating system. Using custom full screen. Press ESC to exit.");
                            goBoardLessMode(window);
                        } else {
                            System.out.println("engine: using general full screen mode. Press ESC to exit.");
                            GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
                            assert vid != null;
                            glfwSetWindowMonitor(
                                    window,
                                    glfwGetPrimaryMonitor(),
                                    0, 0,
                                    vid.width(),
                                    vid.height(),
                                    vid.refreshRate()
                            );
                        }
                    }
                }
            }
        });


        glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            glViewport(0, 0, width, height);
            float ky = (float) height / 1280.0f;
            float kx = (float) width / 720.0f;
            if ((float) width > (float) height) {
                kx = (float) width / 1280.0f;
                ky = (float) height / 720.0f;
            }
            Utils.setDim((float) width, (float) height, kx, ky);
            engine.onSurfaceChanged(width, height);
        });

        // Get the thread stack and push a new frame
        setDisplayRes(window);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        //снова обработка тача
        //начало и конец тача
        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                int motionAction;

                if (action == GLFW_PRESS) {
                    mousePressed = true;
                    TouchProcessor.onLeftButtonPressed((float) mouseX, (float) mouseY);
                    motionAction = MyMotionEvent.ACTION_DOWN;
                } else if (action == GLFW_RELEASE) {
                    mousePressed = false;
                    TouchProcessor.onLeftButtonReleased((float) mouseX, (float) mouseY);
                    motionAction = MyMotionEvent.ACTION_UP;
                } else {
                    return;
                }

                TouchProcessor.onTouch(mouseTouchEvent.set(motionAction, (float) mouseX, (float) mouseY));
                return;
            }

            if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
                TouchProcessor.onRightButtonPressed((float) mouseX, (float) mouseY);
            } else if (button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_RELEASE) {
                TouchProcessor.onRightButtonReleased((float) mouseX, (float) mouseY);
            }
        });
        //touchMoved
        glfwSetCursorPosCallback(window, (w, x, y) -> {
            mouseX = x;
            mouseY = y;

            TouchProcessor.onMouseMoved((float) x, (float) y);

            if (!mousePressed) return;

            TouchProcessor.onTouch(mouseTouchEvent.set(MyMotionEvent.ACTION_MOVE, (float) x, (float) y));
        });
        glfwSetScrollCallback(window, (w, xoffset, yoffset) ->
                TouchProcessor.onMouseWheel((float) mouseX, (float) mouseY, (float) xoffset, (float) yoffset)
        );
    }

    private void goBoardLessMode(long window) {
        glfwSetWindowMonitor(window, NULL, 0, 0, 800, 600, 0);

        glfwSetWindowAttrib(window, GLFW_DECORATED, GLFW_FALSE);

        GLFWVidMode vid = glfwGetVideoMode(glfwGetPrimaryMonitor());
        assert vid != null;
        glfwSetWindowSize(window, vid.width(), vid.height());
        glfwSetWindowPos(window, 0, 0);
        glfwMaximizeWindow(window);
    }

    private void setDisplayRes(long window) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Center the window
            /*glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );*/
        } // the stack frame is popped automatically
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
        coreRenderer.onSurfaceCreated();
        if (launcherParams.isDebug()) {
            Debugger.debuggerInit();
        }
        // Set the clear color
        glClearColor(0.0f, 1.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window) && !engine.isShutdownRequested()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            coreRenderer.draw();
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();

        }
        try {
            com.nikitos.CoreRenderer.engine.getPlatformBridge().getAudioPlayer().stopMusic();
        } catch (Exception ignored) {
        }
        engine.close();
    }
}
