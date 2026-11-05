package org.akropia.test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteStop;
import org.akropia.page.ColonyScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.TradeRoutePlanningPage;
import org.akropia.page.SettingPage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Assigning a wagon to a trade route. New Amsterdam (id 0, ORE 90, WEAPONS 50)
 * stands at (26,16) and Delft (id 1, ORE 0, WEAPONS 50) at (28,16), two squares
 * apart over grass. A wagon moves one square a turn, so each leg takes two
 * turns. Wagon 4 stands in New Amsterdam without orders. Wagon 6 stands there
 * too and was saved following the "Idle cart" route with a waiting reason, so
 * the reason can be read before any turn passes. Each test starts from the same
 * saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_118_F109_trade_route_assignment_test
        extends AbstractAkropiaTest {

    private static final Location NEW_AMSTERDAM_LOCATION = Location.of(26, 16);
    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int NEW_AMSTERDAM_ID = 0;
    private static final int DELFT_ID = 1;
    private static final int WAGON_ID = 4;
    private static final int IDLE_WAGON_ID = 6;
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    private static final TradeRouteStop NEW_AMSTERDAM = TradeRouteStop
            .ofColony(0);
    /** Index of the Delft stop in the Delft, New Amsterdam route. */
    private static final int DELFT_STOP = 0;
    /** Index of the New Amsterdam stop in the Delft, New Amsterdam route. */
    private static final int NEW_AMSTERDAM_STOP = 1;
    private static final String ROUTE_ROW = "Trade route Ore Cart, Wagon #4";
    private static final String ORE_LOADED = "Trade route Ore Cart, Wagon #4, stop 2 (New Amsterdam): loaded 90 Ore.";
    private static final String ORE_UNLOADED = "Trade route Ore Cart, Wagon #4, stop 1 (Delft): unloaded 90 Ore.";
    private static final String WEAPONS_LOADED = "Trade route Ore Cart, Wagon #4, stop 1 (Delft): loaded 50 Weapons.";
    private static final String WEAPONS_UNLOADED = "Trade route Ore Cart, Wagon #4, stop 2 (New Amsterdam): unloaded 50 Weapons.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F109-trade-route-assignment.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_wagon_repeats_the_route_for_two_circuits_and_continues_midleg()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");

        // A land route from New Amsterdam to Delft and back, made in the route
        // editor.
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Ore Cart");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, DELFT);
        route.chooseStop(1, NEW_AMSTERDAM);
        route.create();
        route.verifyStoredRoute("Ore Cart", TradeRouteKind.LAND, "Delft",
                "New Amsterdam");

        // The saved wagon 6 already follows the Idle cart route, and the
        // dialog says that it waits and why.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyOpened();
        cargo.chooseRoute("Idle cart");
        cargo.verifyAssignmentHeading("Following this route");
        cargo.verifyAssigned(
                "Wagon #6: waiting, next stop Delft. The stop cannot be reached now; it tries again next turn.");
        cargo.verifyAssignment(IDLE_WAGON_ID, "Idle cart", 0, true);

        // The new route has no carrier yet. Its cargo: Delft takes the ore off
        // and puts the weapon on, New Amsterdam does the opposite.
        cargo.chooseRoute("Ore Cart");
        cargo.verifyAssigned("No ship or wagon follows this route.");
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.WEAPONS);
        cargo.save();
        cargo.verifyStoredCargo("Ore Cart", DELFT_STOP, List.of(GoodsType.ORE),
                List.of(GoodsType.WEAPONS));
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.addUnload(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.ORE);
        cargo.save();
        cargo.verifyStoredCargo("Ore Cart", NEW_AMSTERDAM_STOP,
                List.of(GoodsType.WEAPONS), List.of(GoodsType.ORE));

        // Both wagons stand at this stop. The one that already has an order
        // is refused with its reason; the free one can be assigned.
        cargo.chooseCarrier(1);
        cargo.verifyAssignEnabled(false);
        cargo.verifyAssignStatus(
                "The unit already has an order, is damaged or is not free to move.");
        cargo.chooseCarrier(0);
        cargo.verifyAssignStatus("");

        // Automatic entry picks the current colony, so the wagon loads here
        // first.
        cargo.verifyAssignEnabled(true);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-assignment.png");
        cargo.assign();
        cargo.verifyMessage("Assigned. The unit starts on your next turn.");
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", NEW_AMSTERDAM_STOP, false);
        cargo.verifyAssigned("Wagon #4: heading to New Amsterdam.");

        // Assigning moved nothing and processed nothing, and a second
        // assignment is refused.
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON_ID, 0);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyAssignEnabled(false);
        cargo.verifyAssignStatus(
                "The unit already has an order, is damaged or is not free to move.");
        cargo.close();
        cargo.verifyDialogClosed();
        game = colony.close();

        // First turn: the wagon loads the ore once in New Amsterdam and takes
        // its first step, and the turn report lists the load once. The
        // waiting wagon resumes and takes its first step.
        Optional<ScreenTurnReport> report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.get().verifyEventMessageCount(ORE_LOADED, 1);
        report.get().verifyEventMessageCount(ROUTE_ROW, 1);
        report.get().close();
        cargo.verifyHold(WAGON_ID, 0, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", DELFT_STOP, false);
        cargo.verifyUnitBetween(IDLE_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyAssignment(IDLE_WAGON_ID, "Idle cart", 0, false);

        // Second turn: in Delft the ore comes off and the weapon goes on, one
        // report row each.
        report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.get().verifyEventMessageCount(ORE_UNLOADED, 1);
        report.get().verifyEventMessageCount(WEAPONS_LOADED, 1);
        report.get().verifyEventMessageCount(ROUTE_ROW, 2);
        report.get().close();
        cargo.verifyUnitAt(WAGON_ID, DELFT_LOCATION);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));
        cargo.verifyHold(WAGON_ID, 0, Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", NEW_AMSTERDAM_STOP, false);

        // Save and Continue keep both rows once and transfer nothing again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        report = Optional.of(game.openTurnReport());
        report.get().verifyEventMessageCount(ORE_UNLOADED, 1);
        report.get().verifyEventMessageCount(WEAPONS_LOADED, 1);
        report.get().verifyEventMessageCount(ROUTE_ROW, 2);
        report.get().close();
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyHold(WAGON_ID, 0, Goods.of(GoodsType.WEAPONS, 50));

        // Third and fourth turn: back in New Amsterdam the weapon comes off
        // and the empty ore stock loads nothing, so only the unload has a
        // report row. The first circuit is done.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.get().verifyEventMessageCount(WEAPONS_UNLOADED, 1);
        report.get().verifyEventMessageCount(ROUTE_ROW, 1);
        report.get().close();
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON_ID, 0);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", DELFT_STOP, false);

        // Fifth turn: the wagon is halfway to Delft again. Save and Continue
        // keep the assignment, the stop and the exact square, and move
        // nothing.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.rememberUnitLocation(WAGON_ID);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyUnitAtRememberedLocation(WAGON_ID);
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", DELFT_STOP, false);
        cargo.verifyHoldEmpty(WAGON_ID, 0);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));

        // Sixth to eighth turn: the second circuit passes both stops again and
        // nothing is transferred twice.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON_ID, DELFT_LOCATION);
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON_ID, 0);
        cargo.verifyHoldEmpty(WAGON_ID, 1);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", DELFT_STOP, false);
    }

    @Test
    @Tag("local")
    void TC_02_closing_the_dialog_without_assigning_changes_nothing()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Unused Cart");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, DELFT);
        route.chooseStop(1, NEW_AMSTERDAM);
        route.create();

        // Everything is chosen, but the player closes the dialog.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Unused Cart");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.verifyAssignEnabled(true);
        cargo.close();
        cargo.verifyDialogClosed();
        cargo.verifyNotAssigned(WAGON_ID);
        colony.close();

        // A turn later the wagon is still where it was.
        game.nextTurnAndCloseDialogs();
        cargo.verifyNotAssigned(WAGON_ID);
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
    }

    @Test
    @Tag("local")
    void TC_03_czech_dialog_shows_the_pause_and_works_from_the_keyboard()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Povozová cesta");
        route.chooseKind(TradeRouteKind.LAND);
        route.chooseStop(0, DELFT);
        route.chooseStop(1, NEW_AMSTERDAM);
        route.create();

        // The waiting wagon and its reason are readable in Czech.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyTitle("Správa obchodní trasy");
        cargo.chooseRoute("Idle cart");
        cargo.verifyAssignmentHeading("Jede podle této trasy");
        cargo.verifyAssigned(
                "Povoz č. 6: čeká, další zastávka Delft. Zastávku teď nelze dosáhnout, příští tah to zkusí znovu.");

        // The free wagon is assigned with the keyboard.
        cargo.chooseRoute("Povozová cesta");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.verifyAssignButtonText("Přidělit trase");
        cargo.verifyAssignEnabled(true);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-assignment-czech.png");
        cargo.assignWithKeyboard();
        cargo.verifyMessage(
                "Přiděleno. Jednotka vyrazí ve vašem příštím tahu.");
        cargo.verifyAssignment(WAGON_ID, "Povozová cesta", NEW_AMSTERDAM_STOP,
                false);
        cargo.verifyAssigned("Povoz č. 4: míří na New Amsterdam.");
        cargo.close();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_04_overlapping_routes_are_chosen_one_by_one_and_get_a_carrier()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TradeRoutePlanningPage planner = new TradeRoutePlanningPage(
                getContext());
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The saved Idle cart route is drawn for its wagon 6.
        planner.open();
        planner.verifyRouteList("Idle cart");
        planner.chooseRoute("Idle cart");
        planner.verifySelectedRoute("Idle cart");
        planner.verifyCarriers("Carriers: Wagon #6");
        planner.verifyPreviewNote(
                "Paths are shown for Wagon #6. Other carriers of the route use their own movement costs.");
        planner.verifyLegsContain("New Amsterdam → Delft: by land, 2 steps");

        // A second route over the same towns, created from the bar.
        planner.newRoute();
        route.enterName("Ore Cart");
        route.chooseStop(0, DELFT);
        route.chooseStop(1, NEW_AMSTERDAM);
        route.create();
        route.verifyStoredRoute("Ore Cart", TradeRouteKind.LAND, "Delft",
                "New Amsterdam");
        planner.verifyRouteList("Idle cart", "Ore Cart");
        planner.verifySelectedRoute("Ore Cart");
        planner.verifyRouteDrawnOver("Idle cart", DELFT_LOCATION);
        planner.verifyRouteDrawnOver("Ore Cart", DELFT_LOCATION);

        // Hovering the shared town names both routes.
        game.hoverTile(DELFT_LOCATION);
        planner.verifyStatusBar(
                "Routes here: Idle cart: Land route, stops: 1, Carriers: Wagon #6. "
                        + "Ore Cart: Land route, stops: 2, No carrier assigned.");
        planner.captureScreen("trade-route-planner-overlap.png");

        // Clicks on the shared square step through the routes there; the
        // list picks either one by name.
        game.clickTile(DELFT_LOCATION);
        planner.verifySelectedRoute("Idle cart");
        game.clickTile(DELFT_LOCATION);
        planner.verifySelectedRoute("Ore Cart");
        planner.chooseRoute("Idle cart");
        planner.verifySelectedRoute("Idle cart");
        planner.chooseRoute("Ore Cart");
        planner.verifySelectedRoute("Ore Cart");

        // Viewing and clicking the map ordered and moved no unit.
        cargo.verifyNotAssigned(WAGON_ID);
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyUnitAt(IDLE_WAGON_ID, NEW_AMSTERDAM_LOCATION);

        // Closing the shared window after choosing a carrier assigns
        // nothing; assigning in it gives the route its wagon.
        planner.editSelected();
        cargo.verifyChosenRoute("Ore Cart");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.close();
        cargo.verifyNotAssigned(WAGON_ID);
        planner.verifyCarriers("No carrier assigned");
        planner.editSelected();
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.assign();
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", NEW_AMSTERDAM_STOP, false);
        cargo.close();
        planner.verifyCarriers("Carriers: Wagon #4");
        planner.verifyPreviewNote(
                "Paths are shown for Wagon #4. Other carriers of the route use their own movement costs.");
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));

        // Save and Continue keep the assignment; nothing moves or transfers.
        planner.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        planner.verifyPlannerShown(false);
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", NEW_AMSTERDAM_STOP, false);
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));
        planner.open();
        planner.chooseRoute("Ore Cart");
        planner.verifyCarriers("Carriers: Wagon #4");
        planner.back();
        planner.verifyPlannerShown(false);
    }

    @Test
    @Tag("local")
    void TC_05_map_context_cancels_assigned_work_without_moving_the_carrier() {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final org.akropia.page.WorldWorkersPage work = new org.akropia.page.WorldWorkersPage(
                getContext());

        // Selecting a paused carrier and requesting movement retains its route.
        game.selectTile(NEW_AMSTERDAM_LOCATION);
        game.selectMapUnit(IDLE_WAGON_ID);
        work.pressMovementShortcut();
        work.verifyMapSelection(IDLE_WAGON_ID, NEW_AMSTERDAM_LOCATION, "t",
                null, 3);

        // Explicit cancellation in the map context preserves cargo and
        // movement.
        game.openUnitContextAt(NEW_AMSTERDAM_LOCATION);
        work.verifyMenuAction("cancelTradeRouteWork", "Cancel work", false);
        work.choose("cancelTradeRouteWork");
        work.verifyMapSelection(IDLE_WAGON_ID, NEW_AMSTERDAM_LOCATION, "", null,
                3);
    }

}
