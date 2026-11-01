package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.MicroColException;
import org.mockito.ArgumentCaptor;

/**
 * Playback of the mission success cue (R-0228) without sound hardware: the WAV
 * is built in memory and the output line is a test double.
 */
class JavaSoundSuccessCuePlayerTest {

    private static final int FRAMES = 10_000;

    private static final AudioFormat CUE_FORMAT = new AudioFormat(44100F, 16, 1,
            true, false);

    private final GamePreferences preferences = mock(GamePreferences.class);

    private final StreamReader streamReader = mock(StreamReader.class);

    private final SourceDataLine line = mock(SourceDataLine.class);

    private final List<ClosingStream> opened = new ArrayList<>();

    private final List<AudioFormat> requestedLines = new ArrayList<>();

    private JavaSoundSuccessCuePlayer.Lines lines = format -> {
        requestedLines.add(format);
        return line;
    };

    @BeforeEach
    void setUp() {
        when(preferences.isSoundEnabled()).thenReturn(true);
        when(preferences.getVolume()).thenReturn(75);
        final byte[] cue = wav(FRAMES);
        when(streamReader.openStream(JavaSoundSuccessCuePlayer.RESOURCE))
                .thenAnswer(invocation -> stream(cue));
    }

    @Test
    void theWholeCueIsPlayedOnceAndEveryResourceIsClosed() {
        player(Runnable::run).play();

        assertEquals(1, requestedLines.size());
        final AudioFormat format = requestedLines.get(0);
        assertEquals(44100F, format.getSampleRate());
        assertEquals(1, format.getChannels());
        verify(line).start();
        final ArgumentCaptor<Integer> lengths = ArgumentCaptor
                .forClass(Integer.class);
        verify(line, atLeastOnce()).write(any(byte[].class), eq(0),
                lengths.capture());
        assertEquals(FRAMES * 2,
                lengths.getAllValues().stream().mapToInt(i -> i).sum());
        verify(line).drain();
        verify(line).close();
        assertStreamsClosed(1);
    }

    @Test
    void soundDisabledByTheSystemPropertySuppressesTheCue() {
        when(preferences.isSoundEnabled()).thenReturn(false);
        final List<Runnable> tasks = new ArrayList<>();
        player(tasks::add).play();

        assertTrue(tasks.isEmpty());
        verifyZeroInteractions(streamReader, line);
    }

    @Test
    void zeroVolumeIsSilence() {
        when(preferences.getVolume()).thenReturn(0);
        final List<Runnable> tasks = new ArrayList<>();
        player(tasks::add).play();

        assertTrue(tasks.isEmpty());
        verifyZeroInteractions(streamReader, line);
    }

    @Test
    void aCueRequestedWhileOneIsPlayingIsDroppedNotQueued() {
        final List<Runnable> tasks = new ArrayList<>();
        final JavaSoundSuccessCuePlayer player = player(tasks::add);
        player.play();
        player.play();
        assertEquals(1, tasks.size());

        // Once the first cue has finished, a later success may sound again.
        tasks.get(0).run();
        player.play();
        assertEquals(2, tasks.size());
    }

    @Test
    void theWrittenSamplesFollowTheSavedVolumeWithoutAGainControl() {
        // Silence, both full-scale extremes and two quiet samples.
        final short[] samples = { 0, 32767, -32768, 1000, -1000, 0 };
        cueOf(samples);
        final List<byte[]> written = recordWrites();

        when(preferences.getVolume()).thenReturn(25);
        player(Runnable::run).play();
        assertArrayEquals(new short[] { 0, 8191, -8192, 250, -250, 0 },
                samples(written));

        written.clear();
        when(preferences.getVolume()).thenReturn(100);
        player(Runnable::run).play();
        // Full volume writes the exported samples unchanged, never louder.
        assertArrayEquals(samples, samples(written));

        // The line's own gain is neither required nor applied twice.
        verify(line, never()).getControl(any());
    }

    @Test
    void aCueFileInAnotherFormatIsRefusedAndClosed() {
        reset(streamReader);
        final byte[] stereo = wav(new byte[400],
                new AudioFormat(44100F, 16, 2, true, false), 100);
        when(streamReader.openStream(JavaSoundSuccessCuePlayer.RESOURCE))
                .thenAnswer(invocation -> stream(stereo));
        player(Runnable::run).play();

        assertTrue(requestedLines.isEmpty());
        assertStreamsClosed(1);
    }

    @Test
    void aMissingCueFileOnlySkipsTheCue() {
        reset(streamReader);
        when(streamReader.openStream(JavaSoundSuccessCuePlayer.RESOURCE))
                .thenThrow(new MicroColException("missing"));
        final JavaSoundSuccessCuePlayer player = player(Runnable::run);
        player.play();
        player.play();

        // Each request fails on its own; nothing is left marked as playing.
        verify(streamReader, times(2))
                .openStream(JavaSoundSuccessCuePlayer.RESOURCE);
        assertTrue(requestedLines.isEmpty());
    }

    @Test
    void anUnsupportedCueFileOnlySkipsTheCueAndClosesTheStream() {
        reset(streamReader);
        when(streamReader.openStream(JavaSoundSuccessCuePlayer.RESOURCE))
                .thenAnswer(invocation -> stream(new byte[] { 1, 2, 3, 4, 5, 6,
                        7, 8, 9, 10, 11, 12, 13, 14, 15, 16 }));
        player(Runnable::run).play();

        assertTrue(requestedLines.isEmpty());
        assertStreamsClosed(1);
    }

    @Test
    void anUnavailableAudioDeviceOnlySkipsTheCueAndClosesTheStream() {
        lines = format -> {
            throw new LineUnavailableException("no audio device");
        };
        final JavaSoundSuccessCuePlayer player = player(Runnable::run);
        player.play();
        player.play();

        assertStreamsClosed(2);
    }

    @Test
    void aFailingOutputStillClosesTheLineAndTheStream() {
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("device lost"));
        player(Runnable::run).play();

        verify(line).close();
        verify(line, never()).drain();
        assertStreamsClosed(1);
    }

    @Test
    void aRejectedPlaybackThreadOnlySkipsTheCue() {
        final int[] attempts = { 0 };
        final JavaSoundSuccessCuePlayer player = player(task -> {
            attempts[0]++;
            throw new RejectedExecutionException("no thread");
        });
        player.play();
        player.play();

        assertEquals(2, attempts[0]);
        verifyZeroInteractions(streamReader, line);
    }

    @Test
    void stoppingTheApplicationEndsAPlayingCueAndAllowsNoFurtherCue() {
        final List<Runnable> tasks = new ArrayList<>();
        final JavaSoundSuccessCuePlayer player = player(tasks::add);
        // The application stops while the first block is written.
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    player.stop();
                    return invocation.getArgument(2);
                });
        player.play();
        tasks.get(0).run();

        verify(line, times(1)).write(any(byte[].class), anyInt(), anyInt());
        verify(line).stop();
        verify(line).flush();
        verify(line, never()).drain();
        verify(line).close();
        assertStreamsClosed(1);

        player.play();
        assertEquals(1, tasks.size());
    }

    private JavaSoundSuccessCuePlayer player(final Executor executor) {
        return new JavaSoundSuccessCuePlayer(preferences, streamReader,
                format -> lines.open(format), executor);
    }

    private ClosingStream stream(final byte[] data) {
        final ClosingStream out = new ClosingStream(data);
        opened.add(out);
        return out;
    }

    private void assertStreamsClosed(final int expected) {
        assertEquals(expected, opened.size());
        opened.forEach(stream -> assertTrue(stream.closed));
    }

    /** Serves a cue of exactly these samples in the expected format. */
    private void cueOf(final short[] samples) {
        final byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            pcm[i * 2] = (byte) samples[i];
            pcm[i * 2 + 1] = (byte) (samples[i] >> 8);
        }
        final byte[] cue = wav(pcm, CUE_FORMAT, samples.length);
        reset(streamReader);
        when(streamReader.openStream(JavaSoundSuccessCuePlayer.RESOURCE))
                .thenAnswer(invocation -> stream(cue));
    }

    /** Copies every block when it is written; the player reuses its buffer. */
    private List<byte[]> recordWrites() {
        final List<byte[]> written = new ArrayList<>();
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    final byte[] data = invocation.getArgument(0);
                    final int offset = invocation.getArgument(1);
                    final int length = invocation.getArgument(2);
                    written.add(
                            Arrays.copyOfRange(data, offset, offset + length));
                    return length;
                });
        return written;
    }

    private static short[] samples(final List<byte[]> written) {
        final ByteArrayOutputStream all = new ByteArrayOutputStream();
        written.forEach(block -> all.write(block, 0, block.length));
        final byte[] data = all.toByteArray();
        final short[] out = new short[data.length / 2];
        for (int i = 0; i < out.length; i++) {
            out[i] = (short) ((data[i * 2] & 0xFF) | (data[i * 2 + 1] << 8));
        }
        return out;
    }

    private static byte[] wav(final int frames) {
        return wav(new byte[frames * 2], CUE_FORMAT, frames);
    }

    private static byte[] wav(final byte[] pcm, final AudioFormat format,
            final int frames) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            AudioSystem
                    .write(new AudioInputStream(new ByteArrayInputStream(pcm),
                            format, frames), AudioFileFormat.Type.WAVE, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static final class ClosingStream extends ByteArrayInputStream {

        private boolean closed;

        ClosingStream(final byte[] data) {
            super(data);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            super.close();
        }

    }

}
