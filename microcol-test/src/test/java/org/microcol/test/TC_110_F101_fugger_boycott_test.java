package org.microcol.test;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.page.CongressPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenMarketBuy;
import org.microcol.page.ScreenMarketSell;
import org.microcol.page.TaxDemandPage;
import org.microcol.page.TaxDemandPage.Fixture;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-097: Jakob Fugger's arrival forgives the existing boycotts and their
 * arrears without payment. A cancelled choice changes nothing, the arrival
 * restores both commodities for Europe trade, and Continue does not repeat it.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_110_F101_fugger_boycott_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.microcol");
    private static final int MUSKET_ARREARS = 400;
    private static final int SILK_ARREARS = 100;
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
     * Muskets and silk are already boycotted with their arrears. Congress holds
     * 99 of the 100 bells needed, so the next national update completes a
     * chosen objective.
     */
    private Fixture fixture() {
        return new Fixture(FIXTURE, "fugger-boycott")
                .warehouse(Map.of(GoodsType.MUSKET, 100, GoodsType.CORN, 40))
                .arrears(Map.of(GoodsType.MUSKET, MUSKET_ARREARS,
                        GoodsType.SILK, SILK_ARREARS))
                .gold(START_GOLD).congress(99, OFFERS);
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
        congress.verifyProgress(99, 100,
                "Choose the next founder. Bells wait until a choice is made.",
                "None");
        congress.select(Founder.JAKOB_FUGGER);
        congress.close();
        congress.verifyWorldUnchanged();
        // Both debts still close their commodities and offer the back taxes.
        taxes.openEurope();
        taxes.verifyBoycottMarked(GoodsType.MUSKET, true);
        taxes.verifyBoycottMarked(GoodsType.SILK, true);
        taxes.verifyBackTaxButton(true, false);
        taxes.verifyTax(TAX_RATE, Map.of(GoodsType.MUSKET, MUSKET_ARREARS,
                GoodsType.SILK, SILK_ARREARS));
        EuropePortScreen europe = EuropePortScreen.of(getContext());
        game = europe.close();
        // Only the explicit choice starts the objective; the arrival is the
        // next national update.
        congress.open();
        congress.select(Founder.JAKOB_FUGGER);
        congress.choose();
        congress.verifyProgress(99, 100, "Working toward: Jakob Fugger",
                "None");
        congress.close();
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Jakob Fugger has joined Congress.");
        report.orElseThrow().close();
        // The arrival clears both debts without deducting gold or changing the
        // tax rate.
        congress.open();
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "Jakob Fugger");
        congress.close();
        taxes.openEurope();
        taxes.verifyBoycottMarked(GoodsType.MUSKET, false);
        taxes.verifyBoycottMarked(GoodsType.SILK, false);
        taxes.verifyBackTaxButton(false, false);
        taxes.verifyTax(TAX_RATE, Map.of());
        taxes.verifyGold(gold);
        // The restored musket trades for the exact model quote.
        europe = EuropePortScreen.of(getContext());
        europe.selectUnitFromPort(0);
        europe.verifyCargoSlotEmpty(0, 0);
        int buyTotal = taxes.quoteMarketPrice(GoodsType.MUSKET, 2, true);
        ScreenMarketBuy buy = europe
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(
                        GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.MUSKET),
                        0);
        buy.selectValueAtSlider(2);
        buy.verifySelectedMarketQuantity(2);
        buy.clickOnBuy();
        europe.verifyGoodsInShip(0, Goods.of(GoodsType.MUSKET, 2));
        gold = gold - buyTotal;
        taxes.verifyGold(gold);
        int saleTotal = taxes.quoteMarketPrice(GoodsType.MUSKET, 2, false);
        ScreenMarketSell sell = europe.sellGoodsWithPressedControl(0,
                GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.MUSKET));
        sell.selectValueAtSlider(2);
        sell.verifySelectedMarketQuantity(2);
        sell.clickOnSell();
        europe.verifyGoodsInShip(0, Goods.of(GoodsType.MUSKET, 0));
        gold = gold + saleTotal;
        taxes.verifyGold(gold);
        // Continue keeps the trade and the arrival once; nothing is cleared
        // again and Fugger is not joined twice.
        europe.close().exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyProgress(0, 200,
                "Choose the next founder. Bells wait until a choice is made.",
                "Jakob Fugger");
        congress.close();
        game.openEuroperPort();
        taxes.verifyBoycottMarked(GoodsType.MUSKET, false);
        taxes.verifyBoycottMarked(GoodsType.SILK, false);
        taxes.verifyBackTaxButton(false, false);
        taxes.verifyTax(TAX_RATE, Map.of());
        taxes.verifyGold(gold);
    }
}
