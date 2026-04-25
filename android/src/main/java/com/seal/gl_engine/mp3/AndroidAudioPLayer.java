package com.seal.gl_engine.mp3;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import com.nikitos.platformBridge.AudioPlayer;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AndroidAudioPLayer implements AudioPlayer {
    private float volume = 1.0f;
    private MediaPlayer musicPlayer;
    private final SoundPool soundPool;
    private final Map<String, SoundEntry> soundEntries = new ConcurrentHashMap<>();
    private final Map<Integer, SoundEntry> soundEntriesById = new ConcurrentHashMap<>();

    private final Context context;

    /**
     * Android audio strategy (simple and reliable):
     * - Music: MediaPlayer (good for longer streams, supports looping, async prepare).
     * - SFX: SoundPool (low-latency), BUT note SoundPool loads samples asynchronously.
     *
     * Key detail:
     * - If you call SoundPool.load() and immediately SoundPool.play(), the first play frequently does nothing
     *   because the sample isn't loaded yet. We therefore queue a pending play and execute it from
     *   OnLoadCompleteListener.
     *
     * Format notes:
     * - WAV/OGG are preferred for SoundPool SFX.
     * - MP3 SFX can be flaky/device-dependent with SoundPool; we fall back to a short-lived MediaPlayer for MP3.
     */
    public AndroidAudioPLayer(Context context) {
        this.context = context.getApplicationContext();
        musicPlayer = new MediaPlayer();

        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(10)
                .build();

        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            SoundEntry entry = soundEntriesById.get(sampleId);
            if (entry == null) return;

            if (status != 0) {
                entry.failed = true;
                entry.pendingPlays = 0;
                Log.e("Audio", "SoundPool load failed: id=" + sampleId + ", status=" + status + ", path=" + entry.path);
                return;
            }

            entry.loaded = true;
            int plays = entry.pendingPlays;
            entry.pendingPlays = 0;

            if (plays > 0) {
                // Only play once for the initial request (avoid accidental burst).
                playLoadedSound(entry.soundId);
            }
        });
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
        // SoundPool loads asynchronously; if you load+play immediately, the first play often does nothing.
        // Fix: track per-sound load state and play after onLoadComplete.
        SoundEntry entry = soundEntries.get(path);
        if (entry != null) {
            if (entry.loaded) {
                playLoadedSound(entry.soundId);
                return;
            }
            if (!entry.failed) {
                entry.pendingPlays++;
                return;
            }
            // Failed previously: fall through to MediaPlayer fallback below.
        }

        // Prefer SoundPool for low-latency WAV/OGG. MP3 support is device-dependent; fallback to MediaPlayer.
        String lower = path.toLowerCase();
        boolean preferMediaPlayer = lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".aac");
        if (preferMediaPlayer) {
            playSoundWithMediaPlayer(path);
            return;
        }

        SoundEntry created = new SoundEntry(path);
        created.pendingPlays = 1;
        SoundEntry race = soundEntries.putIfAbsent(path, created);
        if (race != null) {
            race.pendingPlays++;
            return;
        }

        try (AssetFileDescriptor afd = context.getAssets().openFd(path)) {
            int id = soundPool.load(afd, 1);
            created.soundId = id;
            soundEntriesById.put(id, created);
            if (id == 0) {
                created.failed = true;
                created.pendingPlays = 0;
                playSoundWithMediaPlayer(path);
            }
        } catch (IOException e) {
            created.failed = true;
            created.pendingPlays = 0;
            throw new RuntimeException(e);
        }
    }

    private void playLoadedSound(int soundId) {
        float v = volume;
        // streamId==0 means it didn't start; we log but don't crash.
        int streamId = soundPool.play(soundId, v, v, 1, 0, 1f);
        if (streamId == 0) {
            Log.w("Audio", "SoundPool play returned streamId=0 for soundId=" + soundId);
        }
    }

    private void playSoundWithMediaPlayer(String path) {
        try (AssetFileDescriptor afd = context.getAssets().openFd(path)) {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.setVolume(volume, volume);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.setOnErrorListener((player, what, extra) -> {
                Log.e("Audio", "SFX MediaPlayer error: what=" + what + ", extra=" + extra + ", path=" + path);
                player.release();
                return true;
            });
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class SoundEntry {
        final String path;
        volatile int soundId;
        volatile boolean loaded;
        volatile boolean failed;
        volatile int pendingPlays;

        SoundEntry(String path) {
            this.path = path;
        }
    }

}
