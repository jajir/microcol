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

/**
 * Responsible for playing music.
 * 
 */
public class MusicPlayer {
	private final int BUFFER_SIZE = 128000;
	private AudioInputStream audioStream;
	private AudioFormat audioFormat;
	private SourceDataLine sourceLine;

	/**
	 * Start play background music.
	 * 
	 * @param filename
	 *            the name of the file that is going to be played
	 * @param defaultVolume
	 *            required default volume
	 */
	public void playSound(final String filename, final int defaultVolume) {

		try {
			final ClassLoader cl = ImageProvider.class.getClassLoader();
			final InputStream in = cl.getResourceAsStream(filename);
			audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(in));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		audioFormat = audioStream.getFormat();

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		try {
			sourceLine = (SourceDataLine) AudioSystem.getLine(info);
			sourceLine.open(audioFormat);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		setVolume(defaultVolume);

		sourceLine.start();

		int nBytesRead = 0;
		byte[] abData = new byte[BUFFER_SIZE];
		while (nBytesRead != -1) {
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

	public void setVolume(final int volume) {
		final FloatControl volumeControll = (FloatControl) sourceLine.getControl(FloatControl.Type.MASTER_GAIN);
		float step = 100F / (volumeControll.getMaximum() - volumeControll.getMinimum());
		volumeControll.setValue(volumeControll.getMinimum() + volume / step);
	}

	public void stop() {
		// TODO JJ finish that
		sourceLine.close();
	}

}
