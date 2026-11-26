package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.WelcomePage;
import org.akropia.model.game.po.RecoverySlot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_37_F29_calendar_boundaries_test extends AbstractAkropiaTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F29-calendar-boundaries.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "latest-turn.akropia"));
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "previous-decade.akropia"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_complete_rounds_cross_the_season_boundary() throws Exception {
        // Complete the last summer day and inspect the first autumn date.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyDisplayedDate("Summer 21 · Year 1");
        audit.hoverCalendar();
        audit.verifyCalendarDescription(
                "Day 7 of Boedromion · Autumn begins in 1 turn.");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Autumn 1 · Year 1");
        audit.hoverCalendar();
        audit.verifyCalendarDescription(
                "Day 1 of Pyanepsion · Winter begins in 21 turns.");

        // Continue retains the exact day and the next round advances one day.
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        audit.verifyDisplayedDate("Autumn 1 · Year 1");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Autumn 2 · Year 1");
    }

    @Test
    @Tag("local")
    void TC_02_continue_preserves_the_opened_save() throws Exception {
        // Menu entry explicitly saves the loaded position.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        game.exitToMenu().verifyMainScreen();
        final byte[] saved = audit.readAutosave();
        final FileTime modified = audit.getAutosaveLastModifiedTime();

        // Continue must neither advance the calendar nor rewrite that file.
        audit.continueGame();
        audit.verifyDisplayedDate("Summer 21 · Year 1");
        audit.verifyCalendarAndGold(20, 10000);
        audit.verifyAutosaveUnchanged(saved);
        audit.verifyAutosaveLastModifiedTime(modified);
    }

    @Test
    @Tag("local")
    void TC_03_loading_a_later_start_preserves_its_date() throws Exception {
        // Load an last-day-of-year scenario through the ordinary chooser.
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.chooseLoadFile(
                new File("src/test/scenarios/F29-calendar-late-start.akropia"));
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        audit.verifyDisplayedDate("Spring 21 · Year 1");
        audit.hoverCalendar();
        audit.verifyCalendarDescription(
                "Day 7 of Skirophorion · Summer begins in 1 turn.");

        // Complete a round and preserve the resulting summer through Continue.
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Summer 1 · Year 2");
        game.exitToMenu().verifyMainScreen();
        audit.continueGame();
        audit.verifyDisplayedDate("Summer 1 · Year 2");
    }

    @Test
    @Tag("local")
    void TC_04_fresh_human_turn_writes_recovery() throws Exception {
        // A fresh human turn writes Latest turn without changing Latest
        // position or creating a decade point.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.deliverTurnStartedNotification(true, true);
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_TURN, 20);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
    }

    @Test
    @Tag("local")
    void TC_05_resumed_human_turn_does_not_write_recovery() throws Exception {
        // A resumed human turn leaves all recovery slots unchanged.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.deliverTurnStartedNotification(false, true);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
    }

    @Test
    @Tag("local")
    void TC_06_fresh_computer_turn_does_not_write_recovery() throws Exception {
        // A fresh computer turn leaves all recovery slots unchanged.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.deliverTurnStartedNotification(true, false);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
    }

    @Test
    @Tag("local")
    void TC_07_resumed_computer_turn_does_not_write_recovery()
            throws Exception {
        // A resumed computer turn leaves all recovery slots unchanged.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.deliverTurnStartedNotification(false, false);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
    }
}
