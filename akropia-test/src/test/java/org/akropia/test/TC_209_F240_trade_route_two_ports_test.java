package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteStop;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.TradeRoutePlanningPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * One map line per transport between two ports (R-0425). A bay with a small
 * arctic island in its middle at (26,16) lies between New Amsterdam (id 0) at
 * (24,16) and Delft (id 1) at (28,16), so both towns are ports. Ships cross the
 * bay north or south of the island, and wagons go around it north or south,
 * each way as cheap as its mirror. Dutch's galleon 3 waits in Delft. Dutch has
 * explored the whole area. Each test starts from the same saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_209_F240_trade_route_two_ports_test
        extends AbstractAkropiaTest {

    private static final Location NEW_AMSTERDAM_LOCATION = Location.of(24, 16);
    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int GALLEON_ID = 3;
    private static final TradeRouteStop NEW_AMSTERDAM = TradeRouteStop
            .ofColony(0);
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    /** The wagon way around the north of the bay. */
    private static final Location[] LAND_LINE = { NEW_AMSTERDAM_LOCATION,
            Location.of(24, 15), Location.of(25, 14), Location.of(26, 14),
            Location.of(27, 14), Location.of(28, 15), DELFT_LOCATION };
    /** The sea way along the north row of the bay. */
    private static final Location[] SEA_LINE = { NEW_AMSTERDAM_LOCATION,
            Location.of(25, 15), Location.of(26, 15), Location.of(27, 15),
            DELFT_LOCATION };

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F240-trade-route-two-ports.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_way_there_and_back_is_one_line_per_transport_with_and_without_a_ship()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TradeRoutePlanningPage planner = new TradeRoutePlanningPage(
                getContext());
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // A sea route from Delft to New Amsterdam and back, created from the
        // planner's bar.
        planner.open();
        planner.newRoute();
        route.chooseStartColony("Delft");
        route.enterName("Bay Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, NEW_AMSTERDAM);
        route.chooseStop(1, DELFT);
        route.create();
        route.verifyStoredRoute("Bay Run", TradeRouteKind.SEA, "New Amsterdam",
                "Delft");

        // Without a carrier each leg has a land and a sea path. Each
        // direction keeps its own cost: wagons enter arctic New Amsterdam
        // but grassland Delft.
        planner.verifySelectedRoute("Bay Run");
        planner.verifyCarriers("No carrier assigned");
        planner.verifyLegsContain(
                "Delft → New Amsterdam: by land, 6 steps, 7 movement points");
        planner.verifyLegsContain(
                "Delft → New Amsterdam: by sea, 4 steps, 4 movement points");
        planner.verifyLegsContain(
                "New Amsterdam → Delft: by land, 6 steps, 6 movement points");
        planner.verifyLegsContain(
                "New Amsterdam → Delft: by sea, 4 steps, 4 movement points");

        // The way there and back is one land line north around the bay and
        // one sea line along its north row, each chosen among equally cheap
        // ways.
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.LAND,
                LAND_LINE);
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.SEA, SEA_LINE);
        planner.captureScreen("trade-route-planner-two-ports.png");

        // Reopening the planner draws the same two lines.
        planner.close();
        planner.open();
        planner.chooseRoute("Bay Run");
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.LAND,
                LAND_LINE);
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.SEA, SEA_LINE);

        // The galleon in Delft joins the route. Its own path there and back
        // is the same single sea line, and no land line is drawn.
        planner.editSelected();
        cargo.verifyChosenRoute("Bay Run");
        cargo.openCarriersTab();
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyMessage(
                "Galleon #3 now follows this route, starting at Delft.");
        cargo.close();
        planner.verifyCarriers("Carriers: Galleon #3");
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.SEA, SEA_LINE);
        planner.verifyDrawnConnectionCount("Bay Run", TradeRouteKind.LAND, 0);
        planner.captureScreen("trade-route-planner-two-ports-galleon.png");
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);

        // Save and Continue keep the route, its galleon and its single sea
        // line; nothing has moved.
        planner.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        planner.verifyPlannerShown(false);
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);
        planner.open();
        planner.chooseRoute("Bay Run");
        planner.verifyCarriers("Carriers: Galleon #3");
        planner.verifyDrawnConnection("Bay Run", TradeRouteKind.SEA, SEA_LINE);
        planner.verifyDrawnConnectionCount("Bay Run", TradeRouteKind.LAND, 0);
        planner.close();
        planner.verifyPlannerShown(false);
    }
}
