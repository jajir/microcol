package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.RoyalRoster;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.RoyalScenarioFixtures;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.store.PlayerPo;

/**
 * The Crown's robot sends its reserve from the roster on its own turn (R-0186),
 * in real royal turns.
 */
class KingRobotDispatchTest {

    private static int reserveGround(final RoyalRoster crown) {
        return crown.getCount(Family.INFANTRY, State.RESERVE)
                + crown.getCount(Family.CAVALRY, State.RESERVE)
                + crown.getCount(Family.ARTILLERY, State.RESERVE);
    }

    private static int shippedGround(final RoyalRoster crown) {
        return crown.getCount(Family.INFANTRY, State.SHIPPED)
                + crown.getCount(Family.CAVALRY, State.SHIPPED)
                + crown.getCount(Family.ARTILLERY, State.SHIPPED);
    }

    @Test
    void declaredCrownSendsEveryReserveShipOnItsFirstTurn() throws Exception {
        try (RoyalTurns turns = new RoyalTurns(
                RoyalScenarioFixtures.declaredCrown())) {
            turns.nextRoyalTurn();

            final RoyalRoster crown = turns.king.getRoyalRoster();
            assertEquals(2, crown.getCount(Family.MAN_OF_WAR, State.SHIPPED));
            assertEquals(0, crown.getCount(Family.MAN_OF_WAR, State.RESERVE));
            assertEquals(0, reserveGround(crown));
            assertEquals(12, shippedGround(crown));
            // Every shipped member is exactly one live unit, and no unit of the
            // Crown exists without its member.
            assertEquals(14, turns.king.getAllUnits().size());
            turns.king.getAllUnits().forEach(unit -> assertTrue(
                    crown.findByUnitId(unit.getId()).isPresent()));
            assertEquals("true", turns.king.getExtraData().get("refWasSend"));
        }
    }

    @Test
    void undeclaredCrownKeepsItsReserve() throws Exception {
        try (RoyalTurns turns = new RoyalTurns(RoyalScenarioFixtures
                .crown(org.microcol.model.Difficulty.CONQUISTADOR, false))) {
            turns.nextRoyalTurn();
            turns.nextRoyalTurn();

            final RoyalRoster crown = turns.king.getRoyalRoster();
            assertEquals(2, crown.getCount(Family.MAN_OF_WAR, State.RESERVE));
            assertEquals(0, crown.getCount(Family.MAN_OF_WAR, State.SHIPPED));
            assertEquals(12, reserveGround(crown));
            assertTrue(turns.king.getAllUnits().isEmpty());
            assertNull(turns.king.getExtraData().get("refWasSend"));
        }
    }

    @Test
    void scenarioThatAlreadySentItsExpeditionDoesNotGetASecondArmy()
            throws Exception {
        final GameModelPo authored = RoyalScenarioFixtures.declaredCrown();
        for (final PlayerPo player : authored.getModel().getGameManager()
                .getPlayers()) {
            if (RoyalScenarioFixtures.KING.equals(player.getName())) {
                player.getExtraData().put("refWasSend", "true");
            }
        }
        try (RoyalTurns turns = new RoyalTurns(authored)) {
            turns.nextRoyalTurn();
            turns.nextRoyalTurn();

            final RoyalRoster crown = turns.king.getRoyalRoster();
            assertFalse(crown.hasSailed());
            assertEquals(12, reserveGround(crown));
            assertEquals(2, crown.getCount(Family.MAN_OF_WAR, State.RESERVE));
            assertTrue(turns.king.getAllUnits().stream()
                    .noneMatch(unit -> unit.getType() == UnitType.MAN_OF_WAR));
        }
    }
}
