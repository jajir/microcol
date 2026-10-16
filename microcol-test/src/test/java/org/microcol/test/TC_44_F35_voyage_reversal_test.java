package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.unit.Ship;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.VoyageReversalPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

/** Reverse the deterministic four-turn voyage through the Europe controls. */
@ExtendWith(ApplicationExtension.class)
public class TC_44_F35_voyage_reversal_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F35-voyage-reversal.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_escape_confirm_and_continue_return_to_source()
            throws Exception {
        // The stored voyage has spent one of four updates toward Europe.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        final VoyageReversalPage voyage = new VoyageReversalPage(getContext());
        voyage.verifyTransit(true, 1, 3, "Turns remaining: 3");
        voyage.verifyReverseAction("Reverse course");

        // Cancel and Escape leave the route and cargo untouched.
        voyage.openReverse();
        voyage.verifyConfirmation("Reverse course",
                "Send this ship to New World? Turns remaining: 1.");
        voyage.cancelReverse();
        voyage.verifyTransit(true, 1, 3, "Turns remaining: 3");
        voyage.openReverse();
        voyage.escapeReverse();
        voyage.verifyTransit(true, 1, 3, "Turns remaining: 3");

        // Confirmation selects the recorded coast, one update away.
        voyage.openReverse();
        voyage.confirmReverse();
        voyage.verifyTransit(false, 1, 1, "Turns remaining: 1");
        // Reopen the refreshed action by keyboard; Escape after a prior Yes
        // must not reuse that confirmation or change the route.
        voyage.openReverseWithKeyboard();
        voyage.verifyConfirmation("Reverse course",
                "Send this ship to Europe? Turns remaining: 3.");
        voyage.escapeReverse();
        voyage.verifyTransit(false, 1, 1, "Turns remaining: 1");
        voyage.captureDialog("voyage-reverse-3", "voyage-reversed.png");
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        europe = game.openEuroperPort();
        voyage.verifyTransit(false, 1, 1, "Turns remaining: 1");
        game = europe.close();
        game.nextTurnAndCloseDialogs();
        voyage.verifyReturnedToSource();
    }

    @Test
    @Tag("local")
    void TC_02_stale_confirmation_reports_rejection_and_refreshes_lists()
            throws Exception {
        // Open an offer, then let the route change before pressing Confirm.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.openEuroperPort();
        final VoyageReversalPage voyage = new VoyageReversalPage(getContext());
        voyage.openReverse();
        WaitForAsyncUtils
                .asyncFx(() -> getModel().reverseVoyage(getModel()
                        .offerVoyageReversal((Ship) getModel().getUnitById(3))))
                .get();
        voyage.confirmReverse();
        voyage.verifyStaleFeedback(
                "This voyage changed. Review the ship and try again.");
        voyage.closeStaleFeedback();
        voyage.verifyTransit(false, 1, 1, "Turns remaining: 1");
    }

    @Test
    @Tag("local")
    void TC_03_occupied_source_waits_at_zero_and_retries_without_cargo_loss()
            throws Exception {
        // Occupy the recorded coast before reversing the in-transit ship.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        EuropePortScreen europe = game.openEuroperPort();
        final VoyageReversalPage voyage = new VoyageReversalPage(getContext());
        WaitForAsyncUtils.asyncFx(() -> getModel().getUnitById(1000)
                .placeToLocation(Location.of(2, 17))).get();
        voyage.openReverse();
        voyage.confirmReverse();
        game = europe.close();
        game.nextTurnAndCloseDialogs();
        europe = game.openEuroperPort();
        voyage.verifyWaitingAtSource();
        voyage.captureDialog("voyage-remaining-3",
                "voyage-reversal-waiting.png");
        game = europe.close();

        // Another owner update stays at zero; clearing the square permits one
        // arrival on the following update.
        game.nextTurnAndCloseDialogs();
        europe = game.openEuroperPort();
        voyage.verifyWaitingAtSource();
        game = europe.close();
        WaitForAsyncUtils.asyncFx(() -> getModel().getUnitById(1000)
                .placeToLocation(Location.of(2, 19))).get();
        game.nextTurnAndCloseDialogs();
        voyage.verifyReturnedToSource();
    }

    @Test
    @Tag("local")
    void TC_04_czech_reversal_controls_and_escape_keep_voyage()
            throws Exception {
        // Change the language before opening the voyage in Europe.
        final org.microcol.page.SettingPage setting = WelcomePage
                .of(getContext()).openSetting();
        setting.setCzechLanguage();
        setting.goBack();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.openEuroperPort("Evropa");
        final VoyageReversalPage voyage = new VoyageReversalPage(getContext());
        voyage.verifyReverseAction("Obrátit směr");
        voyage.openReverse();
        voyage.verifyConfirmation("Obrátit směr",
                "Poslat tuto loď do: Nový svět? Zbývající tahy: 1.");
        voyage.captureDialog("voyageReverseConfirm",
                "voyage-reversal-czech.png");
        voyage.escapeReverse();
        voyage.verifyTransit(true, 1, 3, "Zbývající tahy: 3");
    }
}
