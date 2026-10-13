package org.microcol.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ConcurrentModelEvents;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.microcol.page.ShipWrapper;
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
        colony.verifyUnitInPortSelected(0, true);

        // Try unloading 100 tobacco into a warehouse with room for one.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.verifyExplanation(LIMITED);
        dialog.captureDialog("goodsTransferExplanation", "limited.png");

        // Confirm only the offered unit; the other 99 remain aboard.
        dialog.clickOnOk();
        colony.verifyNumberOfGoodsInWrehouse(2, 100);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 99));

        // A repeated attempt cannot unload into the now full warehouse.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        colony.verifyWarehouseWarning(FULL);
        colony.captureDialog("warehouseTransferMessage", "full.png");
        colony.closeWarehouseWarning();
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 99));
        colony.verifyNumberOfGoodsInWrehouse(2, 100);

        // Save, return to the menu and load the saved balances.
        final GamePage afterUnloading = colony.close();
        afterUnloading.saveGame();
        afterUnloading.verifySavedGame(savedFile);
        final ColonyScreen reloaded = afterUnloading.exitToMenu().loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        reloaded.selectUnitFromPort(0);
        reloaded.verifyUnitInPortSelected(0, true);
        reloaded.verifyNumberOfGoodsInWrehouse(2, 100);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 99));
    }

    @Test
    @Tag("local")
    void TC_02_cancel_keeps_both_inventories() throws Exception {
        // Load the defined game and open the colony's cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);

        // Cancel the proposed one-unit transfer with the visible button.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.cancel();

        // No goods left the ship or entered the warehouse.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_03_escape_keeps_both_inventories() throws Exception {
        // Load the defined game and open the colony's cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);

        // Dismiss the proposal with Escape.
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        dialog.pressEscape();

        // Escape has the same safe outcome as the Cancel button.
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_04_full_warehouse_explains_rejection() throws Exception {
        // Load a warehouse already holding 100 cigars.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.verifyNumberOfGoodsInWrehouse(10, 100);

        // Attempt to unload cigars and acknowledge the explanation.
        colony.dragGoodsFromShipCargoSlotToWarehouse(1, 10);
        colony.verifyWarehouseWarning(FULL);
        colony.closeWarehouseWarning();

        // Both inventories remain full, with no discarded cargo.
        colony.verifyNumberOfGoodsInWrehouse(10, 100);
        ship().verifyCargoGoods(1, Goods.of(GoodsType.CIGARS, 100));
    }

    @Test
    @Tag("local")
    void TC_05_all_cargo_fits_without_a_dialog() throws Exception {
        // Load 20 cotton aboard and 50 in the colony warehouse.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.COTTON, 20));

        // Unload the whole cargo directly.
        colony.dragGoodsFromShipCargoSlotToWarehouse(2, 3);

        // Every unit is stored; adjacent cargo is unchanged.
        colony.verifyNumberOfGoodsInWrehouse(3, 70);
        ship().verifyCargoSlotEmpty(2);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_06_keyboard_cannot_choose_zero_or_exceed_capacity()
            throws Exception {
        // Request unloading into one free space.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());

        // Zero disables confirmation; trying to select two stops at one.
        dialog.focusAmountSlider();
        dialog.verifyAmountSliderFocused();
        dialog.selectAmountWithKeyboard(0);
        dialog.verifySelectedAmount(0);
        dialog.verifyZeroCannotBeConfirmed();
        dialog.focusAmountSlider();
        dialog.verifyAmountSliderFocused();
        dialog.selectAmountWithKeyboard(2);
        dialog.verifySelectedAmount(2);
        dialog.verifyOfferedAmount(1);
        dialog.clickOnOk();

        // Only the valid amount was transferred.
        colony.verifyNumberOfGoodsInWrehouse(2, 100);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 99));
    }

    @Test
    @Tag("local")
    void TC_07_player_can_choose_a_smaller_partial_transfer() throws Exception {
        // Explicitly request a partial cotton transfer.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));

        // Reopen without an explanation, select 17 using the keyboard and
        // confirm.
        colony.dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 3);
        dialog.verifyOfferedAmount(20);
        dialog.captureDialog("buttonCancel", "reopened-partial.png");
        dialog.focusAmountSlider();
        dialog.verifyAmountSliderFocused();
        dialog.selectAmountWithKeyboard(17);
        dialog.verifySelectedAmount(17);
        dialog.clickOnOk();

        // The chosen quantity is exact on both sides.
        colony.verifyNumberOfGoodsInWrehouse(3, 67);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.COTTON, 3));
    }

    @Test
    @Tag("local")
    void TC_08_capacity_changed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_09_cargo_removed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyCargoSlotEmpty(0);
    }

    @Test
    @Tag("local")
    void TC_10_carrier_moved_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyLocation(Location.of(23, 12));
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_11_carrier_captured_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyCargoOwner(0, getModel().getPlayerByName("Dutch's King"));
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_12_cargo_type_changed_during_confirmation_is_rejected()
            throws Exception {
        // Leave the one-unit proposal open.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
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
        ship().verifyCargoGoods(0, Goods.of(GoodsType.COTTON, 1));
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
        colony.verifyUnitInPortSelected(0, true);
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
        dialog.verifyOfferedAmount(19);
        dialog.cancel();
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.COTTON, 19));
    }

    @Test
    @Tag("local")
    void TC_14_abandoned_colony_cannot_receive_cargo() throws Exception {
        // Leave the one-unit proposal open in Delft.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        final Colony abandoned = getModel().getColonyByName("Delft").get();

        // Simulate all workers leaving; the last departure destroys the colony.
        WaitForAsyncUtils.asyncFx(() -> {
            abandoned.getColonyFields().forEach(field -> field.getUnit()
                    .ifPresent(unit -> unit.placeToLocation(COLONY_LOCATION)));
            abandoned.getConstructions().forEach(building -> building
                    .getConstructionSlots().forEach(slot -> {
                        if (!slot.isEmpty()) {
                            slot.getUnit().placeToLocation(COLONY_LOCATION);
                        }
                    }));
        }).get(5, TimeUnit.SECONDS);
        colony.verifyColonyValid(false);

        // Reject confirmation instead of consuming cargo into a detached
        // warehouse.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyColonyPresent("Delft", false);
        colony.verifyWarehouseGoods(Goods.of(GoodsType.TOBACCO, 99));
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
        GamePage.of(getContext());
    }

    @Test
    @Tag("local")
    void TC_15_destroyed_carrier_cannot_supply_cargo() throws Exception {
        // Leave the one-unit proposal open and retain its original source.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);
        final CargoSlot removedSource = cargo(0);

        // Simulate the model event that removes a destroyed carrier from the
        // game.
        WaitForAsyncUtils.asyncFx(() -> ConcurrentModelEvents
                .destroyUnit(getModel(), removedSource.getOwnerUnit()))
                .get(5, TimeUnit.SECONDS);
        colony.verifyCarrierPresent(3, false);

        // Stale confirmation cannot credit cargo from a ship that no longer
        // exists.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        new ShipWrapper((UnitWithCargo) removedSource.getOwnerUnit())
                .verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_16_carrier_at_high_seas_cannot_unload() throws Exception {
        // Leave the one-unit proposal open at the colony.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsFromShipCargoSlotToWarehouse(0, 2);
        final DialogChooseGoodsAmount dialog = DialogChooseGoodsAmount
                .of(getContext());
        dialog.verifyOfferedAmount(1);

        // Simulate departure from the map before the player confirms.
        WaitForAsyncUtils.asyncFx(() -> {
            // Another game event sends the carrier away while its offer is
            // open.
            getModel().getUnitById(3).placeToLocation(Location.of(50, 16));
            getModel().getUnitById(3).placeToHighSeas(true);
        }).get(5, TimeUnit.SECONDS);
        ship().verifyAtHighSea(true);

        // Reject the stale proposal without trying to read an off-map location.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        colony.verifyNumberOfGoodsInWrehouse(2, 99);
        ship().verifyCargoGoods(0, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    @Tag("local")
    void TC_17_drop_outside_window_keeps_cargo_and_allows_next_transfer()
            throws Exception {
        // Drop cotton outside the application; no target accepts the cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        colony.dragGoodsOutsideWindow(2);
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.COTTON, 20));

        // The next ordinary transfer must still receive mouse input.
        colony.dragGoodsFromShipCargoSlotToWarehouse(2, 3);
        colony.verifyNumberOfGoodsInWrehouse(3, 70);
        ship().verifyCargoSlotEmpty(2);
    }

    @Test
    @Tag("local")
    void TC_18_changed_amount_can_be_explicitly_reconfirmed() throws Exception {
        // Open an amount choice, then simulate competing use of the cargo.
        final ColonyScreen colony = WelcomePage.of(getContext()).loadGame()
                .openColonyAt(COLONY_LOCATION, "Delft");
        colony.selectUnitFromPort(0);
        colony.verifyUnitInPortSelected(0, true);
        final DialogChooseGoodsAmount dialog = colony
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 3);
        dialog.verifyOfferedAmount(20);
        WaitForAsyncUtils.asyncFx(
                () -> cargo(2).removeCargo(Goods.of(GoodsType.COTTON, 1)))
                .get(5, TimeUnit.SECONDS);

        // The stale confirmation explains the change and offers a new amount.
        dialog.clickOnOk();
        colony.verifyWarehouseWarning(CHANGED);
        colony.closeWarehouseWarning();
        dialog.verifyOfferedAmount(19);
        colony.verifyNumberOfGoodsInWrehouse(3, 50);
        ship().verifyCargoGoods(2, Goods.of(GoodsType.COTTON, 19));

        // Only this fresh confirmation transfers the newly offered cargo.
        dialog.clickOnOk();
        colony.verifyNumberOfGoodsInWrehouse(3, 69);
        ship().verifyCargoSlotEmpty(2);
    }

    private ShipWrapper ship() {
        return new ShipWrapper((UnitWithCargo) getModel().getUnitById(3));
    }

    private CargoSlot cargo(final int index) {
        return ((UnitWithCargo) getModel().getUnitById(3)).getCargo()
                .getSlotByIndex(index);
    }
}
