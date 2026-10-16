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
 * Review a real peace proposal and preserve its one-time outcome on Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_59_F50_peace_negotiation_test extends AbstractMicroColTest {
    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F50-peace-negotiation.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_cancel_preserves_war_and_world() throws Exception {
        // Inspect the terms, then cancel without paying or changing the war.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        PeacePage peace = new PeacePage(getContext());
        relations.open();
        relations.rememberWorld();
        peace.review();
        peace.verifyQuote(200, 500, 300, true);
        peace.captureDialog("peaceDialog", "r87-peace-offer.png");
        peace.escape();
        relations.verifyWorldUnchanged();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, false);
    }

    @Test
    @Tag("local")
    void TC_02_postpone_continue_accept_and_continue_once() throws Exception {
        // Postponing and Continue retain the original offer without payment.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        PeacePage peace = new PeacePage(getContext());
        relations.open();
        peace.review();
        peace.verifyQuote(200, 500, 300, true);
        peace.later();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, true);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, true);
        peace.review();
        peace.verifyQuote(200, 500, 300, true);
        // Repeated confirmation and another Continue cannot charge twice.
        peace.confirmTwice();
        peace.verifyState(300, 1200, EuropeanRelationState.PEACE, false);
        relations.verifyPairs(EuropeanRelationState.PEACE,
                EuropeanRelationState.PEACE, EuropeanRelationState.PEACE);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        peace.verifyState(300, 1200, EuropeanRelationState.PEACE, false);
    }

    @Test
    @Tag("local")
    void TC_03_lost_contact_rejects_confirmation_without_payment()
            throws Exception {
        // A changed contact invalidates reviewed terms, leaving cancel
        // available.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        PeacePage peace = new PeacePage(getContext());
        relations.open();
        peace.review();
        peace.moveVisitorAway();
        relations.rememberWorld();
        peace.confirm();
        peace.verifyFeedback("The contact or offer changed", false);
        relations.verifyWorldUnchanged();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, true);
        peace.cancel();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, false);
        peace.verifyContactUnavailable();
    }

    @Test
    @Tag("local")
    void TC_04_unaffordable_offer_cannot_be_confirmed() throws Exception {
        // A poor treasury can inspect and cancel, but cannot accept the offer.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        PeacePage peace = new PeacePage(getContext());
        peace.setGold(100);
        relations.open();
        relations.rememberWorld();
        peace.review();
        peace.verifyQuote(200, 100, -100, false);
        peace.verifyFeedback("You do not have enough gold", false);
        peace.cancel();
        relations.verifyWorldUnchanged();
        peace.verifyState(100, 1000, EuropeanRelationState.WAR, false);
    }

    @Test
    @Tag("local")
    void TC_05_franklin_offers_peace_on_update_without_replaying_on_continue()
            throws Exception {
        // Existing real membership has no effect merely from loading a saved
        // turn.
        PeacePage peace = new PeacePage(getContext());
        peace.prepareFranklinFixture();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, false);
        // A normal update offers discounted peace and shows the stored
        // proposal.
        var report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(
                "France offers peace for 100 gold");
        peace.verifyAutomaticOffers(1, 100);
        peace.verifyState(500, 1000, EuropeanRelationState.WAR, true);
        report.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        peace.verifyAutomaticOffers(1, 100);
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        peace.review();
        peace.verifyQuote(100, 500, 400, true);
        peace.later();
        // Keeping the pending offer produces no new announcement next update.
        game = relations.close();
        game.nextTurnAndCloseDialogs();
        peace.verifyAutomaticOffers(0, 100);
        relations.open();
        peace.review();
        peace.confirm();
        peace.verifyState(400, 1100, EuropeanRelationState.PEACE, false);
    }

}
