package org.akropia.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteStop;
import org.akropia.page.ColonyScreen;
import org.akropia.page.GamePage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Stations of a route with more than four distinct destinations (R-0421). Dutch
 * owns twelve land-connected colonies A1 to D3 on a grid; every colony can be
 * reached from every other one by wagon.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_206_F222_trade_route_distinct_stations_test
        extends AbstractAkropiaTest {

    private static final Location A1_LOCATION = Location.of(20, 10);
    private static final TradeRouteStop A2 = TradeRouteStop.ofColony(1);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F222-colony-markers.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_five_distinct_stations_are_added_reordered_and_removed()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(A1_LOCATION, "A1");

        // A land route from A1 with A2 as its only stop.
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Grid Run");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, A2);
        route.create();
        route.verifyStoredRoute("Grid Run", TradeRouteKind.LAND, "A2");

        // Add destination offers every legal colony except the chosen one.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Grid Run");
        cargo.verifyStopChoices("1. A2");
        cargo.verifyAddStationOffers("A1", "A3", "B1", "B2", "B3", "C1", "C2",
                "C3", "D1", "D2", "D3");

        // Four more distinct stations make five; each one leaves the list.
        cargo.addStation("B2");
        cargo.addStation("C2");
        cargo.addStation("D2");
        cargo.addStation("D3");
        cargo.verifyStopChoices("1. A2", "2. B2", "3. C2", "4. D2", "5. D3");
        cargo.verifyAddStationOffers("A1", "A3", "B1", "B3", "C1", "C3", "D1");
        cargo.verifyStationButtons(0, false, true, true);
        cargo.verifyStationButtons(4, true, false, true);

        // D3 moves to the front; the goods matrix follows the station order.
        cargo.moveStationUp(4);
        cargo.moveStationUp(3);
        cargo.moveStationUp(2);
        cargo.moveStationUp(1);
        cargo.verifyStopChoices("1. D3", "2. A2", "3. B2", "4. C2", "5. D2");
        cargo.openGoodsTab();
        cargo.verifyGoodsRows("1. D3", "2. A2", "3. B2", "4. C2", "5. D2");
        cargo.clickCell(0, GoodsType.ORE);
        cargo.verifyCell(0, GoodsType.ORE, "+", "D3, Ore: load");

        // B2 is removed and A1 added; the removed colony is offered again.
        cargo.openStationsTab();
        cargo.removeStation(2);
        cargo.verifyAddStationOffers("A1", "A3", "B1", "B2", "B3", "C1", "C3",
                "D1");
        cargo.addStation("A1");
        cargo.verifyStopChoices("1. D3", "2. A2", "3. C2", "4. D2", "5. A1");
        cargo.openGoodsTab();
        cargo.verifyGoodsRows("1. D3", "2. A2", "3. C2", "4. D2", "5. A1");
        cargo.verifyCell(0, GoodsType.ORE, "+", "D3, Ore: load");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-five-stations.png");

        // Save stores the five stations in this order with their goods.
        cargo.save();
        cargo.verifyMessage(
                "Route saved. Each carrier takes the changes at its next stop.");
        cargo.verifyStoredStops("Grid Run", "D3", "A2", "C2", "D2", "A1");
        cargo.verifyStoredCargo("Grid Run", 0, List.of(),
                List.of(GoodsType.ORE));
        cargo.verifyRouteSummary("Land (wagon) route · 5 stops");

        // In the smallest supported window the page scrolls to the last
        // station, whose actions stay usable.
        cargo.resizeWindow(1024, 768);
        cargo.openStationsTab();
        cargo.verifyStationRowFits(4);
        cargo.moveStationUp(4);
        cargo.verifyStopChoices("1. D3", "2. A2", "3. C2", "4. A1", "5. D2");
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-five-stations-1024.png");
        cargo.discardChanges();
        cargo.verifyStopChoices("1. D3", "2. A2", "3. C2", "4. D2", "5. A1");
        cargo.close();
        cargo.verifyDialogClosed();
        colony.close();
    }
}
