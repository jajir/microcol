package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.GamePage;
import org.microcol.page.HallOfFamePage;
import org.microcol.page.ScoreBoundaryPage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * R-0204: with the routine turn summary switched off, the final scored result
 * still reaches its boundary choice and shows its breakdown beside the
 * unchanged ranking. The steps are those of TC_163 TC_01.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_179_F204_summary_off_final_result_test
        extends AbstractMicroColTest {

    /** Undeclared Dutch nation that freezes its score at spring 1800. */
    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
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
                    testFileName, "turn-summary-f204-final.microcol");
            HallOfFameFixture.writeFullRanking();
            WelcomePage.of(getContext()).loadGame();
            final HallOfFamePage hall = new HallOfFamePage(getContext());
            final ScoreBoundaryPage boundary = new ScoreBoundaryPage(
                    getContext());

            // Spring 1800 freezes the result and the player postpones the
            // choice, even though the routine summary is off.
            boundary.endTurnToBoundary();
            boundary.verifyAwaitingChoice();
            boundary.decideLater();

            // The result is below the rivals: it is not ranked, but its
            // breakdown is shown next to the unchanged ranking.
            hall.openFromStatistics();
            hall.verifyRankingRows(HallOfFameFixture.RIVALS);
            hall.verifyNotRanked();
            hall.verifyCurrentResultShown();
            hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
            final GamePage game = hall.closeStatistics();
            game.verifyGameInFront();
        } finally {
            getContext().getClassFromGuice(GamePreferences.class)
                    .setShowTurnSummary(
                            GamePreferences.DEFAULT_SHOW_TURN_SUMMARY);
            HallOfFameFixture.removeRanking();
        }
    }

}
