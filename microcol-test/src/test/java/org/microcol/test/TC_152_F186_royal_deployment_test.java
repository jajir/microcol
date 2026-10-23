package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.DialogMessagePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.IndependencePage;
import org.microcol.page.RoyalDeploymentPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * R-0186: after independence the Crown sends its recorded force on real
 * men-of-war, the carriers cross the Atlantic, the passengers land on legal
 * shore and attack a rebel colony within ten royal turns, and a Continue in
 * mid-voyage keeps the same force.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_152_F186_royal_deployment_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    /** Royal turns allowed from the first dispatch until the attack. */
    private final static int ROYAL_TURN_LIMIT = 10;

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_152_royal_deployment() throws Exception {
        // open the scenario and declare independence, as in TC_03
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final IndependencePage independence = gamePage.declareIndependence();
        independence.confirm();
        independence.verifyConfirmationClosed();
        final DialogMessagePage dialogMessagePage = DialogMessagePage
                .of(getContext());
        dialogMessagePage.verifyReady();
        dialogMessagePage.close();
        dialogMessagePage.close();
        independence.verifyDeclared(true);
        gamePage = GamePage.of(getContext());

        final RoyalDeploymentPage deployment = new RoyalDeploymentPage(
                getContext());
        deployment.watchRoyalAttacks();

        // the first royal turn puts the whole recorded force on two real
        // men-of-war: royal infantry, royal cavalry and artillery
        gamePage.nextTurnAndCloseDialogs();
        deployment.verifyAllReserveSent();
        deployment.verifyVoyageUnderway();
        deployment.verifyPassengerFamiliesAboard(6, 4, 2);
        final String sailing = deployment.crownForce();

        // Continue in mid-voyage keeps all four families and the crossing;
        // the loaded game is a new model, so the page watches it again
        gamePage.exitToMenu();
        gamePage = new FreePlayAuditPage(getContext()).continueGame();
        deployment.watchRoyalAttacks();
        deployment.verifyCrownForceUnchanged(sailing);
        deployment.verifyVoyageUnderway();
        deployment.verifyPassengerFamiliesAboard(6, 4, 2);

        // play on with the ordinary next-turn control until the first royal
        // attack; royal turns are counted from the first dispatch
        deployment.playRoyalTurnsUntilAttack(gamePage, 2, ROYAL_TURN_LIMIT);

        // the carriers reached the coast, the soldiers landed on legal shore
        // and attacked, all within ten royal turns
        deployment.verifyLandedWithin(ROYAL_TURN_LIMIT);
        deployment.verifyRoyalFamiliesAshore();
        deployment.verifyRoyalAttackWithin(ROYAL_TURN_LIMIT);
        deployment.verifyCrownUnitsMatchRoster();
    }

}
