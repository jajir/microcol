package org.microcol.test;

import java.io.File;
import java.util.Map;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GoodsType;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.TaxDemandPage;
import org.microcol.page.TaxDemandPage.Fixture;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-059: the Crown's tax demand pauses one national update. The player can
 * defer it, reopen it from Europe, keep it across Continue, and answer it once
 * by accepting or protesting.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_78_F70_tax_demand_test extends AbstractMicroColTest {
    private static final String RATES = "Current tax: 0%. The Crown demands 2%.";
    private static final String ACCEPT = "Accepting raises the tax to 2% and destroys nothing.";
    private static final String PROTEST = "Protest destroys 60 Rum in New Amsterdam (stock 60), adds 100 liberty there, boycotts that commodity and records 1320 gold of arrears. The tax stays 0%.";
    private static final String NO_PROTEST = "No protest consequence is available now, so only acceptance is possible.";
    private static final int CONGRESS_BELLS = 99;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), TaxDemandPage.CORE_SOURCE);
    }

    private Fixture fixture() {
        // Rum is the most valuable stock: 60 * 11 beats 40 * 11 and 40 * 2.
        return new Fixture(TaxDemandPage.CORE_SOURCE, "tax-demand").due()
                .warehouse(Map.of(GoodsType.RUM, 60, GoodsType.SILK, 40,
                        GoodsType.CORN, 40));
    }

    @Test
    @Tag("local")
    void TC_01_defer_reopen_continue_then_accept_once() throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture());
        GamePage game = WelcomePage.of(getContext()).loadGame();
        // The update falls due: the demand shows its exact consequences and
        // nothing has been produced yet.
        taxes.startUpdate();
        taxes.verifyPaused(0, 0);
        taxes.verifyDemandDialog(RATES, ACCEPT, PROTEST, true);
        taxes.captureDialog("taxDemandDialog", "r102-tax-demand.png");
        taxes.verifyStock(GoodsType.RUM, 60);
        taxes.verifyStock(GoodsType.SILK, 40);
        // Closing the window does not answer; Europe offers the demand again.
        taxes.defer();
        taxes.verifyDialogOpen(false);
        taxes.verifyPaused(0, 0);
        taxes.verifyReopenAction(true);
        taxes.verifyEuropeActionsBlocked();
        taxes.reopen();
        taxes.verifyDemandDialog(RATES, ACCEPT, PROTEST, true);
        // Escape defers in the same way and the map stays paused.
        taxes.escape();
        taxes.verifyPaused(0, 0);
        taxes.returnToMap();
        taxes.verifyTurnCommandsUnavailable();
        // Continue restores the same unanswered demand without any update.
        game.exitToMenu();
        taxes.continuePending();
        taxes.verifyPaused(0, 0);
        taxes.verifyDemandDialog(RATES, ACCEPT, PROTEST, true);
        // Accepting raises the rate once and resumes the remaining update.
        taxes.accept();
        taxes.finishUpdate();
        taxes.verifyTax(2, Map.of());
        taxes.verifyStock(GoodsType.RUM, 60);
        taxes.verifyStock(GoodsType.SILK, 40);
        taxes.verifyResumed(0, CONGRESS_BELLS);
        // Continue does not ask again and does not update again.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        taxes.verifyTax(2, Map.of());
        taxes.verifyStock(GoodsType.RUM, 60);
        taxes.verifyResumed(0, CONGRESS_BELLS);
    }

    @Test
    @Tag("local")
    void TC_02_protest_destroys_the_quoted_stock_and_boycotts_it_once()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture());
        WelcomePage.of(getContext()).loadGame();
        taxes.startUpdate();
        taxes.verifyDemandDialog(RATES, ACCEPT, PROTEST, true);
        // Protesting keeps the rate, destroys the quoted rum and closes it.
        taxes.protest();
        taxes.finishUpdate();
        taxes.verifyTax(0, Map.of(GoodsType.RUM, 1320));
        taxes.verifyStock(GoodsType.RUM, 0);
        taxes.verifyStock(GoodsType.SILK, 40);
        // The protest liberty stays in the colony and earns no Congress bells.
        taxes.verifyResumed(100, CONGRESS_BELLS);
        // The market marks only the closed commodity.
        taxes.openEurope();
        taxes.verifyBoycottMarked(GoodsType.RUM, true);
        taxes.verifyBoycottMarked(GoodsType.SILK, false);
        taxes.returnToMap();
        // Continue keeps the boycott, the loss and the liberty exactly.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        taxes.verifyTax(0, Map.of(GoodsType.RUM, 1320));
        taxes.verifyStock(GoodsType.RUM, 0);
        taxes.verifyResumed(100, CONGRESS_BELLS);
    }

    @Test
    @Tag("local")
    void TC_03_changed_stock_rejects_protest_but_acceptance_still_resolves()
            throws Exception {
        TaxDemandPage taxes = new TaxDemandPage(getContext());
        taxes.prepare(fixture());
        WelcomePage.of(getContext()).loadGame();
        taxes.startUpdate();
        // Another command changes the quoted stock while the window is open.
        taxes.removeStockElsewhere(GoodsType.RUM, 1);
        taxes.protest();
        taxes.verifyFeedback(
                "Protest is no longer possible. Nothing was changed.");
        taxes.verifyPaused(0, 0);
        taxes.verifyStock(GoodsType.RUM, 59);
        taxes.verifyDemandDialog(RATES, ACCEPT, NO_PROTEST, false);
        taxes.accept();
        taxes.finishUpdate();
        taxes.verifyTax(2, Map.of());
        taxes.verifyStock(GoodsType.RUM, 59);
        taxes.verifyResumed(0, CONGRESS_BELLS);
    }
}
