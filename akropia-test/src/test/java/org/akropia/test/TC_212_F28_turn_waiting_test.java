package org.akropia.test;

import java.io.File;

import org.akropia.page.DialogMessagePage;
import org.akropia.page.NationalTurnPage;
import org.akropia.page.SettingPage;
import org.akropia.page.TurnWaitingPage;
import org.akropia.page.WelcomePage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** UC-005: visible, non-dismissable feedback at the national turn boundary. */
@ExtendWith(ApplicationExtension.class)
public class TC_212_F28_turn_waiting_test extends AbstractAkropiaTest {
    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F28-national-turn-order.akropia"));
    }

    @Test
    @Tag("local")
    void TC_01_fast_round_keeps_rowing_for_at_least_one_second()
            throws Exception {
        // Start at a prepared human turn and observe the actual modal lifetime.
        WelcomePage.of(getContext()).loadGame();
        final TurnWaitingPage wait = new TurnWaitingPage(getContext());
        wait.observe();
        try {
            // A normal click completes exactly one round, with visible motion.
            wait.requestTurn();
            wait.awaitClosed();
            wait.verifyClosedAfterMinimum();
            wait.verifyRound(19);
        } finally {
            wait.cleanup();
        }
    }

    @Test
    @Tag("local")
    void TC_02_slow_round_stays_modal_and_animates_until_work_finishes()
            throws Exception {
        // Hold the model at the national boundary to represent a slow AI turn.
        WelcomePage.of(getContext()).loadGame();
        final TurnWaitingPage wait = new TurnWaitingPage(getContext());
        wait.observe();
        wait.holdTurn(false);
        try {
            wait.requestTurn();
            wait.verifyHeldAndAnimated(
                    "Other players are playing, please wait.");
            wait.captureDialog("turn-wait", "turn-wait-rowers.png");

            // Escape, a native close request and owner input cannot dismiss it.
            wait.attemptDismissal();
            wait.verifyHeldAndAnimated(
                    "Other players are playing, please wait.");
            wait.releaseTurn();
            wait.awaitClosed();
            wait.verifyClosedAfterMinimum();
            wait.verifyRound(19);
        } finally {
            wait.cleanup();
        }
    }

    @Test
    @Tag("local")
    void TC_03_failed_turn_releases_waiting_and_shows_recovery_message()
            throws Exception {
        // Fail the worker after the modal is visible, without changing saves.
        WelcomePage.of(getContext()).loadGame();
        final TurnWaitingPage wait = new TurnWaitingPage(getContext());
        wait.observe();
        wait.holdTurn(true);
        try {
            wait.requestTurn();
            wait.verifyHeldAndAnimated(
                    "Other players are playing, please wait.");
            wait.releaseTurn();
            wait.awaitClosed();
            wait.verifyClosedAfterMinimum();

            // The player can acknowledge the failure instead of being trapped.
            new NationalTurnPage(getContext()).verifyRecoveryMessage(
                    "The turn could not finish. No saved game was overwritten. Continue or load a saved game to play again.");
            DialogMessagePage.of(getContext()).close();
        } finally {
            wait.cleanup();
        }
    }

    @Test
    @Tag("local")
    void TC_04_czech_wait_message_and_animation() throws Exception {
        // Choose Czech through settings before loading the same prepared game.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.verifyLanguageOptionsShown();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        final TurnWaitingPage wait = new TurnWaitingPage(getContext());
        wait.observe();
        wait.holdTurn(false);
        try {
            // The localized message stays readable below the moving rowers.
            wait.requestTurn();
            wait.verifyHeldAndAnimated("Ostatní hráči hrají, prosím čekejte.");
            wait.captureDialog("turn-wait", "turn-wait-rowers-cs.png");
            wait.releaseTurn();
            wait.awaitClosed();
            wait.verifyClosedAfterMinimum();
            wait.verifyRound(19);
        } finally {
            wait.cleanup();
        }
    }
}
