package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.page.CongressPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NativeAlarmPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The Aztec capital next to the Dutch colony shows its native attitude, the
 * exact signed alarm change and the provoker in the hover text and in the N
 * details. The scenario starts stable; the Dutch choose Pocahontas in Congress
 * and real turn updates then make the alarm fall with her arrival and rise by a
 * quarter. Continue from disk shows the same values, and no visible rival
 * provokes the capital.
 *
 * <p>
 * The fixture is the current-schema F104 scenario, as in TC_113: saved alarm 30
 * is uneasy with tribal anger 10 and the Congress has 100 bells for the chosen
 * founder. Dutch pressure on the capital is 2.5; Pocahontas halves it to 1.25.
 * The arrival turn is observed at 30, cleared and updated to 1.25, a change of
 * -28.75. The next turn gives 1.25 - 1 + 1.25 = 1.5, a change of +0.25.
 * </p>
 */
@ExtendWith(ApplicationExtension.class)
public class TC_124_F115_native_alarm_display_test
        extends AbstractMicroColTest {

    /**
     * Aztec capital next to the Dutch colony, learned by sight at the start.
     */
    private static final Location AZTEC_CAPITAL = Location.of(23, 15);

    private static final String UNKNOWN_PROVOKER = "Provoker unknown.";

    private static final String UNKNOWN_PROVOKER_CS = "Podněcovatel není znám.";

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F104-native-relations.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_native_alarm_is_shown_for_real_turn_updates() throws Exception {
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        NativeAlarmPage alarm = NativeAlarmPage.of(getContext());

        // Before any turn the alarm is stable at 30: uneasy, with no change.
        game.moveMouseAtLocation(AZTEC_CAPITAL);
        alarm.verifyStatusBarShows("Attitude: uneasy.",
                "Alarm change 0, stable.", UNKNOWN_PROVOKER);

        // N opens the details with focus on the list; Escape closes them.
        alarm.openWithNKey();
        alarm.verifyDialogOpen();
        alarm.verifyListHasFocus();
        alarm.verifyDialogRelation("Attitude: uneasy.",
                "Alarm change 0, stable.", UNKNOWN_PROVOKER);
        alarm.verifyDialogFitsWindow();
        alarm.captureDialog("TC124-native-alarm-start-en.png");
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();

        // Choose Pocahontas in Congress; the 100 saved bells bring her next
        // turn.
        congress.open();
        congress.select(Founder.POCAHONTAS);
        congress.choose();
        congress.close();

        // The arrival turn clears the alarm and leaves 1.25: a fall of 28.75
        // with no exclamation mark, read from the value at the turn start.
        var report = game.nextTurn();
        game.verifyTurnReportPresent(report);
        report.orElseThrow().verifyThatAtLeastOneEventMessageContains(
                "Pocahontas has joined Congress.");
        report.orElseThrow().close();
        game.moveMouseAtLocation(AZTEC_CAPITAL);
        alarm.verifyStatusBarShows("Attitude: content.",
                "Alarm change -28.75, falling.", UNKNOWN_PROVOKER);
        alarm.openWithNKey();
        alarm.verifyListHasFocus();
        alarm.verifyDialogRelation("Attitude: content.",
                "Alarm change -28.75, falling.", UNKNOWN_PROVOKER);
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();

        // Continue from disk keeps the same values, with no second update.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        alarm.openWithNKey();
        alarm.verifyDialogRelation("Attitude: content.",
                "Alarm change -28.75, falling.", UNKNOWN_PROVOKER);
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();

        // With the pointer resting on the capital, the next real turn adds a
        // quarter; the hover text follows the update without moving the mouse.
        game.moveMouseAtLocation(AZTEC_CAPITAL);
        alarm.verifyStatusBarShows("Attitude: content.",
                "Alarm change -28.75, falling.", UNKNOWN_PROVOKER);
        alarm.nextTurnKeepingPointer().ifPresent(ScreenTurnReport::close);
        alarm.verifyStatusBarShows("Attitude: content.",
                "Alarm change +0.25, rising.", UNKNOWN_PROVOKER);
        alarm.openWithNKey();
        alarm.verifyListHasFocus();
        alarm.verifyDialogRelation("Attitude: content.",
                "Alarm change +0.25, rising.", UNKNOWN_PROVOKER);
        alarm.captureDialog("TC124-native-alarm-rising-en.png");
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();

        // Continue after that turn keeps the rise and the unknown provoker.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        alarm.openWithNKey();
        alarm.verifyDialogRelation("Attitude: content.",
                "Alarm change +0.25, rising.", UNKNOWN_PROVOKER);
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();
    }

    @Test
    @Tag("local")
    void TC_02_czech_details_name_all_three_meanings() throws Exception {
        // Select Czech through the existing settings, then load the fixture.
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        GamePage game = settings.goBack().loadGame();
        CongressPage congress = new CongressPage(getContext());
        NativeAlarmPage alarm = NativeAlarmPage.of(getContext());

        // The stable uneasy capital reads in Czech on hover and in the details.
        game.moveMouseAtLocation(AZTEC_CAPITAL);
        alarm.verifyStatusBarShows("Postoj: neklidné.",
                "Změna poplachu 0, stálá.", UNKNOWN_PROVOKER_CS);
        alarm.openWithNKey();
        alarm.verifyDialogOpen();
        alarm.verifyListHasFocus();
        alarm.verifyDialogRelation("Postoj: neklidné.",
                "Změna poplachu 0, stálá.", UNKNOWN_PROVOKER_CS);
        alarm.verifyDialogFitsWindow();
        alarm.captureDialog("TC124-native-alarm-start-cs.png");
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();

        // After choosing Pocahontas, the real arrival turn makes the Czech text
        // fall by the exact value.
        congress.open();
        congress.select(Founder.POCAHONTAS);
        congress.choose();
        congress.close();
        game.nextTurn().ifPresent(ScreenTurnReport::close);
        alarm.openWithNKey();
        alarm.verifyListHasFocus();
        alarm.verifyDialogRelation("Postoj: spokojené.",
                "Změna poplachu -28.75, klesá.", UNKNOWN_PROVOKER_CS);
        alarm.verifyDialogFitsWindow();
        alarm.captureDialog("TC124-native-alarm-falling-cs.png");
        alarm.closeWithEscape();
        alarm.verifyDialogClosed();
    }
}
