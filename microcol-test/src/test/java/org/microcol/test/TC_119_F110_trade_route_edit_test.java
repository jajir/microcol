package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRoutePause;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.TradeRouteCargoDialog;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Editing, canceling and deleting trade routes. New Amsterdam (id 0, ORE 90,
 * MUSKET 50) stands at (26,16), Delft (id 1, ORE 0, MUSKET 50) at (28,16) and
 * Haarlem (id 2, ORE 40, MUSKET 20) at (24,16), each two squares from the next
 * over grass, so a wagon needs two turns for a leg. Wagons 4 and 6 stand free
 * in New Amsterdam. The "Ore cart" route unloads ORE and loads MUSKET in Delft
 * and does the opposite in New Amsterdam. Wagon 8 waits there with a lost stop
 * on "Lost cart", whose second stop names a colony that was never founded.
 * Galleon 7, holding RUM 100 and COTTON 60 in its six holds, is on the Atlantic
 * with two turns left, following "Atlantic run". The Dutch player has 10000
 * gold. Each test starts from the same saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_119_F110_trade_route_edit_test extends AbstractMicroColTest {

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

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F110-trade-route-edit.microcol"));
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
        // stored, and the whole dialog fits the screen.
        cargo.open();
        cargo.verifyOpened();
        cargo.verifyDialogFitsScreen();
        cargo.chooseRoute("Ore cart");
        cargo.verifyAssigned("No ship or wagon follows this route.");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseReplacement("Haarlem");
        cargo.verifyReplaceEnabled(true);
        cargo.verifyReplaceStatus(
                "Carriers keep their current leg and take the change after their next stop: none.");
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "New Amsterdam");

        // Wagon 4 is sent to Delft first, and one turn passes: it takes its
        // first step.
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.chooseFirstStop(DELFT_STOP);
        cargo.verifyAssignEnabled(true);
        cargo.assign();
        cargo.verifyMessage("Assigned. The unit starts on your next turn.");
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", DELFT_STOP, false);
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
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.chooseFirstStop(DELFT_STOP);
        cargo.assign();
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();

        // Wagon 4 took the MUSKET in Delft and is on its way back. Wagon 6
        // stands in Delft; the MUSKET was gone, so it took nothing, and its
        // visit there is processed.
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyHoldEmpty(SECOND_WAGON_ID, 0);
        cargo.verifyVisitProcessed(SECOND_WAGON_ID, true);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));

        // Both wagons are listed although neither stands in New Amsterdam.
        // The player replaces the New Amsterdam stop with Haarlem.
        colony = game.openColonyAt(NEW_AMSTERDAM_LOCATION, "New Amsterdam");
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.verifyAssignedChoices("Wagon #4", "Wagon #6");
        cargo.verifyAssigned("Wagon #4: heading to New Amsterdam.",
                "Wagon #6: heading to New Amsterdam.");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseReplacement("Haarlem");
        cargo.verifyReplaceStatus(
                "Carriers keep their current leg and take the change after their next stop: Wagon #4, Wagon #6.");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-edit-before.png");
        cargo.replaceStop();
        cargo.verifyMessage(
                "The stop changed. Carriers take it at their next stop.");
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyReplaceEnabled(false);

        // Wagon 4 is still on its leg and keeps New Amsterdam as the stop it
        // accepted. Wagon 6 has not left Delft, so it already heads for
        // Haarlem. The edit moved and transferred nothing.
        cargo.verifyAssigned(
                "Wagon #4: heading to New Amsterdam. The change to Haarlem applies after that stop.",
                "Wagon #6: heading to Haarlem.");
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP,
                "New Amsterdam");
        cargo.verifyUnitBetween(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.MUSKET, 50));
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
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyVisitProcessed(SECOND_WAGON_ID, true);
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP,
                "New Amsterdam");
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));

        // Next turn: wagon 4 reaches the stop it had accepted and processes it
        // once, MUSKET off and ORE on. Wagon 6 leaves Delft for Haarlem.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(FIRST_WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHold(FIRST_WAGON_ID, 0, Goods.of(GoodsType.ORE, 90));
        cargo.verifyHoldEmpty(FIRST_WAGON_ID, 1);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.MUSKET, 100));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyAcceptedStop(FIRST_WAGON_ID, NEW_AMSTERDAM_STOP, "Haarlem");
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyUnitBetween(SECOND_WAGON_ID, DELFT_LOCATION,
                NEW_AMSTERDAM_LOCATION);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", NEW_AMSTERDAM_STOP,
                false);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));

        // Three more turns: wagon 4 unloads its ORE in Delft, and wagon 6
        // reaches Haarlem and loads the ORE 40 stored there, once.
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(SECOND_WAGON_ID, HAARLEM_LOCATION);
        cargo.verifyHold(SECOND_WAGON_ID, 0, Goods.of(GoodsType.ORE, 40));
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.MUSKET, 20));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.MUSKET, 100));
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

        // Both free wagons are sent to Delft in the same turn.
        cargo.open();
        cargo.chooseRoute("Ore cart");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseCarrier(0);
        cargo.chooseFirstStop(DELFT_STOP);
        cargo.assign();
        // Wagon 4 is still at the stop, now with an order, so wagon 6 is the
        // second entry of the list.
        cargo.chooseCarrier(1);
        cargo.chooseFirstStop(DELFT_STOP);
        cargo.assign();
        cargo.verifyAssignment(FIRST_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", DELFT_STOP, false);
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
        cargo.verifyAssignedChoices("Wagon #4", "Wagon #6");
        cargo.chooseAssignedCarrier(0);
        cargo.verifyAssignedChoice("Wagon #4");
        cargo.verifyCancelAssignmentEnabled(true);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-cancel-one.png");
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 0);
        cargo.cancelAssignment();
        cargo.verifyMessage(
                "Wagon #4 is back on manual orders. Nothing else changed.");
        cargo.verifyNotAssigned(FIRST_WAGON_ID);
        cargo.verifyNoStandingOrder(FIRST_WAGON_ID);
        // Releasing the wagon keeps where it is and refunds no movement: it
        // already took this turn's step on the route.
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 0);
        cargo.verifyAssignment(SECOND_WAGON_ID, "Ore cart", DELFT_STOP, false);
        cargo.verifyAssignedChoices("Wagon #6");
        cargo.verifyAssigned("Wagon #6: heading to Delft.");
        cargo.close();
        game = colony.close();

        // The next turn: wagon 6 reaches Delft and takes the MUSKET, while
        // wagon 4 stays where it was released and gets its allowance back.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(FIRST_WAGON_ID, ROAD_LOCATION);
        cargo.verifyNotAssigned(FIRST_WAGON_ID);
        cargo.verifyNoStandingOrder(FIRST_WAGON_ID);
        cargo.verifyMovementThirds(FIRST_WAGON_ID, 3);
        cargo.verifyUnitAt(SECOND_WAGON_ID, DELFT_LOCATION);
        cargo.verifyHold(SECOND_WAGON_ID, 0, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyHoldEmpty(FIRST_WAGON_ID, 0);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 90));
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
        cargo.verifyHoldEmpty(FIRST_WAGON_ID, 0);
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
        cargo.verifyAssigned("Galleon #7: heading to Delft.");
        cargo.verifyAssignedChoices("Galleon #7");
        cargo.verifyAtSea(GALLEON_ID, 2);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.COTTON, 60));
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
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.COTTON, 60));
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
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.RUM, 100));
        cargo.verifyGold(10000);

        // The crossing ends as usual. The ship lands on the sea lane with its
        // goods and obeys its owner.
        game.nextTurnAndCloseDialogs();
        cargo.verifyNotAtSea(GALLEON_ID);
        cargo.verifyUnitAt(GALLEON_ID, LANE_LOCATION);
        cargo.verifyNoStandingOrder(GALLEON_ID);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.COTTON, 60));
        cargo.verifyGold(10000);

        // The player sails the landed ship one square west by hand.
        game.panToTile(LANE_LOCATION);
        game.selectTile(LANE_LOCATION);
        game.moveMouseAtLocation(LANE_LOCATION);
        game.dragMouseAtLocation(WEST_OF_LANE_LOCATION);
        cargo.verifyUnitAt(GALLEON_ID, WEST_OF_LANE_LOCATION);
        cargo.verifyNoStandingOrder(GALLEON_ID);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.COTTON, 60));
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
        cargo.verifyAssigned(
                "Wagon #8: waiting, next stop lost colony. The stop colony is lost or no longer yours.");
        cargo.verifyPauseReason(LOST_WAGON_ID, TradeRoutePause.STOP_LOST);
        cargo.verifyUnitAt(LOST_WAGON_ID, NEW_AMSTERDAM_LOCATION);

        // The player repairs the route: the lost stop becomes Haarlem.
        cargo.chooseStop(LOST_STOP);
        cargo.chooseReplacement("Haarlem");
        cargo.verifyReplaceEnabled(true);
        cargo.verifyReplaceStatus(
                "Carriers keep their current leg and take the change after their next stop: Wagon #8.");
        cargo.replaceStop();
        cargo.verifyMessage(
                "The stop changed. Carriers take it at their next stop.");
        cargo.verifyStoredStop("Lost cart", LOST_STOP, "Haarlem");

        // The wagon has not moved. It says that the repair is taken next turn.
        cargo.verifyAssigned(
                "Wagon #8: waiting at the lost stop lost colony. The repaired stop Haarlem is taken next turn.");
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
        cargo.verifyWarehouse(HAARLEM_ID, Goods.of(GoodsType.MUSKET, 20));
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
        cargo.verifyTitle("Náklad na zastávkách trasy");
        cargo.verifyDialogFitsScreen();
        cargo.chooseRoute("Lost cart");
        cargo.verifyAssigned(
                "Povoz č. 8: čeká, další zastávka ztracená kolonie. Kolonie zastávky je ztracena nebo už není vaše.");

        // The player gives up on that route for this wagon with the keyboard.
        cargo.chooseAssignedCarrier(0);
        cargo.verifyCancelAssignmentButtonText("Přestat jet podle trasy");
        cargo.verifyCancelAssignmentEnabled(true);
        cargo.cancelAssignmentWithKeyboard();
        cargo.verifyMessage(
                "Povoz č. 8 je zpět na ručních příkazech. Nic dalšího se nezměnilo.");
        cargo.verifyNotAssigned(LOST_WAGON_ID);
        cargo.verifyCancelAssignmentEnabled(false);

        // The Ore cart route gets Haarlem for its New Amsterdam stop, also
        // with the keyboard.
        cargo.chooseRoute("Ore cart");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseReplacement("Haarlem");
        cargo.verifyReplaceButtonText("Nahradit zastávku");
        cargo.verifyReplaceStatus(
                "Jednotky dokončí současný úsek a změnu převezmou po další zastávce: žádné.");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-edit-czech.png");
        cargo.replaceStopWithKeyboard();
        cargo.verifyMessage(
                "Zastávka se změnila. Jednotky ji převezmou na další zastávce.");
        cargo.verifyStoredStop("Ore cart", NEW_AMSTERDAM_STOP, "Haarlem");

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
}
