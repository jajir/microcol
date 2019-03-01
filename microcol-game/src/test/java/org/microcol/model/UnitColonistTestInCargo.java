package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UnitColonistTestInCargo extends AbstractUnitFreeColonistTest {

    private PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);

    private final CargoSlot cargoSlot = mock(CargoSlot.class);

    @Test
    public void test_placeToEuropePortPier() throws Exception {
        when(placeCargoSlot.isOwnerAtEuropePort()).thenReturn(true);
        when(placeCargoSlot.getCargoSlot()).thenReturn(cargoSlot);
        doNothing().when(cargoSlot).empty();

        unit.placeToEuropePortPier();

        assertTrue(unit.isAtEuropePier());
    }

    @Test
    public void test_placeToEuropePortPier_holding_unit_is_not_in_europe_port() throws Exception {
        when(placeCargoSlot.isOwnerAtEuropePort()).thenReturn(false);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            unit.placeToEuropePortPier();
        });

        assertEquals("Holding unit is not at europe port, cant be placed to port pier.",
                exception.getMessage());
    }

    @BeforeEach
    public void setup() {
        makeColonist(model, 4, placeCargoSlot, owner, 3);
    }

}
