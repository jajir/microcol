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
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A defeated Continental soldier is destroyed together with its equipment, the
 * review says so beforehand and the loss survives Save and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_82_F57_continental_destruction_test
        extends AbstractMicroColTest {

    private static final Location SOLDIER = Location.of(18, 10);
    private static final Location DEFENDER = Location.of(17, 11);

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(),
                new File("src/test/scenarios/F57-military-promotion.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_defeated_continental_is_destroyed_with_its_equipment()
            throws Exception {
        // Load the military scenario with a Continental French defender.
        WelcomePage.of(getContext()).loadGame();
        final AttackPage attack = new AttackPage(getContext());
        final MilitaryRolePage role = new MilitaryRolePage(getContext());
        role.loadContinentalDefenderFixture();

        // The review shows the chart base 4 and announces the destruction.
        attack.chooseAttack(SOLDIER, DEFENDER);
        role.verifyRoleReview("Base: 2\nAttack: +50%\nFinal strength: 3",
                "Base: 4\nFinal strength: 4", "Attacker win chance: 42.86%",
                "destroyed together with its equipment");
        attack.cancel();
        attack.verifyReviewClosed();
        role.verifyArmed(6, true);

        // The attacker wins by a saved draw that does not promote.
        role.chooseOutcome(0, DEFENDER, true);
        attack.chooseAttack(SOLDIER, DEFENDER);
        attack.confirm();

        // The Continental is gone and the victory is announced.
        role.verifyDestroyed(6,
                "a Continental or royal soldier was destroyed together with its equipment");
        role.verifyArmed(0, true);

        // Continue keeps the loss.
        GamePage.of(getContext()).exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        role.verifyAbsent(6);
        role.verifyArmed(0, true);
    }

}
