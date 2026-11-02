package org.akropia.gui.event.model;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.akropia.gui.SuccessCuePlayer;

/**
 * When the victory message of a finished mission asks for the success cue
 * (R-0228).
 */
class MissionVictoryCueTest {

    private final SuccessCuePlayer player = mock(SuccessCuePlayer.class);

    private final MissionVictoryCue cue = new MissionVictoryCue(player);

    @Test
    void aWonMissionRequestsTheCueOnce() {
        cue.onFinishMessageShown(true, "game-1");
        verify(player).play();
    }

    @Test
    void anEndWithoutVictoryNeverRequestsTheCue() {
        cue.onFinishMessageShown(false, "game-1");
        cue.onFinishMessageShown(false, "game-2");
        verifyZeroInteractions(player);
    }

    @Test
    void aRepeatedVictoryOfTheSamePlaythroughIsSilent() {
        cue.onFinishMessageShown(true, "game-1");
        // The same notification again, or the same playthrough won again from
        // an earlier save of it.
        cue.onFinishMessageShown(true, "game-1");
        cue.onFinishMessageShown(true, "game-1");
        verify(player, times(1)).play();
    }

    @Test
    void aNewPlaythroughHasItsOwnCue() {
        cue.onFinishMessageShown(true, "game-1");
        cue.onFinishMessageShown(true, "game-2");
        verify(player, times(2)).play();
    }

    @Test
    void aLossBeforeTheVictoryDoesNotUseUpTheCue() {
        cue.onFinishMessageShown(false, "game-1");
        cue.onFinishMessageShown(true, "game-1");
        verify(player).play();
    }

    @Test
    void aFailingPlayerDoesNotReachTheVictoryMessage() {
        doThrow(new IllegalStateException("no audio device")).when(player)
                .play();
        cue.onFinishMessageShown(true, "game-1");
        cue.onFinishMessageShown(true, "game-2");
        verify(player, times(2)).play();
    }

}
