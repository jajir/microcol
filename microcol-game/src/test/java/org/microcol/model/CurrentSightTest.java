package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.event.UnitMovedStepFinishedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.model.store.ColonySightingPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Explored terrain is permanent, while foreign units and colonies are known
 * only from present sight or from a remembered observation.
 */
class CurrentSightTest {
    private static final Location MONTREAL = Location.of(17, 21);
    private static final Location QUEBEC = Location.of(17, 10);
    /** Two tiles from Montreal: inside a mounted scout's sight only. */
    private static final Location NEAR_MONTREAL = Location.of(19, 21);
    private static final Location FAR_FROM_MONTREAL = Location.of(21, 21);
    private static final Location SCOUT_AT_QUEBEC = Location.of(19, 10);
    private static final Location ENTRY = Location.of(21, 12);
    private static final Location EXIT = Location.of(23, 12);

    private final Gson gson = new Gson();
    private Model model;
    private Player dutch;
    private Player france;
    private Player king;
    private UnitFreeColonist scout;

    @BeforeEach
    void startFreePlay() {
        model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        dutch = model.getPlayerByName("Dutch");
        france = model.getPlayerByName("France");
        king = model.getPlayerByName("France's King");
        scout = (UnitFreeColonist) model.getUnitById(0);
        scout.setMounted(true);
    }

    private void moveScout(final Location to) {
        scout.placeToLocation(to);
        dutch.revealMapForUnit(scout);
    }

    private void passRound() {
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        for (int i = 0; i < 12 && model.getCalendar()
                .getNumberOfPlayedTurns() == before; i++) {
            model.endTurn();
        }
        assertTrue(model.getCalendar().getNumberOfPlayedTurns() > before);
    }

    private void captureMontrealByFrenchKing() {
        final Unit force = model.getUnitById(2);
        force.takeOver(king);
        // The royal force wins the colony's defense battle next to it.
        ColonyBattles.capture(model, model.getColonyAt(MONTREAL).orElseThrow(),
                force);
    }

    @Test
    void departureKeepsExplorationButHidesForeignUnitsAndLiveColony() {
        moveScout(SCOUT_AT_QUEBEC);
        assertTrue(dutch.isCurrentlyVisible(QUEBEC));
        assertEquals(List.of(model.getUnitById(6)),
                dutch.getObservedUnitsAt(QUEBEC));
        assertEquals("Quebec",
                dutch.getKnownColonyAt(QUEBEC).orElseThrow().getColonyName());

        moveScout(NEAR_MONTREAL);
        assertTrue(dutch.isVisible(QUEBEC));
        assertFalse(dutch.isCurrentlyVisible(QUEBEC));
        assertTrue(dutch.getObservedUnitsAt(QUEBEC).isEmpty());
        assertEquals(List.of(model.getUnitById(6)), model.getUnitsAt(QUEBEC));

        moveScout(SCOUT_AT_QUEBEC);
        assertTrue(dutch.isCurrentlyVisible(QUEBEC));
        assertEquals(List.of(model.getUnitById(6)),
                dutch.getObservedUnitsAt(QUEBEC));
    }

    @Test
    void foreignUnitEntryAndExitFollowPresentSight() {
        moveScout(SCOUT_AT_QUEBEC);
        final Unit french = model.getUnitById(6);
        french.placeToLocation(EXIT);
        assertTrue(dutch.getObservedUnitsAt(EXIT).isEmpty());
        assertFalse(dutch.isCurrentlyVisible(EXIT));

        french.placeToLocation(ENTRY);
        assertEquals(List.of(french), dutch.getObservedUnitsAt(ENTRY));

        french.placeToLocation(EXIT);
        assertTrue(dutch.getObservedUnitsAt(EXIT).isEmpty());
        assertTrue(dutch.getObservedUnitsAt(ENTRY).isEmpty());
        assertTrue(dutch.isVisible(ENTRY));
    }

    @Test
    void sightLostByMovingOrRemovalIsImmediate() {
        moveScout(SCOUT_AT_QUEBEC);
        assertTrue(dutch.isCurrentlyVisible(QUEBEC));
        scout.placeToLocation(FAR_FROM_MONTREAL);
        assertFalse(dutch.isCurrentlyVisible(QUEBEC));
        scout.placeToLocation(SCOUT_AT_QUEBEC);
        assertTrue(dutch.isCurrentlyVisible(QUEBEC));
        model.destroyUnit(scout);
        assertFalse(dutch.isCurrentlyVisible(QUEBEC));
        assertTrue(dutch.isVisible(QUEBEC));
    }

    @Test
    void hiddenChangesDoNotRefreshMemoryUntilReobserved() {
        moveScout(NEAR_MONTREAL);
        final ColonySighting first = dutch.getKnownColonyAt(MONTREAL)
                .orElseThrow();
        assertEquals("Montreal", first.getColonyName());
        assertSame(france, first.getOwner());
        assertEquals(model.getCalendar().getCurrentYear(),
                first.getObservedYear());

        moveScout(FAR_FROM_MONTREAL);
        assertTrue(dutch.isVisible(MONTREAL));
        assertFalse(dutch.isCurrentlyVisible(MONTREAL));
        passRound();
        captureMontrealByFrenchKing();

        final ColonySighting remembered = dutch.getKnownColonyAt(MONTREAL)
                .orElseThrow();
        assertSame(france, remembered.getOwner());
        assertEquals(first.getObservedTurn(), remembered.getObservedTurn());
        assertEquals(first.getObservedYear(), remembered.getObservedYear());
        assertSame(king, model.getColonyAt(MONTREAL).orElseThrow().getOwner());

        moveScout(NEAR_MONTREAL);
        final ColonySighting updated = dutch.getKnownColonyAt(MONTREAL)
                .orElseThrow();
        assertSame(king, updated.getOwner());
        assertTrue(updated.getObservedTurn() > first.getObservedTurn());
    }

    @Test
    void queriesAndReloadNeverRefreshHiddenFacts() {
        moveScout(NEAR_MONTREAL);
        final ColonySighting first = dutch.getKnownColonyAt(MONTREAL)
                .orElseThrow();
        moveScout(FAR_FROM_MONTREAL);
        captureMontrealByFrenchKing();

        final String before = gson.toJson(dutch.save().getColonySightings());
        dutch.getKnownColonies();
        dutch.getKnownColonyAt(MONTREAL);
        dutch.getObservedUnitsAt(MONTREAL);
        assertEquals(before, gson.toJson(dutch.save().getColonySightings()));

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player restoredDutch = restored.getPlayerByName("Dutch");
        final ColonySighting loaded = restoredDutch.getKnownColonyAt(MONTREAL)
                .orElseThrow();
        assertEquals("Montreal", loaded.getColonyName());
        assertEquals("France", loaded.getOwner().getName());
        assertEquals(first.getObservedTurn(), loaded.getObservedTurn());
        assertEquals(first.getObservedYear(), loaded.getObservedYear());
        assertTrue(restoredDutch.isVisible(MONTREAL));
        assertFalse(restoredDutch.isCurrentlyVisible(MONTREAL));
        assertEquals(before,
                gson.toJson(restoredDutch.save().getColonySightings()));
        assertEquals("France's King", restored.getColonyAt(MONTREAL)
                .orElseThrow().getOwner().getName());
    }

    @Test
    void ownColonySeesItsTileAndEightNeighborsWithEveryResidentInside() {
        assertTrue(france.getUnits().stream().allMatch(
                unit -> unit.getLocation().getDistanceChebyshev(MONTREAL) > 2));
        for (int x = 16; x <= 18; x++) {
            for (int y = 20; y <= 22; y++) {
                assertTrue(france.isCurrentlyVisible(Location.of(x, y)));
            }
        }
        assertFalse(france.isCurrentlyVisible(NEAR_MONTREAL));

        final Unit visitor = model.getUnitById(1);
        visitor.placeToLocation(Location.of(18, 21));
        assertEquals(List.of(visitor),
                france.getObservedUnitsAt(Location.of(18, 21)));
        visitor.placeToLocation(NEAR_MONTREAL);
        assertTrue(france.getObservedUnitsAt(NEAR_MONTREAL).isEmpty());
        assertEquals(List.of(visitor), model.getUnitsAt(NEAR_MONTREAL));
    }

    @Test
    void captureMovesColonySightToTheNewOwnerAndKeepsExploration() {
        final Location neighbor = Location.of(18, 21);
        assertTrue(france.isCurrentlyVisible(neighbor));
        assertFalse(king.isCurrentlyVisible(neighbor));
        captureMontrealByFrenchKing();

        assertFalse(france.isCurrentlyVisible(MONTREAL));
        assertFalse(france.isCurrentlyVisible(neighbor));
        assertTrue(king.isCurrentlyVisible(neighbor));
        assertSame(king,
                france.getKnownColonyAt(MONTREAL).orElseThrow().getOwner());
    }

    @Test
    void foreignMovesOutsidePresentSightProduceNoAnimationEvents() {
        moveScout(SCOUT_AT_QUEBEC);
        moveScout(NEAR_MONTREAL);
        final Unit french = model.getUnitById(6);
        assertTrue(dutch.isVisible(QUEBEC));

        assertFalse(new UnitMovedStepStartedEvent(model, french, QUEBEC,
                Location.of(18, 10), Direction.east).canPlayerSeeMove(dutch));
        assertFalse(new UnitMovedStepFinishedEvent(model, french, QUEBEC,
                Location.of(18, 10)).canPlayerSeeMove(dutch));
        assertTrue(new UnitMovedStepStartedEvent(model, french, MONTREAL,
                Location.of(18, 21), Direction.east).canPlayerSeeMove(dutch));
        assertTrue(new UnitMovedStepFinishedEvent(model, french,
                Location.of(18, 21), MONTREAL).canPlayerSeeMove(dutch));
        // A step leaving present sight is not animated.
        assertFalse(new UnitMovedStepStartedEvent(model, french, MONTREAL,
                Location.of(16, 21), Direction.west).canPlayerSeeMove(dutch));
    }

    @Test
    void foreignFightsAreShownOnlyInsidePresentSight() {
        final Unit french = model.getUnitById(6);
        final Unit royal = model.getUnitById(2);
        royal.placeToLocation(Location.of(18, 10));
        royal.takeOver(king);
        final UnitAttackedEvent foreignFight = new UnitAttackedEvent(model,
                french, royal, royal, false);
        final UnitAttackedEvent ownFight = new UnitAttackedEvent(model, french,
                scout, scout, false);

        moveScout(NEAR_MONTREAL);
        assertFalse(foreignFight.canPlayerSeeAttack(dutch));
        assertTrue(ownFight.canPlayerSeeAttack(dutch));
        moveScout(SCOUT_AT_QUEBEC);
        assertTrue(foreignFight.canPlayerSeeAttack(dutch));
    }

    @Test
    void ownedColonyExploresItsNeighborsFromAnEmptyMapAndKeepsThemAfterLoss() {
        // Free play starts with an empty explored map and Montreal has no
        // unit outside, so only the colony can have explored its squares.
        for (int x = 16; x <= 18; x++) {
            for (int y = 20; y <= 22; y++) {
                assertTrue(france.isVisible(Location.of(x, y)));
            }
        }
        assertFalse(france.isVisible(NEAR_MONTREAL));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.getPlayerByName("France")
                .isVisible(Location.of(16, 20)));

        captureMontrealByFrenchKing();
        assertFalse(france.isCurrentlyVisible(Location.of(16, 20)));
        assertTrue(france.isVisible(Location.of(16, 20)));
        assertTrue(king.isVisible(Location.of(18, 22)));
        // Foreign colonies are not explored by looking at them from afar.
        assertFalse(dutch.isVisible(Location.of(16, 20)));
    }

    @Test
    void halfVisibleForeignFightDisclosesNothing() {
        final Unit french = model.getUnitById(6);
        final Unit royal = model.getUnitById(2);
        royal.placeToLocation(Location.of(21, 12));
        royal.takeOver(king);
        final UnitAttackedEvent fight = new UnitAttackedEvent(model, french,
                royal, royal, false);

        // Only the royal unit's tile is in sight.
        moveScout(Location.of(23, 12));
        assertTrue(dutch.isCurrentlyVisible(Location.of(21, 12)));
        assertFalse(dutch.isCurrentlyVisible(QUEBEC));
        assertFalse(fight.canPlayerSeeAttack(dutch));
        // Only the French tile is in sight.
        moveScout(Location.of(15, 10));
        assertTrue(dutch.isCurrentlyVisible(QUEBEC));
        assertFalse(dutch.isCurrentlyVisible(Location.of(21, 12)));
        assertFalse(fight.canPlayerSeeAttack(dutch));
        // Both tiles in sight.
        moveScout(Location.of(19, 11));
        assertTrue(fight.canPlayerSeeAttack(dutch));
    }

    @Test
    void invalidSavedMemoryIsRejected() {
        final ModelPo saved = model.save();
        final PlayerPo savedDutch = saved.getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow();
        final ColonySightingPo broken = new ColonySightingPo();
        broken.setLocation(MONTREAL);
        broken.setColonyName("Montreal");
        broken.setOwnerName("France");
        broken.setObservedTurn(
                model.getCalendar().getNumberOfPlayedTurns() + 100);
        savedDutch.getColonySightings().add(broken);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));

        broken.setObservedTurn(0);
        broken.setOwnerName(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
