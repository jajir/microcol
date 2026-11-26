package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.page.FramedSettingsPage;
import org.akropia.page.GamePage;
import org.akropia.page.HallOfFamePage;
import org.akropia.page.RetirePage;
import org.akropia.page.MainMenuPage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** R-0204: disabling routine summaries never suppresses a final result. */
@ExtendWith(ApplicationExtension.class)
public class TC_179_F204_summary_off_final_result_test
        extends AbstractAkropiaTest {

    /** Undeclared Dutch nation that freezes its score at spring 1800. */
    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.akropia");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        HallOfFameFixture.removeRanking();
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_summary_off_keeps_the_final_result_and_its_breakdown()
            throws Exception {
        try {
            // Switch the routine summary off in Settings, then leave Settings.
            final SettingPage settings = WelcomePage.of(getContext())
                    .openSetting();
            final FramedSettingsPage page = new FramedSettingsPage(
                    getContext());
            page.press("settingTurnSummary", KeyCode.SPACE);
            page.verifyTurnSummary(false);
            settings.goBack();

            // The prepared game stands in winter 1799, and ten authored rivals
            // are already saved with totals above the Dutch result.
            ScoreBoundaryFixture.prepareBeforeBoundary(getContext(),
                    testFileName, "turn-summary-f204-final.akropia");
            HallOfFameFixture.writeFullRanking();
            WelcomePage.of(getContext()).loadGame();
            final HallOfFamePage hall = new HallOfFamePage(getContext());
            final RetirePage retire = new RetirePage(getContext());

            // Cross the former calendar boundary, then explicitly retire.
            // The final result must appear even with routine summaries off.
            GamePage.of(getContext()).nextTurnWithoutOpeningReport();
            retire.openRetire();
            retire.verifyPreviewShown();
            retire.rememberPreview();
            retire.confirmRetire();
            retire.verifyLiveRetirementFinished();
            retire.verifyRetirementNotice();
            retire.closeRetirementNotice();
            hall.awaitResultDialog();
            hall.verifyResultDialogShown();

            // The result remains below the ten authored rivals.
            hall.verifyRankingRows(HallOfFameFixture.RIVALS);
            hall.verifyNotRanked();
            hall.verifyCurrentResultShown();
            hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
            hall.closeResultDialog();
            new MainMenuPage(getContext()).verifyMainMenu();
        } finally {
            getContext().getClassFromGuice(GamePreferences.class)
                    .setShowTurnSummary(
                            GamePreferences.DEFAULT_SHOW_TURN_SUMMARY);
            HallOfFameFixture.removeRanking();
        }
    }

}
