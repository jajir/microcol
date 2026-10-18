package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.ColonyDefensePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * UC-075: the colonists defend a colony as one group, Paul Revere arms a worker
 * only when nothing armed defends, and the whole colony with all its workers
 * changes hands only after the defense is defeated.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_76_F68_colony_defense_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F27-attack-eligibility.microcol");
    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location FIELD_SOLDIER = Location.of(17, 11);
    private static final Location FIELD = Location.of(16, 10);

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_civilian_group_review_cancel_and_capture_of_the_whole_colony()
            throws Exception {
        // Quebec has a field worker and a town hall worker and 150 liberty.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("civilians", false, true, 150);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        attack.rememberWorld();
        // The soldier's route into the colony is an attack, never a move.
        attack.preview(SOLDIER, QUEBEC);
        attack.verifyAttackPreview(QUEBEC);
        attack.cancelPreview();
        // The colonists defend together with base strength 1.
        attack.chooseAttack(SOLDIER, QUEBEC);
        colony.verifyColonyReview("Colonists of Quebec", "Base: 1",
                "captures the whole colony");
        // Cancelling the review changes nothing.
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        // A won battle takes the colony with both workers and its liberty.
        colony.chooseColonyOutcome(true);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        colony.closeCapturedColonyMessage();
        colony.verifyColonyCaptured("Dutch", 2, 150, QUEBEC,
                "captured with all its workers");
        // Continue shows the same captured colony.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyCaptured("Dutch", 2, 150, QUEBEC, null);
    }

    @Test
    @Tag("local")
    void TC_02_civilian_defeat_keeps_the_colony_and_disarms_the_attacker()
            throws Exception {
        // Quebec has one field worker and no Paul Revere.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("civilians-held", false, false, 0);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        // A lost battle costs the attacker its muskets, not the colonists.
        colony.chooseColonyOutcome(false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        colony.verifyColonyHeld(1, 0, 50, SOLDIER, "lost its muskets");
        // Continue keeps the result and spends no second draw.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyHeld(1, 0, 50, SOLDIER, null);
    }

    @Test
    @Tag("local")
    void TC_03_review_is_stale_when_a_garrison_soldier_returns()
            throws Exception {
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("civilians-stale", false, false, 0);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        attack.chooseAttack(SOLDIER, QUEBEC);
        // The roster changes while the review is open: an armed defender
        // arrives, so the quoted civilian battle can no longer be fought.
        colony.garrisonColonyTile();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_04_revere_militia_wins_keeps_muskets_and_joins_the_garrison()
            throws Exception {
        // Quebec has two workers, 300 liberty and 50 stored muskets.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("revere-exit", true, true, 300);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        attack.rememberWorld();
        // The review names the armed worker and the muskets it would use.
        attack.chooseAttack(SOLDIER, QUEBEC);
        colony.verifyColonyReview("Free Colonist (armed by Paul Revere)",
                "Base: 2", "Paul Revere arms one worker");
        // Cancelling never consumes the stored muskets.
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        colony.verifyStoredMuskets(50);
        // The militia wins: it keeps its muskets and leaves its job.
        colony.chooseColonyOutcome(false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        colony.verifyColonyHeld(1, 200, 0, SOLDIER,
                "joins the garrison outside");
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, true);
        // Continue keeps the garrison soldier and the capped liberty.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyHeld(1, 200, 0, SOLDIER, null);
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, true);
    }

    @Test
    @Tag("local")
    void TC_05_revere_militia_as_last_resident_stays_inside_its_job()
            throws Exception {
        // Quebec has one worker, so its victory cannot erase the colony.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("revere-stay", true, false, 150);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        colony.chooseColonyOutcome(false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        colony.verifyColonyReview("Free Colonist (armed by Paul Revere)",
                "Base: 2", "Paul Revere arms one worker");
        attack.confirm();
        colony.verifyColonyHeld(1, 150, 0, SOLDIER, "lost its muskets");
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, false);
        // Continue keeps the armed worker in its job.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyHeld(1, 150, 0, SOLDIER, null);
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, false);
    }

    @Test
    @Tag("local")
    void TC_06_defeated_revere_militia_still_loses_the_whole_colony()
            throws Exception {
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("revere-captured", true, true, 0);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        // The attacker wins against the emergency militia.
        colony.chooseColonyOutcome(true);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        colony.closeCapturedColonyMessage();
        colony.verifyColonyCaptured("Dutch", 2, 0, QUEBEC,
                "captured with all its workers");
        // The spent muskets are gone and nobody is armed for free.
        colony.verifyStoredMuskets(0);
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyCaptured("Dutch", 2, 0, QUEBEC, null);
        colony.verifyStoredMuskets(0);
    }

    @Test
    @Tag("local")
    void TC_07_peaceful_colony_review_cancels_without_war_and_confirms_with_it()
            throws Exception {
        // Dutch and France are at peace and Quebec has one field worker.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("peaceful", false, false, 0);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        colony.verifyWarDeclared(false);
        attack.rememberWorld();
        // Attacking the colony asks for the ordinary declaration review.
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.verifyReview("Free Colonist", "Colonists of Quebec",
                "declares war", 0, 4);
        // Cancelling declares nothing.
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        colony.verifyWarDeclared(false);
        // Confirming declares war and the won battle takes the colony.
        colony.chooseColonyOutcome(true);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        colony.closeCapturedColonyMessage();
        colony.verifyColonyCaptured("Dutch", 1, 0, QUEBEC,
                "captured with all its workers");
        colony.verifyWarDeclared(true);
    }

    @Test
    @Tag("local")
    void TC_08_declaration_review_is_stale_when_war_started_meanwhile()
            throws Exception {
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFixture("peaceful-stale", false, false, 0);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.chooseAttack(SOLDIER, QUEBEC);
        // War is declared elsewhere while the review is open.
        attack.establishExistingWar();
        attack.rememberWorld();
        attack.confirm();
        attack.verifyRejection("No battle was started");
        attack.verifyGameplayUnchanged();
        attack.closeMessage();
    }

    @Test
    @Tag("local")
    void TC_09_independent_attack_captures_only_the_field_worker()
            throws Exception {
        // Quebec has a field worker at (16,10), a town hall worker and 400
        // liberty; the soldier stands at (17,11) next to that field.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFieldFixture("field-worker", 400);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        attack.rememberWorld();
        // The worker defends alone on its field tile.
        attack.chooseAttack(FIELD_SOLDIER, FIELD);
        colony.verifyColonyReview("Field worker of Quebec", "Base: 1",
                "defends alone");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
        // A won battle takes only that worker; the colony keeps the other.
        colony.chooseColonyOutcome(true);
        attack.chooseAttack(FIELD_SOLDIER, FIELD);
        attack.confirm();
        colony.verifyWorkerCaptured(ColonyDefensePage.RESIDENT, FIELD_SOLDIER,
                1, 200, "was captured");
        // Continue keeps the captured worker with the Dutch.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyWorkerCaptured(ColonyDefensePage.RESIDENT, FIELD_SOLDIER,
                1, 200, null);
    }

    @Test
    @Tag("local")
    void TC_10_fortified_colony_of_three_sends_the_victorious_militia_outside()
            throws Exception {
        // A stockade colony of three residents with 600 liberty and Revere.
        final ColonyDefensePage colony = new ColonyDefensePage(getContext());
        colony.prepareFortifiedFixture("fortified-exit", 600);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        attack.establishExistingWar();
        // A voluntary exit is refused, but the battle result is not voluntary.
        colony.verifyVoluntaryExitRefused(ColonyDefensePage.RESIDENT);
        colony.chooseColonyOutcome(false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        colony.verifyColonyReview("Free Colonist (armed by Paul Revere)",
                "Base: 2", "Paul Revere arms one worker");
        attack.confirm();
        colony.verifyColonyHeld(2, 400, 0, SOLDIER,
                "joins the garrison outside");
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, true);
        // The two remaining residents stay protected by the fortification.
        colony.verifyVoluntaryExitRefused(8);
        // Continue keeps the garrison soldier and the capped liberty.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        colony.verifyColonyHeld(2, 400, 0, SOLDIER, null);
        colony.verifyMilitia(ColonyDefensePage.RESIDENT, true);
    }
}
