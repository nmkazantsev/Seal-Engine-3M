package mp3;
import com.nikitos.CoreRenderer;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;

import java.io.File;

public class SimpleMp3Player {
    private BasicPlayer player;
    private boolean isPaused = false;

    public SimpleMp3Player() {
        player = new BasicPlayer();
    }

    /**
     * Загрузить и начать воспроизведение.
     */
    public void play(String filePath) {
        try {
            player.open(CoreRenderer.engine.getPlatformBridge().getAssetManager().load(filePath));
            player.play();
            isPaused = false;
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Пауза.
     */
    public void pause() {
        try {
            player.pause();
            isPaused = true;
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возобновить.
     */
    public void resume() {
        if (isPaused) {
            try {
                player.resume();
                isPaused = false;
            } catch (BasicPlayerException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Остановить.
     */
    public void stop() {
        try {
            player.stop();
        } catch (BasicPlayerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Проверить, играет ли.
     */
    public boolean isPlaying() {
        try {
            int status = player.getStatus();
            return status == BasicPlayer.PLAYING;
        } catch (Exception e) {
            return false;
        }
    }
}