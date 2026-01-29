package main.images;

import com.nikitos.main.images.PImage;
import com.nikitos.maths.Section;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class PImageDesktop extends PImage {
    private BufferedImage bitmap;
    private Graphics2D canvas;

    // Состояния рисования (аналоги Paint в Android)
    private Color fillColor = Color.WHITE;
    private Color strokeColor = Color.BLACK;
    private float strokeWidth = 1.0f;
    private Font textFont = new Font("SansSerif", Font.PLAIN, 12);
    private float textSize = 12;
    private boolean useStroke = true;

    public PImageDesktop(float width, float height) {
        bitmap = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        canvas = bitmap.createGraphics();

        // Настройка качества рендеринга
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public PImageDesktop(Object bitmap) {
        if (bitmap instanceof BufferedImage) {
            this.bitmap = (BufferedImage) bitmap;
            this.canvas = this.bitmap.createGraphics();
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
    }

    @Override
    public float getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public float getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public boolean isLoaded() {
        return bitmap != null;
    }

    @Override
    public void background(int r, int g, int b) {
        Color oldColor = canvas.getColor();
        canvas.setColor(new Color(r, g, b));
        canvas.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.setColor(oldColor);
    }

    @Override
    public void background(float r, float g, float b, float a) {
        Color oldColor = canvas.getColor();
        canvas.setColor(new Color(r / 255f, g / 255f, b / 255f, a / 255f));
        canvas.fillRect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.setColor(oldColor);
    }

    @Override
    public void rect(float x, float y, float width, float height) {
        if (useStroke) {
            canvas.setColor(strokeColor);
            canvas.setStroke(new BasicStroke(strokeWidth));
            canvas.drawRect((int) x, (int) y, (int) width, (int) height);
        }

        canvas.setColor(fillColor);
        canvas.fillRect((int) x, (int) y, (int) width, (int) height);
    }

    public void roundRect(float x, float y, float width, float height, float rx, float ry) {
        RoundRectangle2D rect = new RoundRectangle2D.Float(x, y, width, height, rx, ry);

        if (useStroke) {
            canvas.setColor(strokeColor);
            canvas.setStroke(new BasicStroke(strokeWidth));
            canvas.draw(rect);
        }

        canvas.setColor(fillColor);
        canvas.fill(rect);
    }

    @Override
    public void ellipse(float centerX, float centerY, float radiusX, float radiusY) {
        Ellipse2D ellipse = new Ellipse2D.Float(
                centerX - radiusX,
                centerY - radiusY,
                radiusX * 2,
                radiusY * 2
        );

        if (useStroke) {
            canvas.setColor(strokeColor);
            canvas.setStroke(new BasicStroke(strokeWidth));
            canvas.draw(ellipse);
        }

        canvas.setColor(fillColor);
        canvas.fill(ellipse);
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        if (useStroke) {
            canvas.setColor(strokeColor);
            canvas.setStroke(new BasicStroke(strokeWidth));
            canvas.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
    }

    public void line(Section section) {
        // Адаптируйте под вашу структуру Section
        line(section.getBaseVector().x, section.getBaseVector().y,
                section.getBaseVector().x + section.getDirectionVector().x,
                section.getBaseVector().y + section.getDirectionVector().y);
    }

    @Override
    public void text(String text, float x, float y) {
        canvas.setColor(fillColor);
        canvas.setFont(textFont);

        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            canvas.drawString(lines[i], x, y + (i + 1) * textSize * 1.3f);
        }
    }

    @Override
    public void fill(int r, int g, int b) {
        fillColor = new Color(r, g, b);
    }

    @Override
    public void fill(int r, int g, int b, int a) {
        fillColor = new Color(r, g, b, a);
    }

    @Override
    public void stroke(int r, int g, int b) {
        strokeColor = new Color(r, g, b);
        useStroke = true;
    }

    @Override
    public void strokeWeight(float weight) {
        strokeWidth = weight;
    }

    @Override
    public void noStroke() {
        useStroke = false;
    }

    @Override
    public void textSize(float size) {
        textSize = size;
        textFont = textFont.deriveFont(size);
    }

    @Override
    public void image(PImage img, float x, float y) {

        PImageDesktop desktopImg = (PImageDesktop) img;
        canvas.drawImage(desktopImg.bitmap, (int) x, (int) y, null);
        
    }

    @Override
    public void image(PImage img, float x, float y, float width, float height) {
        if (img instanceof PImageDesktop desktopImg) {
            canvas.drawImage(desktopImg.bitmap,
                    (int) x, (int) y, (int) (x + width), (int) (y + height),
                    0, 0, desktopImg.bitmap.getWidth(), desktopImg.bitmap.getHeight(), null);
        }
    }

    @Override
    public void image(PImage img, float x, float y, float scale) {
        if (img instanceof PImageDesktop desktopImg) {
            AffineTransform transform = new AffineTransform();
            transform.translate(x, y);
            transform.scale(scale, scale);
            transform.translate(-desktopImg.bitmap.getWidth() / 2.0, -desktopImg.bitmap.getHeight() / 2.0);

            canvas.drawImage(desktopImg.bitmap, transform, null);
        }
    }

    @Override
    public void rotImage(PImage img, float x, float y, float scale, float rotation) {
        if (img instanceof PImageDesktop desktopImg) {
            AffineTransform transform = new AffineTransform();

            // Центрирование и трансформация
            transform.translate(x, y);
            transform.rotate(Math.toRadians(rotation));
            transform.scale(scale, scale);
            transform.translate(-desktopImg.bitmap.getWidth() / 2.0, -desktopImg.bitmap.getHeight() / 2.0);

            canvas.drawImage(desktopImg.bitmap, transform, null);
        }
    }

    public void drawSector(float centerX, float centerY, float radius,
                           float startAngle, float sweepAngle, boolean includeCenter) {
        Arc2D arc = new Arc2D.Float(
                centerX - radius, centerY - radius,
                radius * 2, radius * 2,
                startAngle, sweepAngle,
                includeCenter ? Arc2D.PIE : Arc2D.OPEN
        );

        if (useStroke) {
            canvas.setColor(strokeColor);
            canvas.setStroke(new BasicStroke(strokeWidth));
            canvas.draw(arc);
        }

        canvas.setColor(fillColor);
        canvas.fill(arc);
    }

    @Override
    public void delete() {
        if (canvas != null) {
            canvas.dispose();
            canvas = null;
        }
        bitmap = null;
    }

    // Дополнительные методы для Desktop
    public BufferedImage getBitmap() {
        return bitmap;
    }

    public void setFont(String fontName) {
        textFont = new Font(fontName, Font.PLAIN, (int) textSize);
    }

    public void textAlign(int align) {
        //todo
        // Реализуйте выравнивание текста при необходимости
        // Используйте FontMetrics для точного позиционирования
    }
}
