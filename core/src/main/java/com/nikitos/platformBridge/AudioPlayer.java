package com.nikitos.platformBridge;

import com.nikitos.maths.Vec3;

public interface AudioPlayer {
    void playMusic(String path, boolean loop);

    void stopMusic();

    void pauseMusic();

    void start();

    void playSound(String path);

    // когда-нибудь сделаю
    void setListenerPosition(Vec3 position);

    void setSourcePosition(int soundId, Vec3 position);

}
