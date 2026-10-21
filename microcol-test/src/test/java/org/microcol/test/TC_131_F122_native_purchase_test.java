package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeMarketPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A second stage after a confirmed sale or gift (R-0161): the emptied wagon
 * still buys one offered good, the pieces are chosen with the keyboard, and the
 * exact stock, cargo and gold move once. A cancelled purchase changes nothing.
 * The one discount request is drawn once and kept through reopening, changing
 * the pieces and Save/Continue, and a confirmed purchase closes its stage. The
 * model rules for the stock, room and gold bounds, the stale terms and the
 * rounding are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_131_F122_native_purchase_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native market tests. */
    private static final File FIXTURE = new File(
            "src/test/scenarios/F106-native-turns.microcol");

    /** The Aztec capital, with corn and tobacco on offer. */
    private static final Location CAPITAL = Location.of(23, 15);

    /** Ordinary scout, land square west of the capital. */
    private static final int SCOUT_ID = 300;
    private static final Location SCOUT_AT = Location.of(22, 15);

    /** Loaded wagon of fifty corn, land square north of the capital. */
    private static final int WAGON_ID = 301;
    private static final Location WAGON_AT = Location.of(23, 14);

    /** Loaded caravel docked in New Amsterdam beside the capital's square. */
    private static final int SHIP_ID = 302;
    private static final Location SHIP_AT = Location.of(24, 16);

    private static final GoodsType CORN = GoodsType.CORN;

    /** The whole load of the wagon. */
    private static final int LOAD = 50;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_emptied_wagon_buys_after_a_confirmed_sale_and_cancel_changes_nothing()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        // The scout's land introduction makes the tribe trade with the wagon.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        visit.escape();
        visit.verifyChooserClosed();

        // Before any sale the second stage is not offered.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.verifyPurchaseShown(false);

        // Selling the whole load empties the wagon and opens the second stage.
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, LOAD);
        market.sell();
        market.confirmDeal();
        market.verifyHeld(WAGON_ID, CORN, 0);
        market.verifyReceived(CAPITAL, CORN, LOAD);
        market.verifyPurchaseShown(true);
        market.verifyPurchaseOffers(WAGON_ID, CAPITAL);

        // The emptied wagon still buys, for the goods the chooser selects
        // first.
        final GoodsType bought = market.purchaseGoods();
        final int stock = market.stock(CAPITAL, bought);
        final int treasury = market.treasury(CAPITAL);
        final int carried = market.carried(WAGON_ID, bought);
        final int gold = market.gold();
        market.choosePurchasePieces(5);
        market.verifyPurchaseQuantity(5);
        market.verifyPurchaseOffered(WAGON_ID, CAPITAL, bought, 5);
        final int price = market.purchaseGold(WAGON_ID, CAPITAL, bought, 5);

        // Cancelling the confirmation keeps the goods, the stock and the gold.
        market.buy();
        market.verifyPurchaseConfirmation(WAGON_ID, CAPITAL, bought, 5);
        market.backFromPurchase();
        market.verifyPurchaseOffered(WAGON_ID, CAPITAL, bought, 5);
        market.verifyHeld(WAGON_ID, bought, carried);
        market.verifyGold(gold);
        market.verifyStock(CAPITAL, bought, stock);

        // Confirmed, the purchase moves the stock, the goods and the gold once.
        market.buy();
        market.confirmPurchase();
        market.verifyPurchaseMessage(bought, 5, price);
        market.closePurchaseMessage();
        market.verifyHeld(WAGON_ID, bought, carried + 5);
        market.verifyGold(gold - price);
        market.verifyStock(CAPITAL, bought, stock - 5);
        market.verifyTreasury(CAPITAL, treasury + price);
        market.verifyPurchaseShown(false);
        visit.escape();
        visit.verifyChooserClosed();

        // Save and Continue keep the purchase: the stage is gone for good.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        market.verifyHeld(WAGON_ID, bought, carried + 5);
        market.verifyGold(gold - price);
        market.verifyStock(CAPITAL, bought, stock - 5);
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyPurchaseShown(false);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_02_partial_purchase_keeps_one_discount_request_through_reopening_and_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        visit.escape();
        visit.verifyChooserClosed();

        // Thirty pieces are sold with the keyboard; twenty remain in the wagon.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.chooseSalePieces(30);
        market.verifySaleQuantity(30);
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, 30);
        market.sell();
        market.confirmDeal();
        market.verifyHeld(WAGON_ID, CORN, LOAD - 30);
        market.verifyReceived(CAPITAL, CORN, 30);
        market.verifyPurchaseShown(true);

        // Four pieces are chosen with the keyboard, then the one request is
        // made.
        final GoodsType bought = market.purchaseGoods();
        final int stock = market.stock(CAPITAL, bought);
        final int carried = market.carried(WAGON_ID, bought);
        final int gold = market.gold();
        market.choosePurchasePieces(4);
        market.verifyPurchaseQuantity(4);
        market.purchaseHaggle();
        market.verifyPurchaseHaggleResultShown(WAGON_ID, CAPITAL, bought, 4);
        market.verifyPurchasePriceRule(WAGON_ID, CAPITAL, bought, 4);
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-purchase-en.png");

        // Changing the pieces draws nothing: the same result comes back.
        market.choosePurchasePieces(2);
        market.verifyPurchaseHaggleResultShown(WAGON_ID, CAPITAL, bought, 2);
        market.choosePurchasePieces(4);
        market.verifyPurchaseHaggleResultShown(WAGON_ID, CAPITAL, bought, 4);

        // Escape keeps the stage and its request; reopening never rerolls.
        visit.escape();
        visit.verifyChooserClosed();
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyPurchaseHaggleResultShown(WAGON_ID, CAPITAL, bought, 4);
        visit.escape();
        visit.verifyChooserClosed();

        // Save and Continue keep the same stage and the same result.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyPurchaseHaggleResultShown(WAGON_ID, CAPITAL, bought, 4);

        // The confirmed purchase pays the quoted gold once and moves the goods.
        market.verifyPurchasePriceRule(WAGON_ID, CAPITAL, bought, 4);
        final int price = market.purchaseGold(WAGON_ID, CAPITAL, bought, 4);
        market.buy();
        market.confirmPurchase();
        market.verifyPurchaseMessage(bought, 4, price);
        market.closePurchaseMessage();
        market.verifyHeld(WAGON_ID, bought, carried + 4);
        market.verifyGold(gold - price);
        market.verifyStock(CAPITAL, bought, stock - 4);
        market.verifyPurchaseShown(false);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_04_declined_purchase_closes_the_stage_and_changes_nothing_across_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        visit.escape();
        visit.verifyChooserClosed();

        // Thirty pieces are sold, and the second stage opens for the wagon.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.chooseSalePieces(30);
        market.verifySaleQuantity(30);
        market.sell();
        market.confirmDeal();
        market.verifyHeld(WAGON_ID, CORN, LOAD - 30);
        market.verifyReceived(CAPITAL, CORN, 30);
        market.verifyPurchaseShown(true);

        final GoodsType bought = market.purchaseGoods();
        final int stock = market.stock(CAPITAL, bought);
        final int treasury = market.treasury(CAPITAL);
        final int carried = market.carried(WAGON_ID, bought);
        final int gold = market.gold();

        // Declining buys nothing: the stage closes with its own button.
        market.choosePurchasePieces(4);
        market.declinePurchase();
        market.verifyPurchaseDeclined();
        market.closePurchaseMessage();
        market.verifyPurchaseShown(false);
        market.verifyHeld(WAGON_ID, bought, carried);
        market.verifyGold(gold);
        market.verifyStock(CAPITAL, bought, stock);
        market.verifyTreasury(CAPITAL, treasury);
        market.verifyReceived(CAPITAL, CORN, 30);
        visit.escape();
        visit.verifyChooserClosed();

        // Save and Continue keep the declined stage closed: no purchase
        // returns.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyPurchaseShown(false);
        market.verifyHeld(WAGON_ID, bought, carried);
        market.verifyGold(gold);
        market.verifyStock(CAPITAL, bought, stock);
        market.verifyTreasury(CAPITAL, treasury);
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_03_czech_purchase_header_and_chooser_fit() throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        visit.escape();
        visit.verifyChooserClosed();

        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.sell();
        market.confirmDeal();
        market.verifyPurchaseShown(true);
        market.verifyCzechPurchaseHeader();
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-purchase-cs.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

}
