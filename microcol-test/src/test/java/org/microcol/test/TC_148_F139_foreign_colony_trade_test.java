package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.page.ForeignColonyTradePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-071/081/100: under Jan de Witt a loaded wagon or frigate beside the French
 * colony Quebec sells or barters goods at the colony's Europe prices. The trade
 * is an explicit choice: cancelling moves nothing, a confirmed trade moves
 * goods and gold once and survives Continue, and the carrier never moves,
 * attacks or captures. Without the founder, or when the colony or the carrier
 * cannot take part, the dialog explains and nothing moves. A full wagon can
 * barter only because the goods it gives free a whole hold.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_148_F139_foreign_colony_trade_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = ForeignColonyTradePage.SOURCE;
    private static final String FRENCH = "France";
    private static final String GIVE_TWO_CORN = "You give: 2 Corn.";
    private static final String GIVE_TWO_SUGAR = "You give: 2 Sugar cane.";
    private static final String RECEIVE_NOTHING = "You receive no goods back.";
    private static final String RECEIVE_TOBACCO = "You receive: 1 Tobacco.";
    private static final String NO_NET = "";
    private static final String NO_FOUNDER = "Jan de Witt must be in your Congress to trade with a foreign colony.";
    private static final String FRENCH_CANNOT_PAY = "The colony's nation cannot pay this much gold.";
    private static final String GIVE_TWO_CORN_CZ = "Dáte: 2 Jídlo.";
    private static final String RECEIVE_NOTHING_CZ = "Zpět nedostanete žádné zboží.";
    private static final int GOLD = ForeignColonyTradePage.START_GOLD;
    private static final int WAGON = ForeignColonyTradePage.WAGON_ID;
    private static final int SHIP = ForeignColonyTradePage.SHIP_ID;
    private static final int WAGON_CORN = 4;
    private static final int SHIP_CORN = 2;
    private static final int COLONY_CORN = 20;
    private static final int COLONY_TOBACCO = 30;
    private static final int FULL_HOLD = 100;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_wagon_sale_cancel_keeps_goods_then_confirm_pays_the_bid_once_and_continue()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-sale", true, false);
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        // The wagon's visit opens the choice as a sale: nothing is received.
        trade.openWagonTrade();
        trade.verifyDialogOpen(true);
        trade.verifyReceiveControlsEnabled(false);
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.CORN, 2);
        trade.verifyLines(GIVE_TWO_CORN, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(gold));
        trade.verifyConfirmEnabled(true);
        // Cancelling changes neither side and does not move or capture.
        trade.cancel();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD);
        trade.verifyGold(FRENCH, GOLD);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Confirming pays the exact bid once and moves the corn to the colony.
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN - 2);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN + 2);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Continue keeps the one result; the sale is not repeated.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN - 2);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN + 2);
    }

    @Test
    @Tag("local")
    void TC_02_wagon_barter_settles_the_difference_in_gold_for_both_goods()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-barter", true, false);
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        // Ticking the barter box enables the goods received.
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        trade.setBarter(true);
        trade.verifyReceiveControlsEnabled(true);
        trade.chooseReceive(GoodsType.TOBACCO);
        trade.setReceiveAmount(1);
        int net = trade.expectedBarterNet(GoodsType.CORN, 2, GoodsType.TOBACCO,
                1);
        trade.verifyLines(GIVE_TWO_CORN, RECEIVE_TOBACCO,
                ForeignColonyTradePage.netEnglish(net));
        trade.verifyConfirmEnabled(true);
        // Confirming moves goods both ways and settles only the difference.
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN - 2);
        trade.verifyCarried(WAGON, GoodsType.TOBACCO, 1);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN + 2);
        trade.verifyColonyStock(GoodsType.TOBACCO, COLONY_TOBACCO - 1);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Continue keeps the exchange exactly once.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.TOBACCO, 1);
        trade.verifyColonyStock(GoodsType.TOBACCO, COLONY_TOBACCO - 1);
    }

    @Test
    @Tag("local")
    void TC_03_frigate_at_sea_sells_through_the_same_dialog() throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-ship", true, false);
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        trade.openShipTrade();
        trade.verifyDialogOpen(true);
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.CORN, 2);
        trade.verifyLines(GIVE_TWO_CORN, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(gold));
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(SHIP, GoodsType.CORN, SHIP_CORN - 2);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN + 2);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
    }

    @Test
    @Tag("local")
    void TC_04_without_jan_de_witt_the_dialog_explains_and_nothing_moves()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-no-founder", false, false);
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        trade.openWagonTrade();
        trade.verifyDialogOpen(true);
        trade.verifyFeedback(NO_FOUNDER);
        trade.verifyLines("", "", "");
        trade.verifyConfirmEnabled(false);
        trade.cancel();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD);
        trade.verifyGold(FRENCH, GOLD);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
    }

    @Test
    @Tag("local")
    void TC_05_rebellion_closes_europe_but_the_foreign_colony_trade_stays_open()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-rebellion", true, true);
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.CORN, 2);
        trade.verifyConfirmEnabled(true);
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN - 2);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN + 2);
        trade.verifyVisitorsUnmoved();
        // Continue keeps the rebel trade exactly once.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN - 2);
    }

    @Test
    @Tag("local")
    void TC_06_czech_dialog_uses_localized_lines() throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepare("f148-czech", true, false);
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.CORN, 2);
        trade.verifyLines(GIVE_TWO_CORN_CZ, RECEIVE_NOTHING_CZ,
                ForeignColonyTradePage.netCzech(gold));
        trade.cancel();
        trade.verifyGold("Dutch", GOLD);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN);
    }

    @Test
    @Tag("local")
    void TC_07_full_wagon_barters_because_giving_its_goods_frees_a_whole_hold()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepareFullWagon("f148-full-wagon");
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        // Both holds are taken, yet the barter is offered: the two sugar canes
        // leave their hold completely, so the tobacco has room.
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.SUGAR);
        trade.setGiveAmount(2);
        trade.setBarter(true);
        trade.chooseReceive(GoodsType.TOBACCO);
        trade.verifyReceiveMaximum(COLONY_TOBACCO);
        trade.setReceiveAmount(1);
        int net = trade.expectedBarterNet(GoodsType.SUGAR, 2, GoodsType.TOBACCO,
                1);
        trade.verifyLines(GIVE_TWO_SUGAR, RECEIVE_TOBACCO,
                ForeignColonyTradePage.netEnglish(net));
        trade.verifyConfirmEnabled(true);
        trade.confirm();
        trade.verifyDialogOpen(false);
        // Goods and gold are conserved: the freed hold now holds one tobacco.
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.SUGAR, 0);
        trade.verifyCarried(WAGON, GoodsType.TOBACCO, FULL_HOLD + 1);
        trade.verifyColonyStock(GoodsType.SUGAR, 2);
        trade.verifyColonyStock(GoodsType.TOBACCO, COLONY_TOBACCO - 1);
        trade.verifyVisitorsUnmoved();
    }

    @Test
    @Tag("local")
    void TC_08_empty_french_treasury_refuses_the_sale_with_an_explanation()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepareEmptyFrenchTreasury("f148-poor-colony");
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.CORN);
        trade.setGiveAmount(2);
        // The colony's nation cannot pay: the choice is shown but cannot be
        // confirmed, and no balance is promised.
        trade.verifyLines(GIVE_TWO_CORN, RECEIVE_NOTHING, NO_NET);
        trade.verifyFeedback(FRENCH_CANNOT_PAY);
        trade.verifyConfirmEnabled(false);
        trade.cancel();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD);
        trade.verifyGold(FRENCH, 0);
        trade.verifyCarried(WAGON, GoodsType.CORN, WAGON_CORN);
        trade.verifyColonyStock(GoodsType.CORN, COLONY_CORN);
        trade.verifyVisitorsUnmoved();
    }
}
