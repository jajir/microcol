package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitGalleon;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseGoodsAmount;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_05_moving_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T05-moving-goods.microcol");

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
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Drag food from first warehouse slot to first ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that 100 corn was transferred.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// Drag tobacco from third warehouse slot to second ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(2, 1);

	// Verify that 54 tobacco was transferred.
	verifyThatGoodsInShip(1, Goods.of(GoodsType.TOBACCO, 54));

	// Drag food from first warehouse slot to first ship's cargo slot. Ship's cargo
	// slot is already full.
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that still just 100 corn is in cargo.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// Drag 100 corn to third cargo slot, press control during dragging
	final DialogChooseGoodsAmount dialog = colonyScreen.dragGoodsFromWarehouseToShipCargoSlotWithPressedControll(0,
		2);

	// Select that just 47 corn will be transferred.
	dialog.selectValueAtSlider(47);

	// Close dialog for choosing transferred amount.
	dialog.clickOnOk();

	// Verify that just 47 corn was transferred.
	verifyThatGoodsInShip(2, Goods.of(GoodsType.CORN, 47));

	// Drag corn from ship cargo slot to warehouse, press control during dragging.
	final DialogChooseGoodsAmount dialog2 = colonyScreen.dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(0,
		0);

	// Select that just 77 goods will be transfered
	dialog2.selectValueAtSlider(77);

	// Close dialog
	dialog2.clickOnOk();

	// Verify that just 23 corn is in first cargo slot.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 23));

    }

    @Test
    @Tag("local")
    void TC_05_moving_goods_without_pressed_control() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open colony Delft
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Drag food from first warehouse slot to first ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that 100 corn was transferred.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// Drag tobacco from third warehouse slot to second ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(2, 1);

	// Verify that 54 tobacco was transferred.
	verifyThatGoodsInShip(1, Goods.of(GoodsType.TOBACCO, 54));

	// Drag food from first warehouse slot to first ship's cargo slot. Ship's cargo
	// slot is already full.
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that still just 100 corn is in cargo.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// Drag 100 corn to third cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 2);

	// Verify that just 100 corn was transferred.
	verifyThatGoodsInShip(2, Goods.of(GoodsType.CORN, 96));

	// Drag corn from ship cargo slot to warehouse
	colonyScreen.dragGoodsFromShipCargoSlotToWarehouse(0, 0);

	// Verify that just 0 corn is in first cargo slot.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

    }

    @Test
    void TC_05_mooving_to_warehouse_more_than_limit() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open colony Delft
	ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Drag food from first warehouse slot to first ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that 100 corn was transferred.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	gamePage = colonyScreen.close();

	gamePage.nextTurnAndCloseDialogs();

	gamePage.nextTurnAndCloseDialogs();

	gamePage.nextTurnAndCloseDialogs();

	// Open colony Delft
	colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Drag corn from ship cargo slot to warehouse
	colonyScreen.dragGoodsFromShipCargoSlotToWarehouse(0, 0);
    }

    private void verifyThatGoodsInShip(final int cargoSlotIndex, final Goods expectedGoods) {
	final List<Unit> ships = getModel().getUnitsAt(Location.of(22, 12)).stream()
		.filter(unit -> unit.getType().equals(UnitType.GALLEON)).collect(Collectors.toList());

	assertEquals(1, ships.size());

	verifyNumberOfGoodsInShip((UnitGalleon) ships.get(0), cargoSlotIndex, expectedGoods);
    }

}
