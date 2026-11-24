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
import org.akropia.page.SettingPage;
import org.akropia.page.TradeRouteCargoDialog;
import org.akropia.page.TradeRouteDialog;
import org.akropia.page.TradeRoutePlanningPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Cargo of trade route stops, edited in the Goods tab of the route screen
 * (R-0421): every stop row has one cell per transportable goods that cycles
 * ignore, load and unload, processed in the fixed goods order. Dutch's galleon
 * 3 stands in the port of Delft with all six holds taken [ORE 60, OLIVE_OIL
 * 100, passenger, OLIVES 100, GRAPES 100, WOOL 100], so only the hold of the
 * ore can take new goods once the ore is unloaded. Delft's warehouse holds ORE
 * 0, HORSE 50 and WEAPONS 50. Each test starts from the same saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_117_F108_trade_route_cargo_test extends AbstractAkropiaTest {

    private static final Location DELFT_LOCATION = Location.of(28, 16);
    private static final int DELFT_ID = 1;
    private static final int GALLEON_ID = 3;
    private static final TradeRouteStop DELFT = TradeRouteStop.ofColony(1);
    private static final TradeRouteStop EUROPE = TradeRouteStop.europe();
    /** Index of the Delft stop in the Europe, Delft route. */
    private static final int DELFT_STOP = 1;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F108-trade-route-cargo.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_cargo_is_processed_in_fixed_goods_order_once_and_kept_by_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");

        // A sea route from Delft to Europe and back, made in the route editor.
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Oil Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();
        route.verifyStoredRoute("Oil Run", TradeRouteKind.SEA, "Home port",
                "Delft");

        // Choose the Delft stop; the galleon in port is the only carrier.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyWorkbenchLayout("trade-route-workbench-en.png");
        cargo.verifyOpened();
        cargo.chooseRoute("Oil Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.verifyUnloadOrder();
        cargo.verifyLoadOrder();
        cargo.verifyDirectEditingControls();

        // Set the goods: ore off first, then weapon and horse on. The
        // matrix keeps the fixed goods order, so the horse precedes the
        // weapon however they were clicked.
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.HORSE);
        cargo.verifyUnloadOrder(GoodsType.ORE);
        cargo.verifyLoadOrder(GoodsType.HORSE, GoodsType.WEAPONS);
        cargo.verifyDirectEditingControls();
        cargo.openGoodsTab();
        cargo.captureDialog("tradeRouteCargoDialog", "trade-route-cargo.png");
        cargo.verifyStoredCargo("Oil Run", DELFT_STOP, List.of(GoodsType.ORE),
                List.of(GoodsType.HORSE, GoodsType.WEAPONS));
        cargo.verifyDirectEditingControls();

        // Editing and viewing orders moved nothing.
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 50));

        // Turn production breeds two horses before automatic service unloads
        // ore and loads all 52 horses in goods order.
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyDirectEditingControls();
        cargo.close();
        colony.close();
        game.nextTurnAndCloseDialogs();
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 52));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.OLIVE_OIL, 100));
        cargo.verifyHold(GALLEON_ID, 3, Goods.of(GoodsType.OLIVES, 100));
        cargo.verifyHold(GALLEON_ID, 4, Goods.of(GoodsType.GRAPES, 100));
        cargo.verifyHold(GALLEON_ID, 5, Goods.of(GoodsType.WOOL, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 50));

        // Reopening the route does not perform another transfer.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Oil Run");
        cargo.close();
        cargo.verifyDialogClosed();
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 60));
        game = colony.close();

        // Save and Continue keep the cargo, the balances and the finished
        // visit.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Oil Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.verifyUnloadOrder(GoodsType.ORE);
        cargo.verifyLoadOrder(GoodsType.HORSE, GoodsType.WEAPONS);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 52));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 50));
        cargo.close();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_02_every_goods_can_be_set_and_a_cell_cycles_ignore_load_unload()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Coast Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Coast Run");
        cargo.chooseStop(DELFT_STOP);

        // Sixteen goods columns in the colony order, named only for
        // assistive technology; hammers, torches and owls are not cargo.
        cargo.verifyGoodsColumns(TradeRouteStop.TRANSPORTABLE_GOODS,
                List.of("Food", "Olives", "Grapes", "Wool", "Clay", "Lumber",
                        "Ore", "Silver", "Horse", "Olive oil", "Wine", "Cloth",
                        "Amphorae", "Goods", "Tools", "Weapons"));
        cargo.verifyGoodsRows("1. Home port", "2. Delft");

        // One cell cycles empty, load, unload and empty again.
        cargo.verifyCell(DELFT_STOP, GoodsType.ORE, "", "Delft, Ore: ignore");
        cargo.clickCell(DELFT_STOP, GoodsType.ORE);
        cargo.verifyCell(DELFT_STOP, GoodsType.ORE, "+", "Delft, Ore: load");
        cargo.clickCell(DELFT_STOP, GoodsType.ORE);
        cargo.verifyCell(DELFT_STOP, GoodsType.ORE, "\u2212",
                "Delft, Ore: unload");
        cargo.clickCell(DELFT_STOP, GoodsType.ORE);
        cargo.verifyCell(DELFT_STOP, GoodsType.ORE, "", "Delft, Ore: ignore");
        cargo.verifyDirectEditingControls();

        // More than six goods in one direction: seven unloads and nine loads
        // fill the whole row, and no goods is loaded and unloaded at once.
        final List<GoodsType> seven = TradeRouteStop.TRANSPORTABLE_GOODS
                .subList(0, 7);
        final List<GoodsType> nine = TradeRouteStop.TRANSPORTABLE_GOODS
                .subList(7, 16);
        seven.forEach(cargo::addUnload);
        nine.forEach(cargo::addLoad);
        cargo.verifyUnloadOrder(seven.toArray(new GoodsType[0]));
        cargo.verifyLoadOrder(nine.toArray(new GoodsType[0]));
        // At Home port the same signs mean buying and selling.
        cargo.clickCell(0, GoodsType.WEAPONS);
        cargo.verifyCell(0, GoodsType.WEAPONS, "+", "Home port, Weapons: buy");
        cargo.verifyStoredCargo("Coast Run", DELFT_STOP, seven, nine);
        cargo.verifyStoredCargo("Coast Run", 0, List.of(),
                List.of(GoodsType.WEAPONS));

        // Leaving keeps the final edit without another confirmation.
        cargo.chooseStop(DELFT_STOP);
        cargo.ignoreGoods(GoodsType.WEAPONS);
        cargo.close();
        cargo.verifyDialogClosed();
        cargo.verifyStoredCargo("Coast Run", DELFT_STOP, seven,
                nine.subList(0, nine.size() - 1));
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_03_czech_tabs_follow_goods_order_and_report_each_entry()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Olejová cesta");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyWorkbenchLayout("trade-route-workbench-cs.png");
        cargo.verifyTitle("Správa obchodní trasy");
        cargo.verifyTabs("Stanice", "Přeprava", "Zboží");
        cargo.chooseRoute("Olejová cesta");
        cargo.chooseStop(DELFT_STOP);

        // The horse precedes the weapon in the goods order, so it gets the
        // only free hold although the weapon was marked first.
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.WEAPONS);
        cargo.addLoad(GoodsType.HORSE);
        cargo.verifyCell(DELFT_STOP, GoodsType.ORE, "\u2212",
                "Delft, Železná ruda: vykládat");
        cargo.verifyModeChoice(false,
                "Projet a pokračovat: provést dostupné převody a jet dál",
                "Čekat na úplné vyložení a naložení");
        // The status bar explains a focused cell; three presses of Space
        // return the grain cell to ignoring.
        cargo.cycleCellWithKeyboard(DELFT_STOP, GoodsType.GRAIN);
        cargo.verifyStatusBar("Delft, Jídlo: nakládat. "
                + "Změníte kliknutím nebo mezerníkem: ignorovat, nakládat, vykládat.");
        cargo.cycleCellWithKeyboard(DELFT_STOP, GoodsType.GRAIN);
        cargo.cycleCellWithKeyboard(DELFT_STOP, GoodsType.GRAIN);
        cargo.verifyCell(DELFT_STOP, GoodsType.GRAIN, "",
                "Delft, Jídlo: ignorovat");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-cargo-czech.png");
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyDirectEditingControls();
        cargo.close();
        colony.close();
        game.nextTurnAndCloseDialogs();
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 52));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.WEAPONS, 50));

    }

    @Test
    @Tag("local")
    void TC_04_planner_panel_edits_cargo_and_carrier_of_a_europe_route()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final TradeRoutePlanningPage planner = new TradeRoutePlanningPage(
                getContext());
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());

        // A sea route from Delft to Europe and back, created from the bar.
        planner.open();
        planner.newRoute();
        route.chooseStartColony("Delft");
        route.enterName("Oil Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();
        route.verifyStoredRoute("Oil Run", TradeRouteKind.SEA, "Home port",
                "Delft");

        // Without a carrier only a ship can reach Europe, so each leg shows
        // its sea path; Europe is drawn out on the high sea.
        planner.chooseRoute("Oil Run");
        planner.verifySelectedRoute("Oil Run");
        planner.verifyKind("Sea route");
        planner.verifyCarriers("No carrier assigned");
        planner.verifyLegsContain("Delft → Home port: by sea");
        planner.verifyLegsContain("Home port → Delft: by sea");
        planner.verifyRouteDrawnOver("Oil Run", DELFT_LOCATION);
        planner.captureScreen("trade-route-planner-europe.png");

        // A map double-click opens the shared route window. Closing it
        // after editing goods keeps the applied stop instructions.
        planner.editSelected();
        cargo.verifyOpened();
        cargo.verifyChosenRoute("Oil Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.close();
        cargo.verifyDialogClosed();
        planner.verifyStops("1. Home port — unload: nothing; load: nothing\n"
                + "2. Delft — unload: Ore; load: nothing");

        // Saved goods and the assigned galleon appear in the panel.
        planner.editSelected();
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.verifyStoredCargo("Oil Run", DELFT_STOP, List.of(GoodsType.ORE),
                List.of());
        cargo.openCarriersTab();
        cargo.verifyNoCarrierRow("No ship or wagon follows this route.");
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyMessage(
                "Galleon #3 now follows this route, starting at Delft.");
        cargo.verifyCarrierRow(GALLEON_ID, "Galleon #3", "\u2192 Delft",
                "On the way.");
        cargo.close();
        planner.verifyStops("1. Home port — unload: nothing; load: nothing\n"
                + "2. Delft — unload: Ore; load: nothing");
        planner.verifyCarriers("Carriers: Galleon #3");
        planner.verifyPreviewNote(
                "Paths are shown for Galleon #3. Other carriers of the route use their own movement costs.");
        planner.verifyLegsContain("Delft → Home port: by sea");
        planner.captureScreen("trade-route-planner-detail.png");

        // Viewing, editing and assigning moved and transferred nothing.
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        planner.close();

        // Save and Continue keep the route and its carrier; nothing is
        // processed again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        planner.open();
        planner.chooseRoute("Oil Run");
        planner.verifyCarriers("Carriers: Galleon #3");
        planner.verifyStops("1. Home port — unload: nothing; load: nothing\n"
                + "2. Delft — unload: Ore; load: nothing");
        planner.close();
        planner.verifyPlannerShown(false);
    }

    @Test
    @Tag("local")
    void TC_05_wait_mode_holds_the_galleon_until_touch_and_go_releases_it()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Horse Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        // Delft unloads the ore and loads horses; the route waits at stops.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Horse Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.HORSE);
        cargo.verifyModeChoice(false,
                "Touch and go: make the available transfers and continue",
                "Wait until the designated unloading and loading are complete");
        cargo.chooseWaitForCompletion();
        cargo.verifyModeChoice(true,
                "Touch and go: make the available transfers and continue",
                "Wait until the designated unloading and loading are complete");
        cargo.verifyStoredMode("Horse Run", TradeRouteMode.WAIT_FOR_COMPLETION);

        // The galleon joins the route at Delft; joining moves nothing.
        cargo.addCarrier(GALLEON_ID);
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.close();
        cargo.verifyDialogClosed();
        game = colony.close();

        // During the turn change Delft's fifty horses breed to fifty-two
        // before the galleon's preparation. It then unloads the ore, loads
        // all fifty-two horses and waits for the rest of the free hold.
        game.nextTurnAndCloseDialogs();
        cargo.verifyUnitAt(GALLEON_ID, DELFT_LOCATION);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 52));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 0));
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_LOAD);
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Horse Run");
        cargo.openCarriersTab();
        cargo.verifyCarrierRow(GALLEON_ID, "Galleon #3", "\u2192 Delft",
                "Waiting: free holds remain and the stop has no more goods to load.");
        // Ignored goods and the passenger keep their real holds.
        cargo.verifyCarrierSlot(GALLEON_ID, 0, "52", "Horse 52");
        cargo.verifyCarrierSlot(GALLEON_ID, 1, "100", "Olive oil 100");
        cargo.verifyCarrierSlot(GALLEON_ID, 2, "", "Passenger");
        cargo.verifyCarrierTable(GALLEON_ID, true);

        // All three tabs at the default size: station actions, all six holds
        // with Remove, and the sixteen goods columns are inside the window.
        cargo.openStationsTab();
        cargo.verifyStationRowFits(1);
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-stations-default.png");
        cargo.openCarriersTab();
        cargo.verifyCarrierRowFits(GALLEON_ID, 6);
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-carriers-default.png");
        cargo.openGoodsTab();
        cargo.verifyGoodsMatrixAccessible();
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-goods-default.png");

        // The same in the smallest supported window.
        cargo.resizeWindow(1024, 768);
        cargo.openStationsTab();
        cargo.verifyStationRowFits(1);
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-stations-1024.png");
        cargo.openCarriersTab();
        cargo.verifyCarrierRowFits(GALLEON_ID, 6);
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-carriers-1024.png");
        cargo.openGoodsTab();
        cargo.verifyGoodsMatrixAccessible();
        cargo.captureDialog("tradeRouteCargoPage",
                "trade-route-goods-1024.png");
        cargo.close();
        game = colony.close();

        // Save and Continue keep the waiting galleon; nothing is repeated.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_LOAD);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 52));

        // Switching the route to touch and go releases it on the next turn.
        colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        cargo.open();
        cargo.chooseRoute("Horse Run");
        cargo.openGoodsTab();
        cargo.chooseTouchAndGo();
        cargo.verifyStoredMode("Horse Run", TradeRouteMode.TOUCH_AND_GO);
        cargo.verifyPauseReason(GALLEON_ID, TradeRoutePause.WAITING_LOAD);
        cargo.close();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        cargo.verifyNotPaused(GALLEON_ID);
        cargo.verifyAssignment(GALLEON_ID, "Horse Run", 0, false);
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
    }

    @Test
    @Tag("local")
    void TC_06_tabs_keep_the_draft_and_work_with_the_keyboard()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Key Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.chooseRoute("Key Run");
        cargo.verifyTabs("Stations", "Carriers", "Goods");
        cargo.verifySelectedTab("Stations");
        cargo.verifyRouteSummary("Sea (ship) route · 2 stops");
        cargo.verifyStopChoices("1. Home port", "2. Delft");
        cargo.verifyStationButtons(0, false, true, true);
        cargo.verifyStationButtons(1, true, false, true);

        // A staged order change survives switching tabs, also by keyboard.
        cargo.moveStationUpWithKeyboard(1);
        cargo.verifyStopChoices("1. Delft", "2. Home port");
        cargo.openNextTabWithKeyboard();
        cargo.verifySelectedTab("Carriers");
        cargo.openNextTabWithKeyboard();
        cargo.verifySelectedTab("Goods");
        cargo.verifyGoodsRows("1. Delft", "2. Home port");

        // Space cycles a focused cell like a click.
        cargo.cycleCellWithKeyboard(0, GoodsType.ORE);
        cargo.verifyCell(0, GoodsType.ORE, "+", "Delft, Ore: load");
        cargo.verifyCellFocused(0, GoodsType.ORE);
        cargo.cycleCellWithKeyboard(0, GoodsType.ORE);
        cargo.verifyCell(0, GoodsType.ORE, "\u2212", "Delft, Ore: unload");
        cargo.openStationsTab();
        cargo.verifyStopChoices("1. Delft", "2. Home port");
        cargo.verifyStationStatus("");
        cargo.openGoodsTab();
        cargo.verifyCell(0, GoodsType.ORE, "\u2212", "Delft, Ore: unload");

        // Escape leaves immediately and preserves both station order and cargo.
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        cargo.verifyStoredStops("Key Run", "Delft", "Home port");
        cargo.verifyStoredCargo("Key Run", 0, List.of(GoodsType.ORE),
                List.of());
        cargo.open();
        cargo.chooseRoute("Key Run");
        cargo.verifyStopChoices("1. Delft", "2. Home port");
        cargo.verifyRouteSummary("Sea (ship) route · 2 stops");
        cargo.closeWithEscape();
        cargo.verifyDialogClosed();
        colony.close();
    }
}
