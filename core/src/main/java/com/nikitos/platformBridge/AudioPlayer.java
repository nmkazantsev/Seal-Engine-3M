package com.nikitos.platformBridge;

/**
 * Cross-platform audio facade used by engine code.
 *
 * Design intent (simple and practical):
 * - Music: one long-running stream controlled by play/pause/resume/stop.
 * - Sound effects (SFX): short one-shot sounds that must not corrupt music playback state.
 *
 * Format notes:
 * - Desktop: WAV is recommended for very short SFX; MP3 works but can have encoder delay/padding.
 * - Android: implementations typically use SoundPool for low-latency SFX (async load) and MediaPlayer for music.
 */
public interface AudioPlayer {
    /**
     * Start music playback from an asset path.
     *
     * @param path asset path (platform-specific lookup; usually assets/classpath)
     * @param loop whether to loop the track
     */
    void playMusic(String path, boolean loop);

    /**
     * Stop music playback and release/clear music state.
     */
    void stopMusic();

    /**
     * Pause music playback (does not reset position).
     */
    void pauseMusic();

    /**
     * Continue playback after {@link #pauseMusic()}.
     */
    void resume();

    /**
     * Play a one-shot sound effect (SFX). This must not stop/pause/reset current music playback.
     *
     * The call is allowed to be asynchronous on some platforms (e.g., Android SoundPool loads samples async).
     */
    void playSound(String path);

    /**
     * Set master volume in [0..1]. Implementations should apply it to both music and SFX where possible.
     */
    void setVolume(float volume);

    float getVolume();
}
