package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Independent expectations for terrain movement. Costs are in movement thirds:
 * one whole point is 3 thirds. Plain colonists have 1 point (3 thirds) and a
 * mounted colonist has 5 points (15 thirds).
 */
public class TerrainMovementTest extends AbstractUnitFreeColonistTest {

    private final PlaceLocation placeLocation = mock(PlaceLocation.class);

    private final Location start = Location.of(7, 4);

    private final Location east = Location.of(8, 4);

    private final Location far = Location.of(9, 4);

    private UnitFreeColonist colonist;

    @Test
    public void test_terrainEntryCosts_matchAidCard4() {
        // Forested / bare costs from the aid card, written here as thirds.
        assertEquals(6, TerrainType.GRASSLAND.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.GRASSLAND.getMoveCostThirds(false, false));
        assertEquals(6, TerrainType.PRAIRIE.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.PRAIRIE.getMoveCostThirds(false, false));
        assertEquals(6, TerrainType.SAVANNAH.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.SAVANNAH.getMoveCostThirds(false, false));
        assertEquals(6, TerrainType.PLAINS.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.PLAINS.getMoveCostThirds(false, false));
        assertEquals(9, TerrainType.MARSH.getMoveCostThirds(true, false));
        assertEquals(6, TerrainType.MARSH.getMoveCostThirds(false, false));
        assertEquals(9, TerrainType.SWAMP.getMoveCostThirds(true, false));
        assertEquals(6, TerrainType.SWAMP.getMoveCostThirds(false, false));
        assertEquals(3, TerrainType.DESERT.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.DESERT.getMoveCostThirds(false, false));
        assertEquals(6, TerrainType.TUNDRA.getMoveCostThirds(true, false));
        assertEquals(3, TerrainType.TUNDRA.getMoveCostThirds(false, false));
        // Arctic cannot hold trees (TerrainTypeTest), so its only legal state
        // is bare; no forested arctic cost exists to assert.
        assertEquals(6, TerrainType.ARCTIC.getMoveCostThirds(false, false));
        assertEquals(6, TerrainType.HILL.getMoveCostThirds(false, false));
        assertEquals(9, TerrainType.MOUNTAIN.getMoveCostThirds(false, false));
        assertEquals(3, TerrainType.OCEAN.getMoveCostThirds(false, false));
        assertEquals(3, TerrainType.LAKE.getMoveCostThirds(false, false));
        assertEquals(3, TerrainType.HIGH_SEA.getMoveCostThirds(false, false));
    }

    @Test
    public void test_connectedRoadOrRiver_costsOneThird() {
        // Road and river construction is separate; the explicit input only
        // proves the common boundary.
        assertEquals(1, TerrainType.MOUNTAIN.getMoveCostThirds(false, true));
        assertEquals(1, TerrainType.GRASSLAND.getMoveCostThirds(true, true));
    }

    @Test
    public void test_moveOneStep_chargesEntryCostOfEachStep() throws Exception {
        makeColonistAt(15, true, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.PLAINS, false);
        setTerrain(far, TerrainType.HILL, false);

        colonist.moveOneStep(east);
        assertEquals(12, colonist.getMovementThirds());
        colonist.moveOneStep(far);

        assertEquals(far, colonist.getLocation());
        assertEquals(6, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_fullAllowance_expensiveStepFinishesAtZero()
            throws Exception {
        makeColonistAt(3, false, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.MOUNTAIN, false);

        colonist.moveOneStep(east);

        assertEquals(east, colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_fullAllowance_allowsOnlyOneExpensiveStep()
            throws Exception {
        makeColonistAt(3, false, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.MOUNTAIN, false);
        setTerrain(far, TerrainType.GRASSLAND, false);
        colonist.moveOneStep(east);

        assertThrows(IllegalStateException.class,
                () -> colonist.moveOneStep(far));

        assertEquals(east, colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_partialAllowance_rejectsWithoutChange()
            throws Exception {
        makeColonistAt(6, true, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.MOUNTAIN, false);

        assertThrows(IllegalStateException.class,
                () -> colonist.moveOneStep(east));

        assertEquals(start, colonist.getLocation());
        assertEquals(6, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_remainderOfFullAllowance_cannotPayTwoPoints()
            throws Exception {
        // After a plains step the unit is no longer at full allowance, so the
        // mountain step costing 9 thirds is rejected with 6 thirds left.
        makeColonistAt(15, true, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.PLAINS, false);
        setTerrain(far, TerrainType.MOUNTAIN, false);
        colonist.moveOneStep(east);
        assertEquals(12, colonist.getMovementThirds());
        setTerrain(far, TerrainType.HILL, false);
        colonist.moveOneStep(far);
        assertEquals(6, colonist.getMovementThirds());
        final Location north = Location.of(9, 3);
        setTerrain(north, TerrainType.MOUNTAIN, false);

        assertThrows(IllegalStateException.class,
                () -> colonist.moveOneStep(north));

        assertEquals(far, colonist.getLocation());
        assertEquals(6, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_zeroMovement_cannotMove() throws Exception {
        makeColonistAt(0, false, TerrainType.GRASSLAND);

        assertThrows(IllegalStateException.class,
                () -> colonist.moveOneStep(east));

        assertEquals(start, colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
    }

    @Test
    public void test_moveOneStep_callbackSpendingMovement_isRejectedBeforeMove()
            throws Exception {
        makeColonistAt(15, true, TerrainType.GRASSLAND);
        // A listener may change the unit before its first mutation; the second
        // check must then reject the step without moving or spending.
        doAnswer(invocation -> {
            colonist.setMovementThirds(0);
            return null;
        }).when(model).fireUnitMovedStepStarted(any(Unit.class),
                any(Location.class), any(Location.class), any());

        assertThrows(IllegalStateException.class,
                () -> colonist.moveOneStep(east));

        assertEquals(start, colonist.getLocation());
        assertEquals(0, colonist.getMovementThirds());
    }

    @Test
    public void test_getAvailableLocations_isWeightedNotStepCounted()
            throws Exception {
        // Every square costs a mountain, 9 thirds, against 15 thirds: the reach
        // is exactly the eight neighbours, not a fifteen-step flood fill.
        makeColonistAt(15, true, TerrainType.MOUNTAIN);

        final List<Location> reach = colonist.getAvailableLocations();

        assertEquals(8, reach.size());
        assertTrue(reach.contains(east));
        assertFalse(reach.contains(far));
    }

    @Test
    public void test_getPath_prefersCheapestRoute() throws Exception {
        makeColonistAt(15, true, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.MOUNTAIN, false);

        final List<Location> path = colonist.getPath(far).orElseThrow();

        assertEquals(2, path.size());
        assertNotEquals(east, path.get(0));
        assertEquals(far, path.get(1));
        assertTrue(colonist.canAffordPath(path));
    }

    @Test
    public void test_canAffordPath_rejectsSecondStepAfterFullExpensiveStep()
            throws Exception {
        makeColonistAt(3, false, TerrainType.GRASSLAND);
        setTerrain(east, TerrainType.MOUNTAIN, false);

        assertTrue(colonist.canAffordPath(List.of(east)));
        assertFalse(colonist.canAffordPath(List.of(east, far)));
    }

    @Test
    public void test_exactThirds_arePersistedInSavedUnit() throws Exception {
        makeColonistAt(4, true, TerrainType.GRASSLAND);

        final UnitPo saved = colonist.save();

        assertEquals(Integer.valueOf(4), saved.getMovementThirds());
    }

    /**
     * Makes a colonist on {@link #start} with an exact allowance. Every square
     * is {@code defaultTerrain} unless a test sets another one.
     */
    private void makeColonistAt(final int movementThirds, final boolean mounted,
            final TerrainType defaultTerrain) {
        when(placeLocation.getLocation()).thenReturn(start);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(any(Location.class))).thenReturn(true);
        when(worldMap.getTerrainTypeAt(any(Location.class)))
                .thenReturn(defaultTerrain);
        colonist = new UnitFreeColonist(model, 23, unit -> placeLocation, owner,
                movementThirds, new UnitActionNoAction(), 0, false, mounted);
        unit = colonist;
    }

    private void setTerrain(final Location location, final TerrainType terrain,
            final boolean trees) {
        when(worldMap.getTerrainTypeAt(location)).thenReturn(terrain);
        when(worldMap.hasTreesAt(location)).thenReturn(trees);
    }

}
