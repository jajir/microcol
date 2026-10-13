package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.Ship;

import com.google.gson.Gson;

public class UnitColonistInCargoTest {
    private Model model;
    private Unit passenger;
    private Ship carrier;

    @BeforeEach
    public void setup() {
        model = Model.make(new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
        passenger = model.getUnitById(16);
        carrier = (Ship) model.getUnitById(3);
    }

    @Test
    public void test_placeToEuropePortPier() {
        carrier.placeToEuropePort(model.getEurope().getPort());
        final CargoSlot slot = passenger.getPlaceCargoSlot().getCargoSlot();
        final int moves = passenger.getActionPoints();
        passenger.placeToEuropePortPier();
        assertTrue(passenger.isAtEuropePier());
        assertTrue(slot.isEmpty());
        assertEquals(moves, passenger.getActionPoints());
        assertSame(passenger, model.getUnitById(16));
    }

    @Test
    public void test_placeToEuropePortPier_holding_unit_is_not_in_europe_port() {
        assertFalse(carrier.isAtEuropePort());
        final String before = new Gson().toJson(model.save());
        final IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> passenger.placeToEuropePortPier());
        assertEquals("Passenger cannot disembark to the European pier",
                exception.getMessage());
        assertEquals(before, new Gson().toJson(model.save()));
        assertSame(passenger, passenger.getPlaceCargoSlot().getCargoSlot()
                .getUnit().orElseThrow());
    }
}
