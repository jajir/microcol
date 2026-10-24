package org.microcol.model.turnevent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

public class TurnEventCriticalityTest {

    private final static String PLAYER_NAME = "Jan";

    @Test
    public void verify_famine_and_colony_loss_notices_are_critical() {
        assertTrue(TurnEventCriticality.isCritical(
                new TurnEventFamineWillPlagueColony(PLAYER_NAME, 1, "Delft")));
        assertTrue(TurnEventCriticality
                .isCritical(mock(TurnEventFaminePlagueColony.class)));
        assertTrue(TurnEventCriticality.isCritical(
                new TurnEventColonyWasLost(PLAYER_NAME, 1, "Delft")));
        assertTrue(TurnEventCriticality.isCritical(
                new TurnEventColonyWasDestroyed(PLAYER_NAME, 1, "Delft")));
    }

    @Test
    public void verify_arrivals_and_new_units_are_routine() {
        assertFalse(TurnEventCriticality
                .isCritical(new TurnEventShipComeToEuropePort(PLAYER_NAME)));
        assertFalse(TurnEventCriticality
                .isCritical(new TurnEventShipComeToHighSeas(PLAYER_NAME)));
        assertFalse(TurnEventCriticality
                .isCritical(new TurnEventNewUnitInEurope(PLAYER_NAME)));
    }

    @Test
    public void verify_missing_notice_is_rejected() {
        assertThrows(NullPointerException.class,
                () -> TurnEventCriticality.isCritical(null));
    }

}
