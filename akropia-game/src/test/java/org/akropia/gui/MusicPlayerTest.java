package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.akropia.gui.event.VolumeChangeEvent;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.StreamReader;
import org.akropia.model.AkropiaException;

import com.google.common.eventbus.EventBus;

/**
 * Music volume (R-0227) without sound hardware: the WAV is built in memory and
 * the output line is a test double, so the written samples show what is heard.
 */
class MusicPlayerTest {

    private static final String MUSIC = "music/test.wav";

    private static final int FRAMES = 10_000;

    /** The bundled music's format: signed 16-bit little-endian stereo. */
    private static final AudioFormat MUSIC_FORMAT = new AudioFormat(44100F, 16,
            2, true, false);

    private final StreamReader streamReader = mock(StreamReader.class);

    private final SourceDataLine line = mock(SourceDataLine.class);

    private final List<ClosingStream> opened = new ArrayList<>();

    private final List<byte[]> written = new ArrayList<>();

    private JavaSoundSuccessCuePlayer.Lines lines = format -> line;

    @BeforeEach
    void setUp() {
        final short[] samples = new short[FRAMES * 2];
        Arrays.fill(samples, (short) 20000);
        musicOf(samples, MUSIC_FORMAT);
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    final byte[] data = invocation.getArgument(0);
                    final int offset = invocation.getArgument(1);
                    final int length = invocation.getArgument(2);
                    written.add(
                            Arrays.copyOfRange(data, offset, offset + length));
                    return length;
                });
    }

    @Test
    void theWholeMusicIsScaledByItsVolumeWithoutAGainControlAndClosed() {
        final short[] samples = { 0, 32767, -32768, 1000, -1000, 0 };
        musicOf(samples, MUSIC_FORMAT);
        final MusicPlayer player = player();
        player.setVolume(25);
        player.playSound(MUSIC);

        assertArrayEquals(new short[] { 0, 8191, -8192, 250, -250, 0 },
                samples(written));
        verify(line, never()).getControl(any());
        verify(line).start();
        verify(line).drain();
        verify(line).close();
        assertStreamsClosed(1);

        written.clear();
        player.setVolume(100);
        player.playSound(MUSIC);
        // Full volume writes the file's samples unchanged, never louder.
        assertArrayEquals(samples, samples(written));
    }

    @Test
    void aVolumeChangedWhilePlayingAppliesAndZeroWritesSilence() {
        final MusicPlayer player = player();
        player.setVolume(75);
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    final byte[] data = invocation.getArgument(0);
                    final int length = invocation.getArgument(2);
                    written.add(Arrays.copyOf(data, length));
                    player.setVolume(written.size() == 1 ? 50 : 0);
                    return length;
                });
        player.playSound(MUSIC);

        assertTrue(written.size() >= 3, "blocks: " + written.size());
        // Silence keeps the music's timeline: every frame is still written.
        assertEquals(FRAMES * 2, samples(written).length);
        assertSamples(15000, written.get(0));
        assertSamples(10000, written.get(1));
        written.subList(2, written.size())
                .forEach(block -> assertSamples(0, block));
        verify(line).close();
    }

    @Test
    void aVolumeSetBeforeAnyLineExistsOrAfterStopIsKept() {
        final MusicPlayer player = player();
        // Settings may change the volume before music starts or with no device.
        player.setVolume(0);
        player.setVolume(40);
        player.playSound(MUSIC);

        assertSamples(8000, written.get(0));
        player.stop();
        player.setVolume(10);
    }

    @Test
    void aVolumeOutsideTheRangeIsRejected() {
        final MusicPlayer player = player();
        assertThrows(IllegalArgumentException.class,
                () -> player.setVolume(101));
        assertThrows(IllegalArgumentException.class,
                () -> player.setVolume(-1));
    }

    @Test
    void anUnavailableAudioDeviceOnlyLeavesTheMusicSilentAndClosesTheStream() {
        lines = format -> {
            throw new LineUnavailableException("no audio device");
        };
        player().playSound(MUSIC);
        assertStreamsClosed(1);

        // Java Sound reports a missing matching line as an illegal argument.
        lines = format -> {
            throw new IllegalArgumentException("no matching line");
        };
        player().playSound(MUSIC);
        assertStreamsClosed(2);
    }

    @Test
    void aMissingMusicFileOnlyLeavesTheMusicSilent() {
        reset(streamReader);
        when(streamReader.openStream(MUSIC))
                .thenThrow(new AkropiaException("missing"));
        player().playSound(MUSIC);

        verifyZeroInteractions(line);
    }

    @Test
    void musicInAFormatThatCannotBeScaledIsRefusedAndClosed() {
        // Unsigned 8-bit PCM, which the 16-bit scaling would distort.
        musicOf(new short[100], new AudioFormat(44100F, 8, 2, false, false));
        lines = format -> {
            fail("No line may be opened for " + format);
            return line;
        };
        player().playSound(MUSIC);

        assertStreamsClosed(1);
    }

    @Test
    void aFailingOutputStillClosesTheLineAndTheStream() {
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("device lost"));
        player().playSound(MUSIC);

        verify(line).close();
        verify(line, never()).drain();
        assertStreamsClosed(1);
    }

    @Test
    void stoppingEndsPlayingMusicAtOnceAndClosesEverything() {
        final MusicPlayer player = player();
        player.setVolume(50);
        when(line.write(any(byte[].class), anyInt(), anyInt()))
                .thenAnswer(invocation -> {
                    player.stop();
                    return invocation.getArgument(2);
                });
        player.playSound(MUSIC);

        verify(line, times(1)).write(any(byte[].class), anyInt(), anyInt());
        verify(line).stop();
        verify(line).flush();
        verify(line, never()).drain();
        verify(line).close();
        assertStreamsClosed(1);
    }

    @Test
    void eachVolumeChangeReachesOnlyItsOwnCategory() {
        final GamePreferences preferences = mock(GamePreferences.class);
        when(preferences.isSoundEnabled()).thenReturn(true);
        final SuccessCuePlayer cue = mock(SuccessCuePlayer.class);
        final MusicPlayer player = player();
        player.setVolume(50);
        final EventBus eventBus = new EventBus();
        eventBus.register(new MusicController(player, cue, preferences));

        eventBus.post(new VolumeChangeEvent(true, 0));
        verify(cue).setVolume(0);
        player.playSound(MUSIC);
        // Silencing the effects leaves the music at its own volume.
        assertSamples(10000, written.get(0));

        written.clear();
        eventBus.post(new VolumeChangeEvent(false, 25));
        player.playSound(MUSIC);
        assertSamples(5000, written.get(0));
        verify(cue, times(1)).setVolume(anyInt());
    }

    @Test
    void soundDisabledByTheSystemPropertyIgnoresVolumeChanges() {
        final GamePreferences preferences = mock(GamePreferences.class);
        when(preferences.isSoundEnabled()).thenReturn(false);
        final SuccessCuePlayer cue = mock(SuccessCuePlayer.class);
        final MusicPlayer player = player();
        player.setVolume(50);
        final EventBus eventBus = new EventBus();
        final MusicController controller = new MusicController(player, cue,
                preferences);
        eventBus.register(controller);

        eventBus.post(new VolumeChangeEvent(false, 100));
        eventBus.post(new VolumeChangeEvent(true, 100));
        controller.start(100);
        verifyZeroInteractions(cue, streamReader);

        // Neither change reached the music, which keeps its earlier volume.
        player.playSound(MUSIC);
        assertSamples(10000, written.get(0));
    }

    private MusicPlayer player() {
        return new MusicPlayer(streamReader, format -> lines.open(format));
    }

    private void assertStreamsClosed(final int expected) {
        assertEquals(expected, opened.size());
        opened.forEach(stream -> assertTrue(stream.closed));
    }

    private static void assertSamples(final int expected, final byte[] block) {
        for (final short sample : samples(List.of(block))) {
            assertEquals(expected, sample);
        }
    }

    /** Serves music of exactly these samples in the given format. */
    private void musicOf(final short[] samples, final AudioFormat format) {
        final byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            pcm[i * 2] = (byte) samples[i];
            pcm[i * 2 + 1] = (byte) (samples[i] >> 8);
        }
        final byte[] music = wav(pcm, format,
                pcm.length / format.getFrameSize());
        reset(streamReader);
        when(streamReader.openStream(MUSIC)).thenAnswer(invocation -> {
            final ClosingStream out = new ClosingStream(music);
            opened.add(out);
            return out;
        });
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
