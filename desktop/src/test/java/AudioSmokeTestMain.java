import com.nikitos.platform.AudioPlayerDesktop;
import com.nikitos.platform.DesktopSealAssetManager;
import com.nikitos.platformBridge.AudioPlayer;

/**
 * Minimal desktop-side audio verification entry point.
 *
 * Expected resources on the classpath (typically from core/src/main/resources):
 * - test.mp3  (optional in this repo; used for long music playback if present)
 * - bsod.mp3  (short MP3)
 * - test.wav  (WAV test sound)
 */
public final class AudioSmokeTestMain {
    public static void main(String[] args) throws Exception {
        AudioPlayer audio = new AudioPlayerDesktop(new DesktopSealAssetManager());
        audio.setVolume(1.0f);

        // 1) Long music (preferred): test.mp3. If absent, loop bsod.mp3 to keep a long-running stream.
        String music = "test.mp3";
        try {
            audio.playMusic(music, true);
            System.out.println("Music started (looping): " + music);
        } catch (RuntimeException missing) {
            music = "bsod.mp3";
            System.out.println("test.mp3 not found on classpath; falling back to looping: " + music);
            audio.playMusic(music, true);
        }

        Thread.sleep(1200);

        // 2) WAV SFX while music is playing.
        System.out.println("Playing SFX: test.wav");
        audio.playSound("test.wav");

        Thread.sleep(700);

        // 3) Short MP3 SFX fully.
        System.out.println("Playing SFX: bsod.mp3");
        audio.playSound("bsod.mp3");

        // Give the short MP3 time to finish and observe that music keeps running.
        Thread.sleep(5000);

        audio.stopMusic();
        System.out.println("Done.");
    }
}
