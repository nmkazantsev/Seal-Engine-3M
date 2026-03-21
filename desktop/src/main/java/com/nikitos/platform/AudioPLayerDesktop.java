package com.nikitos.platform;

import com.nikitos.maths.Vec3;
import com.nikitos.platformBridge.AudioPlayer;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioPLayerDesktop implements AudioPlayer {
    private MediaPlayer musicPlayer;
    private final Map<String, Media> mediaCache = new ConcurrentHashMap<>();

    public AudioPLayerDesktop() {
        initJavaFX();
    }

    private void initJavaFX() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // уже инициализировано
        }
    }

    @Override
    public void playMusic(String path, boolean loop) {
        stopMusic();

        Platform.runLater(() -> {
            Media media = loadMedia(path);
            musicPlayer = new MediaPlayer(media);

            if (loop) {
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }

            musicPlayer.play();
        });
    }

    @Override
    public void stopMusic() {
        if (musicPlayer != null) {
            Platform.runLater(() -> {
                musicPlayer.stop();
                musicPlayer.dispose();
                musicPlayer = null;
            });
        }
    }

    @Override
    public void pauseMusic() {
        if (musicPlayer != null) {
            Platform.runLater(() -> musicPlayer.pause());
        }
    }

    @Override
    public void playSound(String path) {
        Platform.runLater(() -> {
            Media media = loadMedia(path);
            MediaPlayer soundPlayer = new MediaPlayer(media);

            soundPlayer.setCycleCount(1);
            soundPlayer.setOnEndOfMedia(soundPlayer::dispose);
            soundPlayer.play();
        });
    }

    @Override
    public void setListenerPosition(Vec3 position) {
        // not implemented
    }

    @Override
    public void setSourcePosition(int soundId, Vec3 position) {
        // not implemented
    }

    private Media loadMedia(String path) {
        return mediaCache.computeIfAbsent(path, p -> {
            URL url = getClass()
                    .getClassLoader()
                    .getResource(p);

            if (url == null) {
                throw new RuntimeException("Audio not found: " + p);
            }

            return new Media(url.toExternalForm());
        });
    }
}