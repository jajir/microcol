package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenMarketBuy;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_94_F85_national_market_pressure_test
        extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_94_national_market_pressure_and_stale_confirmation()
            throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        europePort.verifyNumberOfShipsInPort(1);

        // The untouched Dutch market quotes corn at bid 2 and ask 4.
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "2/4");

        // Buy 100 corn into the first hold at 4; afterwards the bid moves one
        // step to 3 and the ask follows the same spread to 5.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.verifyQuotedPrice("4 gold", "400 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(94600);
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "3/5");

        // Open a purchase choice for the second hold; it shows the ask 5.
        screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 1);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.verifyQuotedPrice("5 gold", "500 gold");

        // Another action buys 100 corn into the third hold while the choice is
        // open; that purchase moves the market to 4/6.
        europePort.competingPurchase(2, GoodsType.CORN, 100);

        // Confirming the stale price charges nothing and moves no goods.
        screenMarketBuy.clickOnBuy();
        europePort.verifyEuropePriceChangedWarning(
                "The European price changed and the trade was not made. Nothing was charged or moved.");
        europePort.closeGoodsTransferWarning();
        europePort.verifyCargoSlotEmpty(0, 1);
        europePort.verifyGold(94100);
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "4/6");

        // A fresh choice shows the current price 6 and buys at that price; that
        // purchase moves the market one more step, to 5/7.
        screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 1);
        screenMarketBuy.verifyQuotedPrice("6 gold", "600 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(93500);
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "5/7");

        // Save through the menu and continue; the market and treasury remain.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "5/7");
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(93500);
        gamePage = europePort.close();
    }

}
