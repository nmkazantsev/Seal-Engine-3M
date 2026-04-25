package com.nikitos.platformBridge;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class RuntimeFileBridge {
    public String loadTextFile(String path) {
        File file = resolvePath(path);
        if (!file.isFile()) {
            throw new RuntimeException("Text file does not exist or is not a regular file: " + file.getPath());
        }
        try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read text file: " + file.getPath(), e);
        }
    }

    public void saveTextFile(String path, String text) {
        File file = resolvePath(path);
        File parent = file.getParentFile();

        if (text == null) {
            throw new RuntimeException("Text can not be null");
        }

        if (file.exists() && !file.isFile()) {
            throw new RuntimeException("Path points to a directory, not a regular file: " + file.getPath());
        }
        if (parent != null) {
            if (parent.exists() && !parent.isDirectory()) {
                throw new RuntimeException("Parent path is not a directory: " + parent.getPath());
            }
            if (!parent.exists()) {
                throw new RuntimeException("Parent directory does not exist: " + parent.getPath());
            }
        }

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(text);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write text file: " + file.getPath(), e);
        }
    }

    public boolean fileExists(String path) {
        try {
            return resolvePath(path).isFile();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean folderExists(String path) {
        try {
            return resolvePath(path).isDirectory();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public boolean createFolder(String path) {
        try {
            File folder = resolvePath(path);
            if (folder.exists()) {
                return folder.isDirectory();
            }
            return folder.mkdirs();
        } catch (RuntimeException e) {
            return false;
        }
    }

    protected File resolvePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("Path can not be null or blank");
        }

        File rawFile = new File(path);
        boolean absolute = rawFile.isAbsolute();
        File resolved = absolute ? rawFile : new File(getRelativeRootOrThrow(), path);

        try {
            File canonical = resolved.getCanonicalFile();
            if (!absolute) {
                File root = getRelativeRootOrThrow().getCanonicalFile();
                String rootPath = root.getPath();
                String canonicalPath = canonical.getPath();
                String rootPrefix = rootPath.endsWith(File.separator) ? rootPath : rootPath + File.separator;
                if (!canonicalPath.equals(rootPath) && !canonicalPath.startsWith(rootPrefix)) {
                    throw new RuntimeException("Relative path escapes runtime files root: " + path);
                }
            }
            return canonical;
        } catch (IOException e) {
            throw new RuntimeException("Failed to resolve path: " + path, e);
        }
    }

    private File getRelativeRootOrThrow() {
        File root = getRelativeRoot();
        if (root == null) {
            throw new RuntimeException("Runtime files root is unavailable on this platform");
        }
        return root;
    }

    protected abstract File getRelativeRoot();
}
