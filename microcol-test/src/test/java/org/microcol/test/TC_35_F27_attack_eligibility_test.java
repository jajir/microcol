package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Equipment-aware attacks and explicit, revocable war confirmation. */
@ExtendWith(ApplicationExtension.class)
public class TC_35_F27_attack_eligibility_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F27-attack-eligibility.microcol");
    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location DEFENDER = Location.of(17, 11);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_peaceful_target_has_an_attack_preview_without_mutation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        // Previewing a legal attack does not declare war or spend movement.
        attack.rememberWorld();
        attack.preview(SOLDIER, DEFENDER);
        attack.verifyAttackPreview(DEFENDER);
        attack.cancelPreview();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_02_hidden_advisor_still_requires_war_review_and_all_cancel_paths()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        // A hidden optional advisor never disables the treaty-breaking review.
        attack.configureHiddenAdvisor();
        attack.rememberWorld();
        attack.chooseAttack(Location.of(18, 12), DEFENDER);
        attack.verifyReview("Master Blacksmith", "Free Colonist",
                "declares war", 2, 6);
        attack.verifyStrengths("Base: 3\nAttack: +50%\nFinal strength: 4.5",
                "Base: 2\nFinal strength: 2", "Attacker win chance: 69.23%");
        attack.captureDialog("fightReview", "attack-english.png");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        // Escape and native window close leave the same gameplay state intact.
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.escape();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.closeWindow();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_03_unarmed_colonist_and_wagon_cannot_initiate() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.rememberWorld();
        // Neither an unarmed worker nor a carrier shows a fight route.
        attack.preview(Location.of(18, 11), DEFENDER);
        attack.verifyPreviewUnavailable();
        attack.cancelPreview();
        attack.preview(Location.of(17, 12), DEFENDER);
        attack.verifyPreviewUnavailable();
        attack.cancelPreview();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_04_disarmed_attacker_rejects_without_declaring_or_spending()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(SOLDIER, DEFENDER);
        // Simulate equipment changing while the original review is open.
        attack.simulateDisarm(0);
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_05_moved_defender_is_not_chased_after_confirmation()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.simulateMovedDefender(Location.of(17, 12));
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_06_replaced_game_rejects_old_review() throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.rememberWorld();
        attack.replaceGame(FIXTURE);
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyOriginalGameplayUnchanged();
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_07_confirmed_attack_exhausts_survivor_and_continue_does_not_repeat_it()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        // Select the existing attacker-win branch; initiation is a real map
        // action.
        attack.chooseExistingOutcome(2, true);
        attack.chooseAttack(Location.of(18, 12), DEFENDER);
        attack.confirm();
        attack.verifyEquipmentOutcome(2, 6, false, false, Location.of(18, 12),
                null);
        org.microcol.page.GamePage.of(getContext()).exitToMenu();
        new org.microcol.page.FreePlayAuditPage(getContext()).continueGame();
        attack.verifyEquipmentOutcome(2, 6, false, false, Location.of(18, 12),
                null);
        // The same dialog instance must not retain its prior accepted choice.
        attack.replaceGame(FIXTURE);
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.closeWindow();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_08_czech_confirmation_cancel_and_stale_feedback() throws Exception {
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(Location.of(18, 12), DEFENDER);
        attack.verifyReview("mistr kovář", "svobodný kolonista",
                "vyhlásíte válku", 2, 6);
        attack.verifyStrengths("Základ: 3\nÚtok: +50%\nVýsledná síla: 4.5",
                "Základ: 2\nVýsledná síla: 2",
                "Šance útočníka na vítězství: 69.23%");
        attack.captureDialog("fightReview", "attack-czech.png");
        attack.escape();
        attack.verifyReviewClosed();
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.simulateDisarm(0);
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("Boj nezačal");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_09_existing_war_with_hidden_advisor_exhausts_the_surviving_defender()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        // Configure an already-warring position and the existing defender-win
        // branch.
        attack.establishExistingWar();
        attack.configureHiddenAdvisor();
        attack.chooseExistingOutcome(0, false);
        attack.preview(SOLDIER, DEFENDER);
        attack.verifyAttackPreview(DEFENDER);
        attack.release();
        attack.verifyEquipmentOutcome(0, 0, false, false, SOLDIER, null);
        org.microcol.page.GamePage.of(getContext()).exitToMenu();
        new org.microcol.page.FreePlayAuditPage(getContext()).continueGame();
        attack.verifyEquipmentOutcome(0, 0, false, false, SOLDIER, null);
    }

    @Test
    @Tag("local")
    void TC_10_changed_defender_equipment_invalidates_the_quoted_strength()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.verifyStrengths("Base: 2\nAttack: +50%\nFinal strength: 3",
                "Base: 2\nFinal strength: 2", "Attacker win chance: 60%");
        // A mounted defender has different strength; the old quote cannot
        // execute.
        attack.simulateMountedDefender();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_11_defeated_dragoon_loses_only_horses_and_continue_preserves_it()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseExistingOutcome(2, false);
        attack.chooseAttack(Location.of(18, 12), DEFENDER);
        attack.confirm();
        // Defeat preserves this specialist and his muskets on the original
        // tile.
        attack.verifyEquipmentOutcome(2, 2, true, false, Location.of(18, 12),
                "lost its horses");
        attack.verifyPerson(2, org.microcol.model.UnitType.MASTER_BLACKSMITH,
                "Dutch");
        org.microcol.page.GamePage.of(getContext()).exitToMenu();
        new org.microcol.page.FreePlayAuditPage(getContext()).continueGame();
        attack.verifyEquipmentOutcome(2, 2, true, false, Location.of(18, 12),
                null);
        attack.verifyPerson(2, org.microcol.model.UnitType.MASTER_BLACKSMITH,
                "Dutch");
    }

    @Test
    @Tag("local")
    void TC_12_forest_and_effective_fortification_explain_three_against_four()
            throws Exception {
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.loadFortifiedForestFixture(FIXTURE);
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, DEFENDER);
        // The same immutable model quote drives the explanation and eventual
        // odds.
        attack.verifyStrengths("Base: 2\nAttack: +50%\nFinal strength: 3",
                "Base: 2\nTerrain: +50%\nFortified unit: +50%\nFinal strength: 4",
                "Attacker win chance: 42.86%");
        attack.captureDialog("fightReview",
                "combat-strengths-forest-fortified.png");
        attack.escape();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }
}
