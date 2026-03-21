package com.nikitos.platform;

import com.nikitos.maths.Vec3;
import com.nikitos.platformBridge.AudioPlayer;
import mp3.SimpleMp3Player;

public class AudioPLayerDesktop implements AudioPlayer {
    private SimpleMp3Player musicPlayer = new SimpleMp3Player();

    @Override
    public void playMusic(String path, boolean loop) {
        musicPlayer.play(path);

        // Loop можно реализовать через отдельный поток с проверкой isPlaying()
        if (loop) {
            new Thread(() -> {
                while (musicPlayer.isPlaying()) {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
                musicPlayer.play(path);
            }).start();
        }
    }

    @Override
    public void stopMusic() {
        musicPlayer.stop();
    }

    @Override
    public void pauseMusic() {
        musicPlayer.pause();
    }

    @Override
    public void playSound(String path) {

    }

    @Override
    public void setListenerPosition(Vec3 position) {

    }

    @Override
    public void setSourcePosition(int soundId, Vec3 position) {

    }

    // Другие методы (playSound, setListenerPosition и т.д.) — по аналогии

}