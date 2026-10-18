package org.microcol.test;

import static org.microcol.model.EuropeanRelationState.PEACE;
import static org.microcol.model.EuropeanRelationState.WAR;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
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
 * Political readout, explicit declaration, persistence and stale review safety.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_33_F25_european_relations_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F25-european-relations.microcol");
    private static final String HUMAN = "Žluťoučký kapitán 🌊 (Dutch)";
    private static final String FRANCE = "Nová Francie (France)";
    private static final String SPAIN = "Nová Francie (Spain)";
    private static final List<String> NAMES = List.of(HUMAN + " — " + FRANCE,
            HUMAN + " — " + SPAIN, FRANCE + " — " + SPAIN);
    private static final Location START = Location.of(18, 10);
    private static final Location COLONY = Location.of(17, 10);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_read_cancel_escape_declare_once_and_continue() throws Exception {
        // The political report includes all pairs, not hidden military or
        // economic facts.
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        relations.open();
        relations.verifyReport(NAMES, List.of("Peace", "Peace", "Peace"), 2);
        relations.verifyWorldUnchanged();
        relations.captureDialog("europeanRelations", "relations-initial.png");

        // Both cancellation paths leave the complete world unchanged.
        relations.review("0-1");
        relations.verifyReview(HUMAN, FRANCE, "ends peace");
        relations.captureDialog("declarationReview", "declaration-review.png");
        relations.cancel();
        relations.verifyReviewClosed();
        relations.verifyWorldUnchanged();
        relations.review("0-1");
        relations.escape();
        relations.verifyReviewClosed();
        relations.verifyWorldUnchanged();

        // Confirming twice affects the chosen pair once and leaves the rest of
        // the world intact.
        relations.review("0-1");
        relations.confirmTwice();
        relations.verifyReviewClosed();
        relations.verifyPairs(WAR, PEACE, PEACE);
        relations.verifyOnlyRelationsChanged();
        relations.verifyReport(NAMES, List.of("War", "Peace", "Peace"), 1);
        relations.verifyFeedback("are now at war");

        // Continue preserves Unicode labels, duplicate display names and the
        // same bilateral state.
        relations.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        relations.open();
        relations.verifyReport(NAMES, List.of("War", "Peace", "Peace"), 1);
        relations.verifyPairs(WAR, PEACE, PEACE);
    }

    @Test
    @Tag("local")
    void TC_02_peace_blocks_entry_and_declaration_allows_equipped_capture()
            throws Exception {
        // A normal map gesture cannot silently take a peaceful settlement.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(START, COLONY);
        attack.verifyReview("Free Colonist", "Colonists of Quebec",
                "declares war", 0, 4);
        attack.cancel();
        attack.verifyReviewClosed();
        relations.verifyWorldUnchanged();
        relations.verifyPairs(PEACE, PEACE, PEACE);
        relations.verifyColonyAndForce("France", START, 1);

        // An explicit declaration does not make the entry a plain move: the
        // colonists defend, so the same gesture opens the attack review and a
        // won battle takes the colony.
        relations.open();
        relations.review("0-1");
        relations.confirm();
        game = relations.close();
        attack.chooseExistingOutcome(0, true);
        attack.chooseAttack(START, COLONY);
        attack.confirm();
        game.waitForNumberOfUnitsAt(COLONY, 1);
        relations.verifyCaptureFeedback("You captured colony 'Quebec'.");
        relations.closeMapMessage();
        relations.verifyPairs(WAR, PEACE, PEACE);
        relations.verifyColonyAndForce("Dutch", COLONY, 0);
    }

    @Test
    @Tag("local")
    void TC_03_concurrent_relation_change_rejects_the_old_confirmation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.review("0-1");
        // Simulate an independently accepted transition while the old review is
        // visible.
        relations.simulateAcceptedDeclaration("France");
        relations.rememberWorld();
        relations.confirm();
        relations.verifyWorldUnchanged();
        relations.verifyFeedback("Nothing was declared");
        relations.verifyReport(NAMES, List.of("War", "Peace", "Peace"), 1);
    }

    @Test
    @Tag("local")
    void TC_04_replaced_game_cannot_receive_an_old_declaration()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        relations.open();
        relations.review("0-1");
        // A different live model with the same participant keys is still a
        // different game.
        relations.simulateGameReplacement(FIXTURE);
        relations.confirm();
        relations.verifyReplacementUntouched();
        relations.verifyPairs(PEACE, PEACE, PEACE);
        relations.verifyFeedback("Nothing was declared");
    }

    @Test
    @Tag("local")
    void TC_05_stopped_game_rejects_pending_confirmation() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.review("0-1");
        relations.simulateStoppedGame();
        relations.rememberWorld();
        relations.confirm();
        relations.verifyWorldUnchanged();
        relations.verifyPairs(PEACE, PEACE, PEACE);
        relations.verifyFeedback("Nothing was declared");
    }

    @Test
    @Tag("local")
    void TC_06_czech_report_and_keyboard_review_cancel_confirm()
            throws Exception {
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.rememberWorld();
        relations.openWithKeyboard();
        relations.verifyReport(NAMES, List.of("Mír", "Mír", "Mír"), 2);
        relations.reviewWithKeyboard("0-1");
        relations.verifyReview(HUMAN, FRANCE, "skončí mír");
        relations.captureDialog("declarationReview", "declaration-czech.png");
        relations.escape();
        relations.verifyReviewClosed();
        relations.verifyWorldUnchanged();
        relations.reviewWithKeyboard("0-1");
        relations.confirmWithKeyboard();
        relations.verifyPairs(WAR, PEACE, PEACE);
        relations.verifyOnlyRelationsChanged();
        relations.verifyReport(NAMES, List.of("Válka", "Mír", "Mír"), 1);
    }

    @Test
    @Tag("local")
    void TC_07_renamed_target_requires_a_fresh_review() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        EuropeanRelationsPage relations = new EuropeanRelationsPage(
                getContext());
        relations.open();
        relations.review("0-1");
        // A changed visible identity must not silently reuse a previously named
        // target.
        final String longName = "Ž".repeat(63) + "🌊";
        relations.simulateDisplayName("France", longName);
        relations.rememberWorld();
        relations.confirm();
        relations.verifyWorldUnchanged();
        relations.verifyPairs(PEACE, PEACE, PEACE);
        relations.verifyFeedback("Nothing was declared");
        relations.review("0-1");
        relations.verifyReview(HUMAN, longName + " (France)", "ends peace");
        relations.captureDialog("declarationReview",
                "declaration-long-name.png");
        relations.cancel();
        relations.verifyWorldUnchanged();
    }
}
