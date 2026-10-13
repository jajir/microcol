package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.page.BuyUnitScreen;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenMarketBuy;
import org.microcol.page.ScreenMarketSell;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_06_F06_buy_and_sell_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port
        EuropePortScreen europePort = gamePage.openEuroperPort();

        // verify that there are 100000 gold.
        europePort.verifyGold(100000);

        // Open buy unit screen.
        BuyUnitScreen buyUnitScreen = europePort.openBuyUnitScreen();

        // Buy ship called 'galeona' and close screen
        europePort = buyUnitScreen.buyUnitByName("Galleon");

        // Verify that there are 95000 gold.
        europePort.verifyGold(95000);

        // Verify that there is exactly 1 ship in port.
        europePort.verifyNumberOfShipsInPort(1);

        // Verify that ship is empty.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

        /*
         * Buy food, with pressed control, limit food to 63, verify that food is
         * in cargo slot.
         */
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifyNumberOfSliders(1);
        screenMarketBuy.selectValueAtSlider(63);
        screenMarketBuy.verifySelectedMarketQuantity(63);
        screenMarketBuy.verifyQuotedPrice("4 gold", "252 gold");
        screenMarketBuy.clickOnBuy();

        // Verify that in first cargo slot is 63 corn.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 63));

        // Verify that amount of gold dropped.
        europePort.verifyGold(94748);

        /*
         * Sell food with pressed control, sell just 27 food, verify that 26
         * remains in cargo slot.
         */
        ScreenMarketSell screenMarketSell = europePort
                .sellGoodsWithPressedControl(0, 0);
        screenMarketSell.verifyNumberOfSliders(1);
        screenMarketSell.selectValueAtSlider(27);
        screenMarketSell.verifySelectedMarketQuantity(27);
        screenMarketSell.verifyQuotedPrice("2 gold", "54 gold");
        screenMarketSell.clickOnSell();

        // Verify that in first cargo slot is 36 corn.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 36));

        // Verify that amount of gold raised.
        europePort.verifyGold(94802);

        // Close Europe port
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods_buy_cargo_and_move_it_to_occupied_cargo_slot()
            throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port
        EuropePortScreen europePort = gamePage.openEuroperPort();

        // verify that there are 100000 gold.
        europePort.verifyGold(100000);

        // Open buy unit screen.
        BuyUnitScreen buyUnitScreen = europePort.openBuyUnitScreen();

        // Buy ship called 'galeona' and close screen
        europePort = buyUnitScreen.buyUnitByName("Galleon");

        // Verify that there are 95000 gold.
        europePort.verifyGold(95000);

        // Verify that there is exactly 1 ship in port.
        europePort.verifyNumberOfShipsInPort(1);

        // Verify that ship is empty.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 0));

        // Buy 100 of corn and move it into first cargo slot.
        europePort.buyGoodsAndMoveItToCargoSlot(0, 0);

        // Verify that in first cargo slot is 100 corn.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // Verify that amount of gold dropped.
        europePort.verifyGold(94600);

        // Cancel a partial transfer to an empty slot; zero goods must not
        // occupy it.
        europePort.moveCargoWithPressedControl(0, 1).cancel();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));
        europePort.verifyCargoSlotEmpty(0, 1);
        europePort.verifyGold(94600);

        // Buy another 100 of corn and move it into first cargo slot.
        europePort.buyGoodsAndMoveItToCargoSlot(0, 0);

        // Verify that in first cargo slot is 100 corn.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));

        // More than 100 goods can't be in one cargo slot.
        // Verify that amount of gold is not changed from previous state.
        europePort.verifyGold(94600);

        // Close Europe port
        gamePage = europePort.close();
    }

}
