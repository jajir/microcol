package org.microcol.model;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;

public class UnitColonistOnMapTest extends AbstractUnitFreeColonistTest {

    private PlaceLocation placeLocation = mock(PlaceLocation.class);

    private final Location unitLoc = Location.of(7, 4);

    private final PlaceEuropePier placeEuropePier = mock(PlaceEuropePier.class);

    @Test
    public void test_moveOneStep_inHarbor() throws Exception {
        makeColonist(model, 23, placeEuropePier, owner, 10);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            unit.moveOneStep(Location.of(7, 5));
        });

        assertTrue(exception.getMessage().contains("Unit have to be at map."),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_gameIsNotRunning() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);

        doThrow(new IllegalStateException("Example")).when(model).checkGameRunning();

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            unit.moveOneStep(Location.of(7, 5));
        });

        assertTrue(exception.getMessage().contains("Example"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_invalid_currentPlayer() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);

        doThrow(new IllegalStateException("Example")).when(model).checkCurrentPlayer(owner);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            unit.moveOneStep(Location.of(7, 5));
        });

        assertTrue(exception.getMessage().contains("Example"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo_isNull() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);

        assertThrows(NullPointerException.class, () -> {
            unit.moveOneStep(null);
        });
    }

    @Test
    public void test_moveOneStep_moveTo_isNotNeighbor() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(placeLocation.getLocation()).thenReturn(Location.of(10, 10));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            unit.moveOneStep(Location.of(10, 10));
        });

        assertTrue(exception.getMessage().contains("must be neighbor to current location "),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo_isInvalid() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);

        when(placeLocation.getLocation()).thenReturn(unitLoc);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(Location.of(7, 5))).thenReturn(false);


        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            unit.moveOneStep(Location.of(7, 5));
        });

        assertTrue(exception.getMessage().contains("must be valid."),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo_invalid_terrainType() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        final Location moveAt = Location.of(7, 5);

        when(placeLocation.getLocation()).thenReturn(unitLoc);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(moveAt)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(moveAt)).thenReturn(TerrainType.HIGH_SEA);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            unit.moveOneStep(moveAt);
        });

        assertTrue(exception.getMessage().contains("It's not possible to move at"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo_isAttack() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        final Location moveAt = Location.of(7, 5);

        when(placeLocation.getLocation()).thenReturn(unitLoc);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(moveAt)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(moveAt)).thenReturn(TerrainType.GRASSLAND);
        when(owner.getEnemyUnitsAt(moveAt)).thenReturn(Lists.newArrayList(unit));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            unit.moveOneStep(moveAt);
        });

        assertTrue(exception.getMessage().contains("It's not possible to move at"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo_notEnough_availableMoves() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 0);
        final Location moveAt = Location.of(7, 5);

        when(placeLocation.getLocation()).thenReturn(unitLoc);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(moveAt)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(moveAt)).thenReturn(TerrainType.GRASSLAND);
        when(owner.getEnemyUnitsAt(moveAt)).thenReturn(new ArrayList<>());

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            unit.moveOneStep(moveAt);
        });

        assertTrue(exception.getMessage().contains("There is not enough avilable moves"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void test_moveOneStep_moveTo() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        final Location moveAt = Location.of(7, 5);

        when(placeLocation.getLocation()).thenReturn(unitLoc);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(moveAt)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(moveAt)).thenReturn(TerrainType.GRASSLAND);
        when(owner.getEnemyUnitsAt(moveAt)).thenReturn(new ArrayList<>());

        unit.moveOneStep(moveAt);
        assertEquals(9, unit.getActionPoints());
        assertTrue(unit.isAtPlaceLocation());
        // TODO assert new location, it's not easy because PlaceLocation is always mock.
    }

}