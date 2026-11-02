package org.akropia.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.GoodsType;
import org.akropia.page.ForeignColonyTradePage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
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
public class TC_148_F139_foreign_colony_trade_test extends AbstractAkropiaTest {
    private static final File FIXTURE = ForeignColonyTradePage.SOURCE;
    private static final String FRENCH = "France";
    private static final String GIVE_TWO_GRAIN = "You give: 2 Grain.";
    private static final String GIVE_TWO_OLIVES = "You give: 2 Olives.";
    private static final String RECEIVE_NOTHING = "You receive no goods back.";
    private static final String RECEIVE_GRAPES = "You receive: 1 Grapes.";
    private static final String NO_NET = "";
    private static final String NO_FOUNDER = "Jan de Witt must be in your Congress to trade with a foreign colony.";
    private static final String FRENCH_CANNOT_PAY = "The colony's nation cannot pay this much gold.";
    private static final String GIVE_TWO_GRAIN_CZ = "Dáte: 2 Obilí.";
    private static final String RECEIVE_NOTHING_CZ = "Zpět nedostanete žádné zboží.";
    private static final int GOLD = ForeignColonyTradePage.START_GOLD;
    private static final int WAGON = ForeignColonyTradePage.WAGON_ID;
    private static final int SHIP = ForeignColonyTradePage.SHIP_ID;
    private static final int WAGON_GRAIN = 4;
    private static final int SHIP_GRAIN = 2;
    private static final int COLONY_GRAIN = 20;
    private static final int COLONY_GRAPES = 30;
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.GRAIN, 2);
        trade.verifyLines(GIVE_TWO_GRAIN, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(gold));
        trade.verifyConfirmEnabled(true);
        // Cancelling changes neither side and does not move or capture.
        trade.cancel();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD);
        trade.verifyGold(FRENCH, GOLD);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Confirming pays the exact bid once and moves the grain to the colony.
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN - 2);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN + 2);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Continue keeps the one result; the sale is not repeated.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN - 2);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN + 2);
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        trade.setBarter(true);
        trade.verifyReceiveControlsEnabled(true);
        trade.chooseReceive(GoodsType.GRAPES);
        trade.setReceiveAmount(1);
        int net = trade.expectedBarterNet(GoodsType.GRAIN, 2, GoodsType.GRAPES,
                1);
        trade.verifyLines(GIVE_TWO_GRAIN, RECEIVE_GRAPES,
                ForeignColonyTradePage.netEnglish(net));
        trade.verifyConfirmEnabled(true);
        // Confirming moves goods both ways and settles only the difference.
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN - 2);
        trade.verifyCarried(WAGON, GoodsType.GRAPES, 1);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN + 2);
        trade.verifyColonyStock(GoodsType.GRAPES, COLONY_GRAPES - 1);
        trade.verifyVisitorsUnmoved();
        trade.verifyColonyOwner(FRENCH);
        // Continue keeps the exchange exactly once.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.GRAPES, 1);
        trade.verifyColonyStock(GoodsType.GRAPES, COLONY_GRAPES - 1);
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.GRAIN, 2);
        trade.verifyLines(GIVE_TWO_GRAIN, RECEIVE_NOTHING,
                ForeignColonyTradePage.netEnglish(gold));
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(SHIP, GoodsType.GRAIN, SHIP_GRAIN - 2);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN + 2);
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
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN);
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.GRAIN, 2);
        trade.verifyConfirmEnabled(true);
        trade.confirm();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyGold(FRENCH, GOLD - gold);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN - 2);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN + 2);
        trade.verifyVisitorsUnmoved();
        // Continue keeps the rebel trade exactly once.
        trade.exitAndContinue();
        trade.verifyGold("Dutch", GOLD + gold);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN - 2);
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        int gold = trade.expectedSale(GoodsType.GRAIN, 2);
        trade.verifyLines(GIVE_TWO_GRAIN_CZ, RECEIVE_NOTHING_CZ,
                ForeignColonyTradePage.netCzech(gold));
        trade.cancel();
        trade.verifyGold("Dutch", GOLD);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN);
    }

    @Test
    @Tag("local")
    void TC_07_full_wagon_barters_because_giving_its_goods_frees_a_whole_hold()
            throws Exception {
        ForeignColonyTradePage trade = new ForeignColonyTradePage(getContext());
        trade.prepareFullWagon("f148-full-wagon");
        WelcomePage.of(getContext()).loadGame();
        trade.rememberVisitors();
        // Both holds are taken, yet the barter is offered: the two olives canes
        // leave their hold completely, so the grapes has room.
        trade.openWagonTrade();
        trade.chooseGive(GoodsType.OLIVES);
        trade.setGiveAmount(2);
        trade.setBarter(true);
        trade.chooseReceive(GoodsType.GRAPES);
        trade.verifyReceiveMaximum(COLONY_GRAPES);
        trade.setReceiveAmount(1);
        int net = trade.expectedBarterNet(GoodsType.OLIVES, 2, GoodsType.GRAPES,
                1);
        trade.verifyLines(GIVE_TWO_OLIVES, RECEIVE_GRAPES,
                ForeignColonyTradePage.netEnglish(net));
        trade.verifyConfirmEnabled(true);
        trade.confirm();
        trade.verifyDialogOpen(false);
        // Goods and gold are conserved: the freed hold now holds one grapes.
        trade.verifyGold("Dutch", GOLD + net);
        trade.verifyGold(FRENCH, GOLD - net);
        trade.verifyCarried(WAGON, GoodsType.OLIVES, 0);
        trade.verifyCarried(WAGON, GoodsType.GRAPES, FULL_HOLD + 1);
        trade.verifyColonyStock(GoodsType.OLIVES, 2);
        trade.verifyColonyStock(GoodsType.GRAPES, COLONY_GRAPES - 1);
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
        trade.chooseGive(GoodsType.GRAIN);
        trade.setGiveAmount(2);
        // The colony's nation cannot pay: the choice is shown but cannot be
        // confirmed, and no balance is promised.
        trade.verifyLines(GIVE_TWO_GRAIN, RECEIVE_NOTHING, NO_NET);
        trade.verifyFeedback(FRENCH_CANNOT_PAY);
        trade.verifyConfirmEnabled(false);
        trade.cancel();
        trade.verifyDialogOpen(false);
        trade.verifyGold("Dutch", GOLD);
        trade.verifyGold(FRENCH, 0);
        trade.verifyCarried(WAGON, GoodsType.GRAIN, WAGON_GRAIN);
        trade.verifyColonyStock(GoodsType.GRAIN, COLONY_GRAIN);
        trade.verifyVisitorsUnmoved();
    }
}
