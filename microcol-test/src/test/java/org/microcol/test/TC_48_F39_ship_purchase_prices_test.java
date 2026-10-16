package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.UnitType;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Repeated purchases use confirmed per-type prices and survive Continue. */
@ExtendWith(ApplicationExtension.class)
public class TC_48_F39_ship_purchase_prices_test extends AbstractMicroColTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), new File(
                "src/test/scenarios/F39-ship-purchase-prices.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_buy_twice_and_continue() throws Exception {
        // Reading and canceling a quote does not increase its next price.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ShipPurchasePage ships = new ShipPurchasePage(getContext());
        ExpertPurchasePage offer = new ExpertPurchasePage(getContext());
        europe.openBuyUnitScreen();
        ships.verifyQuote("CARAVEL", 1000, true);
        offer.escape();
        ships.verifyHistory(0, 2500, 1000);
        // A queued duplicate confirms only one purchase at the displayed price.
        europe.openBuyUnitScreen();
        ships.confirmCaravel(true);
        ships.verifyHistory(1, 1500, 1100);
        ships.verifyShip(103);
        europe.openBuyUnitScreen();
        ships.verifyQuote("CARAVEL", 1100, true);
        ships.confirmCaravel(false);
        ships.verifyHistory(2, 400, 1200);
        ships.verifyShip(104);
        // The next quote is unaffordable, while other types retain their base.
        europe.openBuyUnitScreen();
        ships.verifyQuote("CARAVEL", 1200, false);
        ships.verifyQuote("MERCHANTMAN", 2000, false);
        offer.escape();
        europe.close().exitToMenu();
        europe = new FreePlayAuditPage(getContext()).continueGame()
                .openEuroperPort();
        ships.verifyHistory(2, 400, 1200);
        ships.verifyShip(103);
        ships.verifyShip(104);
        europe.openBuyUnitScreen();
        ships.verifyQuote("CARAVEL", 1200, false);
        offer.escape();
    }

    @Test
    @Tag("local")
    void TC_02_changed_quote_does_not_charge_again() throws Exception {
        // Another completed purchase makes this open price stale.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ShipPurchasePage ships = new ShipPurchasePage(getContext());
        europe.openBuyUnitScreen();
        getContext().getRobot().interact(() -> getModel()
                .getPlayerByName("Dutch").buy(UnitType.CARAVEL));
        ships.confirmCaravel(false);
        ships.verifyStaleWarning();
        ships.closeStaleWarning();
        ships.verifyHistory(1, 1500, 1100);
        ships.verifyShip(103);
        europe.openBuyUnitScreen();
        ships.verifyQuote("CARAVEL", 1100, true);
        new ExpertPurchasePage(getContext()).escape();
    }

    @Test
    @Tag("local")
    void TC_03_expense_after_quote_keeps_price_history() throws Exception {
        // A later expense is checked again even when the displayed quote is
        // current.
        WelcomePage.of(getContext()).loadGame().openEuroperPort()
                .openBuyUnitScreen();
        ShipPurchasePage ships = new ShipPurchasePage(getContext());
        getContext().getRobot().interact(
                () -> getModel().getPlayerByName("Dutch").setGold(999));
        ships.confirmCaravel(false);
        ExpertPurchasePage offer = new ExpertPurchasePage(getContext());
        offer.verifyInsufficientFundsWarning();
        offer.closeWarning();
        ships.verifyQuote("CARAVEL", 1000, false);
        ships.verifyHistory(0, 999, 1000);
        offer.escape();
    }
}
