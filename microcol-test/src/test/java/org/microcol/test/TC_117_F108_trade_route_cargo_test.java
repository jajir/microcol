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
 * Cargo of trade route stops. Dutch's galleon 3 stands in the port of Delft
 * with all six holds taken [ORE 60, RUM 100, passenger, SUGAR 100, TOBACCO 100,
 * COTTON 100], so only the hold of the ore can take new goods once the ore is
 * unloaded. Delft's warehouse holds ORE 0, HORSE 50 and MUSKET 50. Each test
 * starts from the same saved position.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_117_F108_trade_route_cargo_test extends AbstractMicroColTest {

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
                new File("src/test/scenarios/F108-trade-route-cargo.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cargo_is_reordered_by_priority_processed_once_and_kept_by_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");

        // A sea route from Delft to Europe and back, made in the route editor.
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Rum Run");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();
        route.verifyStoredRoute("Rum Run", TradeRouteKind.SEA, "Europe",
                "Delft");

        // Choose the Delft stop; the galleon in port is the only carrier.
        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyOpened();
        cargo.chooseRoute("Rum Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.verifyUnloadOrder();
        cargo.verifyLoadOrder();
        cargo.verifySaveEnabled(false);

        // Stage the goods: ore off first, then musket and horse on.
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.MUSKET);
        cargo.addLoad(GoodsType.HORSE);
        cargo.verifyUnloadOrder(GoodsType.ORE);
        cargo.verifyLoadOrder(GoodsType.MUSKET, GoodsType.HORSE);
        cargo.verifySaveEnabled(true);
        cargo.verifyProcessEnabled(false);
        cargo.verifyCarrierStatus(
                "Save the cargo changes before processing the stop.");

        // Only one hold is free after unloading, so priority matters: move the
        // horse above the musket.
        cargo.selectLoad(1);
        cargo.moveLoadUp();
        cargo.verifyLoadOrder(GoodsType.HORSE, GoodsType.MUSKET);
        cargo.captureDialog("tradeRouteCargoDialog", "trade-route-cargo.png");
        cargo.save();
        cargo.verifyMessage(
                "Cargo saved. It applies the next time a ship or wagon is processed at this stop.");
        cargo.verifyStoredCargo("Rum Run", DELFT_STOP, List.of(GoodsType.ORE),
                List.of(GoodsType.HORSE, GoodsType.MUSKET));
        cargo.verifySaveEnabled(false);
        cargo.verifyCarrierStatus("Ready to process.");
        cargo.verifyProcessEnabled(true);

        // Saving and viewing moved nothing.
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 50));
        cargo.verifyVisitProcessed(GALLEON_ID, false);

        // Process the stop: unload first, then load by priority.
        cargo.process();
        cargo.verifyResult("Unload Ore: 60 – moved all",
                "Load Horse: 50 – moved all",
                "Load Musket: 0 – nothing moved: no hold has room");
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 50));
        cargo.verifyHold(GALLEON_ID, 1, Goods.of(GoodsType.RUM, 100));
        cargo.verifyHold(GALLEON_ID, 3, Goods.of(GoodsType.SUGAR, 100));
        cargo.verifyHold(GALLEON_ID, 4, Goods.of(GoodsType.TOBACCO, 100));
        cargo.verifyHold(GALLEON_ID, 5, Goods.of(GoodsType.COTTON, 100));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyVisitProcessed(GALLEON_ID, true);

        // The same visit cannot be processed again.
        cargo.verifyProcessEnabled(false);
        cargo.verifyCarrierStatus(
                "Already processed during this visit. Leave the stop and come back to process it again.");
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
        cargo.chooseRoute("Rum Run");
        cargo.chooseStop(DELFT_STOP);
        cargo.verifyUnloadOrder(GoodsType.ORE);
        cargo.verifyLoadOrder(GoodsType.HORSE, GoodsType.MUSKET);
        cargo.verifyResult();
        cargo.verifyProcessEnabled(false);
        cargo.verifyCarrierStatus(
                "Already processed during this visit. Leave the stop and come back to process it again.");
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.HORSE, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.ORE, 60));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 0));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyVisitProcessed(GALLEON_ID, true);
        cargo.close();
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_02_each_direction_lists_at_most_six_distinct_goods()
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

        // Six different goods fill the unload list; no seventh can be added.
        cargo.addUnload(GoodsType.ORE);
        cargo.addUnload(GoodsType.RUM);
        cargo.addUnload(GoodsType.SUGAR);
        cargo.addUnload(GoodsType.TOBACCO);
        cargo.addUnload(GoodsType.CORN);
        cargo.addUnload(GoodsType.TOOLS);
        cargo.verifyUnloadOrder(GoodsType.ORE, GoodsType.RUM, GoodsType.SUGAR,
                GoodsType.TOBACCO, GoodsType.CORN, GoodsType.TOOLS);
        cargo.verifyUnloadAddEnabled(false);
        // A listed goods is not offered again, so a duplicate cannot be made.
        cargo.verifyUnloadChoiceOffers(GoodsType.ORE, false);
        cargo.verifyUnloadChoiceOffers(GoodsType.MUSKET, true);

        // The load list is separate, and a stored list survives closing.
        cargo.addLoad(GoodsType.ORE);
        cargo.verifyLoadOrder(GoodsType.ORE);
        cargo.verifyLoadAddEnabled(false);
        cargo.save();
        cargo.verifyStoredCargo("Coast Run", DELFT_STOP,
                List.of(GoodsType.ORE, GoodsType.RUM, GoodsType.SUGAR,
                        GoodsType.TOBACCO, GoodsType.CORN, GoodsType.TOOLS),
                List.of(GoodsType.ORE));

        // Removing the only load entry is staged until it is saved; closing
        // discards it and the stored list stays.
        cargo.selectLoad(0);
        cargo.removeLoad();
        cargo.verifyLoadOrder();
        cargo.verifySaveEnabled(true);
        cargo.close();
        cargo.verifyStoredCargo("Coast Run", DELFT_STOP,
                List.of(GoodsType.ORE, GoodsType.RUM, GoodsType.SUGAR,
                        GoodsType.TOBACCO, GoodsType.CORN, GoodsType.TOOLS),
                List.of(GoodsType.ORE));
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_03_czech_dialog_explains_priority_and_reports_each_entry()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");
        final TradeRouteDialog route = new TradeRouteDialog(getContext());
        route.open();
        route.enterName("Rumová cesta");
        route.chooseKind(TradeRouteKind.SEA);
        route.chooseStop(0, EUROPE);
        route.chooseStop(1, DELFT);
        route.create();

        final TradeRouteCargoDialog cargo = new TradeRouteCargoDialog(
                getContext());
        cargo.open();
        cargo.verifyTitle("Náklad na zastávkách trasy");
        cargo.chooseRoute("Rumová cesta");
        cargo.chooseStop(DELFT_STOP);

        // Without reordering, the musket listed first gets the only free hold.
        cargo.addUnload(GoodsType.ORE);
        cargo.addLoad(GoodsType.MUSKET);
        cargo.addLoad(GoodsType.HORSE);
        cargo.verifyCarrierStatus(
                "Před zpracováním zastávky uložte změny nákladu.");
        cargo.captureDialog("tradeRouteCargoDialog",
                "trade-route-cargo-czech.png");
        cargo.save();
        cargo.verifyMessage(
                "Náklad je uložen. Použije se při dalším zpracování lodi nebo vozu na této zastávce.");
        cargo.process();
        cargo.verifyResult("Vyložit Železná ruda: 60 – přesunuto vše",
                "Naložit Muškety: 50 – přesunuto vše",
                "Naložit Koně: 0 – nic nepřesunuto: žádné místo nemá volno");
        cargo.verifyHold(GALLEON_ID, 0, Goods.of(GoodsType.MUSKET, 50));
        cargo.verifyWarehouse(DELFT_ID, Goods.of(GoodsType.HORSE, 50));
        cargo.verifyCarrierStatus(
                "Při této návštěvě už bylo zpracováno. Zastávku opusťte a vraťte se, pak ji lze zpracovat znovu.");
        cargo.verifyProcessEnabled(false);
        cargo.close();
        colony.close();
    }
}
