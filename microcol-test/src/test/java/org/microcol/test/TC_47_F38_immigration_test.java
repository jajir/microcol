package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** Production, the next national arrival phase and saved progress. */
@ExtendWith(ApplicationExtension.class)
public class TC_47_F38_immigration_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F38-immigration.microcol");
    private static final String PENDING = "An earned immigrant arrives at the start of your next turn. Paid passage is unavailable until then.";

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_production_pending_continue_arrival_and_continue()
            throws Exception {
        // Opening Europe reads the saved 99 crosses without producing or
        // arriving.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        ImmigrationPage immigration = new ImmigrationPage(getContext());
        RecruitmentPage recruit = new RecruitmentPage(getContext());
        immigration.verifyProgress("Immigration: 99 / 100 crosses", 99, 100,
                false);
        String before = recruit.poolSnapshot();
        europe.close().openEuroperPort();
        recruit.verifyPool(before, 500);
        // One real colony update reaches the target after this turn's arrival
        // phase.
        GamePage game = europe.close();
        game.nextTurnAndCloseDialogs();
        europe = game.openEuroperPort();
        immigration.verifyProgress("Immigration: 100 / 100 crosses\n" + PENDING,
                100, 100, true);
        immigration.captureDialog("immigration-progress",
                "immigration-pending-english.png");
        String pending = recruit.poolSnapshot();
        recruit.open();
        immigration.verifyPendingWarning(PENDING);
        immigration.closePendingWarning(PENDING);
        recruit.verifyPool(pending, 500);
        // Save and Continue preserve the pending arrival; opening does not
        // settle it.
        europe.close().exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        immigration.verifyProgress("Immigration: 100 / 100 crosses\n" + PENDING,
                100, 100, true);
        recruit.verifyPool(pending, 500);
        // Next turn brings one saved candidate free of charge, then produces
        // one cross.
        game = europe.close();
        ScreenTurnReport report = game.nextTurn().orElseThrow();
        report.verifyThatAtLeastOneEventMessageContains(
                "Religious production attracted an immigrant");
        report.close();
        europe = game.openEuroperPort();
        immigration.verifyProgress("Immigration: 1 / 125 crosses", 1, 125,
                false);
        immigration.verifyArrival(103, 500, 1, 0, 5);
        String arrived = recruit.poolSnapshot();
        recruit.open();
        recruit.verifyOffer(null, 199, true, "Pay passage: ", "");
        recruit.cancel();
        // Another Continue keeps exactly one new person and the same candidate
        // pool.
        europe.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame().openEuroperPort();
        immigration.verifyArrival(103, 500, 1, 0, 5);
        recruit.verifyPool(arrived, 500);
        immigration.verifyProgress("Immigration: 1 / 125 crosses", 1, 125,
                false);
    }

    @Test
    @Tag("local")
    void TC_02_czech_progress_is_readable() throws Exception {
        // Language selection changes the visible progress, not the saved
        // accounting.
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame().openEuroperPort("Evropa");
        ImmigrationPage immigration = new ImmigrationPage(getContext());
        immigration.verifyProgress("Přistěhovalectví: 99 / 100 křížů", 99, 100,
                false);
        immigration.captureDialog("immigration-progress",
                "immigration-czech.png");
    }
}
