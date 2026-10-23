package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.Location;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.RebellionFixtures;
import org.microcol.model.RoyalRoster;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.RoyalScenarioFixtures;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.unit.UnitWithCargo;

/**
 * The Crown's robot sails its carriers, lands and attacks, returns for the
 * remaining reserve and never invents a ship or an army (R-0186). Every test
 * plays real royal turns.
 */
class KingRobotRouteTest {

    private static final int RESERVE_INFANTRY = 7;

    private static final int RESERVE_CAVALRY = 12;

    private static final int CARRIER_MEMBER = 15;

    /** Coastal colony that the Crown holds in the logistics scenarios. */
    private static final int DELFT = 1;

    /**
     * The only resident of Delft. The Crown holds it as a colonist after taking
     * Delft; a colonist is a civilian, never a royal force.
     */
    private static final int DELFT_RESIDENT = 6;

    /**
     * The Crown holds Delft, which no carrier of these scenarios targets first.
     * Two living ground units with every member rebel-held would win the
     * rebellion at the round end (R-0191), and these tests need the war open.
     */
    private static GameModelPo crownHoldsDelft(final GameModelPo data) {
        RebellionFixtures.transfer(data.getModel(), RoyalScenarioFixtures.KING,
                DELFT);
        return data;
    }

    private static String ledger(final RoyalRoster crown) {
        return crown.getMembers().stream()
                .map(member -> member.getId() + ":" + member.getState() + ":"
                        + member.getUnitId().orElse(-1) + ":"
                        + member.getTargetColonyId().orElse(-1) + ":"
                        + member.getBlockedTurns())
                .collect(Collectors.joining(","));
    }

    private static int reserveGround(final RoyalRoster crown) {
        return crown.getCount(Family.INFANTRY, State.RESERVE)
                + crown.getCount(Family.CAVALRY, State.RESERVE)
                + crown.getCount(Family.ARTILLERY, State.RESERVE);
    }

    private static int lostGround(final RoyalRoster crown) {
        return crown.getCount(Family.INFANTRY, State.DESTROYED)
                + crown.getCount(Family.CAVALRY, State.DESTROYED)
                + crown.getCount(Family.ARTILLERY, State.DESTROYED);
    }

    /**
     * Each shipped member is one live royal unit of its family, no unit is
     * bound twice and no man-of-war or regular exists outside the roster.
     * Colonists of a captured colony may belong to the Crown; they are not
     * royal forces and are not counted here.
     */
    private static void assertEveryCrownUnitHasItsMember(
            final RoyalTurns turns) {
        assertEquals(List.of(),
                RoyalScenarioFixtures.deploymentViolations(turns.model));
        final long shipped = turns.king.getRoyalRoster().getMembers().stream()
                .filter(member -> member.getState() == State.SHIPPED).count();
        assertEquals(shipped,
                RoyalScenarioFixtures.boundUnits(turns.model).size(),
                "A shipped member has no live unit");
    }

    /** Identities of every unit the Crown owns, royal or civilian. */
    private static Set<Integer> crownUnitIds(final RoyalTurns turns) {
        return turns.king.getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Crown units that no roster member names: captured colonists, which are
     * civilians and never royal forces.
     */
    private static Set<Integer> unboundCrownUnitIds(final RoyalTurns turns) {
        final RoyalRoster crown = turns.king.getRoyalRoster();
        return turns.king.getAllUnits().stream()
                .filter(unit -> crown.findByUnitId(unit.getId()).isEmpty())
                .map(Unit::getId).collect(Collectors.toSet());
    }

    /** Royal units of the roster that stand on land. */
    private static List<Unit> royalAshore(final RoyalTurns turns) {
        return RoyalScenarioFixtures.boundUnits(turns.model).stream().filter(
                unit -> !unit.getType().isShip() && unit.isAtPlaceLocation())
                .collect(Collectors.toList());
    }

    @Test
    void firstWaveLandsOnLegalShoreAndAttacksWithinTenRoyalTurns()
            throws Exception {
        try (RoyalTurns turns = new RoyalTurns(
                RoyalScenarioFixtures.declaredCrown())) {
            final AtomicInteger royalAttacks = new AtomicInteger();
            turns.model.addListener(new ModelListenerAdapter() {
                @Override
                public void onUnitAttacked(final UnitAttackedEvent event) {
                    if (event.getAttacker().getOwner() == turns.king) {
                        royalAttacks.incrementAndGet();
                    }
                }
            });
            int landedTurn = 0;
            int attackTurn = 0;
            // A landing attack can take the last coastal colony, and then the
            // rebellion is lost in the Crown's turn (R-0192); the attack stays
            // the observed event and the loss is checked, not ignored.
            String loss = null;
            for (int turn = 1; turn <= 10 && attackTurn == 0
                    && loss == null; turn++) {
                loss = turns.nextRoyalTurnOrLoss();
                if (landedTurn == 0 && !royalAshore(turns).isEmpty()) {
                    landedTurn = turn;
                    for (final Unit soldier : royalAshore(turns)) {
                        assertTrue(turns.model.getMap()
                                .getTerrainTypeAt(soldier.getLocation())
                                .isLand(), "A soldier landed on water");
                        assertTrue(
                                turns.model.getColonyAt(soldier.getLocation())
                                        .isEmpty(),
                                "A soldier landed in a colony");
                    }
                }
                if (royalAttacks.get() > 0) {
                    attackTurn = turn;
                }
            }
            if (loss != null) {
                assertTrue(loss.startsWith("REBELLION_"),
                        "The game ended without a rebellion loss: " + loss);
            }
            assertTrue(landedTurn > 0, "No soldier landed in ten royal turns");
            assertTrue(attackTurn >= landedTurn && attackTurn > 0,
                    "No royal attack in ten royal turns");
            assertEveryCrownUnitHasItsMember(turns);
            assertEquals(0, reserveGround(turns.king.getRoyalRoster()));
        }
    }

    @Test
    void blockedRouteIsCountedOncePerTurnAndRetargetsOnlyAfterThreeTurns()
            throws Exception {
        // A loaded carrier in open water with land on all eight sides: every
        // route from its square is blocked.
        final Location at = Location.of(40, 15);
        final GameModelPo data = crownHoldsDelft(RoyalScenarioFixtures
                .midWar(at, true, new int[0], new int[] { 1, 8 }));
        RoyalScenarioFixtures.wallAround(data, at);
        try (RoyalTurns turns = new RoyalTurns(data)) {
            final RoyalRoster crown = turns.king.getRoyalRoster();
            final List<String> seen = new ArrayList<>();
            for (int turn = 1; turn <= 4; turn++) {
                turns.nextRoyalTurn();
                final RoyalRoster.Member carrier = crown
                        .findMember(CARRIER_MEMBER).orElseThrow();
                seen.add(carrier.getBlockedTurns() + ":"
                        + carrier.getTargetColonyId().orElse(-1));
            }
            // New Amsterdam (0) is nearest; the third blocked turn moves on to
            // Haarlem (2), the next nearest known colony.
            assertEquals(List.of("1:0", "2:0", "0:2", "1:2"), seen);
            final Unit carrier = turns.model
                    .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
            assertEquals(at, carrier.getLocation(),
                    "A blocked ship never moves or teleports");
            assertFalse(((UnitWithCargo) carrier).getCargo().isEmpty());
            assertEquals(2, turns.king.getAllUnits().stream()
                    .filter(unit -> unit.isAtCargoSlot()).count());
            assertEveryCrownUnitHasItsMember(turns);
        }
    }

    @Test
    void survivingCarrierReturnsAndCarriesTheRemainingReserve()
            throws Exception {
        final GameModelPo data = crownHoldsDelft(RoyalScenarioFixtures.midWar(
                Location.of(30, 14), true,
                new int[] { RESERVE_INFANTRY, RESERVE_CAVALRY }, new int[0]));
        try (RoyalTurns turns = new RoyalTurns(data)) {
            final RoyalRoster crown = turns.king.getRoyalRoster();
            int reloadedTurn = 0;
            for (int turn = 1; turn <= 25 && reloadedTurn == 0; turn++) {
                turns.nextRoyalTurn();
                // An empty carrier alone is weak, but it is still fetching
                // the reserve, so the Crown is not conquered meanwhile.
                assertNull(turns.king.getExtraData().get("kingWasConquered"),
                        "The Crown was conquered while its reserve was on its "
                                + "way");
                if (reserveGround(crown) == 0) {
                    reloadedTurn = turn;
                }
            }
            assertTrue(reloadedTurn > 0,
                    "The carrier did not take the reserve in 25 royal turns");
            assertSecondWaveAboard(turns);
        }
    }

    @Test
    void continueWhileTheCarrierIsHomewardKeepsTheLedgerAndFinishesTheWave()
            throws Exception {
        final GameModelPo data = crownHoldsDelft(RoyalScenarioFixtures.midWar(
                Location.of(30, 14), true,
                new int[] { RESERVE_INFANTRY, RESERVE_CAVALRY }, new int[0]));
        final String before;
        final int remaining;
        try (RoyalTurns turns = new RoyalTurns(data)) {
            final Unit carrier = turns.model
                    .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
            int guard = 0;
            while (!isHomeward(carrier)) {
                assertTrue(++guard <= 12, "The carrier never left for Europe");
                turns.nextRoyalTurn();
            }
            before = ledger(turns.king.getRoyalRoster());
            remaining = ((PlaceHighSea) carrier.getPlace()).getRemainigTurns();
            data.setModel(turns.model.save());
        }

        // Continue: a new game from the save, in the middle of the crossing.
        try (RoyalTurns turns = new RoyalTurns(data)) {
            final Unit carrier = turns.model
                    .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
            assertEquals(before, ledger(turns.king.getRoyalRoster()));
            assertTrue(isHomeward(carrier));
            assertEquals(remaining,
                    ((PlaceHighSea) carrier.getPlace()).getRemainigTurns());
            int reloadedTurn = 0;
            for (int turn = 1; turn <= 15 && reloadedTurn == 0; turn++) {
                turns.nextRoyalTurn();
                if (reserveGround(turns.king.getRoyalRoster()) == 0) {
                    reloadedTurn = turn;
                }
            }
            assertTrue(reloadedTurn > 0, "The carrier did not reload");
            assertSecondWaveAboard(turns);
        }
    }

    private static boolean isHomeward(final Unit carrier) {
        return carrier.getPlace() instanceof PlaceHighSea
                && ((PlaceHighSea) carrier.getPlace()).isTravelToEurope();
    }

    /**
     * The same carrier now holds exactly the two members that waited in
     * reserve; nothing was created beyond them and nothing lost was restored.
     */
    private static void assertSecondWaveAboard(final RoyalTurns turns) {
        final RoyalRoster crown = turns.king.getRoyalRoster();
        assertEquals(0, reserveGround(crown));
        assertEquals(12, lostGround(crown), "Lost members must stay lost");
        assertEquals(State.DESTROYED,
                crown.findMember(CARRIER_MEMBER + 1).orElseThrow().getState());
        assertEquals(RoyalScenarioFixtures.CARRIER_ID,
                crown.findMember(CARRIER_MEMBER).orElseThrow().getUnitId()
                        .orElseThrow().intValue());
        final UnitWithCargo carrier = (UnitWithCargo) turns.model
                .getUnitById(RoyalScenarioFixtures.CARRIER_ID);
        assertEquals(UnitType.MAN_OF_WAR, carrier.getType());
        final List<Integer> aboard = carrier.getCargo().getSlots().stream()
                .flatMap(slot -> slot.getUnit().stream()).map(Unit::getId)
                .collect(Collectors.toList());
        assertEquals(2, aboard.size());
        for (final int memberId : new int[] { RESERVE_INFANTRY,
                RESERVE_CAVALRY }) {
            final RoyalRoster.Member member = crown.findMember(memberId)
                    .orElseThrow();
            assertEquals(State.SHIPPED, member.getState());
            assertTrue(aboard.contains(member.getUnitId().orElseThrow()));
        }
        assertEquals(3, RoyalScenarioFixtures.boundUnits(turns.model).size(),
                "Carrier and two passengers only; no ship was invented");
        assertEquals(Set.of(DELFT_RESIDENT), unboundCrownUnitIds(turns),
                "Only the captured Delft colonist is outside the roster");
        assertEveryCrownUnitHasItsMember(turns);
    }

    @Test
    void carrierLostWhileReserveRemainsStrandsTheReserve() throws Exception {
        final GameModelPo data = crownHoldsDelft(RoyalScenarioFixtures.midWar(
                null, false, new int[] { RESERVE_INFANTRY, RESERVE_CAVALRY },
                new int[0]));
        try (RoyalTurns turns = new RoyalTurns(data)) {
            final RoyalRoster crown = turns.king.getRoyalRoster();
            final String before = ledger(crown);
            for (int turn = 1; turn <= 6; turn++) {
                turns.nextRoyalTurn();
            }
            assertEquals(before, ledger(crown),
                    "With no ship the reserve stays exactly as it was");
            assertEquals(2, reserveGround(crown));
            assertEquals(Set.of(DELFT_RESIDENT), crownUnitIds(turns),
                    "No ship or soldier may be invented; the captured Delft "
                            + "colonist is the Crown's only unit");
            assertEquals(List.of(),
                    RoyalScenarioFixtures.deploymentViolations(turns.model));
            // A declared revolution never uses the legacy military-score flag,
            // even when the stranded reserve can no longer be carried (R-0191).
            assertNull(turns.king.getExtraData().get("kingWasConquered"));
        }
    }

    @Test
    void crownWithNothingLeftLetsTheRebelsWinWithoutTheLegacyFlag()
            throws Exception {
        final GameModelPo data = RoyalScenarioFixtures
                .midWar(Location.of(30, 14), true, new int[0], new int[0]);
        try (RoyalTurns turns = new RoyalTurns(data)) {
            // Every member is rebel-held and no Crown ground unit is alive, so
            // the rebels win at the round end; the game then ends.
            assertEquals(GameOverEvaluator.REASON_REBELLION_VICTORY,
                    turns.nextRoyalTurnOrLoss());
            assertNull(turns.king.getExtraData().get("kingWasConquered"));
        }
    }
}
