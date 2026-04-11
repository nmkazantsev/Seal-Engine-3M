package com.nikitos.main.debugger;

import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.main.camera.Camera;
import com.nikitos.main.images.PFont;
import com.nikitos.main.images.PImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.main.shaders.Shader;
import com.nikitos.main.shaders.default_adaptors.MainShaderAdaptor;
import com.nikitos.main.vertices.SimplePolygon;
import com.nikitos.maths.Matrix;
import com.nikitos.platformBridge.AudioPlayer;
import com.nikitos.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BSODScreen extends GamePageClass {
    private static final String TITLE = "Fatal error occurred!";

    private String errorText;
    private Camera camera;
    private Shader shader;
    private SimplePolygon screenPolygon;
    private float screenWidth;
    private float screenHeight;

    AudioPlayer player;

    public BSODScreen(String errorText) {
        player = CoreRenderer.engine.getPlatformBridge().getAudioPlayer();
        player.playSound("bsod_engine.wav");
        this.errorText = errorText == null ? "" : errorText;
        shader = new Shader(
                "vertex_shader_engine.glsl",
                "fragment_shader_engine.glsl",
                this,
                new MainShaderAdaptor()
        );


        screenPolygon = new SimplePolygon(redrawScreen, false, 0, this);
    }

    @Override
    public void onSurfaceChanged(int x, int y) {
        screenWidth = x;
        screenHeight = y;

        camera = new Camera(x, y);
        camera.resetFor2d();

        screenPolygon.setRedrawNeeded(true);
        screenPolygon.redrawNow();
    }

    @Override
    public void draw() {
        Utils.background(0, 45, 135);
        CoreRenderer.engine.glClear();
        shader.apply();
        camera.resetFor2d();
        camera.apply();
        Matrix.applyMatrix(Matrix.resetTranslateMatrix(new float[16]));
        screenPolygon.prepareAndDraw(0, 0, screenWidth, screenHeight, 0.1f);
    }

    @Override
    public void onResume() {
        player.resume();
    }

    @Override
    public void onPause() {
        player.pauseMusic();
    }

    private final Function<List<Object>, PImage> redrawScreen = objects -> {
        PFont win = PFont.fromAsset("win.otf");
        float kx = Utils.getKx();
        float ky = Utils.getKy();
        screenWidth = Utils.getX();
        screenHeight = Utils.getY();

        PImage image = new PImage(screenWidth, screenHeight);
        image.setFont(win);
        image.setAntiAlias(true);
        image.background(0, 45, 135, 255);
        image.noStroke();

        float titleMarginX = 72f * kx;
        float titleTop = 68f * ky;
        float titleSize = 60f * Math.min(kx, ky);

        image.fill(255, 255, 255, 255);
        image.textAlign(TextAlign.LEFT);
        image.textSize(titleSize);
        image.text(TITLE, titleMarginX, titleTop);

        float boxX = 72f * kx;
        float boxY = 180f * ky;
        float boxWidth = Math.max(screenWidth - boxX * 2f, 200f * kx);
        float boxHeight = Math.max(screenHeight - boxY - 72f * ky, 140f * ky);
        float boxRadius = 18f * Math.min(kx, ky);
        float borderWidth = Math.max(2f, 3f * Math.min(kx, ky));
        float paddingX = 28f * kx;
        float paddingY = 22f * ky;
        float textSize = 28f * Math.min(kx, ky);

        image.fill(0, 70, 180, 255);
        image.stroke(255, 255, 255, 255);
        image.strokeWeight(borderWidth);
        image.roundRect(boxX, boxY, boxWidth, boxHeight, boxRadius, boxRadius);

        image.fill(255, 255, 255, 255);
        image.noStroke();
        image.textAlign(TextAlign.LEFT);
        image.textSize(textSize);
        image.text(wrapText(image, this.errorText, boxWidth - paddingX * 2f), boxX + paddingX, boxY + paddingY);
        return image;
    };

    private static String wrapText(PImage image, String source, float maxWidth) {
        String normalized = source == null ? "" : source.replace("\r", "");
        if (normalized.isEmpty()) {
            return "";
        }

        List<String> lines = new ArrayList<>();
        for (String paragraph : normalized.split("\n", -1)) {
            if (paragraph.isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.trim().split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (line.length() == 0) {
                    appendWrappedWord(image, lines, line, word, maxWidth);
                    continue;
                }

                String candidate = line + " " + word;
                if (image.getTextWidth(candidate) <= maxWidth) {
                    line.append(' ').append(word);
                } else {
                    lines.add(line.toString());
                    line.setLength(0);
                    appendWrappedWord(image, lines, line, word, maxWidth);
                }
            }

            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }

        return String.join("\n", lines);
    }

    private static void appendWrappedWord(PImage image, List<String> lines, StringBuilder line, String word, float maxWidth) {
        if (image.getTextWidth(word) <= maxWidth) {
            line.append(word);
            return;
        }

        StringBuilder chunk = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            String candidate = chunk.toString() + ch;
            if (chunk.length() > 0 && image.getTextWidth(candidate) > maxWidth) {
                lines.add(chunk.toString());
                chunk.setLength(0);
            }
            chunk.append(ch);
        }

        line.append(chunk);
    }
}
