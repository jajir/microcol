package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.page.ColonyJobPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.ReligionReportPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The religion report (R-0199) of the Dutch nation on the F58 fixture. The
 * fixture starts with 74 saved crosses, one short of the Discoverer threshold
 * of 75, and an unattended chapel that gives one cross. The player moves the
 * existing town-hall worker to the cross job, which staffs the chapel and gives
 * two crosses (chapel with one colonist, as in ReligiousProductionTest). The
 * next update credits both, so the threshold is reached and the earned
 * immigrant waits until the next turn starts. That turn settles the arrival
 * before production: 76 - 75 + 2 = 3 crosses, and the next threshold rises to
 * 94. This is an ordinary arrival, not a frozen human choice. Save and Continue
 * keep the pending arrival, and the report only reads the figures.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_167_F200_religion_report_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F58-religious-production.microcol");

    /** New Amsterdam, the colony with the chapel of the fixture. */
    private static final Location COLONY = Location.of(24, 16);

    private static final int COLONY_ID = 0;

    private static final String COLONY_NAME = "New Amsterdam";

    private static final String NONE = "No earned immigrant is waiting.";

    private static final String ARRIVES = "An earned immigrant arrives at the start of your next turn.";

    private static final String CZ_NONE = "Žádný získaný přistěhovalec nečeká.";

    private static final String CZ_ARRIVES = "Získaný přistěhovalec dorazí na začátku vašeho příštího tahu.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_religion_report_output_progress_pending_arrival_and_continue()
            throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ReligionReportPage religion = new ReligionReportPage(
                getContext());
        religion.loadCrossFixture(74);
        game = GamePage.of(getContext());

        // The report opens from the statistics with the current output of one
        // cross and 74 accumulated crosses toward the threshold of 75.
        religion.open();
        religion.verifyTabSelected();
        religion.verifyNationalOutput(1);
        religion.verifyColonyOutput(COLONY_ID, COLONY_NAME, 1);
        religion.verifyAccumulated(74);
        religion.verifyTarget(75, 1);
        religion.verifyImmigration(NONE);
        religion.captureDialog("religionTab", "religion-report-english.png");
        game = religion.closeWithEscape();

        // The town-hall worker takes the cross job; the chapel is staffed at
        // once and the displayed output changes, while the progress does not.
        final ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        game = colony.close();
        religion.open();
        religion.verifyNationalOutput(2);
        religion.verifyColonyOutput(COLONY_ID, COLONY_NAME, 2);
        religion.verifyAccumulated(74);
        religion.verifyTarget(75, 1);
        game = religion.closeWithEscape();

        // The next update credits both crosses and reaches the threshold; the
        // immigrant is pending until the following turn starts.
        game.nextTurnAndCloseDialogs();
        religion.open();
        religion.verifyNationalOutput(2);
        religion.verifyAccumulated(76);
        religion.verifyTarget(75, 0);
        religion.verifyImmigration(ARRIVES);
        religion.captureDialog("religionTab",
                "religion-report-english-pending.png");
        game = religion.closeWithEscape();
        religion.openWithKeyboard();
        religion.verifyTabSelectedAndFocused();
        game = religion.closeWithEscape();

        // Save and Continue keep the same crosses and the same pending arrival.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        religion.open();
        religion.verifyAccumulated(76);
        religion.verifyTarget(75, 0);
        religion.verifyImmigration(ARRIVES);
        game = religion.closeWithEscape();

        // The arrival is settled before production: 76 - 75 + 2 = 3 crosses,
        // and
        // the next threshold rises to 94 for the following immigrant.
        game.nextTurnAndCloseDialogs();
        religion.open();
        religion.verifyNationalOutput(2);
        religion.verifyAccumulated(3);
        religion.verifyTarget(94, 91);
        religion.verifyImmigration(NONE);
        game = religion.closeWithEscape();
    }

    @Test
    @Tag("local")
    void TC_02_czech_labels_match_the_same_figures_and_capture_the_panel()
            throws Exception {
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final ReligionReportPage religion = new ReligionReportPage(
                getContext());
        religion.loadCrossFixture(74);
        GamePage game = GamePage.of(getContext());

        // The Czech labels show the same figures as the English ones.
        religion.open();
        religion.verifyCzechLabels(COLONY_ID, COLONY_NAME, 1, 74, 75, 1,
                CZ_NONE);
        religion.captureDialog("religionTab", "religion-report-czech.png");
        game = religion.closeWithEscape();

        final ColonyScreen colony = game.openColonyAt(COLONY, COLONY_NAME);
        final ColonyJobPage jobs = new ColonyJobPage(getContext());
        jobs.openFromIndoor("id_town_hall", 0);
        jobs.select(ColonyJobChoice.Kind.INDOOR, GoodsType.CROSS);
        jobs.confirm();
        game = colony.close();
        game.nextTurnAndCloseDialogs();

        // The pending arrival is named as an ordinary arrival in Czech too.
        religion.open();
        religion.verifyCzechLabels(COLONY_ID, COLONY_NAME, 2, 76, 75, 0,
                CZ_ARRIVES);
        religion.captureDialog("religionTab",
                "religion-report-czech-pending.png");
        game = religion.closeWithEscape();
    }
}
