package main.images;

import com.nikitos.main.images.AbstractImage;
import com.nikitos.maths.Section;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PImageDesktop extends AbstractImage {
    private BufferedImage image;
    private Graphics2D g;
    private boolean loaded = true;
    private float textSize;

    public PImageDesktop() {}

    public PImageDesktop(BufferedImage image) {
        this.image = image;
        g = image.createGraphics();
        width = image.getWidth();
        height = image.getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    }

    @Override
    public void createBitmap(int w, int h) {
        image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        width = w;
        height = h;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    public void delete() {
        g.dispose();
        loaded = false;
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
    public void background(int r, int g1, int b, int a) {
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(r, g1, b, a));
        g.fillRect(0, 0,  width,height);
    }

    @Override
    public void rect(float x, float y, float w, float h) {
        g.fillRect((int) x, (int) y, (int) w, (int) h);
        g.drawRect((int) x, (int) y, (int) w, (int) h);
    }

    @Override
    public void roundRect(float x, float y, float w, float h, float rx, float ry) {
        g.fillRoundRect((int) x, (int) y, (int) w, (int) h, (int) rx, (int) ry);
        g.drawRoundRect((int) x, (int) y, (int) w, (int) h, (int) rx, (int) ry);
    }

    @Override
    public void ellipse(float cx, float cy, float rx, float ry) {
        g.fillOval((int) (cx - rx), (int) (cy - ry), (int) (rx * 2), (int) (ry * 2));
        g.drawOval((int) (cx - rx), (int) (cy - ry), (int) (rx * 2), (int) (ry * 2));
    }

    @Override
    public void line(float x1, float y1, float x2, float y2) {
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
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
    public void fill(int r, int g1, int b, int a) {
        g.setColor(new Color(r, g1, b, a));
    }

    @Override
    public void stroke(int r, int g1, int b, int a) {
        g.setColor(new Color(r, g1, b, a));
    }

    @Override
    public void strokeWeight(float w) {
        g.setStroke(new BasicStroke(w));
    }

    @Override
    public void noStroke() {
        g.setStroke(new BasicStroke(0));
    }

    @Override
    public void textSize(float size) {
        textSize = size;
        g.setFont(g.getFont().deriveFont(size));
    }

    @Override
    public void textAlign(int align) {
    }

    @Override
    public void text(String s, float x, float y, boolean upperText) {
        String[] lines = s.split("\n");
        float k = 1.3f;
        for (int i = 0; i < lines.length; i++) {
            float dy = upperText ? i * textSize * k : (i + 1) * textSize * k;
            g.drawString(lines[i], x, y + dy);
        }
    }

    @Override
    public void setFont(String font) {
        g.setFont(new Font(font, Font.PLAIN, (int) textSize));
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
        return image;
    }

    @Override
    public void image(AbstractImage img, float x, float y) {
        PImageDesktop d = (PImageDesktop) img;
        g.drawImage(d.image,
                (int) (x - (float) d.width / 2),
                (int) (y - (float) d.height / 2),
                null);
    }

    @Override
    public void image(AbstractImage img, float x, float y, float w, float h) {
        PImageDesktop d = (PImageDesktop) img;
        g.drawImage(d.image,
                (int) (x - w / 2),
                (int) (y - h / 2),
                (int) w,
                (int) h,
                null);
    }

    @Override
    public void image(AbstractImage img, float x, float y, float scale) {
        image(img, x, y, img.getWidth() * scale, img.getHeight() * scale);
    }

    @Override
    public void rotImage(AbstractImage img, float x, float y, float scale, float rot) {
        PImageDesktop d = (PImageDesktop) img;
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.rotate(rot);
        at.scale(scale, scale);
        at.translate((double) -d.width / 2, (double) -d.height / 2);
        g.drawImage(d.image, at, null);
    }
}
