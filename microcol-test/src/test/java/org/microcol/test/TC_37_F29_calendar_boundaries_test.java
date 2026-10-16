package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.microcol.model.campaign.po.RecoverySlot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_37_F29_calendar_boundaries_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F29-calendar-boundaries.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "latest-turn.microcol"));
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "previous-decade.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_complete_rounds_cross_the_season_boundary() throws Exception {
        // Load the last annual turn and advance complete rounds with the UI.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyDisplayedDate("Year 1599 AD");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1600 AD Spring");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1600 AD Summer");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1600 AD Autumn");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1600 AD Winter");

        // Save and continue the winter position, then advance into spring.
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        audit.verifyDisplayedDate("Year 1600 AD Winter");
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1601 AD Spring");
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
        audit.verifyDisplayedDate("Year 1599 AD");
        audit.verifyCalendarAndGold(109, 10000);
        audit.verifyAutosaveUnchanged(saved);
        audit.verifyAutosaveLastModifiedTime(modified);
    }

    @Test
    @Tag("local")
    void TC_03_loading_a_later_start_preserves_its_date() throws Exception {
        // Load an explicit later-date scenario through the ordinary chooser.
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.chooseLoadFile(new File(
                "src/test/scenarios/F29-calendar-late-start.microcol"));
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        audit.verifyDisplayedDate("Year 1630 AD Winter");

        // Complete a round and preserve the resulting spring through Continue.
        game.nextTurnAndCloseDialogs();
        audit.verifyDisplayedDate("Year 1631 AD Spring");
        game.exitToMenu().verifyMainScreen();
        audit.continueGame();
        audit.verifyDisplayedDate("Year 1631 AD Spring");
    }

    @Test
    @Tag("local")
    void TC_04_fresh_human_turn_writes_recovery() throws Exception {
        // A fresh human turn writes Latest turn without changing Latest
        // position or creating a decade point in 1599.
        WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_POSITION);
        audit.verifyRecoveryAbsent(RecoverySlot.LATEST_TURN);
        audit.verifyRecoveryAbsent(RecoverySlot.PREVIOUS_DECADE);
        audit.deliverTurnStartedNotification(true, true);
        audit.verifyRecoveryTurn(RecoverySlot.LATEST_TURN, 109);
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
