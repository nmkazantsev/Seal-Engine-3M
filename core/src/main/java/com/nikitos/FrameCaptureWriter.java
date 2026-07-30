package com.nikitos;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.DeflaterOutputStream;

final class FrameCaptureWriter {
    private FrameCaptureWriter() {
    }

    static void write(Path outputDirectory, String basename, int width, int height, byte[] rgbaBottomFirst,
                      Map<String, Object> metadata) throws IOException {
        if (width <= 0 || height <= 0 || rgbaBottomFirst.length != width * height * 4) {
            throw new IllegalArgumentException("Invalid frame capture dimensions or pixels");
        }
        Files.createDirectories(outputDirectory);
        Files.write(outputDirectory.resolve(basename + ".png"), png(width, height, rgbaBottomFirst));
        Files.writeString(outputDirectory.resolve(basename + ".json"), json(metadata), StandardCharsets.UTF_8);
    }

    private static byte[] png(int width, int height, byte[] rgbaBottomFirst) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(new byte[]{(byte) 137, 80, 78, 71, 13, 10, 26, 10});
        ByteArrayOutputStream header = new ByteArrayOutputStream(13);
        DataOutputStream headerData = new DataOutputStream(header);
        headerData.writeInt(width);
        headerData.writeInt(height);
        headerData.writeByte(8);
        headerData.writeByte(6);
        headerData.writeByte(0);
        headerData.writeByte(0);
        headerData.writeByte(0);
        chunk(output, "IHDR", header.toByteArray());

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed)) {
            for (int y = height - 1; y >= 0; y--) {
                deflater.write(0);
                deflater.write(rgbaBottomFirst, y * width * 4, width * 4);
            }
        }
        chunk(output, "IDAT", compressed.toByteArray());
        chunk(output, "IEND", new byte[0]);
        return output.toByteArray();
    }

    private static void chunk(ByteArrayOutputStream output, String type, byte[] data) throws IOException {
        DataOutputStream stream = new DataOutputStream(output);
        stream.writeInt(data.length);
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        stream.write(typeBytes);
        stream.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        stream.writeInt((int) crc.getValue());
    }

    private static String json(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return '"' + escape((String) value) + '"';
        if (value instanceof Number) {
            if (value instanceof Double && !Double.isFinite((Double) value)
                    || value instanceof Float && !Float.isFinite((Float) value)) {
                throw new IllegalArgumentException("Frame capture JSON numbers must be finite");
            }
            return value.toString();
        }
        if (value instanceof Boolean) return value.toString();
        if (value instanceof Map) {
            StringBuilder result = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (!(entry.getKey() instanceof String)) throw new IllegalArgumentException("JSON keys must be strings");
                if (!first) result.append(',');
                result.append(json(entry.getKey())).append(':').append(json(entry.getValue()));
                first = false;
            }
            return result.append('}').toString();
        }
        if (value instanceof List) {
            StringBuilder result = new StringBuilder("[");
            for (Object item : (List<?>) value) {
                if (result.length() > 1) result.append(',');
                result.append(json(item));
            }
            return result.append(']').toString();
        }
        if (value.getClass().isArray()) {
            StringBuilder result = new StringBuilder("[");
            for (int i = 0; i < Array.getLength(value); i++) {
                if (i > 0) result.append(',');
                result.append(json(Array.get(value, i)));
            }
            return result.append(']').toString();
        }
        throw new IllegalArgumentException("Unsupported frame capture JSON value: " + value.getClass().getName());
    }

    private static String escape(String value) {
        StringBuilder result = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '\\': result.append("\\\\"); break;
                case '"': result.append("\\\""); break;
                case '\n': result.append("\\n"); break;
                case '\r': result.append("\\r"); break;
                case '\t': result.append("\\t"); break;
                default:
                    if (character < 0x20) result.append(String.format("\\u%04x", (int) character));
                    else result.append(character);
            }
        }
        return result.toString();
    }
}
