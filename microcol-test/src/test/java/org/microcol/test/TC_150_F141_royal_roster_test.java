package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * F141: the Congress report shows the Crown's real prewar force, grows it once
 * per threshold of produced bells and keeps that state through Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_150_F141_royal_roster_test extends AbstractMicroColTest {
    /** The turn count of the independence scenario: the year is 1747. */
    private static final int YEAR_1747 = 700;

    private static final int END_YEAR = 1900;

    /** Eight residents in every F03 colony, so this is full support. */
    private static final long FULL = 1600L;

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F47-congress.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_threshold_bell_grows_force_once_and_continue_keeps_it()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        // The fixture starts Conquistador with 6 infantry, 4 cavalry, 2
        // artillery and 2 men-of-war, and 499 produced bells toward the next
        // unit.
        congress.open();
        congress.verifyRoyalForce(6, 4, 2, 2, 499);
        congress.close();
        // The unattended town hall makes one bell: the threshold of 500 adds
        // one infantry, and six ground units per ship require a third ship.
        game.nextTurnAndCloseDialogs();
        congress.open();
        congress.verifyRoyalForce(7, 4, 2, 3, 0);
        congress.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyRoyalForce(7, 4, 2, 3, 0);
        congress.close();
        // The next bell only banks in the remainder; Continue never adds a
        // second unit at the same threshold.
        game.nextTurnAndCloseDialogs();
        congress.open();
        congress.verifyRoyalForce(7, 4, 2, 3, 1);
        congress.close();
    }

    @Test
    @Tag("local")
    void TC_02_declared_independence_freezes_crown_growth_through_bells_and_continue()
            throws Exception {
        // Full support in 1747 with a Crown force that already holds 499 bells.
        IndependenceFixture.prepareWithCrownRemainder(getContext(),
                new File("src/test/scenarios/F03-independence.microcol"),
                "royal-freeze-f141.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL }, 499);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.verifyRoyalForce(6, 4, 2, 2, 499);
        congress.verifyRoyalGrowthRunning();
        congress.close();
        // Declare through the ordinary button and its confirmation.
        IndependencePage independence = game.declareIndependence();
        independence.confirm();
        independence.verifyConfirmationClosed();
        // The mission announces the declaration and the closed port in two
        // modal messages; they must be dismissed before the next turn button
        // can be used.
        DialogMessagePage messages = DialogMessagePage.of(getContext());
        messages.verifyReady();
        messages.close();
        messages.close();
        independence.verifyDeclared(true);
        // The colonies produce bells after the declaration, but the Crown
        // force neither grows nor loses its remainder.
        congress.rememberBells();
        game.nextTurnAndCloseDialogs();
        congress.verifyBellsProducedSinceRemembered();
        congress.open();
        congress.verifyRoyalForce(6, 4, 2, 2, 499, true);
        congress.verifyRoyalGrowthStopped();
        congress.close();
        // Continue keeps the declaration and the frozen force.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyRoyalForce(6, 4, 2, 2, 499, true);
        congress.verifyRoyalGrowthStopped();
        congress.close();
    }
}
