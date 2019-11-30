package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Verify embark from map location and disembark to specific location.
 */
public class UnitColonistEmbarkTest extends AbstractUnitFreeColonistTest {

    /**
     * Move starts at this location.
     */
    private final static Location START_LOCATION = Location.of(1, 1);

    /**
     * Target map location of move.
     */
    private final static Location TARGET_LOCATION = Location.of(1, 2);

    /**
     * Map location that is too far from start location to move there in one
     * step.
     */
    private final static Location TOO_FAR_LOCATION = Location.of(1, 4);

    private Player differentOwner = mock(Player.class);

    private PlaceLocation placeLocation = mock(PlaceLocation.class);

    private CargoSlot cargoSlot = mock(CargoSlot.class);

    private PlaceConstructionSlot placeConstructionSlot = mock(PlaceConstructionSlot.class);

    private PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);

    private Colony colony = mock(Colony.class);

    /**
     * To this unit will be tested unit embarked.
     */
    private Unit targetUnit = mock(Unit.class);

    /**
     * From this unit will be other unit disembarked.
     */
    private Unit sourceUnit = mock(Unit.class);

    @Test
    public void test_embarkFromLocation_cargoSlot_is_null() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);

        assertThrows(NullPointerException.class, () -> unit.embarkFromLocation(null));
    }

    @Test
    public void test_embarkFromLocation_cargoSlot_is_not_empty() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(false);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unit.embarkFromLocation(cargoSlot));

        assertTrue(ex.getMessage().contains("is already loaded"));
    }

    @Test
    public void test_embarkFromLocation_unit_isNotAtMapLocation() throws Exception {
        makeColonist(model, 23, placeConstructionSlot, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.embarkFromLocation(cargoSlot));

        assertTrue(ex.getMessage().contains("Unit have to be at map, it's at"));
    }

    @Test
    public void test_embarkFromLocation_target_unit_isNotAtMapLocation() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);
        when(cargoSlot.getOwnerUnit()).thenReturn(targetUnit);
        when(targetUnit.isAtPlaceLocation()).thenReturn(false);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.embarkFromLocation(cargoSlot));

        assertTrue(ex.getMessage()
                .contains("where other unit would like to embark have to be at map"));
    }

    @Test
    public void test_embarkFromLocation_cargoSlow_belongs_to_different_owner() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);
        when(cargoSlot.getOwnerUnit()).thenReturn(targetUnit);
        when(targetUnit.isAtPlaceLocation()).thenReturn(true);
        when(cargoSlot.getOwnerPlayer()).thenReturn(differentOwner);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unit.embarkFromLocation(cargoSlot));

        assertTrue(ex.getMessage().contains("Cargo slot belongs to different player"));
    }

    @Test
    public void test_embarkFromLocation_embark_location_is_too_far() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);
        when(cargoSlot.getOwnerUnit()).thenReturn(targetUnit);
        when(targetUnit.isAtPlaceLocation()).thenReturn(true);
        when(cargoSlot.getOwnerPlayer()).thenReturn(owner);
        when(placeLocation.getLocation()).thenReturn(START_LOCATION);
        when(targetUnit.getLocation()).thenReturn(TOO_FAR_LOCATION);
        when(placeLocation.getOrientation()).thenReturn(Direction.southWest);
        when(model.fireUnitMoveStarted(eq(unit), any())).thenReturn(false);

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unit.embarkFromLocation(cargoSlot));

        assertTrue(ex.getMessage().contains("have to be neighbours"));

        verify(model, times(0)).fireUnitMovedStepStarted(unit, START_LOCATION, TOO_FAR_LOCATION,
                Direction.southWest);
    }

    @Test
    public void test_embarkFromLocation_move_was_stoped_by_ui() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);
        when(cargoSlot.getOwnerUnit()).thenReturn(targetUnit);
        when(targetUnit.isAtPlaceLocation()).thenReturn(true);
        when(cargoSlot.getOwnerPlayer()).thenReturn(owner);
        when(placeLocation.getLocation()).thenReturn(START_LOCATION);
        when(targetUnit.getLocation()).thenReturn(TARGET_LOCATION);
        when(placeLocation.getOrientation()).thenReturn(Direction.southWest);
        when(model.fireUnitMoveStarted(eq(unit), any())).thenReturn(false);

        unit.embarkFromLocation(cargoSlot);

        verify(model, times(0)).fireUnitMovedStepStarted(unit, START_LOCATION, TARGET_LOCATION,
                Direction.southWest);
    }

    @Test
    public void test_embarkFromLocation() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 10);
        when(cargoSlot.isEmpty()).thenReturn(true);
        when(cargoSlot.getOwnerUnit()).thenReturn(targetUnit);
        when(targetUnit.isAtPlaceLocation()).thenReturn(true);
        when(cargoSlot.getOwnerPlayer()).thenReturn(owner);
        when(placeLocation.getLocation()).thenReturn(START_LOCATION);
        when(targetUnit.getLocation()).thenReturn(TARGET_LOCATION);
        when(placeLocation.getOrientation()).thenReturn(Direction.southWest);
        when(model.fireUnitMoveStarted(eq(unit), any())).thenReturn(true);

        unit.embarkFromLocation(cargoSlot);

        verify(cargoSlot, times(1)).store((PlaceCargoSlot) any());
        verify(model, times(1)).fireUnitMovedStepStarted(unit, START_LOCATION, TARGET_LOCATION,
                Direction.southWest);
        verify(model, times(1)).fireUnitMovedFinished(eq(unit), any());
    }

    @Test
    public void test_disembarkToLocation_cargoSlot_is_null() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);

        assertThrows(NullPointerException.class, () -> unit.disembarkToLocation(null));
    }

    @Test
    public void test_disembarkToLocation_not_enought_action_points() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 0);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.disembarkToLocation(TARGET_LOCATION));

        assertTrue(ex.getMessage().contains("need for unload at least one action point"));
    }

    @Test
    public void test_disembarkToLocation_unit_not_in_cargo_slot() throws Exception {
        makeColonist(model, 23, placeConstructionSlot, owner, 1);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.disembarkToLocation(TARGET_LOCATION));

        assertTrue(ex.getMessage().contains("can't be unload, it's not in cargo slot"));
    }

    @Test
    public void test_disembarkToLocation_unit_cant_move_at_target_location() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.getTerrainTypeAt(TARGET_LOCATION)).thenReturn(TerrainType.OCEAN);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.disembarkToLocation(TARGET_LOCATION));

        assertTrue(ex.getMessage().contains("can't move at target terrain"));
    }

    @Test
    public void test_disembarkToLocation_target_location_is_too_far() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.getTerrainTypeAt(TOO_FAR_LOCATION)).thenReturn(TerrainType.GRASSLAND);
        when(placeCargoSlot.getOwnerUnit()).thenReturn(sourceUnit);
        when(sourceUnit.getLocation()).thenReturn(START_LOCATION);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> unit.disembarkToLocation(TOO_FAR_LOCATION));

        assertTrue(ex.getMessage().contains("have to neighbour of target location"));
    }

    @Test
    public void test_disembarkToLocation_target_location_is_colony() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.getTerrainTypeAt(TARGET_LOCATION)).thenReturn(TerrainType.GRASSLAND);
        when(placeCargoSlot.getOwnerUnit()).thenReturn(sourceUnit);
        when(sourceUnit.getLocation()).thenReturn(START_LOCATION);
        when(model.getColonyAt(TARGET_LOCATION)).thenReturn(Optional.of(colony));

        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> unit.disembarkToLocation(TARGET_LOCATION));

        assertTrue(ex.getMessage().contains("can't be colony"));
    }

    @Test
    public void test_disembarkToLocation_move_was_stoped_by_ui() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.getTerrainTypeAt(TARGET_LOCATION)).thenReturn(TerrainType.GRASSLAND);
        when(placeCargoSlot.getOwnerUnit()).thenReturn(sourceUnit);
        when(sourceUnit.getLocation()).thenReturn(START_LOCATION);
        when(model.getColonyAt(TARGET_LOCATION)).thenReturn(Optional.empty());
        when(model.fireUnitMoveStarted(eq(unit), any())).thenReturn(false);

        unit.disembarkToLocation(TARGET_LOCATION);

        assertEquals(1, unit.getActionPoints());
        verify(model, times(0)).fireUnitMovedStepStarted(unit, START_LOCATION, TARGET_LOCATION,
                Direction.east);
    }

    @Test
    public void test_disembarkToLocation() throws Exception {
        makeColonist(model, 23, placeCargoSlot, owner, 1);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.getTerrainTypeAt(TARGET_LOCATION)).thenReturn(TerrainType.GRASSLAND);
        when(placeCargoSlot.getOwnerUnit()).thenReturn(sourceUnit);
        when(sourceUnit.getLocation()).thenReturn(START_LOCATION);
        when(model.getColonyAt(TARGET_LOCATION)).thenReturn(Optional.empty());
        when(model.fireUnitMoveStarted(eq(unit), any())).thenReturn(true);

        unit.disembarkToLocation(TARGET_LOCATION);

        assertEquals(0, unit.getActionPoints());
        verify(model, times(1)).fireUnitMovedStepStarted(unit, START_LOCATION, TARGET_LOCATION,
                Direction.east);
    }

}