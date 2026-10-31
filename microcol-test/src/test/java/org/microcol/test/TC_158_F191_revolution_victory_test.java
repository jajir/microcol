package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RebellionLossPage;
import org.microcol.page.RebellionVictoryPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

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
 * end.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_158_F191_revolution_victory_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

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
                ".microcol", "autosave.microcol"));
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_the_player_retakes_the_last_crown_colony_and_the_rebellion_is_won()
            throws Exception {
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(false);

        RoyalVictoryFixture.prepare(getContext(), testFileName,
                "rebellion-victory-f191.microcol", DELFT, ATTACK_FROM);
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
                "rebellion-victory-f191-saved.microcol", DELFT, ATTACK_FROM);
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
}
