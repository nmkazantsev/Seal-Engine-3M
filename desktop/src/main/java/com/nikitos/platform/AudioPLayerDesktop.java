package com.nikitos.platform;

import com.nikitos.maths.Vec3;
import com.nikitos.platformBridge.AudioPlayer;
import mp3.SimpleMp3Player;

import java.util.ArrayList;
import java.util.List;

public class AudioPLayerDesktop implements AudioPlayer {
    private float volume = 1.0f;
    private SimpleMp3Player musicPlayer = new SimpleMp3Player();

    private static List<AudioPLayerDesktop> players = new ArrayList<>();

    public AudioPLayerDesktop() {
        players.add(this);
    }

    @Override
    public void playMusic(String path, boolean loop) {
        musicPlayer.play(path);

        // Loop можно реализовать через отдельный поток с проверкой isPlaying()
        if (loop) {
            new Thread(() -> {
                while (musicPlayer.isPlaying()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                }
                musicPlayer.play(path);
            }).start();
        }
    }

    public static void stopAll() {
        // Проходим по элементам
        for (AudioPLayerDesktop element : players) {
            element.stopMusic();
        }
    }

    @Override
    public void setVolume(float volume) {
        this.volume = volume;
        musicPlayer.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return volume;
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
    public void start() {
        musicPlayer.resume();
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