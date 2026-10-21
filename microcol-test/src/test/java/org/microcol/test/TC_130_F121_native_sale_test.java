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
import org.microcol.page.NativeTeachingPage;
import org.microcol.page.NativeVisitPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A loaded wagon sells or gives its corn to the Aztec capital (R-0160). The
 * market shows the exact pieces and gold of the model's terms before any
 * confirmation. One haggle draws its result once and keeps it: cancelling the
 * deal, reopening the chooser and Save/Continue never reroll it. Cancelling a
 * sale or a gift changes no goods or gold; a confirmed sale moves the goods and
 * the gold once, and the tribe records the corn it bought. A confirmed gift
 * moves the goods without any gold. The model rules for the price floor, the
 * caps, the stale terms and the gift reductions are checked in the model tests.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_130_F121_native_sale_test extends AbstractMicroColTest {

    /** Current-schema scenario shared with the native market test. */
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

    /** Pieces the chooser offers first: the whole load the natives can pay. */
    private static final int LOAD = 50;

    private static final String NO_GOODS_EN = "Trade: the carrier must hold goods; passengers alone are not trade goods.";

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_haggled_sale_is_confirmed_once_and_survives_continue()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final NativeMarketPage market = new NativeMarketPage(getContext());
        final NativeVisitPage visit = new NativeVisitPage(getContext());
        final NativeTeachingPage teaching = new NativeTeachingPage(
                getContext());
        final FogPage fog = new FogPage(getContext());
        market.loadFixture(FIXTURE, null, SCOUT_ID, SCOUT_AT, WAGON_ID,
                WAGON_AT, SHIP_ID, SHIP_AT);
        fog.centerFrontier();

        // The scout's land introduction makes the tribe trade with the wagon.
        visit.enterSettlement(SCOUT_AT, CAPITAL);
        market.introduce();
        visit.escape();
        visit.verifyChooserClosed();

        // The open market shows the whole load of corn as the exact offer.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, LOAD);
        final int gold = market.gold();

        // One haggle draws the result; the chooser shows it and the haggle is
        // spent, while the offer carries the terms the model keeps.
        market.haggle();
        market.verifyHaggleResultShown(WAGON_ID, CAPITAL, CORN, LOAD);
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, LOAD);
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-sale-en.png");

        // Cancelling the confirmation keeps the goods and the gold.
        market.sell();
        market.verifySaleConfirmation(WAGON_ID, CAPITAL, CORN, LOAD);
        market.backFromDeal();
        market.verifyHeld(WAGON_ID, CORN, LOAD);
        market.verifyGold(gold);
        market.verifyReceived(CAPITAL, CORN, 0);
        market.verifyHaggleResultShown(WAGON_ID, CAPITAL, CORN, LOAD);

        // Confirmed, the sale moves the goods and the gold exactly once.
        final int price = market.quotedGold(WAGON_ID, CAPITAL, CORN, LOAD);
        market.sell();
        market.confirmDeal();
        market.verifyHeld(WAGON_ID, CORN, 0);
        market.verifyGold(gold + price);
        market.verifyReceived(CAPITAL, CORN, LOAD);
        // The emptied wagon is refused for lack of goods, so no sale controls
        // remain; its confirmed sale only opens the second stage (R-0161).
        market.verifyAdmission(NO_GOODS_EN);
        market.verifyPurchaseShown(true);
        visit.escape();
        visit.verifyChooserClosed();

        // Save and Continue keep the sale: no second sale is possible.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        market.verifyReceived(CAPITAL, CORN, LOAD);
        market.verifyHeld(WAGON_ID, CORN, 0);
        market.verifyGold(gold + price);
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.verifyAdmission(NO_GOODS_EN);
        market.verifyPurchaseShown(true);
        visit.escape();
        visit.verifyChooserClosed();
        teaching.nextTurn();
        market.verifyReceived(CAPITAL, CORN, LOAD);
    }

    @Test
    @Tag("local")
    void TC_02_gift_pays_no_gold_and_a_cancelled_gift_changes_nothing()
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

        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        final int gold = market.gold();
        market.verifyGiftOffered(WAGON_ID, CAPITAL, CORN, LOAD);

        // Backing out of the gift confirmation changes nothing.
        market.gift();
        market.verifyGiftConfirmation(CORN, LOAD);
        market.backFromDeal();
        market.verifyHeld(WAGON_ID, CORN, LOAD);
        market.verifyGold(gold);
        market.verifyReceived(CAPITAL, CORN, 0);

        // The confirmed gift moves the corn, and no gold changes hands.
        market.gift();
        market.confirmDeal();
        market.verifyHeld(WAGON_ID, CORN, 0);
        market.verifyGold(gold);
        market.verifyReceived(CAPITAL, CORN, LOAD);
        visit.escape();
        visit.verifyChooserClosed();

        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        market.verifyReceived(CAPITAL, CORN, LOAD);
        market.verifyHeld(WAGON_ID, CORN, 0);
        market.verifyGold(gold);
    }

    @Test
    @Tag("local")
    void TC_03_czech_sale_and_gift_text() throws Exception {
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
        market.verifyCzechOffered(WAGON_ID, CAPITAL, CORN, LOAD);
        visit.verifyChooserFits(900, 750);
        market.captureChooser("native-sale-cs.png");
        visit.escape();
        visit.verifyChooserClosed();
    }

    @Test
    @Tag("local")
    void TC_04_sale_pieces_follow_the_arrow_keys_and_only_a_confirmed_sale_moves_goods()
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

        // The arrow keys move the pieces one at a time, and each amount is
        // quoted.
        visit.enterSettlement(WAGON_AT, CAPITAL);
        market.openMarket();
        market.chooseSalePieces(12);
        market.verifySaleQuantity(12);
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, 12);
        market.chooseSalePieces(40);
        market.verifySaleQuantity(40);
        market.verifySaleOffered(WAGON_ID, CAPITAL, CORN, 40);

        // Leaving the chooser without confirming sells nothing.
        visit.escape();
        visit.verifyChooserClosed();
        market.verifyHeld(WAGON_ID, CORN, LOAD);
        market.verifyReceived(CAPITAL, CORN, 0);
    }

}
