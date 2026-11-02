package org.akropia.gui.event.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.akropia.model.Model;
import org.akropia.model.Player;

/**
 * Save and leave-save offer the manual Save browser at a ready human turn and
 * at a postponed spring-1800 score choice, which the model saves and asks again
 * on Continue (R-0193, R-0387). Other pauses stay without a save.
 */
class HumanSaveBoundaryTest {

    private static Model model(final boolean running,
            final boolean awaitingScoreChoice, final boolean human) {
        final Model model = mock(Model.class);
        final Player current = mock(Player.class);
        when(model.isGameRunning()).thenReturn(running);
        when(model.isAwaitingScoreChoice()).thenReturn(awaitingScoreChoice);
        when(model.getCurrentPlayer()).thenReturn(current);
        when(current.isHuman()).thenReturn(human);
        return model;
    }

    @Test
    void aReadyHumanTurnIsASaveBoundary() {
        assertTrue(GameModelController
                .isHumanSaveBoundary(model(true, false, true)));
    }

    @Test
    void aPostponedScoreChoiceOfTheHumanIsASaveBoundary() {
        assertTrue(GameModelController
                .isHumanSaveBoundary(model(false, true, true)));
    }

    @Test
    void turnPreparationAndOtherPausesAreNoSaveBoundary() {
        // Preparation, an immigrant or tax choice: neither running nor
        // awaiting the score choice.
        assertFalse(GameModelController
                .isHumanSaveBoundary(model(false, false, true)));
    }

    @Test
    void aComputerTurnIsNoSaveBoundary() {
        assertFalse(GameModelController
                .isHumanSaveBoundary(model(true, false, false)));
        assertFalse(GameModelController
                .isHumanSaveBoundary(model(false, true, false)));
    }
}
