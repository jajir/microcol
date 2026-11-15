package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class CurrentSightDepartureTest {
    @Test
    void sightUsesOnePositionWhileShipLeavesMap() {
        final Model model = mock(Model.class);
        final Player player = new Player("Dutch", false, 0, model, false,
                Map.of(), Set.of(), null, null, null) {
        };
        final Unit ship = mock(Unit.class);
        final Location shore = Location.of(10, 10);
        when(model.isValid(shore)).thenReturn(true);
        when(model.getAllUnits()).thenReturn(List.of(ship));
        when(model.getColonies(player)).thenReturn(List.of());
        when(ship.getOwner()).thenReturn(player);
        when(ship.getSightRadius()).thenReturn(2);
        when(ship.isAtPlaceLocation()).thenReturn(true);
        when(ship.getVisibleLocations()).thenThrow(new IllegalStateException(
                "The ship has left the map since the position check"));
        when(ship.getPlace()).thenReturn(
                new PlaceLocation(ship, Location.of(12, 10), Direction.east),
                mock(PlaceHighSea.class));

        assertTrue(player.isCurrentlyVisible(shore));
        assertFalse(player.isCurrentlyVisible(shore));
    }
}
