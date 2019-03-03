package org.microcol.test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitGalleon;
import org.microcol.page.ColonyScreen;
import org.microcol.page.DialogChooseNumberOfGoods;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_05_moving_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T05-moving-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass());
    }

    @Override
    protected void bind(final Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(testFileName);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    void TC_05_moving_goods(final FxRobot robot) throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open colony Delft
	final ColonyScreen colonyScreen = gamePage.openColonyAt(Location.of(22, 12), "Delft");

	// Select ship from port by clicking at it.
	colonyScreen.selectUnitFromPort(0);

	// Drag food from first warehouse slot to first ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that 100 corn was transferred.
	verifyThatGoodsInShip(0, GoodType.CORN, 100);

	// Drag tobacco from third warehouse slot to second ship's cargo slot
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(2, 1);

	// Verify that 54 tobacco was transferred.
	verifyThatGoodsInShip(1, GoodType.TOBACCO, 54);

	// Drag food from first warehouse slot to first ship's cargo slot. Ship's cargo
	// slot is already full.
	colonyScreen.dragGoodsFromWarehouseToShipCargoSlot(0, 0);

	// Verify that still just 100 corn is in cargo.
	verifyThatGoodsInShip(0, GoodType.CORN, 100);

	// Drag 100 corn to third cargo slot, press control during dragging
	final DialogChooseNumberOfGoods dialog = colonyScreen
		.dragGoodsFromWarehouseToShipCargoSlotWithPressedControll(0, 2);

	// Select that just 47 corn will be transferred.
	dialog.selectValueAtSlider(47);

	// Close dialog for choosing transferred amount.
	dialog.close();

	// Verify that just 47 corn was transferred.
	verifyThatGoodsInShip(2, GoodType.CORN, 47);

	// Drag corn from ship cargo slot to warehouse, press control during dragging.
	final DialogChooseNumberOfGoods dialog2 = colonyScreen
		.dragGoodsFromShipCargoSlotToWarehouseWithPressedControll(0, 0);

	// Select that just 77 goods will be transfered
	dialog2.selectValueAtSlider(77);

	// Close dialog
	dialog2.close();

	// Verify that just 23 corn is in first cargo slot.
	verifyThatGoodsInShip(0, GoodType.CORN, 23);

    }

    private void verifyThatGoodsInShip(final int cargoSlotIndex, final GoodType expectedGoodsType,
	    final int expectedNumberOfGoods) {
	final List<Unit> ships = getModel().getUnitsAt(Location.of(22, 12)).stream()
		.filter(unit -> unit.getType().equals(UnitType.GALLEON)).collect(Collectors.toList());

	assertEquals(1, ships.size());
	final UnitGalleon galleon = (UnitGalleon) ships.get(0);
	final CargoSlot cargoSlot = galleon.getCargo().getSlotByIndex(cargoSlotIndex);

	assertTrue(cargoSlot.getGoods().isPresent(), "Cargo slot should not be empty");
	assertEquals(expectedGoodsType, cargoSlot.getGoods().get().getGoodType());
	assertEquals(expectedNumberOfGoods, cargoSlot.getGoods().get().getAmount());
    }

}
