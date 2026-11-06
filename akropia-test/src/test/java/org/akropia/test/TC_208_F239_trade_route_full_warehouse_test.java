package org.akropia.test;

import java.io.File;
import java.util.List;

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
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A full warehouse holds a waiting carrier (R-0421). Dutch's galleon 3 stands
 * in the port of Delft with [ORE 60, OLIVE_OIL 100, passenger, empty, GRAPES
 * 100, WOOL 100]. Delft's basic warehouse is full of ORE 100 and holds TOOLS
 * 100 and WEAPONS 100; Delft produces none of the three. The empty merchantman
 * 6 lies in the same port.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_208_F239_trade_route_full_warehouse_test
        extends AbstractAkropiaTest {

    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int DELFT_ID = 1;
    private static final int GALLEON_ID = 3;
    private static final int SPARE_SHIP_ID = 6;
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    private static final TradeRouteStop EUROPE = TradeRouteStop.europe();
    private static final int EUROPE_STOP = 0;
    private static final int DELFT_STOP = 1;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F239-trade-route-full-warehouse.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_full_warehouse_holds_the_galleon_until_space_is_freed()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Ore Hold");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        // Delft unloads the ore and loads tools and weapons; the route waits
        // at every stop until its goods are done.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Ore Hold");
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.TOOLS);
        cargo.chooseWaitForCompletion();
        cargo.save();
        cargo.verifyStoredCargo("Ore Hold", DELFT_STOP, List.of(GoodsType.ORE),
                List.of(GoodsType.TOOLS, GoodsType.WEAPONS));
        cargo.verifyStoredMode("Ore Hold", TradeRouteMode.WAIT_FOR_COMPLETION);

        // The galleon joins the route at Delft.
        cargo.openCarriersTab();
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyMessage(
                "Galleon #3 now follows this route, starting at Delft.");
        cargo.close();
        cargo.verifyDialogClosed();
        game = colony.close();

        // Next turn the warehouse cannot take the ore. The galleon keeps it,
        // does not load into its free hold while unloading is unfinished, and
        // waits at Delft.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyHoldEmpty(GALLEON_ID, 3);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.TOOLS, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_UNLOAD);
        cargo.verifyAssignment(GALLEON_ID, "Ore Hold", DELFT_STOP, true);
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Ore Hold");
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(GALLEON_ID, "Galleon #3", "→ Delft",
                "Waiting: goods to unload are still on board, the destination cannot take them now.");
        cargo.verifyCarrierSlot(GALLEON_ID, 0, "60", "Ore 60");
        cargo.verifyCarrierSlot(GALLEON_ID, 3, "", "Empty hold");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-full-warehouse-waiting.png");
        cargo.close();
        game = colony.close();

        // Save and Continue keep the waiting galleon; nothing is unloaded or
        // loaded again.
        cargo.rememberGoldAndCargo(GALLEON_ID);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyGoldAndCargoUnchanged(GALLEON_ID);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.TOOLS, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_UNLOAD);

        // The player frees the store: all the ore is dragged from the
        // warehouse into the spare merchantman.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        colony.selectShipInPort(SPARE_SHIP_ID);
        colony.dragGoodsFromWarehouseToShipCargoSlot(
                GoodsType.GOOD_TYPES.indexOf(GoodsType.ORE), 0);
        colony.verifyCarrierGoods(SPARE_SHIP_ID, 0,
                Goods.of(GoodsType.ORE, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        game = colony.close();

        // Next turn the galleon unloads its ore, fills both free holds with
        // tools and then weapons in the goods order, and leaves for the home
        // port.
        game.nextTurnAndCloseDialogs();
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.TOOLS, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 0));
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.TOOLS, 100));
        cargo.verifyHold(GALLEON_ID, 3, Goods.of(GoodsType.WEAPONS, 100));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyNotPaused(GALLEON_ID);
        cargo.verifyAssignment(GALLEON_ID, "Ore Hold", EUROPE_STOP, false);
        cargo.verifyUnitLeft(GALLEON_ID, DELFT_LOCATION);
    }
}
