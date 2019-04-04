package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

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

    private final  Player player = mock(Player.class);

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
    public void test_isSameOwner_twoUnit_secondIsDifferentOwner() throws Exception {
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
        assertThrows(NullPointerException.class, () -> unit.placeToCargoSlot(null));
    }

    @Test
    public void test_placeToCargoSlot_is_already_in_cargoSlot() throws Exception {
        final PlaceCargoSlot placeSlot1 = mock(PlaceCargoSlot.class);
        final PlaceCargoSlot placeSlot2 = mock(PlaceCargoSlot.class);
        makeColonist(model, 4, placeSlot1, owner, 10);

        assertThrows(IllegalArgumentException.class, () -> unit.placeToCargoSlot(placeSlot2));
    }

    @Test
    public void test_placeToCargoSlot_unit_have_different_owner_than_cargo_slot() throws Exception {
        final PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);
        makeColonist(model, 4, placeLocation, owner, 10);
        when(placeCargoSlot.getCargoSlotOwner()).thenReturn(player);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> unit.placeToCargoSlot(placeCargoSlot));

        assertTrue(exception.getMessage().contains("Cargo slot belongs to different player"));
    }

    @Test
    public void test_placeToCargoSlot_unit_was_at_location() throws Exception {
        final PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);
        final CargoSlot cargoSlot = mock(CargoSlot.class);
        makeColonist(model, 4, placeLocation, owner, 10);
        when(placeCargoSlot.getCargoSlotOwner()).thenReturn(owner);
        when(placeCargoSlot.getCargoSlot()).thenReturn(cargoSlot);

        unit.placeToCargoSlot(placeCargoSlot);

        assertTrue(unit.isAtCargoSlot());
        assertEquals(0, unit.getActionPoints());
        verify(placeLocation, times(1)).destroy();
        verify(model, times(1)).fireUnitEmbarked(unit, cargoSlot);
    }

}
