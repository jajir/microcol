package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.page.FramedSettingsPage;
import org.microcol.page.GamePage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/**
 * R-0204: the routine turn summary preference. The summary defaults on; when it
 * is switched off, a critical famine notice still opens the report at the start
 * of the turn. The report closes without spending anything. The Settings
 * checkbox is checked in English, Czech and a small window.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_177_F204_turn_summary_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F09-famine-destroy-colony.microcol");

    /** Window size of the small-window check; the default is 1000x925. */
    private final static int SMALL_WIDTH = 900;

    private final static int SMALL_HEIGHT = 750;

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_177_turn_summary_off_keeps_critical_famine_visible()
            throws Exception {
        final FramedSettingsPage page = new FramedSettingsPage(getContext());
        try {
            // Open Settings from the welcome screen; the routine summary is on.
            final SettingPage settings = WelcomePage.of(getContext())
                    .openSetting();
            page.verifyTurnSummary(true);
            page.verifyAppearance();

            // Czech labels and the small window keep the checkbox readable.
            settings.setCzechLanguage();
            page.verifyAppearance();
            page.resizeWindow(SMALL_WIDTH, SMALL_HEIGHT);
            page.verifyAppearance();
            settings.setEnglishLanguage();
            page.verifyAppearance();

            // Switch the routine summary off with the keyboard and go back.
            page.press("settingTurnSummary", KeyCode.SPACE);
            page.verifyTurnSummary(false);
            final GamePage gamePage = settings.goBack().loadGame();

            // The famine loss is critical, so the report opens despite the
            // switch.
            final Optional<ScreenTurnReport> report = gamePage.nextTurn();
            gamePage.verifyTurnReportPresent(report);
            report.get().verifyFamineLossCount(1);
            report.get().verifyThatAtLeastOneEventMessageContains(
                    "Famine in colony Delft killed one");
            report.get().close();
        } finally {
            // Restore the defaults even when a step fails, so later scenarios
            // keep the routine summary and the normal window size.
            getContext().getClassFromGuice(GamePreferences.class)
                    .setShowTurnSummary(
                            GamePreferences.DEFAULT_SHOW_TURN_SUMMARY);
            page.resizeWindow(GamePreferences.DEFAULT_WINDOW_WIDTH,
                    GamePreferences.DEFAULT_WINDOW_HEIGHT);
        }
    }

}
