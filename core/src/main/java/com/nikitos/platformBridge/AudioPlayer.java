package com.nikitos.platformBridge;

public interface AudioPlayer {
    void playMusic(String path, boolean loop);

    void stopMusic();

    void pauseMusic();

    /**
     * Continue playback after {@link #pauseMusic()}.
     */
    void resume();

    void playSound(String path);

    void setVolume(float volume);

    float getVolume();
}
