package org.microcol.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.QuickCargoPage;
import org.microcol.page.ShipWrapper;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Verify quick load and unload of the selected ship: the ranked goods, partly
 * filled holds first, passengers kept aboard, refusals without change, and the
 * saved balances after reloading.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_106_F97_quick_cargo_test extends AbstractMicroColTest {

    private static final Location COLONY_LOCATION = Location.of(24, 16);
    private static final String COLONY_NAME = "New Amsterdam";
    private static final int ORE_WAREHOUSE_INDEX = 6;
    private static final int TREASURY = 10000;
    private static final File testFileName = new File(
            "src/test/scenarios/F97-quick-cargo.microcol");
    private static final String LOAD_60_ORE = "Load 60 Ore into hold 3 of the selected carrier? The treasury does not change.";
    private static final String LOAD_30_ORE = "Load 30 Ore into hold 1 of the selected carrier? The treasury does not change.";
    private static final String UNLOAD_30_ORE = "Unload 30 Ore from hold 1 into the warehouse? Any amount that does not fit stays aboard.";
    private static final String UNLOAD_70_ORE = "Unload 70 Ore from hold 3 into the warehouse? Any amount that does not fit stays aboard.";
    private static final String NO_STOCK = "No warehouse goods can be quick-loaded now. Nothing was changed.";
    private static final String FULL = "The warehouse is full for this cargo. Nothing was unloaded. Free storage and try again.";
    private static final String CHANGED = "The cargo, warehouse or price changed. Nothing was transferred. Check the holds and try again.";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_quick_load_and_unload_with_refusals_and_saved_balances()
            throws Exception {
        // Choose an isolated save file and load the defined game.
        final File savedFile = new File(System.getProperty("user.home"),
                "quick-cargo.microcol");
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        when(files.saveFile(any(File.class), anyString()))
                .thenReturn(savedFile);
        when(files.loadFile(any(File.class))).thenReturn(testFileName,
                savedFile);
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        final QuickCargoPage quick = QuickCargoPage.of(getContext());

        // The model ranks ore first and fills the partly loaded hold 3.
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 90);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));
        quick.quickLoad();
        quick.verifyPreview(LOAD_60_ORE);

        // Cancel leaves the warehouse, the hold and the treasury unchanged.
        quick.cancel();
        quick.verifyNoPreview();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 90);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));
        quick.verifyTreasury(TREASURY);

        // Confirm the same load; the other 30 ore stay in the warehouse.
        quick.quickLoad();
        quick.verifyPreview(LOAD_60_ORE);
        quick.confirm();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 30);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 100));
        quick.verifyTreasury(TREASURY);

        // The lowest empty hold, number 1, receives the remaining ore.
        quick.quickLoad();
        quick.verifyPreview(LOAD_30_ORE);
        quick.confirm();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 0);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.ORE, 30));

        // With no stock left, the same command explains the refusal only.
        quick.quickLoad();
        quick.verifyRefusal(NO_STOCK);
        quick.closeRefusal();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 0);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.ORE, 30));

        // Unloading starts with the first goods hold; the passenger stays.
        quick.quickUnload();
        quick.verifyPreview(UNLOAD_30_ORE);
        quick.confirm();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 30);
        ship().verifyCargoSlotEmpty(0);

        // The next unload fills the warehouse exactly and keeps the rest.
        quick.quickUnload();
        quick.verifyPreview(UNLOAD_70_ORE);
        quick.confirm();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 100);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 30));

        // A full warehouse refuses without moving or discarding anything.
        quick.quickUnload();
        quick.verifyRefusal(FULL);
        quick.closeRefusal();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 100);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 30));
        ship().verifyNumberOfOccupiedCargoSlots(2);
        quick.verifyTreasury(TREASURY);

        // Save, return to the menu and load the saved balances once.
        final GamePage afterTransfers = colony.close();
        afterTransfers.saveGame();
        afterTransfers.verifySavedGame(savedFile);
        final ColonyScreen reloaded = afterTransfers.exitToMenu().loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        reloaded.selectUnitFromPort(0);
        reloaded.verifyUnitInPortSelected(0, true);
        reloaded.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 100);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 30));
        ship().verifyNumberOfOccupiedCargoSlots(2);
    }

    @Test
    @Tag("local")
    void TC_02_changed_warehouse_during_confirmation_is_rejected()
            throws Exception {
        // Leave the 60-ore load proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, COLONY_NAME);
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        final QuickCargoPage quick = QuickCargoPage.of(getContext());
        quick.quickLoad();
        quick.verifyPreview(LOAD_60_ORE);

        // Another action adds one ore to the warehouse while the proposal is
        // open.
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getColonyByName(COLONY_NAME).get()
                        .getWarehouse().addGoods(Goods.of(GoodsType.ORE, 1)))
                .get(5, TimeUnit.SECONDS);

        // The stale confirmation explains the change and transfers nothing.
        quick.confirm();
        quick.verifyRefusal(CHANGED);
        quick.closeRefusal();
        colony.verifyNumberOfGoodsInWrehouse(ORE_WAREHOUSE_INDEX, 91);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.ORE, 40));
        quick.verifyTreasury(TREASURY);
    }

    private ShipWrapper ship() {
        return new ShipWrapper((UnitWithCargo) getModel().getUnitById(3));
    }
}
