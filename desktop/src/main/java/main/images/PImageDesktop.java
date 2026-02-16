package main.images;

import com.nikitos.main.images.AbstractImage;
import com.nikitos.main.images.TextAlign;
import com.nikitos.maths.Section;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

public class PImageDesktop extends AbstractImage {

    private Surface surface;
    private Canvas canvas;
    private ImageInfo imageInfo;

    private Paint fillPaint = new Paint().setAntiAlias(true);
    private Paint strokePaint = new Paint()
            .setMode(PaintMode.STROKE)
            .setStrokeWidth(1)
            .setAntiAlias(true);

    private boolean useStroke = true;
    private boolean loaded = true;

    private float textSize = 12f;
    private TextAlign textAlign = TextAlign.LEFT;

    // --- НОВАЯ ТЕКСТОВАЯ ЧАСТЬ ---
    private FontMgr fontMgr;
    private Typeface typeface;
    private Font font;


    public PImageDesktop(Image image) {
        createBitmap(image.getWidth(), image.getHeight());
        canvas.drawImage(image, 0, 0);
    }

    public PImageDesktop() {
    }

    @Override
    public void createBitmap(int w, int h) {

        if (surface != null) {
            surface.close();
        }

        width = w;
        height = h;

        imageInfo = new ImageInfo(
                w,
                h,
                ColorType.RGBA_8888,
                ColorAlphaType.UNPREMUL
        );

        surface = Surface.makeRaster(imageInfo);
        canvas = surface.getCanvas();

        // Инициализация FontMgr и дефолтного шрифта
        fontMgr = FontMgr.getDefault();
        typeface = fontMgr.matchFamilyStyle(null, FontStyle.NORMAL);
        font = new Font(typeface, textSize);

        loaded = true;
    }

    @Override
    public void delete() {
        if (surface != null) {
            surface.close();
        }
        loaded = false;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void textAlign(TextAlign align) {
        this.textAlign = align;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void background(int r, int g, int b, int a) {
        canvas.clear(Color.makeARGB(a, r, g, b));
    }

    @Override
    public void rect(float x, float y, float w, float h) {
        canvas.drawRect(Rect.makeXYWH(x, y, w, h), fillPaint);
        if (useStroke)
            canvas.drawRect(Rect.makeXYWH(x, y, w, h), strokePaint);
    }

    @Override
    public void roundRect(float x, float y, float w, float h, float rx, float ry) {
        RRect rr = RRect.makeXYWH(x, y, w, h, rx, ry);
        canvas.drawRRect(rr, fillPaint);
        if (useStroke)
            canvas.drawRRect(rr, strokePaint);
    }

    @Override
    public void ellipse(float cx, float cy, float rx, float ry) {
        Rect r = Rect.makeXYWH(cx - rx, cy - ry, rx * 2, ry * 2);
        canvas.drawOval(r, fillPaint);
        if (useStroke)
            canvas.drawOval(r, strokePaint);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1, y1, x2, y2, strokePaint);
    }

    @Override
    public void line(Section s) {
        line(
                s.getBaseVector().x,
                s.getBaseVector().y,
                s.getBaseVector().x + s.getDirectionVector().x,
                s.getBaseVector().y + s.getDirectionVector().y
        );
    }

    @Override
    public void fill(int r, int g, int b, int a) {
        fillPaint.setColor(Color.makeARGB(a, r, g, b));
    }

    @Override
    public void stroke(int r, int g, int b, int a) {
        strokePaint.setColor(Color.makeARGB(a, r, g, b));
        useStroke = true;
    }

    @Override
    public void strokeWeight(float w) {
        strokePaint.setStrokeWidth(w);
    }

    @Override
    public void noStroke() {
        useStroke = false;
    }

    // --- TEXT SIZE ---
    @Override
    public void textSize(float size) {
        textSize = size;

        if (typeface == null) {
            typeface = fontMgr.matchFamilyStyle(null, FontStyle.NORMAL);
        }

        font = new Font(typeface, textSize);
    }

    @Override
    public void textAlign(int align) {
        // не используется
    }

    // --- DRAW TEXT ---
    @Override
    public void text(String s, float x, float y, boolean upperText) {

        String[] lines = s.split("\n");
        float k = 1.3f;

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i];
            float textWidth = font.measureTextWidth(line);

            float dx = 0;
            if (textAlign == TextAlign.CENTER)
                dx = -textWidth / 2f;
            else if (textAlign == TextAlign.RIGHT)
                dx = -textWidth;

            float dy = upperText
                    ? i * textSize * k
                    : (i + 1) * textSize * k;

            canvas.drawString(line, x + dx, y + dy, font, fillPaint);
        }
    }

    // --- SET FONT ---
    @Override
    public void setFont(String fontName) {

        if (fontMgr == null)
            fontMgr = FontMgr.getDefault();

        Typeface tf = null;

        if (fontName != null) {
            tf = fontMgr.matchFamilyStyle(fontName, FontStyle.NORMAL);
        }

        if (tf == null) {
            tf = fontMgr.matchFamilyStyle(null, FontStyle.NORMAL);
        }

        typeface = tf;
        font = new Font(typeface, textSize > 0 ? textSize : 16f);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Object getBitmap() {
        return surface;
    }

    @Override
    public void image(AbstractImage img, float x, float y) {
        image(img, x, y, img.getWidth(), img.getHeight());
    }

    @Override
    public void image(AbstractImage img, float x, float y, float w, float h) {

        PImageDesktop d = (PImageDesktop) img;

        Image skImage = d.surface.makeImageSnapshot();

        canvas.drawImageRect(
                skImage,
                Rect.makeXYWH(
                        x - w / 2f,
                        y - h / 2f,
                        w,
                        h
                )
        );

        skImage.close();
    }

    @Override
    public void image(AbstractImage img, float x, float y, float scale) {
        image(img, x, y, img.getWidth() * scale, img.getHeight() * scale);
    }

    @Override
    public void rotImage(AbstractImage img, float x, float y, float scale, float rot) {

        PImageDesktop d = (PImageDesktop) img;

        Image skImage = d.surface.makeImageSnapshot();

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(rot));
        canvas.scale(scale, scale);

        canvas.drawImage(
                skImage,
                -d.width / 2f,
                -d.height / 2f
        );

        canvas.restore();
        skImage.close();
    }
}
