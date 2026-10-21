package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.TradeRouteKind;
import org.microcol.model.TradeRouteStop;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.TradeRouteCargoDialog;
import org.microcol.page.TradeRouteDialog;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Assigning a wagon to a trade route. New Amsterdam (id 0, ORE 90, MUSKET 50)
 * stands at (26,16) and Delft (id 1, ORE 0, MUSKET 50) at (28,16), two squares
 * apart over grass. A wagon moves one square a turn, so each leg takes two
 * turns. Wagon 4 stands in New Amsterdam without orders. Wagon 6 stands there
 * too and was saved following the "Idle cart" route with a waiting reason, so
 * the reason can be read before any turn passes. Each test starts from the same
 * saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_118_F109_trade_route_assignment_test
        extends AbstractMicroColTest {

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

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F109-trade-route-assignment.microcol"));
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
        // and puts the musket on, New Amsterdam does the opposite.
        cargo.chooseRoute("Ore Cart");
        cargo.verifyAssigned("No ship or wagon follows this route.");
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.MUSKET);
        cargo.save();
        cargo.verifyStoredCargo("Ore Cart", DELFT_STOP, List.of(GoodsType.ORE),
                List.of(GoodsType.MUSKET));
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.addUnload(GoodsType.MUSKET);
        cargo.addLoad(GoodsType.ORE);
        cargo.save();
        cargo.verifyStoredCargo("Ore Cart", NEW_AMSTERDAM_STOP,
                List.of(GoodsType.MUSKET), List.of(GoodsType.ORE));

        // Both wagons stand at this stop. The one that already has an order
        // is refused with its reason; the free one can be assigned.
        cargo.chooseCarrier(1);
        cargo.verifyAssignEnabled(false);
        cargo.verifyAssignStatus(
                "The unit already has an order, is damaged or is not free to move.");
        cargo.chooseCarrier(0);
        cargo.verifyAssignStatus("");

        // Choose the first stop: Delft is a legal choice, but the wagon should
        // load first, so New Amsterdam is chosen.
        cargo.chooseFirstStop(DELFT_STOP);
        cargo.verifyAssignEnabled(true);
        cargo.chooseFirstStop(NEW_AMSTERDAM_STOP);
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
        // its first step. The waiting wagon resumes and takes its first step.
        game.nextTurnAndCloseDialogs();
        cargo.verifyHold(WAGON_ID, 0, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", DELFT_STOP, false);
        cargo.verifyUnitBetween(IDLE_WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        cargo.verifyAssignment(IDLE_WAGON_ID, "Idle cart", 0, false);

        // Second turn: in Delft the ore comes off and the musket goes on.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON_ID, DELFT_LOCATION);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));
        cargo.verifyHold(WAGON_ID, 0, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyAssignment(WAGON_ID, "Ore Cart", NEW_AMSTERDAM_STOP, false);

        // Third and fourth turn: back in New Amsterdam the musket comes off
        // and the empty ore stock loads nothing. The first circuit is done.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitBetween(WAGON_ID, NEW_AMSTERDAM_LOCATION,
                DELFT_LOCATION);
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(WAGON_ID, NEW_AMSTERDAM_LOCATION);
        cargo.verifyHoldEmpty(WAGON_ID, 0);
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID,
                Goods.of(GoodsType.MUSKET, 100));
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
                Goods.of(GoodsType.MUSKET, 100));
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
                Goods.of(GoodsType.MUSKET, 100));
        cargo.verifyWarehouse(NEW_AMSTERDAM_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 90));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 0));
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
        cargo.chooseFirstStop(DELFT_STOP);
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
        cargo.verifyTitle("Náklad na zastávkách trasy");
        cargo.chooseRoute("Idle cart");
        cargo.verifyAssignmentHeading("Jede podle této trasy");
        cargo.verifyAssigned(
                "Povoz č. 6: čeká, další zastávka Delft. Zastávku teď nelze dosáhnout, příští tah to zkusí znovu.");

        // The free wagon is assigned with the keyboard.
        cargo.chooseRoute("Povozová cesta");
        cargo.chooseStop(NEW_AMSTERDAM_STOP);
        cargo.chooseFirstStop(NEW_AMSTERDAM_STOP);
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
}
