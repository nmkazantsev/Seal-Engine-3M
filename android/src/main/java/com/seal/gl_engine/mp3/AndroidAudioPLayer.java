package com.seal.gl_engine.mp3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.nikitos.maths.Vec3;
import com.nikitos.platformBridge.AudioPlayer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AndroidAudioPLayer implements AudioPlayer {
    private float volume = 1.0f;
    private MediaPlayer musicPlayer;
    private final SoundPool soundPool;
    private final Map<String, Integer> soundCache = new HashMap<>();

    private final Context context;

    public AndroidAudioPLayer(Context context) {
        this.context = context.getApplicationContext();
        musicPlayer = new MediaPlayer();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        musicPlayer.setVolume(volume, volume);
    }

    @Override
    public float getVolume() {
        musicPlayer.setVolume(volume, volume);
        return volume;
    }
    // 🎵 MUSIC

    //@OptIn(markerClass = UnstableApi.class)
    @Override
    public void playMusic(String path, boolean loop) {
        Log.e("player", "play");
        if (musicPlayer.isPlaying()) {
            musicPlayer.stop();
        }
        AssetFileDescriptor afd = null;
        try {
            afd = context.getAssets().openFd(path);

            musicPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            Log.d("Audio", "File size: " + afd.getLength()); // Убедимся, что файл не пустой

            afd.close();

            musicPlayer.setLooping(loop);
            musicPlayer.setVolume(1f, 1f);

            // Асинхронная подготовка — ключевое исправление!
            musicPlayer.setOnPreparedListener(mp -> {
                Log.e("player", "Prepared, starting playback");
                mp.start();
            });

            musicPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("player", "MediaPlayer error: what=" + what + ", extra=" + extra);
                return true;
            });

            // --- ДИАГНОСТИКА ---
            musicPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("Audio", "MediaPlayer Error: what=" + what + ", extra=" + extra);
                return true;
            });

            musicPlayer.setOnInfoListener((mp, what, extra) -> {
                Log.e("Audio", "MediaPlayer Info: what=" + what + ", extra=" + extra);
                return false;
            });

            musicPlayer.setOnPreparedListener(mp -> {
                Log.e("Audio", "Prepared, duration: " + mp.getDuration() + " ms");
                mp.start();
            });

            musicPlayer.setOnCompletionListener(mp -> {
                Log.e("Audio", "Playback completed (or stopped)");
            });
            // --- КОНЕЦ ДИАГНОСТИКИ ---
            musicPlayer.prepareAsync(); // вместо prepare()
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    @Override
    public void start() {
        musicPlayer.start();
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
