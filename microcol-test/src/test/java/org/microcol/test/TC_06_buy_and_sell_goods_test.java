package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.model.GoodsType;
import org.microcol.model.Goods;
import org.microcol.model.unit.UnitGalleon;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.BuyUnitScreen;
import org.microcol.page.DialogChooseNumberOfGoods;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_06_buy_and_sell_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T06-buy-and-sell-goods.microcol");

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
    void TC_06_moving_goods() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open Europe port
	EuropePortScreen europePort = gamePage.openEuroperPort();

	// verify that there are 100000 gold.
	europePort.assertGold(100000);

	// Open buy unit screen.
	BuyUnitScreen buyUnitScreen = europePort.openBuyUnitScreen();

	// Buy ship called 'galeona' and close screen
	europePort = buyUnitScreen.buyUnitByName("Galleon");

	// Verify that there are 95000 gold.
	europePort.assertGold(95000);

	// Verify that there is exactly 1 ship in port.
	europePort.verifyNumberOfShipsInPort(1);

	// Verify that ship is empty.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

	/*
	 * Buy food, with pressed control, limit food to 63, verify that food is in
	 * cargo slot.
	 */
	DialogChooseNumberOfGoods dialogChooseNumberOfGoods = europePort
		.buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
	dialogChooseNumberOfGoods.selectValueAtSlider(63);
	dialogChooseNumberOfGoods.close();

	// Verify that in first cargo slot is 63 corn.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 63));

	// Verify that amount of gold dropped.
	europePort.assertGold(94496);

	/*
	 * Sell food with pressed control, sell just 27 food, verify that 26 remains in
	 * cargo slot.
	 */
	dialogChooseNumberOfGoods = europePort.sellGoodsWithPressedControl(0, 0);
	dialogChooseNumberOfGoods.selectValueAtSlider(27);
	dialogChooseNumberOfGoods.close();

	// Verify that in first cargo slot is 36 corn.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 36));

	// Verify that amount of gold raised.
	europePort.assertGold(94712);

	// Close Europe port
	gamePage = europePort.close();
    }

    @Test
    void TC_06_moving_goods_buy_cargo_and_move_it_to_occupied_cargo_slot() throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open Europe port
	EuropePortScreen europePort = gamePage.openEuroperPort();

	// verify that there are 100000 gold.
	europePort.assertGold(100000);

	// Open buy unit screen.
	BuyUnitScreen buyUnitScreen = europePort.openBuyUnitScreen();

	// Buy ship called 'galeona' and close screen
	europePort = buyUnitScreen.buyUnitByName("Galleon");

	// Verify that there are 95000 gold.
	europePort.assertGold(95000);

	// Verify that there is exactly 1 ship in port.
	europePort.verifyNumberOfShipsInPort(1);

	// Verify that ship is empty.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

	// Buy 100 of corn and move it into first cargo slot.
	europePort.buyGoodsAndMoveItToCargoSlot(0, 0);

	// Verify that in first cargo slot is 100 corn.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// Verify that amount of gold dropped.
	europePort.assertGold(94200);

	// Buy another 100 of corn and move it into first cargo slot.
	europePort.buyGoodsAndMoveItToCargoSlot(0, 0);

	// Verify that in first cargo slot is 100 corn.
	verifyThatGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

	// More than 100 goods can't be in one cargo slot.
	// Verify that amount of gold is not changed from previous state.
	europePort.assertGold(94200);

	// Close Europe port
	gamePage = europePort.close();
    }

    private void verifyThatGoodsInShip(final int cargoSlotIndex, final Goods expectedGoods) {
	final List<UnitWithCargo> ships = getModel().getEurope().getPort().getShipsInPort(getHumanPlayer());
	assertEquals(1, ships.size());
	final UnitGalleon galleon = (UnitGalleon) ships.get(0);

	verifyNumberOfGoodsInShip(galleon, cargoSlotIndex, expectedGoods);
    }

}
