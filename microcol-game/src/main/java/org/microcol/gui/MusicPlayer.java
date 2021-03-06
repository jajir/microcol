package org.microcol.gui;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
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
 * Responsible for playing music.
 * 
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

    private final static int BUFFER_SIZE = 128000;

    private final StreamReader streamReader;

    private SourceDataLine sourceLine;

    private boolean run = true;

    @Inject
    MusicPlayer(final StreamReader streamReader) {
        this.streamReader = Preconditions.checkNotNull(streamReader);
    }

    /**
     * Start play background music.
     * 
     * @param filename
     *            the name of the file that is going to be played
     * @param defaultVolume
     *            required default volume
     */
    void playSound(final String filename, final int defaultVolume) {
        final AudioInputStream audioStream = getAudioInputStream(filename);
        final AudioFormat audioFormat = audioStream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            sourceLine = (SourceDataLine) AudioSystem.getLine(info);
            sourceLine.open(audioFormat);
        } catch (LineUnavailableException e) {
            logger.error(e.getMessage(), e);
            throw new MicroColException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new MicroColException(e.getMessage(), e);
        }

        setVolume(defaultVolume);

        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        while (nBytesRead != -1 && run) {
            try {
                nBytesRead = audioStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }

    private AudioInputStream getAudioInputStream(final String filename) {
        try {
            final InputStream in = streamReader.openStream(filename);
            return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
        } catch (IOException | UnsupportedAudioFileException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    public void setVolume(final int volume) {
        final FloatControl volumeControll = (FloatControl) sourceLine
                .getControl(FloatControl.Type.MASTER_GAIN);
        float step = 100F / (volumeControll.getMaximum() - volumeControll.getMinimum());
        volumeControll.setValue(volumeControll.getMinimum() + volume / step);
    }

    void stop() {
        run = false;
    }

}
