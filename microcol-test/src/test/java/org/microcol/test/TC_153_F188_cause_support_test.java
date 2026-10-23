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
 * R-0188: cause support of a revolutionary colony attack. The Dutch have
 * declared independence, the Dutch Crown holds Quebec, and the rebel colonial
 * soldier attacks it. Quebec's stored liberty of 120 is exactly 60% of its one
 * resident's 200 points, so the review adds +60% to the soldier's base 2 beside
 * the attack bonus. Cancelling changes nothing, and the real lost battle
 * survives Save and Continue with the same Crown colony.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_153_F188_cause_support_test extends AbstractMicroColTest {

    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location QUEBEC = Location.of(17, 10);

    @Start
    private void start(Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(),
                new File("src/test/scenarios/F57-military-promotion.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_rebel_colonial_attack_on_crown_colony_shows_support_and_survives_continue()
            throws Exception {
        // The Dutch rebel soldier attacks the Crown's Quebec, whose one
        // resident
        // is an unarmed civilian and whose stored liberty is 120 of 200.
        final RebelWarPage rebel = new RebelWarPage(getContext());
        rebel.prepareSupportFixture("cause-support", 120L);
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        final MilitaryRolePage role = new MilitaryRolePage(getContext());

        // The review shows the support between the attack bonus and the total;
        // the civilian group defends with base 1 on grassland, and the odds are
        // 4.2 / (4.2 + 1) = 80.77%.
        attack.rememberWorld();
        attack.chooseAttack(SOLDIER, QUEBEC);
        rebel.verifyAttackerStrength(
                "Base: 2\nAttack: +50%\nCause support: +60%\nFinal strength: 4.2");
        rebel.verifyDefenderStrength("Base: 1\nFinal strength: 1");
        rebel.verifyWinChance("Attacker win chance: 80.77%");
        attack.cancel();
        attack.verifyReviewClosed();
        attack.verifyGameplayUnchanged();

        // A saved draw makes the attacker lose: the soldier keeps its life but
        // loses its muskets, and the Crown keeps Quebec.
        role.chooseOutcome(0, QUEBEC, false);
        attack.chooseAttack(SOLDIER, QUEBEC);
        attack.confirm();
        rebel.verifyLostColonyBattle(0);

        // Continue keeps the lost battle and the Crown's colony.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        role.verifyArmed(0, false);
        rebel.verifyColonyHeldBy("Dutch's King", 1);
    }
}
