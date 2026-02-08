package com.nikitos.main.debugger;

import com.nikitos.CoreRenderer;
import com.nikitos.Engine;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PImage;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.touch.TouchProcessor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.maths.Matrix;
import com.nikitos.maths.PVector;
import com.nikitos.platformBridge.GLConstBridge;
import com.nikitos.platformBridge.GeneralPlatformBridge;
import com.nikitos.platformBridge.PlatformBridge;
import com.nikitos.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static com.nikitos.main.images.TextAlign.*;
import static com.nikitos.maths.Matrix.resetTranslateMatrix;
import static com.nikitos.utils.Utils.*;

public class Debugger {
    private static boolean enabled = false;
    private static Camera debuggerCamera;
    private static SimplePolygon debuggerPage, fpsPolygon;
    private static Shader shader;
    private static float[] matrix = new float[16];
    private static int page = 0;//0 if no page, then numbers of pages from 1
    private static float fps_x;
    private static float fps_y;
    //list is for rendering in strict order, dict is for fast searching for duplicates
    private static final HashMap<String, DebugValueFloat> debugValues = new HashMap<>();//later will be replaced with abstract debug value
    private static final List<DebugValueFloat> debugList = new ArrayList<>();
    private static TouchProcessor mainTP;
    //menu rendering
    private final static float shift = 300 * ky;
    private final static float enter = 75 * ky;
    private final static int maxNum = 5;
    private static DebugValueFloat selectedValue = null;
    private static int totalValues = 0;
    private static boolean redrawNeeded = true;

    public static void debuggerInit() {
        //open menu button
        //no need in blocking openMenu. because it will not be processed (all touches will be blocked by debugger)
        TouchProcessor openMenu = new TouchProcessor(
                TouchPoint -> (TouchPoint.touchX < fps_x && TouchPoint.touchY < fps_y),
                TouchPoint -> {
                    page = 1;
                    mainTP.unblock();
                    //no need in blocking openMenu. because it will not be processed (all touches will be blocked by debugger)
                    return null;
                }, null, null, null
        );
        //main window touch
        mainTP = new TouchProcessor(
                TouchPoint -> true,
                TouchPoint -> {
                    redrawNeeded = true;
                    //shorter the code
                    float tx = TouchPoint.touchX;
                    float ty = TouchPoint.touchY;
                    //process quit
                    if (tx < fps_x && ty < fps_y || (ty > y - 100 * ky) && (tx > x / 2 - 100 * kx && tx < x / 2 + 100 * kx)) {
                        page = 0;//exit
                        selectedValue = null;
                        mainTP.terminate();
                        mainTP.block();
                        return null;
                    }

                    //<<
                    if (tx < 200 * kx && ty > y - 100 * ky) {
                        if (page > 1) {
                            page--;
                        }
                    }
                    if (tx > x - 200 * kx && ty > y - 100 * ky) {
                        if (page < totalValues / maxNum + (totalValues % maxNum > 0 ? 1 : 0)) {
                            page++;
                        }
                    }
                    if (selectedValue == null) {
                        //processing menu
                        if (ty > shift && ty < shift + (maxNum + 1) * enter) {
                            //select value zone
                            ty -= shift;
                            int number = (int) ty / (int) enter;
                            number += (page - 1) * maxNum;
                            if (number >= 0 && number < totalValues) {
                                selectedValue = debugList.get(number);//choose value
                                mainTP.terminate();//not for process slider in this touch
                            }
                        }
                    } else {
                        //back
                        if (TouchPoint.touchY > y - 250 * ky && TouchPoint.touchY < y - 100 * ky) {
                            selectedValue = null;
                            mainTP.terminate();
                            return null;
                        }
                        //processing slider
                        selectSlider(tx);
                    }
                    return null;
                },
                touchPoint -> {
                    if (selectedValue != null) {
                        redrawNeeded = true;
                        selectSlider(touchPoint.touchX);
                    }
                    return null;
                }, null, null
        );
        mainTP.block();
        enabled = true;
        debuggerPage = new SimplePolygon(drawMianPage, true, 0, null);
        FileUtils fileUtils = new FileUtils();
        shader = new Shader(
                fileUtils.readFileFromAssets(Debugger.class, "/vertex_shader.glsl"),
                fileUtils.readFileFromAssets(Debugger.class, "/fragment_shader.glsl"),
                null, new MainShaderAdaptor());
        fpsPolygon = new SimplePolygon(redrawFps, true, 0, null);
        matrix = resetTranslateMatrix(matrix);
    }

    private static void selectSlider(float tx) {
        //convert position to value and mean borders
        selectedValue.value = max(selectedValue.min, min(selectedValue.max, map(tx, 100 * kx, x - 100 * kx, selectedValue.min, selectedValue.max)));
    }

    public static TouchProcessor getMainPageTouchProcessor() {
        return mainTP;
    }

    /**
     * Create a debug value.
     *
     * @param min  minimum on slider
     * @param max  maximum on slider
     * @param name shown name of this value, unique not null
     * @return new (or already created in case of repeated call) debug value
     */
    public static DebugValueFloat addDebugValueFloat(float min, float max, String name) {
        //check for duplicate
        DebugValueFloat debugValueFloat = debugValues.getOrDefault(name, null);
        if (debugValueFloat == null) {//not a duplicate
            DebugValueFloat d = new DebugValueFloat(min, max, name);
            debugValues.put(d.name, d);
            debugList.add(d);
            totalValues++;
            return d;
        } else {
            return debugValueFloat;
        }
    }

    public static void draw() {
        PlatformBridge pf = CoreRenderer.engine.getPlatformBridge();
        GeneralPlatformBridge gl = pf.getGeneralPlatformBridge();
        GLConstBridge glc = pf.getGLConstBridge();
        if (enabled) {
            shader.apply();
            debuggerCamera.apply();
            Matrix.applyMatrix(matrix);
            if (page == 0) {
                fpsPolygon.setRedrawNeeded(true);
                fpsPolygon.redrawNow();
                fpsPolygon.prepareAndDraw(new PVector(0 * kx, 0, 10), new PVector(fps_x, 0, 10), new PVector(0 * kx, fps_y, 10));
            } else {
                gl.glBlendFunc(glc.GL_SRC_ALPHA(), glc.GL_ONE_MINUS_SRC_ALPHA());
                gl.glEnable(glc.GL_BLEND());
                if (redrawNeeded) {
                    debuggerPage.setRedrawNeeded(true);
                    debuggerPage.redrawNow();
                    redrawNeeded = false;
                }
                debuggerPage.prepareAndDraw(0, 0, 0, x, y, 9);
                gl.glDisable(glc.GL_BLEND());
            }
        }
    }

    public static void onResChange(int x, int y) {
        fps_x = 100 * kx;
        fps_y = 100 * ky;
        debuggerCamera = new Camera(x, y);
        debuggerCamera.resetFor2d();
    }

    public static void setEnabled(boolean debuggerEnabled) {
        enabled = debuggerEnabled;
    }

    private static final Function<List<Object>, PImage> drawMianPage = objects -> {
        PImage image = new PImage((int) x, (int) y);
        image.background(255, 255, 255, 140);
        image.textSize(30 * kx);
        image.textAlign(RIGHT);
        image.fill(50);
        image.text("version: " + Engine.getVersion(), x - 10 * kx, 0 * ky);
        image.textSize(26 * kx);
        image.fill(0);
        image.textAlign(LEFT);
        image.text((int) CoreRenderer.engine.fps, 10, 10);
        image.textSize(45 * kx);
        image.textAlign(CENTER);
        if (selectedValue == null) {
            for (int i = (int) max(0, (page - 1) * maxNum); i < min(page * maxNum, totalValues); i++) {
                image.text(debugList.get(i).name + ": " + debugList.get(i).value, x / 2, shift + enter * (i - page + 1));
            }
        } else {
            image.textAlign(CENTER);
            image.text(selectedValue.name + ":", x / 2, y / 3);
            image.noStroke();
            image.fill(255, 255, 255, 100);
            image.roundRect(100 * kx, y / 2, x - 200 * kx, 50 * ky, 20 * kx, 25 * ky);
            image.fill(100, 100, 100, 255);
            image.strokeWeight(4 * kx);
            image.stroke(0, 0, 0, 255);
            image.roundRect(100 * kx, y / 2, x - map(selectedValue.value, selectedValue.max, selectedValue.min, 200 * kx, x - 40 * kx), 50 * ky, 20 * kx, 25 * ky);
            image.fill(0, 0, 0, 255);
            image.stroke(0, 0, 0, 255);
            image.textAlign(LEFT);
            image.text(selectedValue.min, 20 * kx, y / 2 + 50 * ky);
            image.textAlign(RIGHT);
            image.text(selectedValue.max, x - 20 * kx, y / 2 + 50 * ky);
            image.textAlign(CENTER);
            image.text(selectedValue.value, x / 2, y / 2 - 70 * ky);
            image.text("<< back", x / 2, y - 200 * ky);
        }
        //navigation
        image.textAlign(CENTER);
        image.text("X quit", x / 2, y - 100 * ky);
        if (page > 1) {
            image.textAlign(LEFT);
            image.text("<<", 20 * kx, y - 100 * ky);
        }
        if (page < totalValues / maxNum + (totalValues % maxNum > 0 ? 1 : 0)) {
            image.textAlign(RIGHT);
            image.text(">>", x - 20 * kx, y - 100 * ky);
        }
        return image;
    };


    private static final Function<List<Object>, PImage> redrawFps = objects -> {
        PImage image = new PImage(100, 100);
        image.background(150);
        image.textSize(20);
        image.fill(0);
        image.text(CoreRenderer.engine.fps, 10, 10);
        return image;
    };

    public static int getPage() {
        return page;
    }
}
