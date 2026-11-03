package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.unit.UnitActionType;
import org.akropia.model.unit.UnitFreeColonist;

class UnitTaskSignTest {
    private Model model() {
        return Model.make(new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.akropia")
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
    }

    @ParameterizedTest
    @CsvSource(value = { "noAction,''", "sentry,s", "fortify,f", "plowField,p",
            "clearForest,c", "buildRoad,r" })
    void retainedActionHasItsSignAtZeroMovement(final UnitActionType action,
            final String sign) {
        final Model model = model();
        final Unit worker = new UnitFreeColonist(model, 999,
                unit -> new PlaceLocation(unit, Location.of(25, 14),
                        Direction.east),
                model.getUnitById(0).getOwner(), 0, action.make(), 100, false,
                false);
        assertEquals(sign, worker.getTaskSign());
    }

    @Test
    void pausedDestinationAndBlockedRouteRemainVisible() {
        final Model model = model();
        final AbstractUnit ship = (AbstractUnit) model.getUnitById(3);
        ship.startGoTo(
                new GoToOrder(Location.of(25, 14), null, "destination", true));
        ship.setActionPoints(0);
        assertEquals("g", ship.getTaskSign());
        ship.cancelGoTo();
        assertEquals("", ship.getTaskSign());
        ship.startTradeRouteAssignment(new TradeRouteAssignment(0, 0,
                TradeRoutePause.STOP_UNREACHABLE,
                List.of(TradeRouteStop.ofColony(0), TradeRouteStop.europe())));
        assertEquals("t", ship.getTaskSign());
    }
}
