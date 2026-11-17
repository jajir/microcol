package org.akropia.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Founder;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.page.CongressPage;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.ScreenMarketBuy;
import org.akropia.page.ScreenMarketSell;
import org.akropia.page.TaxDemandPage;
import org.akropia.page.TaxDemandPage.Fixture;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-097: Jakob Fugger's arrival forgives the existing boycotts and their
 * arrears without payment. A cancelled choice changes nothing, the arrival
 * restores both commodities for Europe trade, and Continue does not repeat it.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_110_F101_fugger_boycott_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.akropia");
    private static final int WEAPONS_ARREARS = 400;
    private static final int CLOTH_ARREARS = 100;
    private static final int START_GOLD = 1000;
    private static final int TAX_RATE = 17;
    /** One founder per category, so the offer list is valid. */
    private static final List<Founder> OFFERS = List.of(Founder.JAKOB_FUGGER,
            Founder.THOMAS_JEFFERSON, Founder.WILLIAM_PENN,
            Founder.FERDINAND_MAGELLAN, Founder.JOHN_PAUL_JONES);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    /**
     * Weapons and cloth are already boycotted with their arrears. Congress
     * holds the 100 owls needed to purchase debt settlement immediately.
     */
    private Fixture fixture() {
        return new Fixture(FIXTURE, "fugger-boycott")
                .warehouse(Map.of(GoodsType.WEAPONS, 100, GoodsType.GRAIN, 40))
                .arrears(Map.of(GoodsType.WEAPONS, WEAPONS_ARREARS,
                        GoodsType.CLOTH, CLOTH_ARREARS))
                .gold(START_GOLD).congress(100, OFFERS);
    }

    @Test
    @Tag("local")
    void TC_01_cancelled_choice_keeps_debts_then_arrival_clears_them_once()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        int gold = taxes.gold();
        // Selecting Fugger and closing the dialog is a cancelled choice; it
        // changes nothing in the world.
        congress.rememberWorld();
        congress.open();
        congress.verifyOffer(OFFERS);
        congress.verifyProgress(100, 100,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "None");
        congress.select(Founder.JAKOB_FUGGER);
        congress.close();
        congress.verifyWorldUnchanged();
        // Both debts still close their commodities and offer the back taxes.
        taxes.openEurope();
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, true);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, true);
        taxes.verifyBackTaxButton(true, false);
        taxes.verifyTax(TAX_RATE, Map.of(GoodsType.WEAPONS, WEAPONS_ARREARS,
                GoodsType.CLOTH, CLOTH_ARREARS));
        EuropePortScreen europe = EuropePortScreen.of(getContext());
        game = europe.close();
        // Explicit confirmation pays for debt settlement immediately.
        congress.open();
        congress.select(Founder.JAKOB_FUGGER);
        congress.choose();
        congress.verifyProgress(0, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Debt settlement");
        congress.close();
        var report = java.util.Optional.of(game.openTurnReport());
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Knowledge acquired: Debt settlement.");
        report.orElseThrow().close();
        game.nextTurnAndCloseDialogs();
        // Acquisition clears both debts without deducting gold or changing the
        // tax rate.
        congress.open();
        congress.verifyProgress(1, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Debt settlement");
        congress.close();
        taxes.openEurope();
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, false);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, false);
        taxes.verifyBackTaxButton(false, false);
        taxes.verifyTax(TAX_RATE, Map.of());
        taxes.verifyGold(gold);
        // The restored weapon trades for the exact model quote.
        europe = EuropePortScreen.of(getContext());
        europe.selectUnitFromPort(0);
        europe.verifyCargoSlotEmpty(0, 0);
        int buyTotal = taxes.quoteMarketPrice(GoodsType.WEAPONS, 2, true);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-fugger-before-buy.png");
        ScreenMarketBuy buy = europe
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(
                        GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.WEAPONS),
                        0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0378-fugger-after-drag.png");
        buy.selectValueAtSlider(2);
        buy.verifySelectedMarketQuantity(2);
        buy.clickOnBuy();
        europe.verifyGoodsInShip(0, Goods.of(GoodsType.WEAPONS, 2));
        gold = gold - buyTotal;
        taxes.verifyGold(gold);
        int saleTotal = taxes.quoteMarketPrice(GoodsType.WEAPONS, 2, false);
        ScreenMarketSell sell = europe.sellGoodsWithPressedControl(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.WEAPONS));
        sell.selectValueAtSlider(2);
        sell.verifySelectedMarketQuantity(2);
        sell.clickOnSell();
        europe.verifyGoodsInShip(0, Goods.of(GoodsType.WEAPONS, 0));
        gold = gold + saleTotal;
        taxes.verifyGold(gold);
        // Continue keeps the trade and the arrival once; nothing is cleared
        // again and Fugger is not joined twice.
        europe.close().exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyProgress(1, 200,
                "Choose a discovery. Acquiring it costs the displayed number of owls.",
                "Debt settlement");
        congress.close();
        game.openEuroperPort();
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, false);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, false);
        taxes.verifyBackTaxButton(false, false);
        taxes.verifyTax(TAX_RATE, Map.of());
        taxes.verifyGold(gold);
    }
}
