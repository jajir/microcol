package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.ScreenMarketBuy;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Dutch grain purchases move the Dutch market one step at 200 units, while a
 * non-Dutch market receiving the same units stays put; Continue keeps both.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_95_F86_dutch_market_pressure_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_95_dutch_market_steps_at_two_hundred_units_and_continue_keeps_it()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        europePort.verifyNumberOfShipsInPort(1);

        // The untouched Dutch market quotes grain at bid 2 and ask 4.
        europePort.verifyGoodsBidAndAsk(GoodsType.GRAIN, "2/4");

        // The first 100 units press the Dutch market by 400 eighths, half of
        // the 800 an ordinary own market takes for the same units. No step yet.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.verifyQuotedPrice("4 gold", "400 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94600);
        europePort.verifyGoodsBidAndAsk(GoodsType.GRAIN, "2/4");

        // The second 100 units complete 800 eighths: one Dutch step at 200
        // units. The bid and ask move to 3/5.
        screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 1);
        screenMarketBuy.verifyQuotedPrice("4 gold", "400 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94200);
        europePort.verifyGoodsBidAndAsk(GoodsType.GRAIN, "3/5");
        // A non-Dutch market receives the same 200 units as quarter-strength
        // pressure, 400 eighths, so it stays at 2/4.
        europePort.verifyNationalGoodsBidAndAsk(EuropeanNation.ENGLISH,
                GoodsType.GRAIN, "2/4");

        // Save through the menu and continue; both markets and treasury remain.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        europePort.verifyGoodsBidAndAsk(GoodsType.GRAIN, "3/5");
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94200);
        europePort.verifyNationalGoodsBidAndAsk(EuropeanNation.ENGLISH,
                GoodsType.GRAIN, "2/4");
        gamePage = europePort.close();
    }

}
