package org.akropia.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.ConstructionType;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Industrial upgrades and scarce input conserve actual warehouse stocks. */
@ExtendWith(ApplicationExtension.class)
public class TC_50_F41_industrial_production_test extends AbstractAkropiaTest {
    private static final Location COLONY = Location.of(24, 16);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F41-industrial-production.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_lumber_mill_doubles_same_worker_and_continues()
            throws Exception {
        // One ordinary carpenter consumes three lumber before the upgrade.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        IndustrialProductionPage industry = new IndustrialProductionPage(
                getContext());
        industry.verifyForecast(GoodsType.LUMBER, 100, -3);
        colony.verifyThatProductionIs(GoodsType.HAMMERS, 3);
        ConstructionPage construction = new ConstructionPage(getContext());
        construction.open();
        construction.select("LUMBER_MILL");
        construction.verifyOffer(true, "Available.", 52, 0);
        construction.confirm();
        construction.cancel();
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyBuilding(ConstructionType.LUMBER_MILL, 1);
        construction.verifyStocksAndPopulation(0, 0, 3);
        industry.verifyForecast(GoodsType.LUMBER, 97, -6);
        colony.verifyThatProductionIs(GoodsType.HAMMERS, 6);
        colony.verifyGoodsForecast(GoodsType.HAMMERS, 0, 6, "turn");
        // The same worker now consumes and produces six, including after
        // Continue.
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        construction.verifyStocksAndPopulation(6, 0, 3);
        colony.verifyGoodsForecast(GoodsType.HAMMERS, 6, 6, "turn");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.LUMBER, 91));
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        construction.verifyStocksAndPopulation(6, 0, 3);
        colony.verifyGoodsForecast(GoodsType.HAMMERS, 6, 6, "turn");
        industry.verifyForecast(GoodsType.LUMBER, 91, -6);
    }

    @Test
    @Tag("local")
    void TC_02_factory_rounds_shortage_once_for_all_workers() throws Exception {
        // Two servants share eleven grapes: sixteen wine, not fifteen.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        IndustrialProductionPage industry = new IndustrialProductionPage(
                getContext());
        industry.verifyForecast(GoodsType.GRAPES, 11, -11);
        industry.verifyForecast(GoodsType.WINE, 0, 16);
        game = colony.close();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        industry.verifyForecast(GoodsType.WINE, 0, 16);
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.GRAPES, 0));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.WINE, 16));
        industry.verifyForecast(GoodsType.WINE, 16, 0);
        game = colony.close();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openColonyAt(COLONY,
                "New Amsterdam");
        industry.verifyForecast(GoodsType.WINE, 16, 0);
    }

    @Test
    @Tag("local")
    void TC_03_ironworks_feed_arsenal_once_in_same_turn() throws Exception {
        // Assign real workers: five ore yields seven tools, three feed six
        // weapons.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ColonyScreen colony = game.openColonyAt(COLONY, "New Amsterdam");
        IndustrialProductionPage industry = new IndustrialProductionPage(
                getContext());
        industry.assignWorker(4, "id_blacksmith");
        industry.assignWorker(201, "id_armory");
        industry.verifyForecast(GoodsType.ORE, 5, -5);
        industry.verifyForecast(GoodsType.TOOLS, 0, 4);
        industry.verifyForecast(GoodsType.WEAPONS, 0, 6);
        industry.captureDialog("id_armory", "r79-industrial-production.png");
        game = colony.close();
        game.nextTurnAndCloseDialogs();
        colony = game.openColonyAt(COLONY, "New Amsterdam");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.ORE, 0));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 4));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.WEAPONS, 6));
        game = colony.close();
        game.exitToMenu();
        colony = new FreePlayAuditPage(getContext()).continueGame()
                .openColonyAt(COLONY, "New Amsterdam");
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOOLS, 4));
        colony.verifyWarehouseGoods(Goods.of(GoodsType.WEAPONS, 6));
    }
}
