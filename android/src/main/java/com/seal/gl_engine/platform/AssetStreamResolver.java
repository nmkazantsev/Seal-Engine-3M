package com.seal.gl_engine.platform;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

final class AssetStreamResolver {
    private final AssetStreamSource packagedAssets;
    private final AssetStreamSource classpathResources;

    AssetStreamResolver(
            AssetStreamSource packagedAssets,
            AssetStreamSource classpathResources
    ) {
        this.packagedAssets = Objects.requireNonNull(packagedAssets);
        this.classpathResources = Objects.requireNonNull(classpathResources);
    }

    InputStream open(String path) {
        Objects.requireNonNull(path, "path");
        FileNotFoundException packagedMissing;
        try {
            return requireStream(packagedAssets.open(path), path, "packaged asset");
        } catch (FileNotFoundException missing) {
            packagedMissing = missing;
        } catch (IOException failure) {
            throw new RuntimeException(
                    "Failed to open packaged asset: " + path,
                    failure
            );
        }

        try {
            return requireStream(
                    classpathResources.open(path),
                    path,
                    "classpath resource"
            );
        } catch (IOException fallbackFailure) {
            RuntimeException failure = new RuntimeException(
                    "Asset not found: " + path,
                    packagedMissing
            );
            failure.addSuppressed(fallbackFailure);
            throw failure;
        }
    }

    private static InputStream requireStream(
            InputStream stream,
            String path,
            String source
    ) throws FileNotFoundException {
        if (stream != null) {
            return stream;
        }
        throw new FileNotFoundException(source + " not found: " + path);
    }
}
