package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.gson.Gson;

class FirstColonyPlannerTest {
    private static final Location START = Location.of(10, 10);
    private static final Location EAST = Location.of(11, 10);
    private static final Location WEST = Location.of(9, 10);

    private Model world(final Map<Location, TerrainType> land,
            final boolean allKnown) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Map<Location, TerrainType> terrain = new HashMap<>();
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                final Location at = Location.of(x, y);
                terrain.put(at, TerrainType.OCEAN);
                if (allKnown || (x >= 8 && x <= 12 && y >= 8 && y <= 12)) {
                    known.add(at);
                }
            }
        }
        terrain.putAll(land);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Set.of());
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow().getVisible().setVisibility(known, 50, 30);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 5)
                .findFirst().orElseThrow().getPlaceMap().setLocation(START);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Optional<Location> next(final Model model) {
        return FirstColonyPlanner.nextStep(model, model.getUnitById(4));
    }

    private Map<Location, TerrainType> easternIsland() {
        return Map.of(EAST, TerrainType.ARCTIC, Location.of(12, 10),
                TerrainType.GRASSLAND);
    }

    @Test
    void bundledExpeditionStartsByExploringAKnownLegalWaterStep() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String before = new Gson().toJson(model.save());
        final Unit ship = model.getUnitById(5);
        final Location step = next(model).orElseThrow();
        assertTrue(ship.getLocation().isNeighbor(step));
        assertTrue(ship.getOwner().isVisible(step));
        assertTrue(ship.isPossibleToMoveAt(step));
        assertTrue(model.getMap().getTerrainTypeAt(step).isSea());
        assertFalse(
                model.getPlayerByName("France").isVisible(Location.of(26, 15)));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void adjacentViableSiteAllowsPlanningWhileAboardWithNoMovement() {
        final Model model = world(easternIsland(), true);
        final Unit founder = model.getUnitById(4);
        assertTrue(founder.isAtCargoSlot());
        assertEquals(0, founder.getActionPoints());
        final String before = new Gson().toJson(model.save());
        assertEquals(Optional.of(EAST), next(model));
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void equalDistancePrefersHigherAvailableLandFoodYield() {
        final Map<Location, TerrainType> land = new HashMap<>(easternIsland());
        land.put(WEST, TerrainType.ARCTIC);
        land.put(Location.of(8, 10), TerrainType.SWAMP);
        final Model model = world(land, true);
        assertEquals(Optional.of(EAST), next(model));
    }

    @Test
    void equalDistanceAndFoodUseSiteCoordinatesConsistentlyAfterRestore() {
        final Map<Location, TerrainType> land = new HashMap<>(easternIsland());
        land.put(WEST, TerrainType.ARCTIC);
        land.put(Location.of(8, 10), TerrainType.GRASSLAND);
        final Model model = world(land, true);
        assertEquals(Optional.of(WEST), next(model));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(Optional.of(WEST), next(restored));
    }

    @Test
    void closerAdequateSiteWinsOverMoreDistantRicherFood() {
        final Model model = world(Map.of(WEST, TerrainType.ARCTIC,
                Location.of(8, 10), TerrainType.SWAMP, Location.of(13, 10),
                TerrainType.ARCTIC, Location.of(14, 10), TerrainType.GRASSLAND),
                true);
        assertEquals(Optional.of(WEST), next(model));
    }

    @Test
    void foreignOccupiedSiteIsNeverSelectedEvenAtWar() {
        final Map<Location, TerrainType> land = new HashMap<>(easternIsland());
        land.put(WEST, TerrainType.ARCTIC);
        land.put(Location.of(8, 10), TerrainType.SWAMP);
        final Model model = world(land, true);
        model.getUnitById(0).placeToLocation(EAST);
        model.declareHostilities(model.getPlayerByName("France"),
                model.getPlayerByName("Dutch"));
        assertEquals(Optional.of(WEST), next(model));
    }

    @Test
    void foodFromWaterAloneDoesNotSatisfyTheLandFoodPolicy() {
        final Model model = world(Map.of(EAST, TerrainType.ARCTIC), true);
        assertTrue(next(model).isEmpty());
    }

    @Test
    void enclosedFleetAndFullyKnownOceanReturnWithoutUnboundedSearching() {
        final Map<Location, TerrainType> ring = new HashMap<>();
        START.getNeighbors().forEach(at -> ring.put(at, TerrainType.MOUNTAIN));
        final Model enclosed = world(ring, true);
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertTrue(next(enclosed).isEmpty()));
        final Model ocean = world(Map.of(), true);
        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> assertTrue(next(ocean).isEmpty()));
    }

    @Test
    void highSeaRingIsNotUsedAsAnExplorationExit() {
        final Map<Location, TerrainType> ring = new HashMap<>();
        START.getNeighbors().forEach(at -> ring.put(at, TerrainType.HIGH_SEA));
        assertTrue(next(world(ring, false)).isEmpty());
    }

    @Test
    void unseenTerrainCannotChangeTheChosenKnownExplorationStep() {
        final Model ocean = world(Map.of(), false);
        final Model hiddenIsland = world(
                Map.of(Location.of(20, 10), TerrainType.GRASSLAND,
                        Location.of(20, 11), TerrainType.GRASSLAND),
                false);
        final String before = new Gson().toJson(hiddenIsland.save());
        final Location step = next(ocean).orElseThrow();
        assertEquals(Optional.of(step), next(hiddenIsland));
        assertTrue(START.isNeighbor(step));
        assertTrue(hiddenIsland.getPlayerByName("France").isVisible(step));
        assertEquals(before, new Gson().toJson(hiddenIsland.save()));
    }

    @Test
    void onLandFounderAlreadyAtViableSiteReturnsItsCurrentTile() {
        final Model model = world(easternIsland(), true);
        model.getUnitById(4).placeToLocation(EAST);
        assertEquals(Optional.of(EAST), next(model));
    }

    @Test
    void existingColonyEndsTheObjectiveAndForeignModelFounderIsRejected() {
        final Model model = world(easternIsland(), true);
        final Model other = world(easternIsland(), true);
        assertTrue(FirstColonyPlanner.nextStep(model, other.getUnitById(4))
                .isEmpty());
        final Unit founder = model.getUnitById(4);
        founder.placeToLocation(EAST);
        founder.startTurn();
        model.buildColony(founder.getOwner(), founder, "Rouen");
        assertTrue(next(model).isEmpty());
    }
}
