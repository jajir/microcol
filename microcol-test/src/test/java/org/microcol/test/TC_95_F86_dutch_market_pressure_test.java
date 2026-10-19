package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanNation;
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

/**
 * Dutch corn purchases move the Dutch market one step at 200 units, while a
 * non-Dutch market receiving the same units stays put; Continue keeps both.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_95_F86_dutch_market_pressure_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_95_dutch_market_steps_at_two_hundred_units_and_continue_keeps_it()
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

        // The first 100 units press the Dutch market by 400 eighths, half of
        // the 800 an ordinary own market takes for the same units. No step yet.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.verifyQuotedPrice("4 gold", "400 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(94600);
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "2/4");

        // The second 100 units complete 800 eighths: one Dutch step at 200
        // units. The bid and ask move to 3/5.
        screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 1);
        screenMarketBuy.verifyQuotedPrice("4 gold", "400 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(94200);
        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "3/5");
        // A non-Dutch market receives the same 200 units as quarter-strength
        // pressure, 400 eighths, so it stays at 2/4.
        europePort.verifyNationalGoodsBidAndAsk(EuropeanNation.ENGLISH,
                GoodsType.CORN, "2/4");

        // Save through the menu and continue; both markets and treasury remain.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        europePort.verifyGoodsBidAndAsk(GoodsType.CORN, "3/5");
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.CORN, 100));
        europePort.verifyGold(94200);
        europePort.verifyNationalGoodsBidAndAsk(EuropeanNation.ENGLISH,
                GoodsType.CORN, "2/4");
        gamePage = europePort.close();
    }

}
