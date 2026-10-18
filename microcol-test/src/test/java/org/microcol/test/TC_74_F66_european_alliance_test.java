package org.microcol.test;

import static org.microcol.model.EuropeanRelationState.ALLIANCE;
import static org.microcol.model.EuropeanRelationState.PEACE;
import static org.microcol.model.EuropeanRelationState.WAR;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AlliancePage;
import org.microcol.page.AttackPage;
import org.microcol.page.EuropeanRelationsPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Explicit alliance: reviewed proposal, one-time acceptance, history limits,
 * termination wait, narrow passage, mutual defense and the Crown-war event.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_74_F66_european_alliance_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F50-peace-negotiation.microcol");
    private static final List<String> NAMES = List.of("Dutch — France",
            "Dutch — Spain", "France — Spain");
    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location DEFENDER = Location.of(17, 11);
    private static final Location COLONY = Location.of(17, 10);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_review_shows_terms_and_every_cancel_path_keeps_peace()
            throws Exception {
        // The proposal names the partner, price and obligations before any
        // commitment.
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.rememberWorld();
        alliance.review();
        alliance.verifyQuote(200, 500, 300, true);
        alliance.verifyObligations();
        alliance.captureDialog("allianceDialog", "r89-alliance-offer.png");
        // Escape and the cancel button both refuse without charging.
        alliance.escape();
        relations.verifyWorldUnchanged();
        alliance.verifyState(500, 1000, PEACE, false);
        alliance.review();
        alliance.cancel();
        relations.verifyWorldUnchanged();
        alliance.verifyState(500, 1000, PEACE, false);
    }

    @Test
    @Tag("local")
    void TC_02_postpone_continue_accept_once_and_continue() throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        alliance.review();
        alliance.verifyQuote(200, 500, 300, true);
        // Deciding later retains the saved proposal without payment.
        alliance.later();
        alliance.verifyState(500, 1000, PEACE, true);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        alliance.verifyState(500, 1000, PEACE, true);
        alliance.review();
        alliance.verifyQuote(200, 500, 300, true);
        // Enter plus a repeated event pays and allies exactly once.
        alliance.confirmTwice();
        alliance.verifyState(300, 1200, ALLIANCE, false);
        relations.verifyPairs(ALLIANCE, PEACE, PEACE);
        relations.verifyReport(NAMES, List.of("Alliance", "Peace", "Peace"), 1);
        relations.verifyFeedback("Dutch and France are now allied");
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        alliance.verifyState(300, 1200, ALLIANCE, false);
        relations.verifyReport(NAMES, List.of("Alliance", "Peace", "Peace"), 1);
    }

    @Test
    @Tag("local")
    void TC_03_lost_contact_rejects_confirmation_without_payment()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        alliance.review();
        // The visible contact leaves while the proposal is open.
        alliance.moveVisitorAway();
        relations.rememberWorld();
        alliance.confirm();
        alliance.verifyFeedback("The contact, relation or terms changed",
                false);
        relations.verifyWorldUnchanged();
        alliance.verifyState(500, 1000, PEACE, true);
        alliance.cancel();
        alliance.verifyState(500, 1000, PEACE, false);
        alliance.verifyContactUnavailable();
    }

    @Test
    @Tag("local")
    void TC_04_unaffordable_proposal_can_be_inspected_but_not_accepted()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        alliance.setGold(100);
        relations.open();
        relations.rememberWorld();
        alliance.review();
        alliance.verifyQuote(200, 100, -100, false);
        alliance.verifyFeedback("You do not have enough gold", false);
        alliance.cancel();
        relations.verifyWorldUnchanged();
        alliance.verifyState(100, 1000, PEACE, false);
    }

    @Test
    @Tag("local")
    void TC_05_recent_tribute_blocks_the_proposal_with_a_visible_wait()
            throws Exception {
        // Ten complete rounds after a confirmed tribute, five have passed.
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareHistoryFixture("PEACE", "PEACE", "PEACE", 5, 0, null);
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        alliance.verifyBlocked(5);
        alliance.verifyState(500, 1000, PEACE, false);
    }

    @Test
    @Tag("local")
    void TC_06_recent_attack_blocks_the_proposal_with_a_visible_wait()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareHistoryFixture("PEACE", "PEACE", "PEACE", 5, null, 0);
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        alliance.verifyBlocked(5);
        alliance.verifyState(500, 1000, PEACE, false);
    }

    @Test
    @Tag("local")
    void TC_07_declaration_alone_is_not_aggression_but_a_battle_is()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        AttackPage attack = new AttackPage(getContext());
        // A declaration followed by a treaty leaves the alliance available.
        relations.open();
        relations.review("0-1");
        relations.confirm();
        alliance.simulateTreaty();
        relations.close();
        relations.open();
        alliance.verifyProposalAvailable();
        relations.close();
        // A real battle starts the ten-round history even after a treaty.
        attack.chooseExistingOutcome(0, true);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.confirm();
        alliance.waitForBattle();
        alliance.simulateTreaty();
        relations.open();
        alliance.verifyBlocked(10);
    }

    @Test
    @Tag("local")
    void TC_08_termination_is_explicit_and_shows_the_five_round_wait()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("ALLIANCE", "PEACE", "PEACE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.verifyReport(NAMES, List.of("Alliance", "Peace", "Peace"), 1);
        alliance.verifyProposalAbsent();
        relations.rememberWorld();
        // Keeping the alliance and Escape both change nothing.
        alliance.endAlliance();
        alliance.verifyEndReview("Dutch", "France", 5);
        alliance.captureDialog("allianceEndDialog", "r89-alliance-end.png");
        alliance.keepAlliance();
        alliance.verifyEndReviewClosed();
        relations.verifyWorldUnchanged();
        alliance.endAlliance();
        alliance.escapeEnd();
        alliance.verifyEndReviewClosed();
        relations.verifyWorldUnchanged();
        // Only the confirmed end restores peace and starts the wait.
        alliance.endAlliance();
        alliance.confirmEnd();
        alliance.verifyEndReviewClosed();
        alliance.verifyWait(5);
        relations.verifyFeedback("The alliance between Dutch and France has "
                + "ended. War cannot be declared for 5 rounds.");
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        alliance.verifyWait(5);
    }

    @Test
    @Tag("local")
    void TC_09_allied_unit_passes_through_a_colony_without_taking_anything()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("ALLIANCE", "PEACE", "PEACE");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        // The ordinary movement gesture enters the allied colony square.
        game.moveMouseAtLocation(SOLDIER);
        game.dragMouseAtLocation(COLONY);
        game.waitForNumberOfUnitsAt(COLONY, 1);
        alliance.verifyNoMessage();
        alliance.verifyPassageOnly(COLONY);
        relations.verifyColonyAndForce("France", COLONY, 0);
    }

    @Test
    @Tag("local")
    void TC_10_ally_offers_no_attack_route() throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("ALLIANCE", "PEACE", "PEACE");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        AttackPage attack = new AttackPage(getContext());
        attack.rememberWorld();
        // The gesture toward an ally is a legal movement preview, not a fight.
        attack.preview(SOLDIER, DEFENDER);
        alliance.verifyMovementPreviewWithoutBattle(DEFENDER);
        attack.cancelPreview();
        attack.verifyGameplayUnchanged();
        // Releasing it shares the square without a review, battle or war.
        attack.preview(SOLDIER, DEFENDER);
        attack.release();
        game.waitForNumberOfUnitsAt(DEFENDER, 2);
        alliance.verifySharedSquare(DEFENDER, ALLIANCE);
    }

    @Test
    @Tag("local")
    void TC_14_shared_square_survives_continue_and_alliance_termination()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("ALLIANCE", "PEACE", "PEACE");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        // An ally enters the square of a French soldier and Continue keeps it.
        game.moveMouseAtLocation(SOLDIER);
        game.dragMouseAtLocation(DEFENDER);
        game.waitForNumberOfUnitsAt(DEFENDER, 2);
        alliance.verifySharedSquare(DEFENDER, ALLIANCE);
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        alliance.verifySharedSquare(DEFENDER, ALLIANCE);
        // Ending the alliance relocates nobody and saves the evidence.
        relations.open();
        alliance.endAlliance();
        alliance.confirmEnd();
        alliance.verifyWait(5);
        alliance.verifySharedSquare(DEFENDER, PEACE);
        alliance.verifyRetainedEvidence(DEFENDER);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        alliance.verifyWait(5);
        alliance.verifySharedSquare(DEFENDER, PEACE);
        alliance.verifyRetainedEvidence(DEFENDER);
    }

    @Test
    @Tag("local")
    void TC_11_battle_obliges_the_victims_ally_but_a_declaration_does_not()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "ALLIANCE");
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        AttackPage attack = new AttackPage(getContext());
        // A declaration alone leaves the French ally of Spain out of the war.
        relations.open();
        relations.review("0-1");
        relations.confirm();
        relations.verifyPairs(WAR, PEACE, ALLIANCE);
        relations.verifyReport(NAMES, List.of("War", "Peace", "Alliance"), 1);
        relations.close();
        // An actual attack on France obliges its ally Spain to join.
        attack.chooseExistingOutcome(0, true);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.confirm();
        alliance.waitForBattle();
        relations.open();
        relations.verifyPairs(WAR, WAR, ALLIANCE);
        relations.verifyReport(NAMES, List.of("War", "War", "Alliance"), 0);
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        relations.verifyPairs(WAR, WAR, ALLIANCE);
    }

    @Test
    @Tag("local")
    void TC_12_crown_war_happens_once_at_round_twenty_and_not_on_continue()
            throws Exception {
        // Only the Dutch-French pair is both at peace and eligible.
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareLongGameFixture("PEACE", "WAR", "WAR", 19);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        alliance.verifyCrownRound(19, 0);
        alliance.verifyCrownProcessed(null);
        var report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(
                "The Crown forced a war with France.");
        alliance.verifyCrownRound(20, 1);
        alliance.verifyCrownProcessed(20);
        alliance.verifyPairStates(WAR, WAR, WAR);
        report.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        alliance.verifyCrownRound(20, 1);
        alliance.verifyCrownProcessed(20);
        alliance.verifyPairStates(WAR, WAR, WAR);
        // The next complete round does not repeat the event.
        game.nextTurnAndCloseDialogs();
        alliance.verifyCrownRound(21, 0);
        alliance.verifyCrownProcessed(20);
    }

    @Test
    @Tag("local")
    void TC_13_czech_proposal_and_report_use_the_same_decisions()
            throws Exception {
        AlliancePage alliance = new AlliancePage(getContext());
        alliance.prepareFixture("PEACE", "PEACE", "PEACE");
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        relations.open();
        alliance.review();
        alliance.verifyQuoteText("France přijme spojenectví za 200 zlatých.\n"
                + "Vaše pokladnice: 500 zlatých.\nPo zaplacení: 300 zlatých.",
                true);
        alliance.verifyObligationsContain("nesmějí útočit");
        alliance.captureDialog("allianceDialog", "r89-alliance-czech.png");
        alliance.escape();
        relations.verifyWorldUnchanged();
        alliance.review();
        alliance.confirm();
        relations.verifyReport(NAMES, List.of("Spojenectví", "Mír", "Mír"), 1);
        relations.verifyFeedback("Dutch a France jsou nyní spojenci");
    }
}
