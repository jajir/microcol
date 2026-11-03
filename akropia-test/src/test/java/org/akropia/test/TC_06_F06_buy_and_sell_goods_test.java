package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.page.BuyUnitScreen;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.ScreenMarketBuy;
import org.akropia.page.ScreenMarketSell;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_06_F06_buy_and_sell_goods_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F06-buy-and-sell-goods.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port
        EuropePortScreen europePort = gamePage.openEuroperPort();

        // The empty harbor keeps all four labeled drop areas available.
        final org.akropia.page.EuropeHarborPage harbor = new org.akropia.page.EuropeHarborPage(
                getContext());
        harbor.resize(1280, 900);
        harbor.verifyGroups(0);
        harbor.verifyHorizontalScrollbars(false);
        harbor.verifyVoyageHeadingSpacing();
        harbor.verifyImmigrationAppearance();
        harbor.verifyGoodsPricesCentered();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0343-harbor-empty.png");

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
        harbor.verifyHorizontalScrollbars(false);
        harbor.verifyLayoutAndNativeImages();

        // Verify that ship is empty.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));

        new InGameLayoutPage(getContext())
                .saveEvidence("r0307-europe-before-trade.png");

        // Cancel a purchase choice; hold and treasury stay unchanged.
        europePort.buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0)
                .clickOnCancel();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(95000);

        /*
         * Buy food, with pressed control, limit food to 63, verify that food is
         * in cargo slot.
         */
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        final InGameLayoutPage frame = new InGameLayoutPage(getContext());
        frame.verifyFrame(95000,
                getContext().getModel().getCalendar().getCurrentYear());
        frame.saveEvidence("r0307-market-buy.png");
        screenMarketBuy.verifyNumberOfSliders(1);
        screenMarketBuy.selectValueAtSlider(63);
        screenMarketBuy.verifySelectedMarketQuantity(63);
        screenMarketBuy.verifyQuotedPrice("4 gold", "252 gold");
        screenMarketBuy.clickOnBuy();

        // Verify that in first cargo slot is 63 grain.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 63));

        // Verify that amount of gold dropped.
        europePort.verifyGold(94748);

        /*
         * Sell food with pressed control, sell just 27 food, verify that 26
         * remains in cargo slot.
         */
        ScreenMarketSell screenMarketSell = europePort
                .sellGoodsWithPressedControl(0, 0);
        frame.verifyFrame(94748,
                getContext().getModel().getCalendar().getCurrentYear());
        frame.saveEvidence("r0307-market-sell.png");
        screenMarketSell.verifyNumberOfSliders(1);
        screenMarketSell.selectValueAtSlider(27);
        screenMarketSell.verifySelectedMarketQuantity(27);
        screenMarketSell.verifyQuotedPrice("2 gold", "54 gold");
        screenMarketSell.clickOnSell();

        // Verify that in first cargo slot is 36 grain.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 36));

        // Verify that amount of gold raised.
        europePort.verifyGold(94802);

        // Close Europe port
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods_stale_purchase_reduced_capacity() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));

        // Open the purchase choice; the offer is the full 100 units.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);

        // Another action fills half of the hold while the choice is open.
        europePort.competingPurchase(0, GoodsType.GRAIN, 50);

        // Confirming the stale 100 units is rejected without any charge.
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsTransferChangedWarning(
                "The cargo hold changed and the purchase was not made. Nothing was charged.");
        europePort.closeGoodsTransferWarning();

        // Europe is shown again: 50 grain at 4 gold were charged to the
        // competing purchase only (95000 - 200).
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 50));
        europePort.verifyGold(94800);

        // Choose again; only the current free space of 50 is offered.
        screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(50);
        screenMarketBuy.verifyQuotedPrice("4 gold", "200 gold");
        screenMarketBuy.clickOnBuy();

        // Exactly the offered quantity was bought and charged.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94600);

        // Close Europe port, save through the menu and continue the game.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        // The continued game keeps the exact cargo and treasury.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94600);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods_stale_purchase_incompatible_commodity()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));

        // Open the grain purchase choice for the empty first slot.
        ScreenMarketBuy screenMarketBuy = europePort
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);

        // Another action stores different goods in that slot.
        europePort.competingPurchase(0, GoodsType.WINE, 10);

        // Confirming grain is rejected; hold and treasury stay unchanged.
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsTransferChangedWarning(
                "The cargo hold changed and the purchase was not made. Nothing was charged.");
        europePort.closeGoodsTransferWarning();

        // Only the competing purchase of 10 wine at 12 gold was charged
        // (95000 - 120).
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.WINE, 10));
        europePort.verifyGold(94880);

        // Close Europe port, save through the menu and continue the game.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        // The continued game keeps the exact cargo and treasury.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.WINE, 10));
        europePort.verifyGold(94880);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_moving_goods_buy_cargo_and_move_it_to_occupied_cargo_slot()
            throws Exception {
        // open Akropia and load defined game
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
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));

        // Quick-buy 100 of grain into the first cargo slot with the platform
        // key held at the drop.
        europePort.quickBuyGoods(0, 0);
        europePort.verifyNoPurchaseChoice();
        europePort
                .verifyStatusMessage("Grain: bought 100 pieces for 400 gold.");

        // Verify that in first cargo slot is 100 grain.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));

        // Verify that amount of gold dropped.
        europePort.verifyGold(94600);

        // Cancel a partial transfer to an empty slot; zero goods must not
        // occupy it.
        europePort.moveCargoWithPressedControl(0, 1).cancel();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyCargoSlotEmpty(0, 1);
        europePort.verifyGold(94600);

        // Drag more grain onto the full first cargo slot; no choice opens and
        // the status bar explains why.
        europePort.buyGoodsAndMoveItToCargoSlot(0, 0);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyStatusMessage(
                "Grain: this cargo hold is full. Nothing was bought.");

        // Verify that in first cargo slot is 100 grain.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));

        // More than 100 goods can't be in one cargo slot.
        // Verify that amount of gold is not changed from previous state.
        europePort.verifyGold(94600);

        // Close Europe port
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_purchase_choice_explains_gold_limit_tax_and_gold_left()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // The Crown taxes 10% and the treasury holds 200 gold: 45 grain cost
        // 198 gold, 46 would cost 202.
        europePort.changeCrownTax(10);
        europePort.changeTreasury(200);
        europePort.verifyGold(200);

        // A normal drag to the empty hold opens the choice with 45 preselected,
        // although the hold has room for 100; nothing is bought yet.
        ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(45);
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(200);

        // The choice explains the gold limit and names the numeric maximum.
        screenMarketBuy.verifyLimitExplanation(
                "You can buy at most 45 pieces. You don't have enough gold for more.");
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 45 pcs");
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 45 pieces for ...", "4 gold", "180 gold",
                "King tax is 10%. It's totally ...", "18 gold", "198 gold",
                "2 gold");
        screenMarketBuy.verifyQuickBuyHint(
                "To buy the maximum amount immediately, hold ⌘ while dragging.",
                "To buy the maximum amount immediately, hold Ctrl while dragging.",
                "To buy the maximum amount immediately, hold Command while dragging.");
        screenMarketBuy.verifyBuyEnabled(true);

        // Every text is drawn in full in the wide and in the narrow window.
        screenMarketBuy.resize(1280, 900);
        screenMarketBuy.verifyTextShownInFull();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-purchase-gold-limit-wide.png");
        screenMarketBuy.resize(1000, 760);
        screenMarketBuy.verifyTextShownInFull();
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 45 pcs");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-purchase-gold-limit-narrow.png");

        // Reduce the amount to 30; every detail follows and nothing is bought.
        screenMarketBuy.selectValueAtSlider(30);
        screenMarketBuy.verifySelectedMarketQuantity(30);
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 30 pieces for ...", "4 gold", "120 gold",
                "King tax is 10%. It's totally ...", "12 gold", "132 gold",
                "68 gold");
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(200);

        // Confirm; exactly the shown amount is bought for the shown total.
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 30));
        europePort.verifyGold(68);
        europePort.verifyStatusMessage("Grain: bought 30 pieces for 132 gold.");

        // Close Europe port, save through the menu and continue the game.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        // The continued game keeps the exact cargo and treasury.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 30));
        europePort.verifyGold(68);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_purchase_choice_explains_hold_and_both_limits()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // Buy 55 grain through the choice, leaving room for 45 more.
        ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifyLimitExplanation(
                "This cargo hold has room for 100 more pieces.");
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 100 pcs");
        screenMarketBuy.selectValueAtSlider(55);
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 55));
        europePort.verifyGold(94780);

        // With plenty of gold only the hold limits the choice.
        screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(45);
        screenMarketBuy.verifyLimitExplanation(
                "This cargo hold has room for 45 more pieces.");
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 45 pcs");
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 45 pieces for ...", "4 gold", "180 gold",
                "King tax is 0%. It's totally ...", "0 gold", "180 gold",
                "94600 gold");

        // Back returns to the harbor without a trade.
        screenMarketBuy.clickOnCancel();
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 55));
        europePort.verifyGold(94780);

        // With a 10% tax, 200 gold pay exactly the 45 pieces that still fit.
        europePort.changeCrownTax(10);
        europePort.changeTreasury(200);
        screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(45);
        screenMarketBuy.verifyLimitExplanation(
                "You can buy at most 45 pieces. You don't have enough gold for more, and this cargo hold has no room for more.");
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 45 pieces for ...", "4 gold", "180 gold",
                "King tax is 10%. It's totally ...", "18 gold", "198 gold",
                "2 gold");
        screenMarketBuy.verifyTextShownInFull();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-purchase-both-limits.png");

        // Escape returns to the harbor without a trade.
        screenMarketBuy.cancelWithEscape();
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 55));
        europePort.verifyGold(200);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_purchase_choice_zero_cannot_be_confirmed_and_keyboard_buys()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // Select nothing; Buy is disabled and pressing it buys nothing.
        ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.selectValueAtSlider(0);
        screenMarketBuy.verifySelectedMarketQuantity(0);
        screenMarketBuy.verifyBuyEnabled(false);
        screenMarketBuy.verifyPurchaseDetails("You are buying 0 pieces for ...",
                "4 gold", "0 gold", "King tax is 0%. It's totally ...",
                "0 gold", "0 gold", "95000 gold");
        screenMarketBuy.clickOnBuy();
        screenMarketBuy.verifySelectedMarketQuantity(0);
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(95000);
        screenMarketBuy.clickOnCancel();

        // Open the choice again and use only the keyboard: lower 100 to 63
        // with the arrow key and press Buy with the space bar.
        screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.reduceQuantityWithKeyboard(37);
        screenMarketBuy.verifySelectedMarketQuantity(63);
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 63 pieces for ...", "4 gold", "252 gold",
                "King tax is 0%. It's totally ...", "0 gold", "252 gold",
                "94748 gold");
        screenMarketBuy.buyWithKeyboard();

        // Exactly the shown amount was bought for the shown total.
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 63));
        europePort.verifyGold(94748);
        europePort.verifyStatusMessage("Grain: bought 63 pieces for 252 gold.");
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_purchase_blocked_drops_explain_and_keep_state()
            throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // Another action stores 10 wine in the first hold (95000 - 120).
        europePort.competingPurchase(0, GoodsType.WINE, 10);
        europePort.verifyGold(94880);

        // Grain dropped on the wine hold opens no choice and explains why.
        europePort.buyGoodsAndMoveItToCargoSlot(0, 0);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyStatusMessage(
                "Grain: this cargo hold holds other cargo or can't be used now. Nothing was bought.");
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.WINE, 10));
        europePort.verifyGold(94880);

        // Another action fills the second hold with 100 grain (94880 - 400).
        europePort.competingPurchase(1, GoodsType.GRAIN, 100);
        europePort.verifyGold(94480);

        // Grain dropped on the full hold opens no choice and explains why.
        europePort.quickBuyGoods(0, 1);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyStatusMessage(
                "Grain: this cargo hold is full. Nothing was bought.");
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94480);

        // With 3 gold not one piece of grain at 4 gold can be bought.
        europePort.changeTreasury(3);
        europePort.buyGoodsAndMoveItToCargoSlot(0, 2);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyStatusMessage(
                "Grain: you don't have enough gold for even one piece. Nothing was bought.");
        europePort.verifyCargoSlotEmpty(0, 2);
        europePort.verifyGold(3);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_purchase_stale_tax_and_gold_are_rejected() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // Open the choice for 100 grain at 400 gold.
        ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 100 pieces for ...", "4 gold", "400 gold",
                "King tax is 0%. It's totally ...", "0 gold", "400 gold",
                "94600 gold");

        // The Crown raises the tax while the choice is open.
        europePort.changeCrownTax(10);

        // Confirming the stale price is rejected without any charge.
        screenMarketBuy.clickOnBuy();
        europePort.verifyEuropePriceChangedWarning(
                "The European price changed and the trade was not made. Nothing was charged or moved.");
        europePort.closeGoodsTransferWarning();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(95000);

        // A new choice shows the current taxed total.
        screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 100 pieces for ...", "4 gold", "400 gold",
                "King tax is 10%. It's totally ...", "40 gold", "440 gold",
                "94560 gold");

        // The treasury changes while the choice is open, still enough gold.
        europePort.changeTreasury(90000);

        // Confirming the stale treasury is rejected without any charge.
        screenMarketBuy.clickOnBuy();
        europePort.verifyEuropeGoldChangedWarning(
                "Your gold changed and the purchase was not made. Nothing was charged.");
        europePort.closeGoodsTransferWarning();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(90000);

        // The next valid choice buys exactly the shown amount and total.
        screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.verifyPurchaseDetails(
                "You are buying 100 pieces for ...", "4 gold", "400 gold",
                "King tax is 10%. It's totally ...", "40 gold", "440 gold",
                "89560 gold");
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(89560);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_quick_buy_uses_the_key_held_at_the_drop() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port and buy a ship.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");

        // The Crown taxes 10% and the treasury holds 200 gold.
        europePort.changeCrownTax(10);
        europePort.changeTreasury(200);

        // Command (macOS) or Ctrl held for the whole drag buys the current
        // maximum of 45 at once, without a choice.
        europePort.quickBuyGoods(0, 0);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 45));
        europePort.verifyGold(2);
        europePort.verifyStatusMessage("Grain: bought 45 pieces for 198 gold.");

        // The key pressed only over the hold, before the drop, also buys the
        // maximum: here the full hold of 100 for 440 gold.
        europePort.changeTreasury(95000);
        europePort.quickBuyWithKeyPressedDuringDrag(0, 1);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94560);
        europePort
                .verifyStatusMessage("Grain: bought 100 pieces for 440 gold.");

        // The key released before the drop opens the normal choice instead.
        ScreenMarketBuy screenMarketBuy = europePort
                .openPurchaseChoiceAfterReleasingQuickBuyKey(0, 2);
        screenMarketBuy.verifySelectedMarketQuantity(100);
        screenMarketBuy.clickOnCancel();
        europePort.verifyCargoSlotEmpty(0, 2);
        europePort.verifyGold(94560);

        // Close Europe port, save through the menu and continue the game.
        gamePage = europePort.close();
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        europePort = gamePage.openEuroperPort();

        // The continued game keeps both quick purchases once.
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 45));
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 100));
        europePort.verifyGold(94560);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_sale_and_cargo_moves_keep_their_gestures() throws Exception {
        // open Akropia and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        WaitForAsyncUtils.waitForFxEvents();

        // Open Europe port, buy a ship and 63 grain through the choice.
        EuropePortScreen europePort = gamePage.openEuroperPort();
        europePort = europePort.openBuyUnitScreen().buyUnitByName("Galleon");
        ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0, 0);
        screenMarketBuy.selectValueAtSlider(63);
        screenMarketBuy.clickOnBuy();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 63));
        europePort.verifyGold(94748);

        // A plain drag between holds still moves all goods at once.
        europePort.moveCargo(0, 1);
        europePort.verifyCargoSlotEmpty(0, 0);
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 63));

        // The special move still asks for the amount; cancel keeps the cargo.
        europePort.moveCargoWithPressedControl(1, 0).cancel();
        europePort.verifyCargoSlotEmpty(0, 0);
        europePort.verifyGoodsInShip(1, Goods.of(GoodsType.GRAIN, 63));

        // A plain drag to the market still sells everything at bid 2 without
        // a choice.
        europePort.sellGoods(1, 0);
        europePort.verifyNoPurchaseChoice();
        europePort.verifyCargoSlotEmpty(0, 1);
        europePort.verifyGold(94874);
        gamePage = europePort.close();
    }

    @Test
    @Tag("local")
    void TC_06_czech_purchase_choice_in_wide_and_narrow_window()
            throws Exception {
        // Select Czech through the existing settings screen before loading.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        EuropePortScreen europePort = settings.goBack().loadGame()
                .openEuroperPort("Domovský přístav");

        // Buy a ship; the Crown taxes 10% and the treasury holds 200 gold.
        europePort = europePort.openBuyUnitScreen().buyUnitByName("galeona",
                "Domovský přístav");
        europePort.changeCrownTax(10);
        europePort.changeTreasury(200);

        // A normal drag opens the Czech choice with 45 preselected.
        final ScreenMarketBuy screenMarketBuy = europePort.openPurchaseChoice(0,
                0);
        screenMarketBuy.verifySelectedMarketQuantity(45);
        screenMarketBuy.verifyLimitExplanation(
                "Můžeš koupit nejvýše 45 kusů. Na více nemáš dost zlata.");
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 45 ks");
        screenMarketBuy.verifyPurchaseDetails("Chceš koupit 45 kusů, za  ...",
                "4 zlatých", "180 zlatých", "Královská daň je 10%. To je ...",
                "18 zlatých", "198 zlatých", "2 zlatých");
        screenMarketBuy.verifyQuickBuyHint(
                "Pro okamžitý nákup maximálního množství drž při přetahování ⌘.",
                "Pro okamžitý nákup maximálního množství drž při přetahování Ctrl.",
                "Pro okamžitý nákup maximálního množství drž při přetahování Command.");

        // Every Czech text is drawn in full in the wide and narrow window.
        screenMarketBuy.resize(1280, 900);
        screenMarketBuy.verifyTextShownInFull();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-purchase-czech-wide.png");
        screenMarketBuy.resize(1000, 760);
        screenMarketBuy.verifyTextShownInFull();
        screenMarketBuy.verifyMaximumAtSliderEnd("Maximum: 45 ks");
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-purchase-czech-narrow.png");

        // Back (Zpět) returns to the harbor without a trade.
        screenMarketBuy.clickOnCancel();
        europePort.verifyNoPurchaseChoice();
        europePort.verifyGoodsInShip(0, Goods.of(GoodsType.GRAIN, 0));
        europePort.verifyGold(200);
    }

}
