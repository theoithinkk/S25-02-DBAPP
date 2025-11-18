package app.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

public class BackgroundMusicPlayer {
    private static MediaPlayer mediaPlayer;
    private static boolean available = false;
    private static boolean muted = false;
    private static double volume = 0.3; // default volume

    /**
     * Loads the background music file, but does not auto-play.
     * Should be called once after successful login or main window load.
     */
    public static void initialize() {
        try {
            if (mediaPlayer != null) return; // already initialized

            URL resource = BackgroundMusicPlayer.class.getResource("/audio/background_music.mp3");
            if (resource == null) {
                System.err.println("✗ Background music file not found in /resources/audio/");
                available = false;
                return;
            }

            Media media = new Media(resource.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(volume);
            available = true;

            System.out.println("♫ Background music ready.");
        } catch (Exception e) {
            available = false;
            System.err.println("✗ Error loading background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Plays the background music WITHOUT fade-in to prevent dialog focus issues.
     * The fade-in thread was causing dialogs to go behind the main window!
     */
    public static void play() {
        if (!available) initialize();
        if (mediaPlayer == null) return;

        // FIX: Set volume directly instead of using fadeIn() thread
        // The fadeIn() thread's Platform.runLater() calls were stealing focus!
        mediaPlayer.setVolume(volume);
        mediaPlayer.play();
        System.out.println("♫ Music playing...");
    }

    /**
     * Toggles mute state.
     */
    public static void toggleMute() {
        if (mediaPlayer == null) return;
        muted = !muted;
        mediaPlayer.setMute(muted);
        System.out.println(muted ? "🔇 Music muted" : "🔊 Music unmuted");
    }

    /**
     * Returns whether music is muted.
     */
    public static boolean isMuted() {
        return muted;
    }

    /**
     * Sets the music volume (0.0–1.0).
     */
    public static void setVolume(double v) {
        volume = v;
        if (mediaPlayer != null && !mediaPlayer.isMute()) {
            mediaPlayer.setVolume(v);
        }
    }

    /**
     * Gets current volume level (0.0–1.0).
     */
    public static double getVolume() {
        return volume;
    }

    /**
     * Pauses the music playback.
     */
    public static void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            System.out.println("⏸ Music paused.");
        }
    }

    /**
     * Completely stops and releases the music player.
     */
    public static void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            available = false;
            System.out.println("✦ Music player disposed.");
        }
    }

    /**
     * Returns if music can be played.
     */
    public static boolean isAvailable() {
        return available;
    }
}