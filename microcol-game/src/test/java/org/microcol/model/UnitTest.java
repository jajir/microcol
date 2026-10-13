package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Optional;

import org.microcol.model.unit.UnitWithCargo;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

/**
 * Verify basic unit operations.
 * <p>
 * This test shows how to mock all unit's private final members.
 * </p>
 */
public class UnitTest extends AbstractUnitFreeColonistTest {

    private final PlaceEuropePier placeEuropePier = mock(PlaceEuropePier.class);

    private final Unit u1 = mock(Unit.class);

    private final Unit u2 = mock(Unit.class);

    private final Player player = mock(Player.class);

    private final PlaceLocation placeLocation = mock(PlaceLocation.class);

    @Test
    public void test_isSameOwner_emptyList() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertEquals(4, unit.getId());

        boolean ret = unit.isSameOwner(new ArrayList<>());

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_oneUnit_different() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);

        when(u1.getOwner()).thenReturn(null);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1));

        assertFalse(ret);
    }

    @Test
    public void test_isSameOwner_oneUnit_sameOwner() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1));

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_twoUnit_bothSameOwner() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);
        when(u2.getOwner()).thenReturn(owner);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_twoUnit_secondIsDifferentOwner()
            throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);
        when(u2.getOwner()).thenReturn(null);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));

        assertFalse(ret);
    }

    @Test
    public void test_placeToCargoSlot_place_is_required() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);
        assertThrows(NullPointerException.class,
                () -> unit.placeToCargoSlot(null));
    }

    @Test
    public void test_placeToCargoSlot_is_already_in_cargoSlot()
            throws Exception {
        final PlaceCargoSlot placeSlot1 = mock(PlaceCargoSlot.class);
        makeColonist(model, 4, placeSlot1, owner, 10);
        final UnitWithCargo carrier = mock(UnitWithCargo.class);
        final CargoSlot oldSlot = mock(CargoSlot.class);
        final CargoSlot target = prepareBoardingSlot(carrier);
        when(cargo.getSlots()).thenReturn(Lists.newArrayList(oldSlot, target));
        when(carrier.isAtEuropePort()).thenReturn(true);
        when(placeSlot1.getCargoSlot()).thenReturn(oldSlot);
        when(placeSlot1.getOwnerUnit()).thenReturn(carrier);
        when(oldSlot.getOwnerUnit()).thenReturn(carrier);
        when(oldSlot.getUnit()).thenReturn(Optional.of(unit));
        final PlaceCargoSlot placeSlot2 = new PlaceCargoSlot(unit, target);

        unit.placeToCargoSlot(placeSlot2);

        assertTrue(unit.isAtCargoSlot());
        assertSame(placeSlot2, unit.getPlaceCargoSlot());
        assertEquals(10, unit.getActionPoints());
        verify(placeSlot1).destroy();
        verify(target).store(placeSlot2);
        verify(model).fireUnitEmbarked(unit, target);
    }

    @Test
    public void test_placeToCargoSlot_unit_have_different_owner_than_cargo_slot()
            throws Exception {
        final PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);
        makeColonist(model, 4, placeLocation, owner, 10);
        when(placeCargoSlot.getCargoSlotOwner()).thenReturn(player);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> unit.placeToCargoSlot(placeCargoSlot));

        assertTrue(exception.getMessage()
                .contains("Cargo slot belongs to different player"));
    }

    @Test
    public void test_placeToCargoSlot_unit_was_at_location() throws Exception {
        makeColonist(model, 4, placeLocation, owner, 10);
        final UnitWithCargo carrier = mock(UnitWithCargo.class);
        final CargoSlot cargoSlot = prepareBoardingSlot(carrier);
        final Location shore = Location.of(1, 1);
        final Location water = Location.of(1, 2);
        when(placeLocation.getLocation()).thenReturn(shore);
        when(carrier.isAtPlaceLocation()).thenReturn(true);
        when(carrier.getLocation()).thenReturn(water);
        when(carrier.isPossibleToMoveAt(water)).thenReturn(true);
        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(shore)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(shore))
                .thenReturn(TerrainType.GRASSLAND);
        final PlaceCargoSlot placeCargoSlot = new PlaceCargoSlot(unit,
                cargoSlot);

        unit.placeToCargoSlot(placeCargoSlot);

        assertTrue(unit.isAtCargoSlot());
        assertSame(placeCargoSlot, unit.getPlaceCargoSlot());
        assertEquals(9, unit.getActionPoints());
        verify(placeLocation).destroy();
        verify(cargoSlot).store(placeCargoSlot);
        verify(model).fireUnitEmbarked(unit, cargoSlot);
    }

    private CargoSlot prepareBoardingSlot(UnitWithCargo carrier) {
        final CargoSlot slot = mock(CargoSlot.class);
        when(model.isGameRunning()).thenReturn(true);
        when(model.getCurrentPlayer()).thenReturn(owner);
        when(owner.getAllUnits()).thenReturn(Lists.newArrayList(unit, carrier));
        when(carrier.getOwner()).thenReturn(owner);
        when(carrier.isShip()).thenReturn(true);
        when(carrier.getCargo()).thenReturn(cargo);
        when(cargo.getSlots()).thenReturn(Lists.newArrayList(slot));
        when(slot.getOwnerPlayer()).thenReturn(owner);
        when(slot.getOwnerUnit()).thenReturn(carrier);
        when(slot.isEmpty()).thenReturn(true);
        return slot;
    }
}
