package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.gui.SuccessCuePlayer;
import org.microcol.mock.RecordingSuccessCuePlayer;

/**
 * The mission success cue the game requested (R-0228). A scenario that uses
 * this page binds {@link RecordingSuccessCuePlayer} in place of the real
 * player, so nothing is heard and only the requests are counted. The victory
 * itself stays visible through the other pages.
 */
public final class SuccessCuePage extends AbstractScreen {

    public SuccessCuePage(final TestContext context) {
        super(context);
    }

    /**
     * From now on, every cue fails like an unavailable audio device.
     */
    public void makeAudioDeviceUnavailable() {
        recorder().makeDeviceUnavailable();
    }

    /**
     * Exactly the given number of success cues was requested so far.
     *
     * @param expected expected number of requested cues
     */
    public void verifySuccessCueRequests(final int expected) {
        assertEquals(expected, recorder().getRequests(),
                "Requested mission success cues");
    }

    private RecordingSuccessCuePlayer recorder() {
        return (RecordingSuccessCuePlayer) getContext()
                .getClassFromGuice(SuccessCuePlayer.class);
    }

}
