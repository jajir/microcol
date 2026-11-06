package org.akropia.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRoutePause;
import org.akropia.model.TradeRouteStop;
import org.akropia.page.ColonyScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.SettingPage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.TradeRoutePlanningPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Editing, canceling and deleting trade routes. New Amsterdam (id 0, ORE 90,
 * WEAPONS 50) stands at (26,16), Delft (id 1, ORE 0, WEAPONS 50) at (28,16) and
 * Haarlem (id 2, ORE 40, WEAPONS 20) at (24,16), each two squares from the next
 * over grass, so a wagon needs two turns for a leg. Wagons 4 and 6 stand free
 * in New Amsterdam. The "Ore cart" route unloads ORE and loads WEAPONS in Delft
 * and does the opposite in New Amsterdam. Wagon 8 waits there with a lost stop
 * on "Lost cart", whose second stop names a colony that was never founded.
 * Galleon 7, holding OLIVE_OIL 100 and WOOL 60 in its six holds, is on the
 * Atlantic with two turns left, following "Atlantic run". The Dutch player has
 * 10000 gold. Each test starts from the same saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_119_F110_trade_route_edit_test extends AbstractAkropiaTest {

    private static final Location NEW_AMSTERDAM_LOCATION = Location.of(26, 16);
    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final Location HAARLEM_LOCATION = Location.of(24, 16);
    private static final Location LANE_LOCATION = Location.of(49, 15);
    /** Ocean square west of the sea lane where the crossing lands. */
    private static final Location WEST_OF_LANE_LOCATION = Location.of(48, 15);
    /** Grass square the wagons take on their first step toward Delft. */
    private static final Location ROAD_LOCATION = Location.of(27, 16);
    /** Grass square south of that first step, free of units. */
    private static final Location SOUTH_OF_ROAD_LOCATION = Location.of(27, 17);
    private static final int NEW_AMSTERDAM_ID = 0;
    private static final int DELFT_ID = 1;
    private static final int HAARLEM_ID = 2;
    private static final int FIRST_WAGON_ID = 4;
    private static final int SECOND_WAGON_ID = 6;
    private static final int GALLEON_ID = 7;
    private static final int LOST_WAGON_ID = 8;
    /** Index of the Delft stop in the Ore cart route. */
    private static final int DELFT_STOP = 0;
    /** Index of the New Amsterdam stop in the Ore cart route. */
    private static final int NEW_AMSTERDAM_STOP = 1;
    /** Index of the lost stop in the Lost cart route. */
    private static final int LOST_STOP = 1;
    /** Route name at the 64-character limit for player-entered text. */
    private static final String LONG_ROUTE_NAME = "Northern Ore and Weapons Supply Line from Delft to New Amsterdam";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F110-trade-route-edit.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_stop_replacement_reaches_each_carrier_at_its_own_next_stop()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The player prepares replacing the New Amsterdam stop of the Ore cart
        // route with Haarlem, then abandons the dialog with Escape. Nothing is
        // stored, and the whole dialog fits the screen. The heading is on the
        // left; the route menu and Back are on the right, Back last.
        cargo.open();
        cargo.verifyOpened();
        cargo.verifyDialogFitsScreen();
        cargo.verifyHeaderLayout();
        cargo.verifyHeaderControls("Back");
        cargo.verifyRouteChoices("Ore cart", "Lost cart", "Atlantic run");
        cargo.chooseRoute("Ore cart");
        cargo.openCarriersTab();
        cargo.verifyNoCarrierRow("No ship or wagon follows this route.");
        cargo.replaceStation(NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifySaveEnabled(true);
        cargo.verifyStationStatus(
                "Unsaved route changes. Save them or cancel them.");
        // Escape first warns about the unsaved change; a second press
        // discards it.
        cargo.closeWithEscape();
        cargo.verifyMessage(
                "The route has unsaved changes. Save or cancel them, or press again to discard them.");
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "New Amsterdam");

        // Wagon 4 joins at New Amsterdam, and one turn passes: it takes its
        // first step.
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.addCarrier(FIRST_WAGON_ID);
        cargo.verifyMessage(
                "Wagon #4 now follows this route, starting at New Amsterdam.");
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);

        // Wagon 6, now the first free wagon in New Amsterdam, follows the same
        // route a turn behind. Two more turns pass.
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.addCarrier(SECOND_WAGON_ID);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();

        // Wagon 4 took the WEAPONS in Delft and is on its way back. Wagon 6
        // stands in Delft; the WEAPONS was gone, so it took nothing, and its
        // visit there is processed.
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyHoldEmpty(SECOND_WAGON_ID, 0);
        cargo.verifyVisitProcessed(SECOND_WAGON_ID, true);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));

        // Both wagons are listed although neither stands in New Amsterdam.
        // The player replaces the New Amsterdam stop with Haarlem.
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.openCarriersTab();
        cargo.verifyCarrierRows(FIRST_WAGON_ID, SECOND_WAGON_ID);
        cargo.verifyCarrierRow(FIRST_WAGON_ID, "Wagon #4",
                "\u2192 New Amsterdam", "On the way.");
        cargo.verifyCarrierRow(SECOND_WAGON_ID, "Wagon #6",
                "\u2192 New Amsterdam", "On the way.");
        cargo.replaceStation(NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyStopChoices("1. Delft", "2. Haarlem");
        // A new destination starts without goods; Haarlem gets the cargo the
        // New Amsterdam stop had: WEAPONS off, then ORE on.
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.verifyUnloadOrder();
        cargo.verifyLoadOrder();
        cargo.addUnload(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.ORE);
        cargo.verifyUnloadOrder(GoodsType.WEAPONS);
        cargo.verifyLoadOrder(GoodsType.ORE);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-edit-before.png");
        cargo.save();
        cargo.verifyMessage(
                "Route saved. Each carrier takes the changes at its next stop.");
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyStoredCargo("Ore cart", NEW_AMSTERDAM_STOP,
                List.of(GoodsType.WEAPONS), List.of(GoodsType.ORE));
        cargo.verifySaveEnabled(false);

        // Wagon 4 is still on its leg and keeps New Amsterdam as the stop it
        // accepted. Wagon 6 has not left Delft, so it already heads for
        // Haarlem. The edit moved and transferred nothing.
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(FIRST_WAGON_ID, "Wagon #4",
                "\u2192 New Amsterdam", "On the way.");
        cargo.verifyCarrierRow(SECOND_WAGON_ID, "Wagon #6", "\u2192 Haarlem",
                "On the way.");
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP,
                "New Amsterdam");
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyGold(10000);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-edit-pending.png");
        cargo.close();
        game = colony.close();

        // Save and Continue keep the pending change and both legs, and move
        // or transfer nothing.
        cargo.rememberUnitLocation(FIRST_WAGON_ID);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyUnitAtRememberedLocation(FIRST_WAGON_ID);
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyVisitProcessed(SECOND_WAGON_ID, true);
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP,
                "New Amsterdam");
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));

        // Next turn: wagon 4 reaches the stop it had accepted and processes it
        // once, WEAPONS off and ORE on. Wagon 6 leaves Delft for Haarlem.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(FIRST_WAGON_ID, 0);
        cargo.verifyHoldEmpty(FIRST_WAGON_ID, 1);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyUnitBetween(SECOND_WAGON_ID, DELFT_LOCATION,
                NEW_AMSTERDAM_LOCATION);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));

        // Three more turns: wagon 4 unloads its ORE in Delft, and wagon 6
        // reaches Haarlem and loads the ORE 40 stored there, once.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(SECOND_WAGON_ID, HAARLEM_LOCATION);
        cargo.verifyHold(SECOND_WAGON_ID, 0, Goods.of(GoodsType.ORE, 40));
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.WEAPONS, 20));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_02_canceling_one_of_two_carriers_on_the_road_releases_only_that_unit()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // Both free wagons join at New Amsterdam before heading to Delft.
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.addCarrier(FIRST_WAGON_ID);
        // Wagon 4 now has an order, so it is no longer offered; wagon 6 is.
        cargo.openAddCarrierList();
        cargo.verifyAddCarrierOffered("Wagon #4", false);
        cargo.chooseOfferedCarrier(SECOND_WAGON_ID);
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyUnitBetween(SECOND_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);

        // Both wagons are on the road, away from any stop. The player still
        // finds wagon 4 in the list of followers and releases only that one.
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.openCarriersTab();
        cargo.verifyCarrierRows(FIRST_WAGON_ID, SECOND_WAGON_ID);
        cargo.verifyCarrierRow(FIRST_WAGON_ID, "Wagon #4", "\u2192 Delft",
                "On the way.");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-cancel-one.png");
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 0);
        cargo.removeCarrier(FIRST_WAGON_ID);
        cargo.verifyMessage(
                "Wagon #4 is back on manual orders. Nothing else changed.");
        cargo.verifyNotAssigned(FIRST_WAGON_ID);
        cargo.verifyNoStandingOrder(FIRST_WAGON_ID);
        // Releasing the wagon keeps where it is and refunds no movement: it
        // already took this turn's step on the route.
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 0);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyCarrierRows(SECOND_WAGON_ID);
        cargo.verifyCarrierRow(SECOND_WAGON_ID, "Wagon #6", "\u2192 Delft",
                "On the way.");
        cargo.close();
        game = colony.close();

        // The next turn: wagon 6 reaches Delft and takes the WEAPONS, while
        // wagon 4 stays where it was released and gets its allowance back.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyNotAssigned(FIRST_WAGON_ID);
        cargo.verifyNoStandingOrder(FIRST_WAGON_ID);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 3);
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyHold(SECOND_WAGON_ID, 0, Goods.of(GoodsType.WEAPONS, 50));
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.verifyGold(10000);

        // The player now moves the released wagon by hand, one square south
        // onto grass, which uses its whole allowance.
        game.panToTile(ROAD_LOCATION);
        game.selectTile(ROAD_LOCATION);
        game.moveMouseAtLocation(ROAD_LOCATION);
        game.dragMouseAtLocation(SOUTH_OF_ROAD_LOCATION);
        cargo.verifyUnitAt(FIRST_WAGON_ID, SOUTH_OF_ROAD_LOCATION);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 0);
        cargo.verifyNoStandingOrder(FIRST_WAGON_ID);
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.ORE, 90));
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
    }

    @Test
    @Tag("local")
    void TC_03_deleting_a_route_releases_a_ship_on_the_atlantic_without_restarting_its_voyage()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The galleon is on the Atlantic with two turns left, holding its
        // goods. It is not at any stop, yet the dialog lists it.
        cargo.open();
        cargo.chooseRoute("Atlantic run");
        cargo.openCarriersTab();
        cargo.verifyCarrierRows(GALLEON_ID);
        cargo.verifyCarrierRow(GALLEON_ID, "Galleon #7", "\u2192 Delft",
                "Crossing the Atlantic.");
        cargo.verifyAtSea(GALLEON_ID, 2);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.WOOL, 60));
        cargo.verifyGold(10000);

        // The first press on Delete only names the carrier it would release.
        cargo.verifyDeleteButtonText("Delete route");
        cargo.askToDeleteRoute();
        cargo.verifyDeleteButtonText("Confirm: delete route");
        cargo.verifyDeleteInfo(
                "Press again to delete this route. Released to manual orders where they stand: Galleon #7.");
        cargo.verifyRouteStored("Atlantic run", true);
        cargo.verifyAssignment(GALLEON_ID, "Atlantic run", 1, false);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-delete-confirm.png");

        // The second press deletes it. The ship keeps its place, countdown,
        // goods and the player's gold, and another carrier is not touched.
        cargo.confirmDeleteRoute();
        cargo.verifyMessage(
                "Route deleted. Carriers released to manual orders: 1.");
        cargo.verifyRouteOffered("Atlantic run", false);
        cargo.verifyRouteStored("Atlantic run", false);
        cargo.verifyNotAssigned(GALLEON_ID);
        cargo.verifyAtSea(GALLEON_ID, 2);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.WOOL, 60));
        cargo.verifyGold(10000);
        cargo.verifyAssignment(LOST_WAGON_ID, "Lost cart", LOST_STOP, true);
        cargo.close();
        game = colony.close();

        // One turn: the countdown goes on, it does not restart.
        game.nextTurnAndCloseDialogs();
        cargo.verifyAtSea(GALLEON_ID, 1);
        cargo.verifyNotAssigned(GALLEON_ID);

        // Save and Continue bring back neither the route nor the order.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyRouteStored("Atlantic run", false);
        cargo.verifyNotAssigned(GALLEON_ID);
        cargo.verifyAtSea(GALLEON_ID, 1);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyGold(10000);

        // The crossing ends as usual. The ship lands on the sea lane with its
        // goods and obeys its owner.
        game.nextTurnAndCloseDialogs();
        cargo.verifyNotAtSea(GALLEON_ID);
        cargo.verifyUnitAt(GALLEON_ID, LANE_LOCATION);
        cargo.verifyNoStandingOrder(GALLEON_ID);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.WOOL, 60));
        cargo.verifyGold(10000);

        // The player sails the landed ship one square west by hand.
        game.panToTile(LANE_LOCATION);
        game.selectTile(LANE_LOCATION);
        game.moveMouseAtLocation(LANE_LOCATION);
        game.dragMouseAtLocation(WEST_OF_LANE_LOCATION);
        cargo.verifyUnitAt(GALLEON_ID, WEST_OF_LANE_LOCATION);
        cargo.verifyNoStandingOrder(GALLEON_ID);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.WOOL, 60));
        cargo.verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_04_repairing_a_lost_stop_resumes_the_waiting_wagon_in_finite_steps()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // Wagon 8 waits at New Amsterdam because the second stop of its route
        // names a colony that is gone. The reason is shown.
        cargo.open();
        cargo.verifyDialogFitsScreen();
        cargo.chooseRoute("Lost cart");
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(LOST_WAGON_ID, "Wagon #8", "\u2192 lost colony",
                "The stop colony is lost or no longer yours.");
        cargo.verifyPauseReason(LOST_WAGON_ID, TradeRoutePause.STOP_LOST);
        cargo.verifyUnitAt(LOST_WAGON_ID, NEW_AMSTERDAM_LOCATION);

        // The player repairs the route: the lost stop becomes Haarlem.
        cargo.replaceStation(LOST_STOP, "Haarlem");
        cargo.verifySaveEnabled(true);
        cargo.save();
        cargo.verifyMessage(
                "Route saved. Each carrier takes the changes at its next stop.");
        cargo.verifyStoredStop("Lost cart", LOST_STOP, "Haarlem");

        // The wagon has not moved. It says that the repair is taken next turn.
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(LOST_WAGON_ID, "Wagon #8", "\u2192 Haarlem",
                "The stop colony is lost or no longer yours.");
        cargo.verifyAcceptedStop(LOST_WAGON_ID, LOST_STOP, "lost 3");
        cargo.verifyUnitAt(LOST_WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.captureDialog("tradeRouteCargoDialog", "trade-route-repair.png");
        cargo.close();
        game = colony.close();

        // One turn: an ordinary step toward Haarlem and the reason is gone.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(LOST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                HAARLEM_LOCATION);
        cargo.verifyNotPaused(LOST_WAGON_ID);
        cargo.verifyAssignment(LOST_WAGON_ID, "Lost cart", LOST_STOP, false);
        cargo.verifyAcceptedStop(LOST_WAGON_ID, LOST_STOP, "Haarlem");

        // Another turn: it arrives in Haarlem, processes the stop once and
        // heads for the first stop of its route.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(LOST_WAGON_ID, HAARLEM_LOCATION);
        cargo.verifyVisitProcessed(LOST_WAGON_ID, true);
        cargo.verifyAssignment(LOST_WAGON_ID, "Lost cart", 0, false);
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.ORE, 40));
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.WEAPONS, 20));
        cargo.verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_05_czech_dialog_cancels_replaces_and_deletes_from_the_keyboard()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The waiting wagon and its reason are readable in Czech, and the
        // whole dialog fits the screen.
        cargo.open();
        cargo.verifyTitle("Správa obchodní trasy");
        cargo.verifyDialogFitsScreen();
        cargo.verifyHeaderLayout();
        cargo.verifyHeaderControls("Zpět");
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-header-czech.png");
        cargo.chooseRouteWithKeyboard("Lost cart");
        cargo.verifyChosenRoute("Lost cart");
        cargo.verifyRouteSummary("Trasa Pozemní (vůz) · počet zastávek: 2");
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(LOST_WAGON_ID, "Povoz č. 8",
                "\u2192 ztracená kolonie",
                "Kolonie zastávky je ztracena nebo už není vaše.");

        // The player gives up on that route for this wagon with the keyboard.
        cargo.verifyCarrierRemoveText(LOST_WAGON_ID, "Odebrat");
        cargo.removeCarrierWithKeyboard(LOST_WAGON_ID);
        cargo.verifyMessage(
                "Povoz č. 8 je zpět na ručních příkazech. Nic dalšího se nezměnilo.");
        cargo.verifyNotAssigned(LOST_WAGON_ID);
        cargo.verifyNoCarrierRow("Žádná loď ani vůz nejede podle této trasy.");

        // The Ore cart route gets Haarlem for its New Amsterdam stop, also
        // with the keyboard.
        cargo.chooseRoute("Ore cart");
        cargo.openStationsTab();
        cargo.verifyTabs("Stanice", "Přeprava", "Zboží");
        cargo.replaceStation(NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyStationStatus(
                "Trasa má neuložené změny. Uložte je nebo je zrušte.");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.addUnload(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.ORE);
        cargo.openStationsTab();
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-edit-czech.png");
        cargo.saveWithKeyboard();
        cargo.verifyMessage(
                "Trasa je uložena. Každý dopravce převezme změny na své příští zastávce.");
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyStoredCargo("Ore cart", NEW_AMSTERDAM_STOP,
                List.of(GoodsType.WEAPONS), List.of(GoodsType.ORE));

        // Deleting needs a second press that names the affected carriers.
        cargo.askToDeleteRouteWithKeyboard();
        cargo.verifyDeleteButtonText("Potvrdit: smazat trasu");
        cargo.verifyDeleteInfo(
                "Stiskněte znovu pro smazání trasy. Na ruční příkazy tam, kde stojí, se vrátí: žádné.");
        cargo.verifyRouteStored("Ore cart", true);
        cargo.confirmDeleteRouteWithKeyboard();
        cargo.verifyMessage(
                "Trasa smazána. Jednotky vrácené na ruční příkazy: 0.");
        cargo.verifyRouteStored("Ore cart", false);

        // Escape closes the dialog; the colony screen is back.
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_08_long_route_name_keeps_the_header_usable_in_a_small_window()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION,
                "New Amsterdam");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The player creates a land route whose name uses the whole
        // 64-character limit.
        route.open();
        route.enterName(LONG_ROUTE_NAME);
        route.chooseStop(0, TradeRouteStop.ofColony(DELFT_ID));
        route.chooseStop(1, TradeRouteStop.ofColony(NEW_AMSTERDAM_ID));
        route.create();
        route.verifyDialogClosed();

        // In the smallest supported window, the route menu has the focus and
        // offers every route; the long route is chosen with the keyboard.
        cargo.resizeWindow(1024, 768);
        cargo.open();
        cargo.verifyRouteMenuFocused();
        cargo.verifyRouteChoices("Ore cart", "Lost cart", "Atlantic run",
                LONG_ROUTE_NAME);
        cargo.chooseRouteWithKeyboard(LONG_ROUTE_NAME);
        cargo.verifyChosenRoute(LONG_ROUTE_NAME);
        cargo.verifyChosenRouteLength(64);
        cargo.verifyRouteSummary("Land (wagon) route · 2 stops");
        cargo.verifyStopChoices("1. Delft", "2. New Amsterdam");

        // The shortened name leaves the heading, the menu and Back side by
        // side, Back last and fully inside the window.
        cargo.verifyHeaderLayout();
        cargo.verifyHeaderControls("Back");
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-header-long-name.png");

        // Switching with the mouse shows the other route's stops.
        cargo.chooseRoute("Lost cart");
        cargo.verifyChosenRoute("Lost cart");
        cargo.verifyStopChoices("1. Delft", "2. lost colony");
        cargo.verifyHeaderLayout();

        // Back with the keyboard returns to the colony; Escape does the same
        // after the dialog is opened again.
        cargo.closeWithKeyboard();
        cargo.verifyDialogClosed();
        cargo.open();
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        cargo.verifyRouteStored(LONG_ROUTE_NAME, true);
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_07_planner_explains_unshown_legs_and_repairs_the_lost_stop()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final TradeRoutePlanningPage planner = new TradeRoutePlanningPage(
                getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // The lost stop draws no path; each leg touching it says why.
        planner.open();
        planner.verifyRouteList("Ore cart", "Lost cart", "Atlantic run");
        planner.chooseRoute("Lost cart");
        planner.verifyCarriers("Carriers: Wagon #8");
        planner.verifyStops("1. Delft — unload: nothing; load: nothing\n"
                + "2. lost colony — unload: nothing; load: nothing");
        planner.verifyLegsContain("New Amsterdam → Delft: by land");
        planner.verifyLegsContain(
                "Delft → lost colony: not shown, a stop is no longer your colony.");
        planner.verifyLegsContain(
                "lost colony → Delft: not shown, a stop is no longer your colony.");
        planner.captureScreen("trade-route-planner-lost-stop.png");

        // Replacing the stop in the shared route window brings the leg back.
        planner.editSelected();
        cargo.verifyChosenRoute("Lost cart");
        cargo.replaceStation(LOST_STOP, "Haarlem");
        cargo.save();
        cargo.verifyMessage(
                "Route saved. Each carrier takes the changes at its next stop.");
        cargo.close();
        planner.verifyStops("1. Delft — unload: nothing; load: nothing\n"
                + "2. Haarlem — unload: nothing; load: nothing");
        planner.verifyLegsContain("Delft → Haarlem: by land");
        planner.verifyLegsContain("Haarlem → Delft: by land");
        planner.verifyRouteDrawnOver("Lost cart", HAARLEM_LOCATION);
        cargo.verifyUnitAt(LOST_WAGON_ID, NEW_AMSTERDAM_LOCATION);

        // The galleon at sea stands for the Atlantic run; its return leg
        // starts where it will come back from Europe.
        planner.chooseRoute("Atlantic run");
        planner.verifyCarriers("Carriers: Galleon #7");
        planner.verifyPreviewNote(
                "Paths are shown for Galleon #7. Other carriers of the route use their own movement costs.");
        planner.verifyLegsContain("Delft → Home port: by sea");
        planner.verifyLegsContain("Home port → Delft: by sea");
        planner.verifyRouteDrawnOver("Atlantic run", DELFT_LOCATION);
        planner.captureScreen("trade-route-planner-atlantic.png");
        planner.close();
        planner.verifyPlannerShown(false);
    }
}
