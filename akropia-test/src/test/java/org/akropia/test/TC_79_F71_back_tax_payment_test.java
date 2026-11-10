package org.akropia.test;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.page.EuropePortScreen;
import org.akropia.page.EuropeHarborPage;
import org.akropia.page.InGameLayoutPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.ScreenMarketBuy;
import org.akropia.page.ScreenMarketSell;
import org.akropia.page.TaxDemandPage;
import org.akropia.page.TaxDemandPage.Fixture;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-059: a protested commodity stays boycotted until its back taxes are paid
 * from Europe. Cancelling charges nothing; paying charges the exact frozen
 * amount once and restores only that commodity for a real Europe trade.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_79_F71_back_tax_payment_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F19-europe-equipment.akropia");
    private static final int WEAPONS_ARREARS = 400;
    private static final int CLOTH_ARREARS = 100;
    private static final int START_GOLD = 1000;
    private static final int TAX_AFTER_PROTEST = 17;
    private static final String WEAPONS_OWED = "Arrears to pay: 400 gold.";
    private static final String WEAPONS_RESTORE = "Paying lifts the boycott: buying and selling Weapons in Home port will be allowed again. Other boycotts stay.";
    private static final String SHORT = "Not enough gold. This payment needs 400 gold.";
    private static final String CHANGED = "The back taxes changed or the payment is no longer possible. Nothing was charged.";
    private static final String NO_SHORTAGE = "";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    /**
     * Cloth is already boycotted; the protest destroys weapons and closes them
     * with the same 400 gold arrears as the protest scenario.
     */
    private Fixture fixture(final int gold) {
        return new Fixture(FIXTURE, "back-tax").due()
                .warehouse(Map.of(GoodsType.WEAPONS, 100, GoodsType.GRAIN, 40))
                .arrears(Map.of(GoodsType.CLOTH, CLOTH_ARREARS)).gold(gold);
    }

    private static String treasury(final int gold) {
        return "Treasury: " + gold + " gold. Payment: 400 gold.";
    }

    @Test
    @Tag("local")
    void TC_01_cancel_keeps_back_taxes_then_pay_and_trade_the_restored_weapon()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture(START_GOLD));
        WelcomePage.of(getContext()).loadGame();
        // The protest closes weapons with their exact arrears; cloth stays
        // closed.
        taxes.startUpdate();
        taxes.verifyDemandOffersProtest();
        taxes.protest();
        taxes.finishUpdate();
        taxes.verifyTax(TAX_AFTER_PROTEST, Map.of(GoodsType.WEAPONS,
                WEAPONS_ARREARS, GoodsType.CLOTH, CLOTH_ARREARS));
        int gold = taxes.gold();
        // Europe offers the back taxes; cancelling shows the exact consequence
        // and charges nothing.
        taxes.openEurope();
        taxes.verifyBackTaxButton(true, false);
        // Keep every commodity visible before dragging the last goods type.
        final EuropeHarborPage harbor = new EuropeHarborPage(getContext());
        harbor.resize(1000, 900);
        taxes.openBackTaxes();
        taxes.verifyBackTaxDialogOpen(true);
        taxes.chooseBackTaxCommodity(GoodsType.WEAPONS);
        taxes.verifyBackTaxDialog(WEAPONS_OWED, WEAPONS_RESTORE, treasury(gold),
                NO_SHORTAGE, true);
        taxes.cancelBackTax();
        taxes.verifyBackTaxDialogOpen(false);
        taxes.verifyGold(gold);
        taxes.verifyTax(TAX_AFTER_PROTEST, Map.of(GoodsType.WEAPONS,
                WEAPONS_ARREARS, GoodsType.CLOTH, CLOTH_ARREARS));
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, true);

        // Boycott marks keep the price lines centered below their icons.
        harbor.showGoods();
        harbor.verifyAllGoodsVisible();
        harbor.verifyGoodsPricesCentered();
        harbor.verifyContentAlignment();
        new InGameLayoutPage(getContext())
                .saveEvidence("r0346-goods-boycott.png");
        // Paying once charges the exact arrears and lifts only the weapons.
        taxes.openBackTaxes();
        taxes.chooseBackTaxCommodity(GoodsType.WEAPONS);
        taxes.payBackTax();
        taxes.verifyBackTaxDialogOpen(false);
        gold = gold - WEAPONS_ARREARS;
        taxes.verifyGold(gold);
        taxes.verifyTax(TAX_AFTER_PROTEST,
                Map.of(GoodsType.CLOTH, CLOTH_ARREARS));
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, false);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, true);
        taxes.verifyBackTaxButton(true, false);
        // The restored weapon trades for the exact model quote, while cloth
        // remains boycotted and its gold is untouched.
        EuropePortScreen europe = EuropePortScreen.of(getContext());
        europe.selectUnitFromPort(0);
        europe.verifyCargoSlotEmpty(0, 0);
        int buyTotal = taxes.quoteMarketPrice(GoodsType.WEAPONS, 2, true);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0346-restored-goods-before-buy.png");
        ScreenMarketBuy buy = europe
                .buyGoodsAndMoveItToCargoSlotWithPressedControl(
                        GoodsType.BUYABLE_GOOD_TYPES.indexOf(GoodsType.WEAPONS),
                        0);
        new InGameLayoutPage(getContext())
                .saveEvidence("r0346-restored-goods-after-drag.png");
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
        taxes.verifyBoycottMarked(GoodsType.CLOTH, true);
        // Continue keeps the payment, the trade result and the cloth boycott.
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        taxes.verifyGold(gold);
        taxes.verifyTax(TAX_AFTER_PROTEST,
                Map.of(GoodsType.CLOTH, CLOTH_ARREARS));
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, false);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, true);
    }

    @Test
    @Tag("local")
    void TC_02_insufficient_gold_shows_the_shortfall_and_charges_nothing()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture(399));
        WelcomePage.of(getContext()).loadGame();
        taxes.startUpdate();
        taxes.protest();
        taxes.finishUpdate();
        taxes.verifyTax(TAX_AFTER_PROTEST, Map.of(GoodsType.WEAPONS,
                WEAPONS_ARREARS, GoodsType.CLOTH, CLOTH_ARREARS));
        int gold = taxes.gold();
        taxes.openEurope();
        taxes.openBackTaxes();
        taxes.chooseBackTaxCommodity(GoodsType.WEAPONS);
        // Pay is unavailable while the treasury is short; nothing is charged.
        taxes.verifyBackTaxDialog(WEAPONS_OWED, WEAPONS_RESTORE, treasury(gold),
                SHORT, false);
        taxes.cancelBackTax();
        taxes.verifyBackTaxDialogOpen(false);
        taxes.verifyGold(gold);
        taxes.verifyTax(TAX_AFTER_PROTEST, Map.of(GoodsType.WEAPONS,
                WEAPONS_ARREARS, GoodsType.CLOTH, CLOTH_ARREARS));
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, true);
        taxes.verifyBoycottMarked(GoodsType.CLOTH, true);
    }

    @Test
    @Tag("local")
    void TC_03_changed_debt_rejects_the_open_payment_without_a_second_charge()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture(START_GOLD));
        WelcomePage.of(getContext()).loadGame();
        taxes.startUpdate();
        taxes.protest();
        taxes.finishUpdate();
        int gold = taxes.gold();
        taxes.openEurope();
        taxes.openBackTaxes();
        taxes.chooseBackTaxCommodity(GoodsType.WEAPONS);
        // Another command pays the same debt while the dialog is open.
        taxes.payBackTaxElsewhere(GoodsType.WEAPONS);
        taxes.payBackTax();
        taxes.verifyBackTaxFeedback(CHANGED);
        taxes.verifyBackTaxDialogOpen(true);
        taxes.verifyGold(gold - WEAPONS_ARREARS);
        taxes.verifyTax(TAX_AFTER_PROTEST,
                Map.of(GoodsType.CLOTH, CLOTH_ARREARS));
        taxes.cancelBackTax();
        taxes.verifyBoycottMarked(GoodsType.WEAPONS, false);
    }
}
