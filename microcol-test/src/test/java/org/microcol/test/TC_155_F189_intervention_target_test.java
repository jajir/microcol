package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.CongressPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.InterventionPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A declared nation's foreign-intervention target is announced by its first
 * update, shows its progress in Congress, is earned once when a real update
 * reaches it, and keeps both through Save and Continue.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_155_F189_intervention_target_test extends AbstractMicroColTest {

    /** The year 1747 of the shared independence scenario, as in TC_154. */
    private final static int YEAR_1747 = 700;

    private final static int END_YEAR = 1900;

    /** Full liberty in the four colonies of the scenario, as in TC_154. */
    private final static long FULL = 1600L;

    /** The scenario is Conquistador, so the target is 500 bells. */
    private final static int TARGET = 500;

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_first_update_announces_the_target_counts_its_bells_and_survives_continue()
            throws Exception {
        // The nation is declared, but its first update has not happened yet.
        IndependenceFixture.prepareDeclared(getContext(), testFileName,
                "intervention-f189-announce.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL }, null);
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        congress.open();
        congress.verifyInterventionAwaiting();
        congress.close();

        // The first real update fixes the target, announces it once and counts
        // the bells that its colonies produce.
        final int planned = intervention.plannedBells();
        final ScreenTurnReport report = gamePage.nextTurn().orElseThrow();
        intervention.verifyAnnouncement(report, TARGET);
        intervention.closeReport(report);
        congress.open();
        congress.verifyInterventionProgress(planned, TARGET);
        congress.close();

        // Save and Continue keep the same target and progress.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyInterventionProgress(planned, TARGET);
        congress.close();
    }

    @Test
    @Tag("local")
    void TC_02_real_update_earns_the_target_once_and_later_bells_earn_nothing_again()
            throws Exception {
        // Saved progress stands one bell short of the target, so the next real
        // update of the colonies reaches it.
        IndependenceFixture.prepareDeclared(getContext(), testFileName,
                "intervention-f189-earn.microcol", YEAR_1747, END_YEAR,
                new long[] { FULL, FULL, FULL, FULL },
                IndependenceFixture.intervention(TARGET, TARGET - 1, false));
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final CongressPage congress = new CongressPage(getContext());
        final InterventionPage intervention = new InterventionPage(
                getContext());
        congress.open();
        congress.verifyInterventionProgress(TARGET - 1, TARGET);
        congress.close();

        // The update that reaches the target reports the earning.
        final Optional<ScreenTurnReport> earning = gamePage.nextTurn();
        intervention.verifyEarnedNotice(earning, TARGET);
        intervention.closeReport(earning);
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.close();

        // A later update adds bells, but the earning is not reported again.
        final Optional<ScreenTurnReport> later = gamePage.nextTurn();
        intervention.verifyNoEarnedNotice(later);
        intervention.closeReport(later);
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.close();

        // Save and Continue keep the earned outcome.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyInterventionEarned(TARGET);
        congress.close();
    }
}
