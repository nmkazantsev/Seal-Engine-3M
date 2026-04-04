package com.seal.gl_engine.engine.main.images;

import android.graphics.*;
import com.nikitos.CoreRenderer;
import com.nikitos.main.images.AbstractImage;
import com.nikitos.main.images.PFont;
import com.nikitos.main.images.TextAlign;
import com.nikitos.maths.Section;
import com.seal.gl_engine.platform.AndroidBridge;


public class PImageAndroid extends AbstractImage {
    private Bitmap bitmap;
    private Canvas canvas;
    private final Paint paint = new Paint();
    private final Paint stroke = new Paint();
    private final Paint paintImg = new Paint();
    private boolean loaded;

    private float textSize;

    public PImageAndroid() {
    }

    public PImageAndroid(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public void createBitmap(int w, int h) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        width = w;
        height = h;

        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
        loaded = true;
    }

    @Override
    public void delete() {
        if (loaded) {
            bitmap.recycle();
            bitmap = null;
            loaded = false;
        }
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    @Override
    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    @Override
    public void background(int r, int g, int b, int a) {
        canvas.drawColor(Color.argb(a, r, g, b));
    }

    @Override
    public void rect(float x, float y, float w, float h) {
        canvas.drawRect(x, y, x + w, y + h, stroke);
        canvas.drawRect(x, y, x + w, y + h, paint);
    }

    @Override
    public void textAlign(TextAlign align) {
        if (align == TextAlign.LEFT) {
            paint.setTextAlign(Paint.Align.LEFT);
        } else if (align == TextAlign.CENTER) {
            paint.setTextAlign(Paint.Align.CENTER);
        } else {
            paint.setTextAlign(Paint.Align.RIGHT);
        }
    }

    @Override
    public float getTextWidth(String s) {
        return paint.measureText(s);
    }

    @Override
    public float getTextHeight(String s) {
        String[] lines = s.split("\n");

        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = (fm.descent - fm.ascent);

        return lineHeight * lines.length;

    }

    @Override
    public void setAntiAlias(boolean b) {
        paint.setAntiAlias(b);
        stroke.setAntiAlias(b);
        paintImg.setAntiAlias(b);
    }

    @Override
    public void drawSector(float cx, float cy, float radius, float startAngle, float sweepAngle, boolean fill) {
        RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        // Сначала рисуем заливку (если требуется)
        if (fill) {
            canvas.drawArc(oval, startAngle, sweepAngle, true, paint);
        }

        // Затем рисуем обводку (если активна)
        // Обводка считается активной, если stroke имеет ненулевую ширину и цвет не полностью прозрачный
        if (stroke.getStrokeWidth() > 0 && stroke.getAlpha() > 0) {
            canvas.drawArc(oval, startAngle, sweepAngle, true, stroke);
        }
    }

    @Override
    public void clear() {
        canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
    }

    @Override
    public void roundRect(float x, float y, float w, float h, float rx, float ry) {
        RectF r = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(r, rx, ry, paint);
        canvas.drawRoundRect(r, rx, ry, stroke);
    }

    @Override
    public void ellipse(float cx, float cy, float rx, float ry) {
        RectF r = new RectF(cx - rx, cy - ry, cx + rx, cy + ry);
        canvas.drawOval(r, stroke);
        canvas.drawOval(r, paint);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1, y1, x2, y2, stroke);
    }

    @Override
    public void line(Section s) {
        canvas.drawLine(s.getBaseVector().x, s.getBaseVector().y, s.getBaseVector().x + s.getDirectionVector().x, s.getBaseVector().y + s.getDirectionVector().y, stroke);
    }

    @Override
    public void fill(int r, int g, int b, int a) {
        paint.setARGB(a, r, g, b);
    }

    @Override
    public void stroke(int r, int g, int b, int a) {
        stroke.setARGB(a, r, g, b);
        stroke.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void strokeWeight(float w) {
        stroke.setStrokeWidth(w);
    }

    @Override
    public void noStroke() {
        stroke.setARGB(0, 0, 0, 0);
        stroke.setStrokeWidth(0);
    }

    @Override
    public void textSize(float size) {
        textSize = size;
        paint.setTextSize(size);
    }

    @Override
    public void textAlign(int align) {
        paint.setTextAlign(Paint.Align.values()[align]);
    }

    @Override
    public void text(String s, float x, float y, boolean upperText) {
        String[] lines = s.split("\n");
        float k = 1.3f;
        for (int i = 0; i < lines.length; i++) {
            float dy = upperText ? i * textSize * k : (i + 1) * textSize * k;
            canvas.drawText(lines[i], x, y + dy, paint);
        }
    }

    @Override
    public void setFont(String font) {
        paint.setTypeface(Typeface.createFromAsset(((AndroidBridge) CoreRenderer.engine.getPlatformBridge()).getContext().getAssets(), font));
    }

    @Override
    public void image(AbstractImage img, float x, float y) {
        PImageAndroid a = (PImageAndroid) img;
        canvas.drawBitmap(a.bitmap, x - (float) a.width / 2, y - (float) a.height / 2, paintImg);
    }

    @Override
    public void image(AbstractImage img, float x, float y, float w, float h) {
        PImageAndroid a = (PImageAndroid) img;
        Matrix m = new Matrix();
        m.postScale(w / a.width, h / a.height);
        m.postTranslate(x - w / 2, y - h / 2);
        canvas.drawBitmap(a.bitmap, m, paintImg);
    }

    @Override
    public void image(AbstractImage img, float x, float y, float scale) {
        image(img, x, y, img.getWidth() * scale, img.getHeight() * scale);
    }

    @Override
    public void rotImage(AbstractImage img, float x, float y, float scale, float rotRad) {
        PImageAndroid a = (PImageAndroid) img;
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(degrees(rotRad));
        canvas.scale(scale, scale);
        canvas.drawBitmap(a.bitmap, (float) -a.width / 2, (float) -a.height / 2, paintImg);
        canvas.restore();
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
        return bitmap;
    }

    @Override
    public void setFont(PFont font) {
        Object pf = font.getPlatformFont();
        if (pf instanceof Typeface) {
            Typeface currentTypeface = (Typeface) pf;
            paint.setTypeface(currentTypeface);
        } else {
            throw new IllegalArgumentException("Unsupported font type for Android");
        }
    }
}