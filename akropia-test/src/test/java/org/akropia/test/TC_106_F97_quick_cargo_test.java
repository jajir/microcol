package org.akropia.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.gui.FileSelectingService;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.Location;
import org.akropia.model.unit.UnitWithCargo;
import org.akropia.page.ColonyScreen;
import org.akropia.page.ColonyJobPage;
import org.akropia.page.GameBarWidthPage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.SettingPage;
import org.akropia.page.GamePage;
import org.akropia.page.QuickCargoPage;
import org.akropia.page.ShipWrapper;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Removed quick cargo controls and retained manual cargo transfers.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_106_F97_quick_cargo_test extends AbstractAkropiaTest {

    private static final Location COLONY_LOCATION = Location.of(24, 16);
    private static final String COLONY_NAME = "New Amsterdam";
    private static final int ORE_WAREHOUSE_INDEX = 6;
    private static final int TREASURY = 10000;
    private static final File testFileName = new File(
            "src/test/scenarios/F97-quick-cargo.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_manual_cargo_transfers_keep_saved_balances() throws Exception {
        final File savedFile = getContext()
                .getClassFromGuice(
                        org.akropia.model.campaign.po.SavedGames.class)
                .target("quick-cargo.akropia");
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        when(files.loadFile(any(File.class))).thenReturn(testFileName,
                savedFile);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);

        // Removed quick buttons stay absent with a selected carrier.
        new org.akropia.page.ColonyLayoutPage(getContext())
                .verifyRemovedCargoActions();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 90);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));

        // Drag stock to an empty hold, then unload up to warehouse capacity.
        colony.dragGoodsFromWarehouseToShipCargoSlot(ORE_WAREHOUSE_INDEX, 0);
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 0);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.ORE, 90));
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, ORE_WAREHOUSE_INDEX);
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 90);
        ship().verifyCargoSlotEmpty(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(2, ORE_WAREHOUSE_INDEX);
        final org.akropia.page.DialogChooseGoodsAmount amount = org.akropia.page.DialogChooseGoodsAmount
                .of(getContext());
        amount.verifyOfferedAmount(10);
        amount.clickOnOk();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 100);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 30));
        ship().verifyNumberOfOccupiedCargoSlots(2);
        QuickCargoPage.of(getContext()).verifyTreasury(TREASURY);

        // Save and reload preserves transferred goods and the passenger.
        final GamePage game = colony.close();
        game.saveGame(savedFile);
        game.verifySavedGame(savedFile);
        final ColonyScreen reloaded = game.exitToMenu().loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        reloaded.selectUnitFromPort(0);
        reloaded.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 100);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 30));
        ship().verifyNumberOfOccupiedCargoSlots(2);
        QuickCargoPage.of(getContext()).verifyTreasury(TREASURY);
    }

    @Test
    @Tag("local")
    void TC_02_removed_actions_stay_absent_without_changing_goods()
            throws Exception {
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        final ColonyJobPage state = new ColonyJobPage(getContext());
        state.rememberWorld();
        final org.akropia.page.ColonyLayoutPage layout = new org.akropia.page.ColonyLayoutPage(
                getContext());
        layout.verifyRemovedCargoActions();
        colony.selectUnitFromPort(0);
        layout.verifyRemovedCargoActions();
        state.verifyWorldUnchanged();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 90);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));
        QuickCargoPage.of(getContext()).verifyTreasury(TREASURY);
    }

    private ShipWrapper ship() {
        return new ShipWrapper((UnitWithCargo) getModel().getUnitById(3));
    }

    @Test
    @Tag("local")
    void TC_03_colony_production_is_visible_but_not_cargo() throws Exception {
        // Read stock and next-turn change on one row at both window widths.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        final GameBarWidthPage width = new GameBarWidthPage(getContext());
        width.resizeContent(1440, 1000);
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.showWarehouse();
        colony.verifyWarehouseGoodsLayout(19, 45);
        colony.verifyGoodsForecast(GoodsType.HAMMERS, 46, 0);
        colony.verifyGoodsForecast(GoodsType.LUMBER, 0, 0);
        colony.verifyGoodsForecast(GoodsType.GRAIN, 0, 4);
        colony.verifyLocalProduction(GoodsType.HAMMERS, "Hammers", "46", "");
        colony.verifyLocalProduction(GoodsType.TORCHES, "Torches", "", "+1");
        colony.verifyLocalProduction(GoodsType.OWLS, "Owls", "", "+1");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0400-production-en.png");

        // None of the three production icons can start a transfer into a hold.
        final ColonyJobPage state = new ColonyJobPage(getContext());
        state.rememberWorld();
        colony.dragGoodsFromWarehouseToShipCargoSlot(
                GoodsType.GOOD_TYPES.indexOf(GoodsType.HAMMERS), 0);
        colony.dragGoodsFromWarehouseToShipCargoSlot(
                GoodsType.GOOD_TYPES.indexOf(GoodsType.TORCHES), 0);
        colony.dragGoodsFromWarehouseToShipCargoSlot(
                GoodsType.GOOD_TYPES.indexOf(GoodsType.OWLS), 0);
        state.verifyWorldUnchanged();
        ship().verifyCargoSlotEmpty(0);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));

        // Ordinary warehouse goods still move into the selected ship.
        colony.dragGoodsFromWarehouseToShipCargoSlot(ORE_WAREHOUSE_INDEX, 0);
        colony.verifyWarehouseStock(GoodsType.ORE, 0);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.ORE, 90));
        width.resizeContent(1000, 925);
        colony.showWarehouse();
        colony.verifyWarehouseGoodsLayout(19, 45);
        colony.verifyGoodsForecast(GoodsType.HAMMERS, 46, 0);
        colony.verifyGoodsForecast(GoodsType.LUMBER, 0, 0);
        colony.verifyGoodsForecast(GoodsType.GRAIN, 0, 4);
        colony.verifyLocalProduction(GoodsType.HAMMERS, "Hammers", "46", "");
        colony.verifyLocalProduction(GoodsType.TORCHES, "Torches", "", "+1");
        colony.verifyLocalProduction(GoodsType.OWLS, "Owls", "", "+1");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0400-production-small.png");

        // The same compact row remains readable with Czech labels.
        final SettingPage settings = colony.close().exitToMenu().openSetting();
        settings.setCzechLanguage();
        final ColonyScreen czech = settings.goBack().loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        width.resizeContent(1000, 925);
        czech.showWarehouse();
        czech.verifyWarehouseGoodsLayout(19, 45);
        czech.verifyGoodsForecast(GoodsType.HAMMERS, 46, 0);
        czech.verifyGoodsForecast(GoodsType.LUMBER, 0, 0);
        czech.verifyGoodsForecast(GoodsType.GRAIN, 0, 4);
        czech.verifyLocalProduction(GoodsType.HAMMERS, "Kladiva", "46", "");
        czech.verifyLocalProduction(GoodsType.TORCHES, "Pochodně", "", "+1");
        czech.verifyLocalProduction(GoodsType.OWLS, "Sovy", "", "+1");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0400-production-cs.png");
        czech.close();
    }
}
