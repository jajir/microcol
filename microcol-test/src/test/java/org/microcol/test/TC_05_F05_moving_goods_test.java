package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_05_F05_moving_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F05-moving-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_05_moving_goods() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open colony Delft
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");

        // Drag food from first warehouse slot to first ship's cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

        // Verify that 100 corn was transferred.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // Drag tobacco from third warehouse slot to second ship's cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(2, 1);

        // Verify that 54 tobacco was transferred.
        colonyScreen.verifyGoodsInShip(1, Goods.of(GoodsType.TOBACCO, 54));

        // Drag food from first warehouse slot to first ship's cargo slot.
        // Ship's cargo
        // slot is already full.
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

        // Verify that still just 100 corn is in cargo.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // Drag 100 corn to third cargo slot, press control during dragging
        final DialogChooseGoodsAmount dialog = colonyScreen
                .dragGoodsFromWarehouseToShipCargoSlotWithPressedControll(0, 2);

        // Select that just 47 corn will be transferred.
        dialog.verifyNumberOfSliders(1);
        dialog.selectValueAtSlider(47);

        // Close dialog for choosing transferred amount.
        dialog.clickOnOk();

        // Verify that just 47 corn was transferred.
        colonyScreen.verifyGoodsInShip(2, Goods.of(GoodsType.CORN, 47));

        // Drag corn from ship cargo slot to warehouse, press control during
        // dragging.
        final DialogChooseGoodsAmount dialog2 = colonyScreen
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(0, 0);

        // Select that just 77 goods will be transfered
        dialog2.verifyNumberOfSliders(1);
        dialog2.selectValueAtSlider(77);

        // Close dialog
        dialog2.clickOnOk();

        // Verify that just 23 corn is in first cargo slot.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 23));

        // Unload 17 corn from the third cargo slot into the first warehouse
        // slot.
        final DialogChooseGoodsAmount dialog3 = colonyScreen
                .dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(2, 0);
        dialog3.verifyNumberOfSliders(1);
        dialog3.selectValueAtSlider(17);
        dialog3.clickOnOk();

        // Verify that the selected cargo slot lost goods and the first slot did
        // not.
        colonyScreen.verifyGoodsInShip(2, Goods.of(GoodsType.CORN, 30));
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 23));

    }

    @Test
    @Tag("local")
    void TC_05_moving_goods_without_pressed_control() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open colony Delft
        final ColonyScreen colonyScreen = gamePage
                .openColonyAt(Location.of(22, 12), "Delft");

        // Drag food from first warehouse slot to first ship's cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

        // Verify that 100 corn was transferred.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // Drag tobacco from third warehouse slot to second ship's cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(2, 1);

        // Verify that 54 tobacco was transferred.
        colonyScreen.verifyGoodsInShip(1, Goods.of(GoodsType.TOBACCO, 54));

        // Drag food from first warehouse slot to first ship's cargo slot.
        // Ship's cargo
        // slot is already full.
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

        // Verify that still just 100 corn is in cargo.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // Drag 100 corn to third cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 2);

        // Verify that just 100 corn was transferred.
        colonyScreen.verifyGoodsInShip(2, Goods.of(GoodsType.CORN, 96));

        // Drag corn from ship cargo slot to warehouse
        colonyScreen.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        DialogChooseGoodsAmount.of(getContext()).clickOnOk();

        // Verify that just 0 corn is in first cargo slot.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

    }

    @Test
    @Tag("local")
    void TC_05_mooving_to_warehouse_more_than_limit() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open colony Delft
        ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12),
                "Delft");

        // Drag food from first warehouse slot to first ship's cargo slot
        colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

        // Verify that 100 corn was transferred.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        gamePage = colonyScreen.close();

        gamePage.nextTurnAndCloseDialogs();

        gamePage.nextTurnAndCloseDialogs();

        gamePage.nextTurnAndCloseDialogs();

        // Open colony Delft
        colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

        // Record the warehouse corn before unloading the full cargo slot.
        final int cornBefore = getModel().getColonyAt(Location.of(22, 12)).get()
                .getWarehouse().getGoods(GoodsType.CORN).getAmount();

        // Drag corn from ship cargo slot to warehouse.
        colonyScreen.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
        DialogChooseGoodsAmount.of(getContext()).clickOnOk();

        // All 100 corn should reach the warehouse; the ship's slot should be
        // empty.
        colonyScreen.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 0));
        colonyScreen.verifyWarehouseGoods(
                Goods.of(GoodsType.CORN, cornBefore + 100));
    }

}
