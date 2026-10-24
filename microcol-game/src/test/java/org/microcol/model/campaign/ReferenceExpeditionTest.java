package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.InterventionTarget;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.PoliticalProduction;
import org.microcol.model.RoyalRoster;
import org.microcol.model.TaxState;
import org.microcol.model.Unit;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;

class ReferenceExpeditionTest {

    /** Four colonial nations, then their four Crowns, in one round. */
    private static final int ACTORS_PER_ROUND = 8;

    @TempDir
    Path directory;

    private WorldMapPo terrain(final int highSeaCount, final boolean coast) {
        final WorldMapPo map = new WorldMapPo();
        map.setMaxX(8);
        map.setMaxY(8);
        map.setSeed(1);
        final Map<Location, TerrainType> types = new HashMap<>();
        if (coast) {
            types.put(Location.of(1, 4), TerrainType.GRASSLAND);
        }
        for (int y = 1; y <= highSeaCount; y++) {
            types.put(Location.of(8, y), TerrainType.HIGH_SEA);
        }
        map.setTerrainType(types);
        map.setTrees(Set.of());
        map.setFields(Set.of());
        final VisibilityPo authoredVisibility = new VisibilityPo();
        authoredVisibility.setVisibility(Set.of(Location.of(1, 4)), 8, 8);
        map.setVisibility(authoredVisibility);
        return map;
    }

    @Test
    void completeRosterAcrossNationsAndDifficulties() {
        final WorldMapPo source = terrain(8, true);
        final String[] sourceTiles = source.getTiles().clone();
        final String[] sourceTrees = source.getTrees().clone();
        final String[] sourceFields = source.getFields().clone();
        for (final EuropeanNation selected : EuropeanNation.values()) {
            for (final Difficulty difficulty : Difficulty.values()) {
                final ModelPo po = ReferenceExpedition.create(source, selected,
                        difficulty);
                final ModelPo repeat = ReferenceExpedition.create(source,
                        selected, difficulty);
                assertEquals(
                        po.getUnits().stream()
                                .filter(unit -> unit.getType().isShip())
                                .map(unit -> unit.getPlaceMap().getLocation())
                                .collect(Collectors.toList()),
                        repeat.getUnits().stream()
                                .filter(unit -> unit.getType().isShip())
                                .map(unit -> unit.getPlaceMap().getLocation())
                                .collect(Collectors.toList()));
                assertEquals(8, po.getGameManager().getPlayers().size());
                assertEquals(12, po.getUnits().size());
                assertEquals(6, po.getEuropeanRelations().size());
                assertTrue(po.getEuropeanRelations().stream()
                        .allMatch(relation -> relation
                                .getState() == EuropeanRelationState.PEACE));
                assertEquals(0, po.getNextColonyId());
                assertTrue(po.getColonies().isEmpty());
                assertTrue(po.getMap().getVisibility().getVisibilitySet()
                        .isEmpty());
                assertTrue(po.getGameManager().getPlayers().stream()
                        .allMatch(player -> player.getVisible()
                                .getVisibilitySet().isEmpty()));
                assertEquals(1490, po.getCalendar().getStartYear());
                assertEquals(1850, po.getCalendar().getEndYear());
                assertTrue(po.getCalendar().isScoredPlay());
                assertFalse(po.getGameManager().isGameStarted());
                assertEquals(selected.getPlayerName(),
                        po.getGameManager().getCurrentPlayer());
                assertTrue(po.getUnits().stream().allMatch(
                        unit -> unit.getAction() instanceof UnitActionNoActionPo
                                && unit.getAvailableMoves() == unit.getType()
                                        .getSpeed()));
                final Set<Location> starts = new HashSet<>();
                for (final EuropeanNation nation : EuropeanNation.values()) {
                    final String owner = nation.getPlayerName();
                    assertEquals(1000 - 250 * difficulty.ordinal(), po
                            .getGameManager().getPlayers().stream()
                            .filter(player -> owner.equals(player.getName()))
                            .findFirst().orElseThrow().getGold());
                    final UnitPo ship = po.getUnits().stream()
                            .filter(unit -> owner.equals(unit.getOwnerId())
                                    && unit.getType().isShip())
                            .findFirst().orElseThrow();
                    assertEquals(nation == EuropeanNation.DUTCH
                            ? UnitType.MERCHANTMAN
                            : UnitType.CARAVEL, ship.getType());
                    assertEquals(ship.getType().getSpeed(),
                            ship.getAvailableMoves());
                    assertEquals(2, ship.getCargo().getSlots().size());
                    assertTrue(starts.add(ship.getPlaceMap().getLocation()));
                    if (nation == selected) {
                        assertEquals(ship.getPlaceMap().getLocation(),
                                po.getFocusedField());
                    }
                    final var passengers = po.getUnits().stream()
                            .filter(unit -> owner.equals(unit.getOwnerId())
                                    && unit.getType() == UnitType.COLONIST)
                            .collect(Collectors.toList());
                    assertEquals(2, passengers.size());
                    assertEquals(1, passengers.stream()
                            .filter(UnitPo::isHoldingGuns).count());
                    assertEquals(1, passengers.stream()
                            .filter(unit -> unit.getTools() == 100).count());
                    assertTrue(
                            passengers.stream().noneMatch(UnitPo::isMounted));
                    assertTrue(passengers.stream().allMatch(unit -> ship
                            .getCargo().containsUnitInCargo(unit.getId())));
                }
                final Model loaded = Model.make(po,
                        MissionImpl.getPredefinedGameOverEvaluators());
                loaded.startGame();
                final ModelPo saved = loaded.save();
                assertEquals(12, saved.getUnits().size());
                assertEquals(8, saved.getGameManager().getPlayers().size());
                assertEquals(6, saved.getEuropeanRelations().size());
                final Model restored = Model.make(saved,
                        MissionImpl.getPredefinedGameOverEvaluators());
                assertEquals(difficulty, restored.getDifficulty());
                assertEquals("England", loaded.getCurrentPlayer().getName());
                assertEquals(loaded.getCurrentPlayer().getName(),
                        restored.getCurrentPlayer().getName());
                assertEquals(12, restored.getAllUnits().size());
                for (final EuropeanNation nation : EuropeanNation.values()) {
                    final String owner = nation.getPlayerName();
                    assertEquals(1000 - 250 * difficulty.ordinal(),
                            restored.getPlayerByName(owner).getGold());
                    assertEquals(nation != selected,
                            restored.getPlayerByName(owner).isComputer());
                    final Ship ship = (Ship) restored.getAllUnits().stream()
                            .filter(unit -> unit.isShip()
                                    && owner.equals(unit.getOwner().getName()))
                            .findFirst().orElseThrow();
                    assertEquals(nation == EuropeanNation.DUTCH
                            ? UnitType.MERCHANTMAN
                            : UnitType.CARAVEL, ship.getType());
                    assertEquals(2, ship.getCargo().getSlots().stream()
                            .filter(slot -> slot.isLoadedUnit()).count());
                    final var people = restored.getAllUnits().stream()
                            .filter(unit -> unit.getType() == UnitType.COLONIST
                                    && owner.equals(unit.getOwner().getName()))
                            .map(unit -> (UnitFreeColonist) unit)
                            .collect(Collectors.toList());
                    assertEquals(2, people.size());
                    assertTrue(people.stream().allMatch(Unit::isAtCargoSlot));
                    assertEquals(1, people.stream()
                            .filter(UnitFreeColonist::isHoldingGuns).count());
                    assertEquals(1, people.stream()
                            .filter(unit -> unit.getTools() == 100).count());
                    assertTrue(people.stream()
                            .noneMatch(UnitFreeColonist::isMounted));
                }
            }
        }
        assertEquals(8, source.getMaxX());
        assertEquals(8, source.getMaxY());
        assertArrayEquals(sourceTiles, source.getTiles());
        assertArrayEquals(sourceTrees, source.getTrees());
        assertArrayEquals(sourceFields, source.getFields());
        assertEquals(1, source.getSeed());
        assertEquals(Set.of(Location.of(1, 4)),
                source.getVisibility().getVisibilitySet());
    }

    @Test
    void unsuitableTerrainIsRejectedWithoutMutation() {
        final WorldMapPo few = terrain(3, true);
        final String[] original = few.getTiles().clone();
        assertThrows(IllegalArgumentException.class, () -> ReferenceExpedition
                .create(few, EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
        assertArrayEquals(original, few.getTiles());
        assertThrows(IllegalArgumentException.class,
                () -> ReferenceExpedition.create(terrain(8, false),
                        EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
    }

    static Stream<Arguments> referenceProfiles() {
        return Arrays.stream(EuropeanNation.values())
                .flatMap(nation -> Arrays.stream(Difficulty.values())
                        .map(difficulty -> Arguments.of(nation, difficulty)));
    }

    /**
     * R-0208 model cell: one nation at one difficulty on the bundled reference
     * map. Expected values come from the accepted rule table and the reference
     * start squares, not from the code under test. The owner turn and the disk
     * round trip keep the human and Crown identity, the funds and the profile.
     * The national effects of England, France, the Netherlands and Spain are
     * owned by their own tests, which are mapped in the functional evidence
     * rather than repeated in every cell.
     */
    @ParameterizedTest(name = "{index} {0} at {1}")
    @MethodSource("referenceProfiles")
    void referenceProfileKeepsIdentityExpeditionRulesAndSaveAfterTurn(
            final EuropeanNation nation, final Difficulty difficulty)
            throws IOException {
        final ProfileRule rule = ruleFor(difficulty);
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo game = dao
                .loadFromClassPath(FreePlayMap.REFERENCE.getResource());
        game.setModel(ReferenceExpedition.create(game.getModel().getMap(),
                nation, difficulty));
        assertEquals(1490, game.getModel().getCalendar().getStartYear());
        final Model fresh = Model.make(game.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertIdentityAndRules(fresh, nation, difficulty, rule);
        assertEquals(rule.gold,
                fresh.getPlayerByName(nation.getPlayerName()).getGold());
        assertExpedition(fresh, nation);

        // The unstarted position written to disk, as the UI scenario loads it,
        // keeps the same profile and expedition.
        final Path start = directory.resolve(
                nation.name() + "-" + difficulty.name() + "-start.microcol");
        dao.saveToFile(start.toString(), game);
        final Model loadedStart = Model.make(
                dao.loadFromFile(start.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertIdentityAndRules(loadedStart, nation, difficulty, rule);
        assertExpedition(loadedStart, nation);

        // The current player is readable only once the game has started.
        fresh.startGame();
        assertEquals("England", fresh.getCurrentPlayer().getName());
        endActorsUntil(fresh, nation);
        assertEquals(0, fresh.getCalendar().getNumberOfPlayedTurns());

        // The human's own update ends with its endTurn. The round is not over
        // until the Crowns have acted, so the played turn is still 0 here.
        fresh.endTurn();
        assertEquals(0, fresh.getCalendar().getNumberOfPlayedTurns());
        endActorsUntil(fresh, nation);
        assertEquals(1, fresh.getCalendar().getNumberOfPlayedTurns());
        assertIdentityAndRules(fresh, nation, difficulty, rule);
        assertEquals(rule.gold,
                fresh.getPlayerByName(nation.getPlayerName()).getGold());

        game.setModel(fresh.save());
        final Path file = directory
                .resolve(nation.name() + "-" + difficulty.name() + ".microcol");
        dao.saveToFile(file.toString(), game);
        final Model restored = Model.make(
                dao.loadFromFile(file.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(1, restored.getCalendar().getNumberOfPlayedTurns());
        assertIdentityAndRules(restored, nation, difficulty, rule);
        assertEquals(rule.gold,
                restored.getPlayerByName(nation.getPlayerName()).getGold());
        restored.startGame();
        assertEquals(nation.getPlayerName(),
                restored.getCurrentPlayer().getName());
        assertEquals(1, restored.getCalendar().getNumberOfPlayedTurns());
    }

    /**
     * Ends actors in turn order until the human is current. The model is
     * synchronous: each endTurn finishes one actor and starts the next actor's
     * preparation before it returns. One full round has
     * {@link #ACTORS_PER_ROUND} actors: four nations, then their four Crowns.
     */
    private static void endActorsUntil(final Model model,
            final EuropeanNation human) {
        for (int actor = 0; actor < ACTORS_PER_ROUND
                && !model.getCurrentPlayer().getName()
                        .equals(human.getPlayerName()); actor++) {
            model.endTurn();
        }
        assertEquals(human.getPlayerName(), model.getCurrentPlayer().getName());
    }

    /** Accepted difficulty and Crown values of one profile. */
    private static final class ProfileRule {
        private final int gold;
        private final int factor;
        private final int toryThreshold;
        private final int taxInterval;
        private final int infantry;
        private final int cavalry;

        private ProfileRule(final int gold, final int factor,
                final int toryThreshold, final int taxInterval,
                final int infantry, final int cavalry) {
            this.gold = gold;
            this.factor = factor;
            this.toryThreshold = toryThreshold;
            this.taxInterval = taxInterval;
            this.infantry = infantry;
            this.cavalry = cavalry;
        }
    }

    /**
     * Rule table in difficulty index order. The factor is k in percent; bells
     * per ground unit are ceil(500 / k) and the intervention target is ceil(500
     * × k). Tax demands come every max(5, 20 − 3d) updates.
     */
    private static ProfileRule ruleFor(final Difficulty difficulty) {
        switch (difficulty) {
        case DISCOVERER:
            return new ProfileRule(1000, 75, 10, 20, 4, 2);
        case EXPLORER:
            return new ProfileRule(750, 90, 9, 17, 5, 3);
        case CONQUISTADOR:
            return new ProfileRule(500, 100, 8, 14, 6, 4);
        case GOVERNOR:
            return new ProfileRule(250, 115, 7, 11, 7, 5);
        case VICEROY:
            return new ProfileRule(0, 130, 6, 8, 8, 6);
        default:
            throw new IllegalArgumentException(
                    "Unknown difficulty " + difficulty);
        }
    }

    /** Reference start squares on the bundled map, one per nation. */
    private static Location startOf(final EuropeanNation nation) {
        switch (nation) {
        case DUTCH:
            return Location.of(48, 2);
        case FRENCH:
            return Location.of(50, 11);
        case ENGLISH:
            return Location.of(50, 20);
        case SPANISH:
            return Location.of(50, 29);
        default:
            throw new IllegalArgumentException("Unknown nation " + nation);
        }
    }

    private static int ceilDivide(final int numerator, final int denominator) {
        return (numerator + denominator - 1) / denominator;
    }

    private static long members(final RoyalRoster roster,
            final RoyalRoster.Family family) {
        return roster.getMembers().stream()
                .filter(member -> member.getFamily() == family).count();
    }

    private static void assertIdentityAndRules(final Model model,
            final EuropeanNation nation, final Difficulty difficulty,
            final ProfileRule rule) {
        assertEquals(difficulty, model.getDifficulty());
        for (final EuropeanNation colonial : EuropeanNation.values()) {
            final Player player = model
                    .getPlayerByName(colonial.getPlayerName());
            assertEquals(colonial == nation, player.isHuman());
            assertEquals(colonial != nation, player.isComputer());
            final PlayerKing crown = (PlayerKing) model
                    .getPlayerByName(colonial.getPlayerName() + "'s King");
            assertSame(player, crown.getWhosKingThisPlayerIs());
            assertTrue(crown.isComputer());
            // The roster is read through the colonial nation it belongs to; a
            // Crown has no roster of a Crown of its own.
            assertTrue(crown.getCrownRoster().isEmpty());
            final RoyalRoster roster = player.getCrownRoster().orElseThrow();
            assertSame(roster, crown.getRoyalRoster());
            assertEquals(rule.infantry,
                    members(roster, RoyalRoster.Family.INFANTRY));
            assertEquals(rule.cavalry,
                    members(roster, RoyalRoster.Family.CAVALRY));
            assertEquals(2, members(roster, RoyalRoster.Family.ARTILLERY));
            assertEquals(2, members(roster, RoyalRoster.Family.MAN_OF_WAR));
            assertEquals(ceilDivide(500 * 100, rule.factor),
                    roster.getBellThreshold());
        }
        assertEquals(rule.toryThreshold,
                PoliticalProduction.getToryThreshold(difficulty));
        assertEquals(rule.taxInterval, TaxState.interval(difficulty));
        assertEquals(ceilDivide(500 * rule.factor, 100),
                InterventionTarget.targetFor(difficulty));
    }

    /** Human ship and crew at the reference start, before the first turn. */
    private static void assertExpedition(final Model model,
            final EuropeanNation nation) {
        final String owner = nation.getPlayerName();
        assertEquals(12, model.getAllUnits().size());
        final Ship ship = (Ship) model.getAllUnits().stream()
                .filter(unit -> unit.isShip()
                        && owner.equals(unit.getOwner().getName()))
                .findFirst().orElseThrow();
        assertEquals(nation == EuropeanNation.DUTCH ? UnitType.MERCHANTMAN
                : UnitType.CARAVEL, ship.getType());
        assertEquals(startOf(nation), ship.getLocation());
        assertEquals(2, ship.getCargo().getSlots().stream()
                .filter(slot -> slot.isLoadedUnit()).count());
        final List<UnitFreeColonist> people = model.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.COLONIST
                        && owner.equals(unit.getOwner().getName()))
                .map(unit -> (UnitFreeColonist) unit)
                .collect(Collectors.toList());
        assertEquals(2, people.size());
        assertTrue(people.stream().allMatch(Unit::isAtCargoSlot));
        assertEquals(1, people.stream().filter(UnitFreeColonist::isHoldingGuns)
                .count());
        assertEquals(1,
                people.stream().filter(unit -> unit.getTools() == 100).count());
    }
}
