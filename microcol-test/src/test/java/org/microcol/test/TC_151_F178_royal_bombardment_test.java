package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.MilitaryRolePage;
import org.microcol.page.RebelWarPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * R-0178: revolution battle modifiers in the review of the human rebel's own
 * attack. Intervention troops bombard a colony held by their Crown with +50%
 * and an ordinary Continental does not; a rebel soldier attacking its own
 * Crown's regular in the countryside gets the terrain ambush and never a
 * bombardment. Every review shows the modifiers beside the odds, cancel changes
 * nothing, and the real battle survives Save and Continue. The Crown's own
 * bombardment of a rebel colony is a computer decision with no review, so the
 * model tests cover it.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_151_F178_royal_bombardment_test extends AbstractMicroColTest {

    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location COUNTRYSIDE = Location.of(17, 11);
    private static final String DESTROYED = "a Continental or royal soldier was destroyed together with its equipment";

    @Start
    private void start(Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(),
                new File("src/test/scenarios/F57-military-promotion.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_intervention_troops_bombard_crown_colony_and_defeat_survives_continue()
            throws Exception {
        // The Dutch have declared independence, the Dutch Crown holds Quebec
        // with its one resident, and the Dutch soldier beside it is a
        // Continental delivered by the intervention.
        final RebelWarPage rebel = new RebelWarPage(getContext());
        rebel.prepareCrownColonyFixture("intervention-bombardment", true);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        final MilitaryRolePage role = new MilitaryRolePage(getContext());

        // The review adds the colony bombardment to the Continental base 4 and
        // the attack bonus; cancelling it changes nothing.
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, QUEBEC);
        rebel.verifyAttackerStrength(
                "Base: 4\nAttack: +50%\nColony bombardment: +50%\nFinal strength: 8");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // A saved draw makes the attacker lose: the Continental is destroyed
        // with its equipment and the colony stays with the Crown.
        role.chooseOutcome(0, QUEBEC, false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        role.verifyDestroyed(0, DESTROYED);
        rebel.verifyColonyHeldBy("Dutch's King", 1);

        // Continue keeps the loss and the Crown's colony.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        role.verifyAbsent(0);
        rebel.verifyColonyHeldBy("Dutch's King", 1);
    }

    @Test
    @Tag("local")
    void TC_02_ordinary_continental_gets_no_bombardment_against_the_same_colony()
            throws Exception {
        // The same fixture, but the Continental did not come with the
        // intervention, so only its base and the attack bonus count.
        final RebelWarPage rebel = new RebelWarPage(getContext());
        rebel.prepareCrownColonyFixture("plain-continental", false);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());

        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, QUEBEC);
        rebel.verifyAttackerStrength(
                "Base: 4\nAttack: +50%\nFinal strength: 6");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }

    @Test
    @Tag("local")
    void TC_03_rebel_soldier_ambushes_crown_regular_in_forest_and_victory_survives_continue()
            throws Exception {
        // A regular of the Dutch Crown stands in the trees of the countryside
        // beside the Dutch rebel soldier, outside any colony.
        final RebelWarPage rebel = new RebelWarPage(getContext());
        rebel.prepareCountrysideFixture("forest-ambush", false);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        final MilitaryRolePage role = new MilitaryRolePage(getContext());

        // The rebel gets the forest ambush while the regular keeps its
        // ordinary forest defense; cancelling the review changes nothing.
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, COUNTRYSIDE);
        rebel.verifyAttackerStrength(
                "Base: 2\nAttack: +50%\nRebel ambush: +50%\nFinal strength: 4");
        rebel.verifyDefenderStrength(
                "Base: 5\nTerrain: +50%\nFinal strength: 7.5");
        rebel.verifyWinChance("Attacker win chance: 34.78%");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // A saved draw makes the rebel win: the regular is destroyed with its
        // equipment and the winner keeps its muskets.
        role.chooseOutcome(0, COUNTRYSIDE, true);
        attack.chooseAttack(SOLDIER, COUNTRYSIDE);
        attack.confirm();
        role.verifyDestroyed(6, DESTROYED);
        role.verifyArmed(0, true);

        // Continue keeps the result.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        role.verifyAbsent(6);
        role.verifyArmed(0, true);
    }

    @Test
    @Tag("local")
    void TC_04_delivered_continental_gets_ambush_but_no_bombardment_in_the_countryside()
            throws Exception {
        // The same countryside, but the rebel soldier is a Continental that
        // came with the intervention. Bombardment is for colonies only.
        final RebelWarPage rebel = new RebelWarPage(getContext());
        rebel.prepareCountrysideFixture("countryside-intervention", true);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());

        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, COUNTRYSIDE);
        rebel.verifyAttackerStrength(
                "Base: 4\nAttack: +50%\nRebel ambush: +50%\nFinal strength: 8");
        rebel.verifyDefenderStrength(
                "Base: 5\nTerrain: +50%\nFinal strength: 7.5");
        rebel.verifyWinChance("Attacker win chance: 51.61%");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();
    }
}
