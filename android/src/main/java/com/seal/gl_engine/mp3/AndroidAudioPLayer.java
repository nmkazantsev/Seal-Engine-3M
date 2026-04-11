package com.seal.gl_engine.mp3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

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
        if (musicPlayer != null) {
            musicPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }
    // 🎵 MUSIC

    //@OptIn(markerClass = UnstableApi.class)
    @Override
    public void playMusic(String path, boolean loop) {
        Log.i("Audio", "playMusic: " + path + ", loop=" + loop);
        ensureMusicPlayer();
        try (AssetFileDescriptor afd = context.getAssets().openFd(path)) {
            musicPlayer.reset();
            musicPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            musicPlayer.setLooping(loop);
            musicPlayer.setVolume(volume, volume);
            musicPlayer.setOnPreparedListener(MediaPlayer::start);
            musicPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e("Audio", "MediaPlayer error: what=" + what + ", extra=" + extra);
                return true;
            });
            musicPlayer.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void ensureMusicPlayer() {
        if (musicPlayer == null) {
            musicPlayer = new MediaPlayer();
        }
    }

    @Override
    public void stopMusic() {
        if (musicPlayer == null) return;
        try {
            if (musicPlayer.isPlaying()) {
                musicPlayer.stop();
            }
        } catch (IllegalStateException ignored) {
            // If it's not in a valid state, just release below.
        }
        musicPlayer.reset();
    }

    @Override
    public void pauseMusic() {
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.pause();
        }
    }

    @Override
    public void resume() {
        if (musicPlayer == null) return;
        try {
            musicPlayer.start();
        } catch (IllegalStateException ignored) {
            // If stopped/reset, resume isn't valid. Consumer should call playMusic again.
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

}
