package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.TradeRouteKind;
import org.akropia.model.TradeRouteMode;
import org.akropia.model.TradeRoutePause;
import org.akropia.model.TradeRouteStop;
import org.akropia.page.ColonyScreen;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Home port row of a sea route in waiting mode (R-0421). Dutch's galleon 3 is
 * docked at the home port with [ORE 60, OLIVE_OIL 100, passenger, OLIVES 100,
 * GRAPES 100, WOOL 100]; the treasury is empty. Selling the ore pays for only
 * part of a hold of weapons.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_207_F238_trade_route_home_port_test
        extends AbstractAkropiaTest {

    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int GALLEON_ID = 3;
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    private static final TradeRouteStop EUROPE = TradeRouteStop.europe();
    private static final int EUROPE_STOP = 0;
    private static final int DELFT_STOP = 1;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F238-trade-route-home-port.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_home_port_sells_first_waits_for_funds_and_recovers()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Arms Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        // At the home port the ore is sold and weapons are bought; the
        // status bar names the goods and state of the focused cell.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Arms Run");
        cargo.chooseStop(EUROPE_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.verifyCell(EUROPE_STOP, GoodsType.ORE, "−",
                "Home port, Ore: sell");
        cargo.cycleCellWithKeyboard(EUROPE_STOP, GoodsType.WEAPONS);
        cargo.verifyCell(EUROPE_STOP, GoodsType.WEAPONS, "+",
                "Home port, Weapons: buy");
        cargo.verifyStatusBar("Home port, Weapons: buy. "
                + "Click or press Space to change: empty ignores, + loads, − unloads.");
        cargo.chooseWaitForCompletion();
        cargo.save();
        cargo.verifyStoredMode("Arms Run", TradeRouteMode.WAIT_FOR_COMPLETION);

        // The docked galleon joins the route at the home port.
        cargo.openCarriersTab();
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyMessage(
                "Galleon #3 now follows this route, starting at Home port.");
        cargo.verifyGold(0);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.close();
        cargo.verifyDialogClosed();
        game = colony.close();

        // Next turn the ore is sold before buying; the money buys only part
        // of the freed hold, and the galleon waits for funds.
        game.nextTurnAndCloseDialogs();
        cargo.verifyHoldPartlyFilled(GALLEON_ID, 0, GoodsType.WEAPONS);
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyGoldBelowPriceOf(GoodsType.WEAPONS);
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_FUNDS);
        cargo.rememberGoldAndCargo(GALLEON_ID);
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Arms Run");
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(GALLEON_ID, "Galleon #3", "→ Home port",
                "Waiting: free holds remain and the treasury cannot pay for more goods.");
        cargo.verifyCarrierSlot(GALLEON_ID, 1, "100", "Olive oil 100");
        cargo.verifyCarrierRowFits(GALLEON_ID, 6);
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-home-port-waiting.png");
        cargo.close();
        game = colony.close();

        // Save and Continue keep the waiting galleon; nothing is sold or
        // bought again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyGoldAndCargoUnchanged(GALLEON_ID);
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_FUNDS);

        // The player sells the olive oil by hand at the home port.
        final EuropePortScreen europe = game.openEuroperPort();
        europe.selectUnitFromPort(0);
        europe.sellGoods(1,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.OLIVE_OIL));
        europe.verifyCargoSlotEmpty(0, 1);
        game = europe.close();
        cargo.verifyGoldIncreased();

        // The next turn buys the rest; the full galleon leaves for Delft.
        game.nextTurnAndCloseDialogs();
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyNotPaused(GALLEON_ID);
        cargo.verifyAssignment(GALLEON_ID, "Arms Run", DELFT_STOP, false);
    }
}
