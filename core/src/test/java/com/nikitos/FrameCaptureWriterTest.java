package com.nikitos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameCaptureWriterTest {
    @TempDir
    Path outputDirectory;

    @Test
    void writesOneTopOrientedPngAndMatchingJson() throws Exception {
        byte[] rgbaBottomFirst = {
                0, 0, (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, (byte) 255,
                (byte) 255, 0, 0, (byte) 255, 0, (byte) 255, 0, (byte) 255
        };
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("frame", 7);
        metadata.put("game", Map.of("score", 3));

        FrameCaptureWriter.write(outputDirectory, "capture-test-frame-7", 2, 2, rgbaBottomFirst, metadata);

        Path png = outputDirectory.resolve("capture-test-frame-7.png");
        Path json = outputDirectory.resolve("capture-test-frame-7.json");
        assertTrue(Files.isRegularFile(png));
        assertTrue(Files.isRegularFile(json));
        assertEquals("capture-test-frame-7", png.getFileName().toString().replaceFirst("\\.png$", ""));
        assertEquals("capture-test-frame-7", json.getFileName().toString().replaceFirst("\\.json$", ""));

        BufferedImage image = ImageIO.read(png.toFile());
        assertEquals(2, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(0xFFFF0000, image.getRGB(0, 0));
        assertEquals(0xFF00FF00, image.getRGB(1, 0));
        assertEquals(0xFF0000FF, image.getRGB(0, 1));

        String jsonText = Files.readString(json);
        assertTrue(jsonText.startsWith("{"));
        assertTrue(jsonText.endsWith("}"));
        assertTrue(jsonText.contains("\"frame\":7"));
        assertTrue(jsonText.contains("\"score\":3"));
        assertFalse(jsonText.contains("NaN"));
    }

    @Test
    void rejectsNonFiniteNumbersInsteadOfWritingInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> FrameCaptureWriter.write(
                outputDirectory, "invalid", 1, 1, new byte[]{0, 0, 0, 0}, Map.of("value", Double.NaN)));
    }
}
