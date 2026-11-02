package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.EuropeanTradePage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.ScreenMarketBuy;
import org.akropia.page.ScreenMarketSell;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/**
 * Economic trade report (R-0145): ordinary Europe purchases and sales settle
 * once per nation and commodity, a cancelled choice adds nothing, the report
 * reopens unchanged, and Continue from disk keeps the totals. The fixture
 * already holds a French rival account with revolutionary custom-house sales.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_108_F99_trade_accounts_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.akropia");

    private static final String FRENCH_CLOTH = "trade-FRENCH-CLOTH";

    private static final String DUTCH_GRAIN = "trade-DUTCH-GRAIN";

    private static final String ENGLISH_ORE = "trade-ENGLISH-ORE";

    private static final String SPANISH_GRAPES = "trade-SPANISH-GRAPES";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_trades_cancel_reopen_and_continue_keep_each_settlement_once()
            throws Exception {
        // Before any trade the report shows the saved rival accounts, among
        // them the French one: 4 cloth bought for 48 gold, 10 sold for 110
        // gold,
        // of which 6 were
        // revolutionary custom-house exports worth 60 net gold.
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();
        final EuropeanTradePage trade = new EuropeanTradePage(getContext());
        trade.open();
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        trade.verifyRevolutionarySource(FRENCH_CLOTH,
                "Revolutionary custom house: 6 units, 60 net gold");
        gamePage = trade.close();

        // Buying a ship is not a goods transaction; cancelling a goods choice
        // settles nothing.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort.verifyGold(100000);
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        europePort.verifyGold(95000);
        europePort.buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0)
                .clickOnCancel();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(95000);

        // Buy 63 grain: 252 gold including tax, settled once.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.selectValueAtSlider(63);
        screenMarketBuy.verifySelectedMarketQuantity(63);
        screenMarketBuy.verifyQuotedPrice("4 gold", "252 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 63));
        europePort.verifyGold(94748);

        // Sell 27 grain: 54 gold received, leaving 36 in the hold.
        ScreenMarketSell screenMarketSell = europePort
                .sellGoodsWithPressedControl(0, 0);
        screenMarketSell.selectValueAtSlider(27);
        screenMarketSell.verifySelectedMarketQuantity(27);
        screenMarketSell.verifyQuotedPrice("2 gold", "54 gold");
        screenMarketSell.clickOnSell();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 36));
        europePort.verifyGold(94802);
        gamePage = europePort.close();

        // The report shows the Dutch grain once and the French rival unchanged.
        trade.open();
        trade.verifyTradeRow(DUTCH_GRAIN, "Bought: 63 units", "Gold spent: 252",
                "Sold: 27 units", "Gold received: 54");
        trade.verifyNoRevolutionarySource(DUTCH_GRAIN);
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        trade.verifyRevolutionarySource(FRENCH_CLOTH,
                "Revolutionary custom house: 6 units, 60 net gold");

        // Closing and reopening the report adds nothing.
        gamePage = trade.close();
        trade.open();
        trade.verifyTradeRow(DUTCH_GRAIN, "Bought: 63 units", "Gold spent: 252",
                "Sold: 27 units", "Gold received: 54");
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        gamePage = trade.close();

        // Save through the menu and continue from disk: the totals are the same
        // and the last trade is not replayed.
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        trade.open();
        trade.verifyTradeRow(DUTCH_GRAIN, "Bought: 63 units", "Gold spent: 252",
                "Sold: 27 units", "Gold received: 54");
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        trade.verifyRevolutionarySource(FRENCH_CLOTH,
                "Revolutionary custom house: 6 units, 60 net gold");
        trade.close();
    }

    @Test
    @Tag("local")
    void TC_02_keyboard_scroll_escape_and_reopen_in_english() throws Exception {
        // Reach the trade tab with Tab and arrow keys only.
        WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();
        final EuropeanTradePage trade = new EuropeanTradePage(getContext());
        trade.openWithKeyboard();
        trade.verifyTradeTabSelectedAndFocused(true);
        trade.verifyTradeTabLabel("European trade");
        trade.verifyTradeTabInsideWindow();

        // The first accounts are shown; the Spanish account is below the fold.
        trade.verifyTradeRow(ENGLISH_ORE, "Bought: 2 units", "Gold spent: 10",
                "Sold: 3 units", "Gold received: 12");
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        trade.verifyRowFullyVisible(SPANISH_GRAPES, false);
        trade.verifyReportScrolled(false);
        trade.captureDialog("europeanTrade", "trade-report-en-top.png");

        // Scroll to the lowest rival account and read its counters and source.
        trade.scrollReportDown();
        trade.verifyReportScrolled(true);
        trade.verifyRowFullyVisible(SPANISH_GRAPES, true);
        trade.verifyTradeRow(SPANISH_GRAPES, "Bought: 1 units", "Gold spent: 6",
                "Sold: 5 units", "Gold received: 20");
        trade.verifyRevolutionarySource(SPANISH_GRAPES,
                "Revolutionary custom house: 2 units, 8 net gold");
        trade.captureDialog("europeanTrade", "trade-report-en-scrolled.png");

        // Escape returns to the map; reopening by keyboard keeps every total.
        trade.closeWithEscape();
        trade.openWithKeyboard();
        trade.verifyTradeTabSelectedAndFocused(true);
        trade.verifyTradeRow(ENGLISH_ORE, "Bought: 2 units", "Gold spent: 10",
                "Sold: 3 units", "Gold received: 12");
        trade.verifyTradeRow(FRENCH_CLOTH, "Bought: 4 units", "Gold spent: 48",
                "Sold: 10 units", "Gold received: 110");
        trade.verifyTradeRow(SPANISH_GRAPES, "Bought: 1 units", "Gold spent: 6",
                "Sold: 5 units", "Gold received: 20");
        trade.verifyRevolutionarySource(SPANISH_GRAPES,
                "Revolutionary custom house: 2 units, 8 net gold");
        trade.verifyNoRevolutionarySource(ENGLISH_ORE);
    }

    @Test
    @Tag("local")
    void TC_03_czech_report_labels_keyboard_and_screenshots() throws Exception {
        // Switch to Czech before loading the game.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        WaitForAsyncUtils.waitForFxEvents();
        final EuropeanTradePage trade = new EuropeanTradePage(getContext());

        // The tab is reached by keyboard and named in Czech.
        trade.openWithKeyboard();
        trade.verifyTradeTabSelectedAndFocused(true);
        trade.verifyTradeTabLabel("Evropský obchod");
        trade.verifyTradeTabInsideWindow();

        // All four counters and the revolutionary source are Czech.
        trade.verifyTradeRow(FRENCH_CLOTH, "Nakoupeno: 4 ks",
                "Utraceno zlata: 48", "Prodáno: 10 ks", "Přijato zlata: 110");
        trade.verifyRevolutionarySource(FRENCH_CLOTH,
                "Revoluční celnice: 6 ks, 60 čistého zlata");
        trade.captureDialog("europeanTrade", "trade-report-cs-top.png");

        // The lowest rival account is readable after scrolling.
        trade.scrollReportDown();
        trade.verifyRowFullyVisible(SPANISH_GRAPES, true);
        trade.verifyTradeRow(SPANISH_GRAPES, "Nakoupeno: 1 ks",
                "Utraceno zlata: 6", "Prodáno: 5 ks", "Přijato zlata: 20");
        trade.verifyRevolutionarySource(SPANISH_GRAPES,
                "Revoluční celnice: 2 ks, 8 čistého zlata");
        trade.captureDialog("europeanTrade", "trade-report-cs-scrolled.png");

        // Escape returns and the totals survive a keyboard reopen.
        trade.closeWithEscape();
        trade.openWithKeyboard();
        trade.verifyTradeRow(FRENCH_CLOTH, "Nakoupeno: 4 ks",
                "Utraceno zlata: 48", "Prodáno: 10 ks", "Přijato zlata: 110");
    }

}
