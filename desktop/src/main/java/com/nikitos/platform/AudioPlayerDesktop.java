package com.nikitos.platform;

import com.nikitos.platformBridge.AudioPlayer;
import com.nikitos.platformBridge.SealAssetManager;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Desktop audio implementation:
 * - Music: one long-lived streaming player (BasicPlayer).
 * - Sound effects: short-lived independent Clips (WAV and MP3 via mp3spi).
 *
 * Idea:
 * - Keep music and SFX isolated: SFX playback must never share the same underlying player/state as music.
 * - Prefer re-creating the low-level music player on each {@link #playMusic(String, boolean)} call to avoid
 *   stale state issues across open/play/stop cycles (which was a likely cause of short MP3 cut-offs).
 *
 * Limitations:
 * - MP3 SFX support depends on JavaSound providers (mp3spi). For very short effects WAV is usually more reliable.
 */
public final class AudioPlayerDesktop implements AudioPlayer {
    private final SealAssetManager assetManager;

    private volatile float volume = 1.0f;

    // Music state (single dedicated instance).
    private final Object musicLock = new Object();
    private BasicPlayer musicPlayer = new BasicPlayer();
    private volatile String musicPath = null;
    private volatile boolean musicLoop = false;
    private volatile boolean musicPaused = false;
    private final AtomicBoolean musicStopRequested = new AtomicBoolean(false);
    private Thread musicLoopThread = null;

    // Sound effects state (each playSound creates its own Clip).
    private final Set<Clip> liveClips = ConcurrentHashMap.newKeySet();

    public AudioPlayerDesktop(SealAssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public void playMusic(String path, boolean loop) {
        synchronized (musicLock) {
            musicPath = path;
            musicLoop = loop;
            musicPaused = false;
            musicStopRequested.set(false);

            // Stop current playback if any.
            try {
                musicPlayer.stop();
            } catch (Exception ignored) {
            }

            // Recreate the underlying player to avoid stale state across open/play cycles.
            musicPlayer = new BasicPlayer();
            applyMusicVolume();

            try {
                // Use a buffered stream to reduce resource-stream edge cases.
                InputStream is = new BufferedInputStream(assetManager.load(path));
                musicPlayer.open(is);
                musicPlayer.play();
            } catch (BasicPlayerException e) {
                throw new RuntimeException(e);
            }

            ensureLoopThreadStarted();
        }
    }

    private void ensureLoopThreadStarted() {
        if (musicLoopThread != null && musicLoopThread.isAlive()) return;
        musicLoopThread = new Thread(this::musicLoopWorker, "desktop-music-loop");
        musicLoopThread.setDaemon(true);
        musicLoopThread.start();
    }

    private void musicLoopWorker() {
        // Polling loop: BasicPlayer doesn't expose a reliable blocking "wait until finished" API here.
        while (true) {
            try {
                Thread.sleep(80);
            } catch (InterruptedException ignored) {
            }

            if (musicStopRequested.get()) {
                return;
            }

            if (!musicLoop) {
                continue;
            }

            if (musicPaused) {
                continue;
            }

            BasicPlayer playerSnapshot;
            String pathSnapshot;
            synchronized (musicLock) {
                playerSnapshot = musicPlayer;
                pathSnapshot = musicPath;
            }

            if (playerSnapshot == null || pathSnapshot == null) {
                continue;
            }

            int status;
            try {
                status = playerSnapshot.getStatus();
            } catch (Exception e) {
                continue;
            }

            if (status == BasicPlayer.STOPPED) {
                // Restart the same track.
                synchronized (musicLock) {
                    if (musicStopRequested.get() || !musicLoop || musicPaused) continue;
                    try {
                        musicPlayer = new BasicPlayer();
                        applyMusicVolume();
                        InputStream is = new BufferedInputStream(assetManager.load(musicPath));
                        musicPlayer.open(is);
                        musicPlayer.play();
                    } catch (BasicPlayerException ignored) {
                        // If loop restart fails, keep the thread alive but don't spin.
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ignored2) {
                        }
                    }
                }
            }
        }
    }

    @Override
    public void stopMusic() {
        synchronized (musicLock) {
            musicStopRequested.set(true);
            musicLoop = false;
            musicPaused = false;
            try {
                musicPlayer.stop();
            } catch (BasicPlayerException ignored) {
            }
        }
    }

    @Override
    public void pauseMusic() {
        synchronized (musicLock) {
            try {
                musicPlayer.pause();
                musicPaused = true;
            } catch (BasicPlayerException ignored) {
            }
        }
    }

    @Override
    public void resume() {
        synchronized (musicLock) {
            if (!musicPaused) return;
            try {
                musicPlayer.resume();
                musicPaused = false;
            } catch (BasicPlayerException ignored) {
            }
        }
    }

    @Override
    public void playSound(String path) {
        // Play sound effects via Clip so they don't share any state with music.
        try (InputStream raw = assetManager.load(path)) {
            BufferedInputStream bis = new BufferedInputStream(raw);
            try (AudioInputStream in = AudioSystem.getAudioInputStream(bis)) {
                Clip clip = AudioSystem.getClip();
                liveClips.add(clip);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                        liveClips.remove(clip);
                        try {
                            clip.close();
                        } catch (Exception ignored) {
                        }
                    }
                });
                clip.open(in);
                applyClipVolume(clip);
                clip.start();
            }
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException("Unsupported sound format: " + path, e);
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Audio line unavailable for: " + path, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setVolume(float volume) {
        if (Float.isNaN(volume)) volume = 1.0f;
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        synchronized (musicLock) {
            applyMusicVolume();
        }
        // Best-effort update for currently playing SFX.
        for (Clip clip : liveClips) {
            applyClipVolume(clip);
        }
    }

    @Override
    public float getVolume() {
        return volume;
    }

    private void applyMusicVolume() {
        try {
            musicPlayer.setGain(volume);
        } catch (BasicPlayerException ignored) {
        }
    }

    private void applyClipVolume(Clip clip) {
        // Some mixers may not expose MASTER_GAIN; fail silently.
        try {
            if (!clip.isOpen()) return;
            Control c = clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (!(c instanceof FloatControl fc)) return;

            // Map [0..1] to gain in dB.
            // Use a floor to avoid -Inf at 0.
            float v = Math.max(0.0001f, volume);
            float dB = (float) (20.0 * Math.log10(v));
            dB = Math.max(fc.getMinimum(), Math.min(fc.getMaximum(), dB));
            fc.setValue(dB);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
