package org.akropia.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.StreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Plays the mission success cue through Java Sound on its own daemon thread,
 * beside the music line. The cue follows the effects volume (R-0227) by scaling
 * its samples; a change while it plays applies from the next written block.
 * Zero is silence and the <code>soundEnabled=false</code> property always
 * suppresses it. A missing file, a file in another format or an unavailable
 * audio device only skips the cue; the input stream and the line are always
 * closed.
 */
@Singleton
public final class JavaSoundSuccessCuePlayer implements SuccessCuePlayer {

    /**
     * Class path name of the cue; exported by
     * <code>src/audio/mission-success-01.py</code>.
     */
    static final String RESOURCE = "sounds/mission-success-01.wav";

    private final static int BUFFER_SIZE = 4096;

    /**
     * Opens an output line for the cue's format; the music uses it too.
     */
    interface Lines {

        SourceDataLine open(AudioFormat format) throws LineUnavailableException;

    }

    private final Logger logger = LoggerFactory
            .getLogger(JavaSoundSuccessCuePlayer.class);

    private final GamePreferences gamePreferences;

    private final StreamReader streamReader;

    private final Lines lines;

    private final Executor executor;

    private final AtomicBoolean playing = new AtomicBoolean();

    private volatile boolean stopped;

    private volatile SourceDataLine line;

    private volatile int volume;

    @Inject
    JavaSoundSuccessCuePlayer(final GamePreferences gamePreferences,
            final StreamReader streamReader) {
        this(gamePreferences, streamReader, format -> {
            final SourceDataLine out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            return out;
        }, task -> {
            final Thread thread = new Thread(task, "mission-success-cue");
            thread.setDaemon(true);
            thread.start();
        });
    }

    JavaSoundSuccessCuePlayer(final GamePreferences gamePreferences,
            final StreamReader streamReader, final Lines lines,
            final Executor executor) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.streamReader = Preconditions.checkNotNull(streamReader);
        this.lines = Preconditions.checkNotNull(lines);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public void play() {
        if (stopped || !gamePreferences.isSoundEnabled()) {
            return;
        }
        final int saved = gamePreferences.getEffectsVolume();
        if (saved <= MusicPlayer.MIN_VOLUME
                || !playing.compareAndSet(false, true)) {
            return;
        }
        volume = saved;
        try {
            executor.execute(() -> {
                try {
                    playOnce();
                } finally {
                    playing.set(false);
                }
            });
        } catch (RuntimeException e) {
            playing.set(false);
            logger.warn("Mission success cue was not started: {}",
                    e.getMessage());
        }
    }

    private void playOnce() {
        try (InputStream in = streamReader.openStream(RESOURCE);
                AudioInputStream audio = AudioSystem
                        .getAudioInputStream(new BufferedInputStream(in))) {
            if (!isCueFormat(audio.getFormat())) {
                throw new UnsupportedAudioFileException(
                        "Unexpected cue format " + audio.getFormat());
            }
            final SourceDataLine out = lines.open(audio.getFormat());
            line = out;
            try {
                out.start();
                final byte[] buffer = new byte[BUFFER_SIZE];
                int read = 0;
                while (!stopped && read != -1) {
                    read = audio.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        scale(buffer, read, volume);
                        out.write(buffer, 0, read);
                    }
                }
                if (!stopped) {
                    out.drain();
                }
            } finally {
                line = null;
                out.close();
            }
        } catch (IOException | UnsupportedAudioFileException
                | LineUnavailableException | RuntimeException e) {
            logger.warn("Mission success cue was not played: {}",
                    e.getMessage());
        }
    }

    /**
     * The cue is the fixed export of its source: signed 16-bit little-endian
     * mono PCM at 44.1 kHz. Any other file is refused rather than scaled
     * wrongly.
     */
    static boolean isCueFormat(final AudioFormat format) {
        return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                && format.getSampleSizeInBits() == 16
                && format.getChannels() == 1 && !format.isBigEndian()
                && format.getSampleRate() == 44100F;
    }

    /**
     * Scales signed 16-bit little-endian samples to the volume, 0 to 100
     * percent of the exported amplitude; the music uses it too. The line's own
     * gain control is not used, because not every device has one. Scaling never
     * raises a sample, so it cannot clip, and zero writes silence.
     */
    static void scale(final byte[] data, final int length, final int volume) {
        for (int i = 0; i + 1 < length; i += 2) {
            final int sample = (short) ((data[i] & 0xFF) | (data[i + 1] << 8));
            final int scaled = sample * volume / MusicPlayer.MAX_VOLUME;
            data[i] = (byte) scaled;
            data[i + 1] = (byte) (scaled >> 8);
        }
    }

    @Override
    public void setVolume(final int volume) {
        Preconditions.checkArgument(
                volume >= MusicPlayer.MIN_VOLUME
                        && volume <= MusicPlayer.MAX_VOLUME,
                "Volume %s is out of range", volume);
        this.volume = volume;
    }

    @Override
    public void stop() {
        stopped = true;
        final SourceDataLine out = line;
        if (out != null) {
            // A stopped and flushed line releases the blocked write at once;
            // the playing thread then closes it.
            try {
                out.stop();
                out.flush();
            } catch (RuntimeException e) {
                logger.warn("Mission success cue was not stopped: {}",
                        e.getMessage());
            }
        }
    }

}
