package org.microcol.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.microcol.gui.util.StreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Responsible for playing music. The music volume scales the samples like the
 * success cue does, so it does not depend on the line's gain control and zero
 * writes silence. A change applies from the next written block, also while
 * playing or before the line is open. A missing file, a file other than signed
 * 16-bit little-endian PCM or an unavailable audio device only leave the music
 * silent; the input stream and the line are always closed.
 */
@Singleton
public final class MusicPlayer {

    private final Logger logger = LoggerFactory.getLogger(MusicPlayer.class);

    /**
     * Minimal volume value.
     */
    public static final int MIN_VOLUME = 0;

    /**
     * Maximal volume value.
     */
    public static final int MAX_VOLUME = 100;

    /**
     * About 0.1 s of the stereo music, so a volume change is heard soon.
     */
    private final static int BUFFER_SIZE = 16384;

    private final StreamReader streamReader;

    private final JavaSoundSuccessCuePlayer.Lines lines;

    private volatile SourceDataLine sourceLine;

    private volatile int volume;

    private volatile boolean run = true;

    @Inject
    MusicPlayer(final StreamReader streamReader) {
        this(streamReader, format -> {
            final SourceDataLine out = AudioSystem.getSourceDataLine(format);
            out.open(format);
            return out;
        });
    }

    MusicPlayer(final StreamReader streamReader,
            final JavaSoundSuccessCuePlayer.Lines lines) {
        this.streamReader = Preconditions.checkNotNull(streamReader);
        this.lines = Preconditions.checkNotNull(lines);
    }

    /**
     * Play background music once to its end, at the volume last set.
     *
     * @param filename the name of the file that is going to be played
     */
    void playSound(final String filename) {
        try (InputStream in = streamReader.openStream(filename);
                AudioInputStream audio = AudioSystem
                        .getAudioInputStream(new BufferedInputStream(in))) {
            if (!isMusicFormat(audio.getFormat())) {
                throw new UnsupportedAudioFileException(
                        "Unexpected music format " + audio.getFormat());
            }
            final SourceDataLine out = lines.open(audio.getFormat());
            sourceLine = out;
            try {
                out.start();
                final byte[] buffer = new byte[BUFFER_SIZE];
                int read = 0;
                while (run && read != -1) {
                    read = audio.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        JavaSoundSuccessCuePlayer.scale(buffer, read, volume);
                        out.write(buffer, 0, read);
                    }
                }
                if (run) {
                    out.drain();
                }
            } finally {
                sourceLine = null;
                out.close();
            }
        } catch (IOException | UnsupportedAudioFileException
                | LineUnavailableException | RuntimeException e) {
            logger.warn("Music was not played: {}", e.getMessage());
        }
    }

    /**
     * Music is scaled as signed 16-bit little-endian PCM, which the bundled
     * file uses; any channel count and rate the line accepts. Any other file is
     * refused rather than scaled wrongly.
     */
    static boolean isMusicFormat(final AudioFormat format) {
        return AudioFormat.Encoding.PCM_SIGNED.equals(format.getEncoding())
                && format.getSampleSizeInBits() == 16 && !format.isBigEndian();
    }

    /**
     * Set the music volume; it may be called at any time from any thread.
     *
     * @param volume from {@link #MIN_VOLUME}, silence, to {@link #MAX_VOLUME}
     */
    public void setVolume(final int volume) {
        Preconditions.checkArgument(
                volume >= MIN_VOLUME && volume <= MAX_VOLUME,
                "Volume %s is out of range", volume);
        this.volume = volume;
    }

    void stop() {
        run = false;
        final SourceDataLine out = sourceLine;
        if (out != null) {
            // A stopped and flushed line releases the blocked write at once;
            // the playing thread then closes it.
            try {
                out.stop();
                out.flush();
            } catch (RuntimeException e) {
                logger.warn("Music was not stopped: {}", e.getMessage());
            }
        }
    }

}
