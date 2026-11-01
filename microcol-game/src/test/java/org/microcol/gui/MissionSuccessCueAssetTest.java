package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;

/**
 * The bundled mission success cue (R-0228) is the export of
 * <code>src/audio/mission-success-01.py</code>: a short, quiet, uncompressed
 * mono WAV that starts and ends in silence. This checks the file, not how it
 * sounds.
 */
class MissionSuccessCueAssetTest {

    @Test
    void theBundledCueIsAShortQuietMonoPcmExport() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(
                "/" + JavaSoundSuccessCuePlayer.RESOURCE)) {
            assertNotNull(in, "Missing " + JavaSoundSuccessCuePlayer.RESOURCE);
            try (AudioInputStream audio = AudioSystem
                    .getAudioInputStream(new BufferedInputStream(in))) {
                final AudioFormat format = audio.getFormat();
                assertEquals(AudioFormat.Encoding.PCM_SIGNED,
                        format.getEncoding());
                assertEquals(44100F, format.getSampleRate());
                assertEquals(16, format.getSampleSizeInBits());
                assertEquals(1, format.getChannels());
                assertFalse(format.isBigEndian());

                final byte[] data = audio.readAllBytes();
                final int frames = data.length / 2;
                assertEquals(audio.getFrameLength(), frames);
                final double seconds = frames / 44100D;
                assertTrue(seconds >= 0.5 && seconds <= 2,
                        "Cue length " + seconds + " s");

                int peak = 0;
                for (int i = 0; i < frames; i++) {
                    peak = Math.max(peak, Math.abs(sample(data, i)));
                }
                // About -6 dBFS: audible, but clear of clipping.
                assertTrue(peak > 8000 && peak < 20000, "Peak " + peak);
                assertEquals(0, sample(data, 0), "First sample");
                assertEquals(0, sample(data, frames - 1), "Last sample");
            }
        }
    }

    private static int sample(final byte[] data, final int frame) {
        return (short) ((data[frame * 2] & 0xFF) | (data[frame * 2 + 1] << 8));
    }

}
