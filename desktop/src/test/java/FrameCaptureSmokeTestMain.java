import com.nikitos.CoreRenderer;
import com.nikitos.GamePageClass;
import com.nikitos.platform.DesktopLauncher;
import com.nikitos.platformBridge.LauncherParams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

/** Runs one desktop frame capture and exits after the PNG/JSON pair appears. */
public final class FrameCaptureSmokeTestMain {
    private FrameCaptureSmokeTestMain() {
    }

    public static void main(String[] args) {
        Path outputDirectory = Paths.get("build", "frame-capture-smoke-" + System.nanoTime());
        LauncherParams params = new LauncherParams()
                .setWindowTitle("Seal Engine Frame Capture Smoke Test")
                .setFullScreen(false)
                .setDebug(false)
                .setStartPage(unused -> new CapturePage(outputDirectory));
        System.out.println("FRAME_CAPTURE_DIR=" + outputDirectory.toAbsolutePath());
        new DesktopLauncher(params).run();
    }

    private static final class CapturePage extends GamePageClass {
        private final Path outputDirectory;
        private boolean captureRequested;

        private CapturePage(Path outputDirectory) {
            this.outputDirectory = outputDirectory;
        }

        @Override
        public void onSurfaceChanged(int x, int y) {
        }

        @Override
        public void update(float dtMillis) {
            if (!captureRequested) {
                CoreRenderer.engine.setFrameCaptureDataProvider(() -> Map.of("smoke", true, "dtMillis", dtMillis));
                CoreRenderer.engine.requestFrameCapture(outputDirectory);
                captureRequested = true;
                return;
            }
            if (capturePairExists()) {
                System.out.println("FRAME_CAPTURE_SMOKE_OK=" + outputDirectory.toAbsolutePath());
                CoreRenderer.engine.requestShutdown();
            }
        }

        @Override
        public void render() {
            CoreRenderer.engine.glClear();
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onPause() {
        }

        private boolean capturePairExists() {
            try (Stream<Path> files = Files.list(outputDirectory)) {
                return files.filter(Files::isRegularFile).count() == 2;
            } catch (IOException ignored) {
                return false;
            }
        }
    }
}
