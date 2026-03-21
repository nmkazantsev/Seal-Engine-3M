package com.seal.gl_engine.mp3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.nikitos.CoreRenderer;
import com.nikitos.maths.Vec3;
import com.nikitos.platformBridge.AudioPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AndroidAudioPLayer implements AudioPlayer {

    private MediaPlayer musicPlayer;
    private final SoundPool soundPool;
    private final Map<String, Integer> soundCache = new HashMap<>();

    private final Context context;

    public AndroidAudioPLayer(Context context) {
        this.context = context.getApplicationContext();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
    }

    // 🎵 MUSIC

    @Override
    public void playMusic(String path, boolean loop) {
        stopMusic();
        try {
            // Пытаемся загрузить из assets (куда скопировали ресурсы)
            AssetFileDescriptor afd = context.getAssets().openFd(path);
            musicPlayer = new MediaPlayer();
            musicPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength()
            );
            // ... остальное
        } catch (IOException e) {
            // fallback: если нет в assets, пробуем загрузить из classpath через временный файл
            InputStream is = CoreRenderer.engine.getPlatformBridge().getAssetManager().load(path);
            if (is == null) throw new RuntimeException("Audio not found: " + path);

            File tempFile = null;
            try {
                tempFile = File.createTempFile("audio", ".mp3", context.getCacheDir());
                tempFile.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                }
                musicPlayer.setDataSource(tempFile.getAbsolutePath());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            musicPlayer.setLooping(loop);
            try {
                musicPlayer.prepare();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            musicPlayer.start();
        }
    }


    @Override
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
            musicPlayer = null;
        }
    }

    @Override
    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

// 🔊 SOUND

    @Override
    public void playSound(String path) {
        int soundId = soundCache.computeIfAbsent(path, p -> {
            try {
                AssetFileDescriptor afd = context.getAssets().openFd(p);
                return soundPool.load(afd, 1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
    }

    @Override
    public void setListenerPosition(Vec3 position) {

    }

    @Override
    public void setSourcePosition(int soundId, Vec3 position) {

    }

}
