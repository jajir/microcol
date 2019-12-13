package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.microcol.model.unit.UnitFrigate;

import com.google.common.collect.Lists;

/**
 * Verify embark from map location and disembark to specific location.
 */
public class UnitFrigateEmbarkTest extends AbstractUnitFreeColonistTest {

    /**
     * Move starts at this location.
     */
    private final static Location START_LOCATION = Location.of(1, 1);

    /**
     * Target map location of move.
     */
    private final static Location TARGET_LOCATION = Location.of(1, 2);

    /**
     * Too far location to disembark there.
     */
    private final static Location FAR_LOCATION = Location.of(1, 3);

    private PlaceLocation placeLocation = mock(PlaceLocation.class);

    private PlaceEuropePort placeEuropePort = mock(PlaceEuropePort.class);

    private CargoSlot cargoSlot = mock(CargoSlot.class);

    /**
     * From this unit will be other unit disembarked.
     */
    private Unit sourceUnit = mock(Unit.class);

    @Test
    public void test_isPossibleToDisembarkAt_targetLocation_is_null() throws Exception {
        final UnitFrigate frigate = makeFrigate(model, 23, placeLocation, owner, 10);

        assertThrows(NullPointerException.class,
                () -> frigate.isPossibleToDisembarkAt(null, false));
    }

    @Test
    public void test_isPossibleToDisembarkAt_ship_in_in_Europe() throws Exception {
        final UnitFrigate frigate = makeFrigate(model, 23, placeEuropePort, owner, 10);

        final IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> frigate.isPossibleToDisembarkAt(TARGET_LOCATION, false));

        assertTrue(ex.getMessage().contains("should by at map"));
    }
    
    @Test
    public void test_isPossibleToDisembarkAt_target_is_too_far() throws Exception {
        final UnitFrigate frigate = makeFrigate(model, 23, placeLocation, owner, 10);
        when(placeLocation.getLocation()).thenReturn(START_LOCATION);
        when(model.getColonyAt(TARGET_LOCATION)).thenReturn(Optional.empty());
        when(cargo.getSlots()).thenReturn(Lists.newArrayList(cargoSlot));
        when(cargoSlot.isEmpty()).thenReturn(false);
        when(cargoSlot.isLoadedGood()).thenReturn(false);
        when(cargoSlot.getUnit()).thenReturn(Optional.of(sourceUnit));
        when(sourceUnit.getActionPoints()).thenReturn(3);
        when(sourceUnit.canUnitDisembarkAt(FAR_LOCATION)).thenReturn(true);

        assertFalse(frigate.isPossibleToDisembarkAt(FAR_LOCATION, false));
    }

    @Test
    public void test_isPossibleToDisembarkAt() throws Exception {
        final UnitFrigate frigate = makeFrigate(model, 23, placeLocation, owner, 10);
        when(placeLocation.getLocation()).thenReturn(START_LOCATION);
        when(model.getColonyAt(TARGET_LOCATION)).thenReturn(Optional.empty());
        when(cargo.getSlots()).thenReturn(Lists.newArrayList(cargoSlot));
        when(cargoSlot.isEmpty()).thenReturn(false);
        when(cargoSlot.isLoadedGood()).thenReturn(false);
        when(cargoSlot.getUnit()).thenReturn(Optional.of(sourceUnit));
        when(sourceUnit.getActionPoints()).thenReturn(3);
        when(sourceUnit.canUnitDisembarkAt(TARGET_LOCATION)).thenReturn(true);

        assertTrue(frigate.isPossibleToDisembarkAt(TARGET_LOCATION, false));
    }

}