package com.nikitos.platform;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.nikitos.main.debugger.Debugger;
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

    private GLFWVidMode vidmode;

    private boolean fullScreenOpened = false;

    //приколхохим сюда обработку мыши
    private boolean mousePressed = false;
    private double mouseX = 0;
    private double mouseY = 0;

    public DesktopLauncher(LauncherParams launcherParams) {
        this.launcherParams = launcherParams;
        engine = new Engine(new DesktopBridge(), launcherParams);
        init();
        coreRenderer = new CoreRenderer(vidmode.width(), vidmode.height(), engine);
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

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        if (launcherParams.getMSAA()) {
            glfwWindowHint(GLFW_SAMPLES, 4);
        }

        //glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        //glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        //glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Get the resolution of the primary monitor
        vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Create the window
        assert vidmode != null;

        if (launcherParams.getFullScreen()) {
            fullScreenOpened = true;
            String osName = System.getProperty("os.name").toLowerCase();
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
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
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
                        String osName = System.getProperty("os.name").toLowerCase();
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
            float x = width;
            float y = height;
            float ky = y / 1280.0f;
            float kx = x / 720.0f;
            if (x > y) {
                kx = x / 1280.0f;
                ky = y / 720.0f;
            }
            Utils.setDim(x, y, kx, ky);
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
            if (button != GLFW_MOUSE_BUTTON_LEFT) return;

            int motionAction;

            if (action == GLFW_PRESS) {
                mousePressed = true;
                motionAction = MyMotionEvent.ACTION_DOWN;
            } else if (action == GLFW_RELEASE) {
                mousePressed = false;
                motionAction = MyMotionEvent.ACTION_UP;
            } else {
                return;
            }

            DesktopMotionEventAdapter event =
                    new DesktopMotionEventAdapter(motionAction, (float) mouseX, (float) mouseY);
            TouchProcessor.onTouch(event);
        });
        //touchMoved
        glfwSetCursorPosCallback(window, (w, x, y) -> {
            mouseX = x;
            mouseY = y;

            if (!mousePressed) return;

            DesktopMotionEventAdapter event =
                    new DesktopMotionEventAdapter(MyMotionEvent.ACTION_MOVE, (float) x, (float) y);

            TouchProcessor.onTouch(event);
        });
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
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
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
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            coreRenderer.draw();
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwSwapBuffers(window); // swap the color buffers
            glfwPollEvents();

        }
    }
}
