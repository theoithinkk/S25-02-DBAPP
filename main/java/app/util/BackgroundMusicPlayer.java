package app.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class BackgroundMusicPlayer {

    private static MediaPlayer mediaPlayer;
    private static boolean isMuted = false;
    private static boolean isAvailable = false;

    /**
     * Initialize and start playing background music
     */
    public static void initialize() {
        try {
            // Load the music file from resources
            String musicFile = BackgroundMusicPlayer.class.getResource("/audio/background_music.mp3").toExternalForm();
            Media media = new Media(musicFile);

            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(0.3);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            mediaPlayer.setOnError(() -> {
                System.err.println("Error playing background music: " + mediaPlayer.getError());
            });

            if (!isMuted) {
                mediaPlayer.play();
            }

            isAvailable = true;
            System.out.println("✓ Background music initialized");

        } catch (Throwable e) {  // CHANGED FROM Exception to Throwable
            isAvailable = false;
            System.err.println("⚠ Background music not available");
            System.err.println("  Reason: " + e.getClass().getSimpleName());
            if (e.getMessage() != null) {
                System.err.println("  " + e.getMessage());
            }
            // App continues without music
        }
    }

    public static boolean isAvailable() {
        return isAvailable;
    }

    public static void toggleMute() {
        if (mediaPlayer != null && isAvailable) {
            isMuted = !isMuted;
            if (isMuted) {
                mediaPlayer.pause();
            } else {
                mediaPlayer.play();
            }
        }
    }

    public static void mute() {
        if (mediaPlayer != null && isAvailable && !isMuted) {
            isMuted = true;
            mediaPlayer.pause();
        }
    }

    public static void unmute() {
        if (mediaPlayer != null && isAvailable && isMuted) {
            isMuted = false;
            mediaPlayer.play();
        }
    }

    public static boolean isMuted() {
        return isMuted;
    }

    public static void setVolume(double volume) {
        if (mediaPlayer != null && isAvailable) {
            mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        }
    }

    public static double getVolume() {
        return (mediaPlayer != null && isAvailable) ? mediaPlayer.getVolume() : 0.3;
    }

    public static void dispose() {
        if (mediaPlayer != null && isAvailable) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                // Ignore errors during cleanup
            }
            mediaPlayer = null;
        }
    }
}