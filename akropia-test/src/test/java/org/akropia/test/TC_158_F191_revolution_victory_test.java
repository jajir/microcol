package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.gui.SuccessCuePlayer;
import org.akropia.mock.RecordingSuccessCuePlayer;
import org.akropia.model.Location;
import org.akropia.page.AttackPage;
import org.akropia.page.CampaignScreenPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.GamePage;
import org.akropia.page.RebellionLossPage;
import org.akropia.page.RebellionVictoryPage;
import org.akropia.page.SuccessCuePage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.inject.Binder;

import javafx.stage.Stage;

/**
 * A declared rebellion is won when the rebels hold every surviving member and
 * the Crown has at most two living ground units (R-0191). In the shared
 * independence scenario the Dutch have declared independence with all four of
 * their colonies as members. Here the Crown holds Delft with its unarmed
 * residents, and only two royal regulars are left in its reserve. The player's
 * armed colonist beside Delft attacks it in its own turn and wins; the colony
 * falls back to the rebels, and the rebellion is won at that moment with its
 * explicit reason. The game then leaves for the campaign screen, saves no end
 * position, and a saved open position still continues without repeating the
 * end. The victory message requests one success cue that Continue does not
 * repeat; without an audio device the victory is shown and recorded the same
 * way (R-0228).
 */
@ExtendWith(ApplicationExtension.class)
public class TC_158_F191_revolution_victory_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.akropia");

    /**
     * Colony of the scenario that the Crown holds here. No unit of the shared
     * scenario stands on its square, so the Crown can hold it lawfully.
     */
    private final static int DELFT = 1;

    private final static Location DELFT_AT = Location.of(22, 12);

    private final static String DELFT_NAME = "Delft";

    /** Free land square beside Delft where the Dutch colonist attacks from. */
    private final static Location ATTACK_FROM = Location.of(23, 12);

    private final static String DUTCH = "Dutch";

    private final static String CROWN = RoyalVictoryFixture.CROWN;

    private final static String REASON_TEXT = "every surviving rebel colony is yours";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(primaryStage, getClass(), testFileName);
    }

    /** The success cue is only counted, never heard (R-0228). */
    @Override
    protected void bind(final Binder binder) {
        binder.bind(SuccessCuePlayer.class)
                .toInstance(new RecordingSuccessCuePlayer());
    }

    @Test
    @Tag("local")
    void TC_01_the_player_retakes_the_last_crown_colony_and_the_rebellion_is_won()
            throws Exception {
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(false);

        RoyalVictoryFixture.prepare(getContext(), testFileName,
                "rebellion-victory-f191.akropia", DELFT, ATTACK_FROM);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        rebellion.verifyRebellionHolds();
        rebellion.verifyColonyOwner(DELFT, CROWN);

        // The player's colonist attacks Delft, whose unarmed residents defend
        // as a group, and wins the real battle.
        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();

        // The battle that retakes Delft ends the rebellion with a win.
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyCaptureNotice(DELFT_NAME);
        victory.verifyVictoryRecorded();
        rebellion.verifyColonyOwner(DELFT, DUTCH);

        // Acknowledging the victory message and then the capture notice beneath
        // it leaves the game for the campaign screen.
        victory.closeVictoryDialog(REASON_TEXT);
        victory.closeCaptureNotice(DELFT_NAME);
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        campaign.verifyCampaignScreen();

        // The ending does not replace the last successfully loaded position.
        campaign.back();
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        new FreePlayAuditPage(getContext()).continueGame();
        rebellion.verifyRebellionHolds();
        rebellion.verifyColonyOwner(DELFT, CROWN);
        victory.verifyNoVictoryDialogShown(REASON_TEXT);
    }

    @Test
    @Tag("local")
    void TC_02_a_saved_open_position_still_continues_and_the_victory_is_not_repeated()
            throws Exception {
        RoyalVictoryFixture.prepare(getContext(), testFileName,
                "rebellion-victory-f191-saved.akropia", DELFT, ATTACK_FROM);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());

        // Save: leaving to the menu stores the open position before the battle,
        // and Continue opens it again.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        final GamePage continued = new FreePlayAuditPage(getContext())
                .continueGame();
        rebellion.verifyRebellionHolds();
        rebellion.verifyColonyOwner(DELFT, CROWN);

        // The same real battle ends the rebellion with a win.
        continued.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyCaptureNotice(DELFT_NAME);
        victory.verifyVictoryRecorded();
        victory.closeVictoryDialog(REASON_TEXT);
        victory.closeCaptureNotice(DELFT_NAME);
        campaign.verifyCampaignScreen();
        campaign.back();

        // The won end was not saved. Continue opens the saved open position
        // again: Delft is still Crown-held, and no victory is shown again.
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        new FreePlayAuditPage(getContext()).continueGame();
        rebellion.verifyRebellionHolds();
        rebellion.verifyColonyOwner(DELFT, CROWN);
        victory.verifyNoVictoryDialogShown(REASON_TEXT);
    }

    @Test
    @Tag("local")
    void TC_03_the_won_rebellion_requests_one_success_cue_and_continue_requests_none()
            throws Exception {
        RoyalVictoryFixture.prepare(getContext(), testFileName,
                "rebellion-victory-f191-cue.akropia", DELFT, ATTACK_FROM);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final SuccessCuePage cue = new SuccessCuePage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        rebellion.verifyRebellionHolds();
        cue.verifySuccessCueRequests(0);

        // The real battle that retakes Delft wins the rebellion; one success
        // cue accompanies the victory message.
        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyCaptureNotice(DELFT_NAME);
        victory.verifyVictoryRecorded();
        cue.verifySuccessCueRequests(1);

        // Acknowledging both messages leaves for the campaign screen without
        // another cue.
        victory.closeVictoryDialog(REASON_TEXT);
        victory.closeCaptureNotice(DELFT_NAME);
        campaign.verifyCampaignScreen();
        cue.verifySuccessCueRequests(1);

        // Continue reopens the open position; neither the victory message nor
        // its cue is repeated.
        campaign.back();
        new FreePlayAuditPage(getContext()).continueGame();
        rebellion.verifyRebellionHolds();
        victory.verifyNoVictoryDialogShown(REASON_TEXT);
        cue.verifySuccessCueRequests(1);
    }

    @Test
    @Tag("local")
    void TC_04_without_an_audio_device_the_victory_is_still_shown_and_recorded()
            throws Exception {
        RoyalVictoryFixture.prepare(getContext(), testFileName,
                "rebellion-victory-f191-no-audio.akropia", DELFT, ATTACK_FROM);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final SuccessCuePage cue = new SuccessCuePage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        rebellion.verifyRebellionHolds();

        // The audio device is gone before the battle.
        cue.makeAudioDeviceUnavailable();

        // The same real battle wins the rebellion; the failed cue changes
        // nothing that the player sees or the model records.
        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyCaptureNotice(DELFT_NAME);
        victory.verifyVictoryRecorded();
        rebellion.verifyColonyOwner(DELFT, DUTCH);
        cue.verifySuccessCueRequests(1);

        // Acknowledging both messages still leaves for the campaign screen.
        victory.closeVictoryDialog(REASON_TEXT);
        victory.closeCaptureNotice(DELFT_NAME);
        campaign.verifyCampaignScreen();
    }
}
