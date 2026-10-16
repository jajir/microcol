package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.EuropeanRelationState;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * A visible tribute demand pays or refuses once, retaining the pair cooldown.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_60_F51_european_tribute_test extends AbstractMicroColTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F51-european-tribute.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_preserves_world_and_allows_new_demand() throws Exception {
        // Escape cancels a reviewed demand without starting the cooldown.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        TributePage tribute = new TributePage(getContext());
        relations.open();
        relations.rememberWorld();
        tribute.review();
        tribute.verifyQuote(200, 500, 700, "meet the strength requirement");
        tribute.captureDialog("tributeDialog", "r88-tribute-offer.png");
        tribute.escape();
        relations.verifyWorldUnchanged();
        tribute.verifyState(500, 1000, EuropeanRelationState.WAR, false, 0);
        tribute.review();
        tribute.cancel();
        relations.verifyWorldUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_postpone_continue_pay_once_and_preserve_cooldown()
            throws Exception {
        // Continue restores the same unconfirmed amount without payment.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        TributePage tribute = new TributePage(getContext());
        relations.open();
        tribute.review();
        tribute.later();
        tribute.verifyState(500, 1000, EuropeanRelationState.WAR, true, 0);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        tribute.review();
        tribute.verifyQuote(200, 500, 700, "meet the strength requirement");
        // Enter and a repeated event transfer gold only once, without peace.
        tribute.confirmTwice();
        tribute.verifyState(700, 800, EuropeanRelationState.WAR, false, 10);
        tribute.verifyCooldown(10);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        tribute.verifyState(700, 800, EuropeanRelationState.WAR, false, 10);
        tribute.verifyCooldown(10);
    }

    @Test
    @Tag("local")
    void TC_03_refusal_keeps_peace_and_starts_cooldown() throws Exception {
        // A weak visitor sees the refusal before choosing to make the demand.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        TributePage tribute = new TributePage(getContext());
        tribute.weakenAndMakePeace();
        relations.open();
        tribute.review();
        tribute.verifyQuote(0, 500, 500, "the rival will refuse");
        tribute.confirm();
        tribute.verifyState(500, 1000, EuropeanRelationState.PEACE, false, 10);
        relations.verifyFeedback("France refused the demand");
        tribute.verifyCooldown(10);
    }

    @Test
    @Tag("local")
    void TC_04_stale_treasury_rejects_without_payment_or_cooldown()
            throws Exception {
        // Another transaction invalidates the reviewed terms before
        // confirmation.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        TributePage tribute = new TributePage(getContext());
        relations.open();
        tribute.review();
        tribute.setGold("France", 999);
        relations.rememberWorld();
        tribute.confirm();
        tribute.verifyChanged();
        relations.verifyWorldUnchanged();
        tribute.verifyState(500, 999, EuropeanRelationState.WAR, true, 0);
        tribute.cancel();
        tribute.verifyState(500, 999, EuropeanRelationState.WAR, false, 0);
    }

    @Test
    @Tag("local")
    void TC_05_empty_rival_treasury_has_visible_zero_payment()
            throws Exception {
        // A successful strength check cannot create gold in an empty treasury.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        TributePage tribute = new TributePage(getContext());
        tribute.setGold("France", 0);
        relations.open();
        tribute.review();
        tribute.verifyQuote(0, 500, 500, "The rival has no gold to pay");
        tribute.confirm();
        tribute.verifyState(500, 0, EuropeanRelationState.WAR, false, 10);
        relations.verifyFeedback("France had no gold to pay");
    }
}
