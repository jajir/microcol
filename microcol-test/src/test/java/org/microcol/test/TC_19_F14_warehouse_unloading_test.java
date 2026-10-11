package org.microcol.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Verify safe unloading, cancellation and rejection of stale confirmation.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_19_F14_warehouse_unloading_test extends AbstractMicroColTest {

    private static final Location COLONY_LOCATION = Location.of(22, 12);
    private static final File testFileName = new File(
            "src/test/scenarios/F14-warehouse-unloading.microcol");
    private static final String LIMITED = "Available warehouse space for this cargo: 1. Choose an amount or cancel. The rest stays aboard.";
    private static final String FULL = "The warehouse is full for this cargo. Nothing was unloaded. Free storage and try again.";
    private static final String CHANGED = "The cargo or colony changed. Nothing was unloaded. Check the cargo and available storage, then try again.";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_confirm_one_unit_and_reload() throws Exception {
        // Choose an isolated save file and load the defined game.
        final File savedFile = new File(System.getProperty("user.home"),
                "warehouse.microcol");
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        when(files.saveFile(any(File.class), anyString()))
                .thenReturn(savedFile);
        when(files.loadFile(any(File.class))).thenReturn(testFileName,
                savedFile);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);

        // Try unloading 100 tobacco into a warehouse with room for one.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.verifyExplanation(LIMITED);
        dialog.captureDialog("goodsTransferExplanation", "limited.png");

        // Confirm only the offered unit; the other 99 remain aboard.
        dialog.clickOnOk();
        colony.verifyNumberOfGoodsInWrehouse(2, 100);
        assertEquals(Goods.of(GoodsType.TOBACCO, 99),
                cargo(0).getGoods().get());

        // A repeated attempt cannot unload into the now full warehouse.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        colony.verifyWarehouseWarning(FULL);
        colony.captureDialog("warehouseTransferMessage", "full.png");
        colony.closeWarehouseWarning();
        assertEquals(Goods.of(GoodsType.TOBACCO, 99),
                cargo(0).getGoods().get());
        colony.verifyNumberOfGoodsInWrehouse(2, 100);

        // Save, return to the menu and load the saved balances.
        final GamePage afterUnloading = colony.close();
        afterUnloading.saveGame();
        assertTrue(savedFile.isFile());
        assertTrue(savedFile.length() > 0);
        final ColonyScreen reloaded = afterUnloading.exitToMenu().loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        reloaded.selectUnitFromPort(0);
        reloaded.verifyNumberOfGoodsInWrehouse(2, 100);
        assertEquals(Goods.of(GoodsType.TOBACCO, 99),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_02_cancel_keeps_both_inventories() throws Exception {
        // Load the defined game and open the colony's cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);

        // Cancel the proposed one-unit transfer with the visible button.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.cancel();

        // No goods left the ship or entered the warehouse.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_03_escape_keeps_both_inventories() throws Exception {
        // Load the defined game and open the colony's cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);

        // Dismiss the proposal with Escape.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.pressEscape();

        // Escape has the same safe outcome as the Cancel button.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_04_full_warehouse_explains_rejection() throws Exception {
        // Load a warehouse already holding 100 cigars.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyNumberOfGoodsInWrehouse(10, 100);

        // Attempt to unload cigars and acknowledge the explanation.
        colony.dragGoodsFromShipCargoSlotToWarehouse(1, 10);
        colony.verifyWarehouseWarning(FULL);
        colony.closeWarehouseWarning();

        // Both inventories remain full, with no discarded cargo.
        colony.verifyNumberOfGoodsInWrehouse(10, 100);
        assertEquals(Goods.of(GoodsType.CIGARS, 100),
                cargo(1).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_05_all_cargo_fits_without_a_dialog() throws Exception {
        // Load 20 cotton aboard and 50 in the colony warehouse.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        assertEquals(Goods.of(GoodsType.COTTON, 20), cargo(2).getGoods().get());

        // Unload the whole cargo directly.
        colony.dragGoodsFromShipCargoSlotToWarehouse(2, 3);

        // Every unit is stored; adjacent cargo is unchanged.
        colony.verifyNumberOfGoodsInWrehouse(3, 70);
        assertTrue(cargo(2).isEmpty());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_06_keyboard_cannot_choose_zero_or_exceed_capacity()
            throws Exception {
        // Request unloading into one free space.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());

        // Zero disables confirmation; trying to select two stops at one.
        dialog.selectAmountWithKeyboard(0);
        dialog.verifyZeroCannotBeConfirmed();
        dialog.selectAmountWithKeyboard(2);
        dialog.verifyOfferedAmount(1);
        dialog.clickOnOk();

        // Only the valid amount was transferred.
        colony.verifyNumberOfGoodsInWrehouse(2, 100);
        assertEquals(Goods.of(GoodsType.TOBACCO, 99),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_07_player_can_choose_a_smaller_partial_transfer() throws Exception {
        // Explicitly request a partial cotton transfer.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        final DialogChooseGoodsAmount dialog = colony
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 3);
        dialog.verifyOfferedAmount(20);

        // Cancel, then reopen the same dialog for a capacity-limited transfer.
        dialog.cancel();
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        dialog.verifyOfferedAmount(1);
        dialog.verifyExplanation(LIMITED);
        dialog.captureDialog("goodsTransferExplanation",
                "reopened-limited.png");
        dialog.cancel();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());

        // Reopen without an explanation, select 17 using the keyboard and
        // confirm.
        colony.dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 3);
        dialog.verifyOfferedAmount(20);
        dialog.captureDialog("buttonCancel", "reopened-partial.png");
        dialog.selectAmountWithKeyboard(17);
        dialog.clickOnOk();

        // The chosen quantity is exact on both sides.
        colony.verifyNumberOfGoodsInWrehouse(3, 67);
        assertEquals(Goods.of(GoodsType.COTTON, 3), cargo(2).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_08_capacity_changed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate competing production on the FX thread while the modal is
        // open.
        WaitForAsyncUtils.asyncFx(() -> getModel().getColonyByName("Delft")
                .get().getWarehouse().addGoods(Goods.of(GoodsType.TOBACCO, 1)))
                .get(5, TimeUnit.SECONDS);

        // Confirm the stale proposal through the UI and acknowledge its
        // rejection.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.captureDialog("warehouseTransferMessage", "changed.png");
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 100);
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_09_cargo_removed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate a competing action consuming the source cargo.
        WaitForAsyncUtils.asyncFx(
                () -> cargo(0).removeCargo(Goods.of(GoodsType.TOBACCO, 100)))
                .get(5, TimeUnit.SECONDS);

        // Stale confirmation cannot create goods from an empty source.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertTrue(cargo(0).isEmpty());
    }

    @Test
    @Tag("local")
    void TC_10_carrier_moved_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate the carrier leaving while its old proposal is open.
        WaitForAsyncUtils.asyncFx(() -> {
            getModel().getUnitById(3).placeToLocation(Location.of(23, 12));
        }).get(5, TimeUnit.SECONDS);

        // Confirm through the UI; the stale action must not change either
        // inventory.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Location.of(23, 12),
                getModel().getUnitById(3).getLocation());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_11_carrier_captured_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate a change of ownership while the proposal is open.
        WaitForAsyncUtils.asyncFx(() -> {
            getModel().getUnitById(3)
                    .takeOver(getModel().getPlayerByName("Dutch's King"));
        }).get(5, TimeUnit.SECONDS);

        // Confirm through the UI; the stale action must not change either
        // inventory.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(getModel().getPlayerByName("Dutch's King"),
                cargo(0).getOwnerPlayer());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                cargo(0).getGoods().get());
    }

    @Test
    @Tag("local")
    void TC_12_cargo_type_changed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate a competing transfer replacing tobacco with cotton.
        WaitForAsyncUtils.asyncFx(() -> {
            cargo(0).removeCargo(Goods.of(GoodsType.TOBACCO, 100));
            cargo(0).storeFromColonyWarehouse(Goods.of(GoodsType.COTTON, 1),
                    getModel().getColonyByName("Delft").get());
        }).get(5, TimeUnit.SECONDS);

        // Confirm through the UI; the stale action must not change either
        // inventory.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        assertEquals(Goods.of(GoodsType.COTTON, 1), cargo(0).getGoods().get());
        colony.verifyNumberOfGoodsInWrehouse(3, 49);
    }

    @Test
    @Tag("local")
    void TC_13_insufficient_cargo_during_confirmation_is_rejected()
            throws Exception {
        // Open a partial cotton transfer offering all 20 units aboard.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        final DialogChooseGoodsAmount dialog = colony
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 3);
        dialog.verifyOfferedAmount(20);

        // Simulate another action consuming one unit while the proposal is
        // open.
        WaitForAsyncUtils.asyncFx(
                () -> cargo(2).removeCargo(Goods.of(GoodsType.COTTON, 1)))
                .get(5, TimeUnit.SECONDS);

        // Confirmation must reject the original quantity, not silently reduce
        // it.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        assertEquals(Goods.of(GoodsType.COTTON, 19), cargo(2).getGoods().get());
    }

    private CargoSlot cargo(final int index) {
        return ((UnitWithCargo) getModel().getUnitById(3)).getCargo()
                .getSlotByIndex(index);
    }
}
