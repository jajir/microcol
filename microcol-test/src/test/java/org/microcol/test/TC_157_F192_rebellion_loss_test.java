package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.GameOverEvaluator;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RebellionLossPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A declared rebellion is lost when the Crown holds every surviving rebellion
 * colony (R-0192). In the shared loosing-battles scenario the Dutch have
 * declared independence with Delft and Haarlem as their rebellion colonies;
 * here the Crown already holds Haarlem, as if it had taken it earlier. The
 * player only ends turns: in its own turn the Crown storms Delft, and the
 * rebellion is over, with its explicit reason, in the same turn. The game then
 * leaves for the campaign screen, saves no end position, and a saved open
 * position still continues without repeating the end.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_157_F192_rebellion_loss_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F04-loosing-battles.microcol");

    /** Both rebellion colonies: Delft and Haarlem. */
    private final static Set<Integer> MEMBERS = Set.of(1, 2);

    private final static int DELFT = 1;

    private final static int HAARLEM = 2;

    private final static String DUTCH = "Dutch";

    private final static String CROWN = "Dutch's King";

    /**
     * The Crown storms Delft in its second turn of the shared scenario (TC_04);
     * a margin covers its route with Haarlem already royal. The page stops at
     * the end of the game, and the next verifications fail if it never came.
     */
    private final static int MOST_TURNS = 5;

    private final static String REASON_CODE = GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN;

    private final static String REASON_TEXT = "every surviving rebel colony is back under the Crown";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_the_crown_storms_the_last_rebel_colony_and_the_rebellion_is_lost()
            throws Exception {
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(false);

        // Haarlem is already royal; Delft is the last colony that the rebels
        // hold.
        RebellionLossFixture.prepare(getContext(), testFileName,
                "rebellion-loss-f192.microcol", HAARLEM);
        WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage loss = new RebellionLossPage(getContext());
        loss.verifyRebellionHolds();
        loss.verifyRebellionMembers(MEMBERS);
        loss.verifyColonyOwner(DELFT, DUTCH);
        loss.verifyColonyOwner(HAARLEM, CROWN);

        // The player ends turns until the Crown storms Delft in its own turn.
        loss.endTurnsUntilTheGameEnds(MOST_TURNS);

        // The game explains exactly why the rebellion is lost.
        loss.verifyLossDialog(REASON_TEXT);
        loss.verifyGameOverReason(REASON_CODE);
        loss.verifyColonyOwner(DELFT, CROWN);
        loss.verifyRebellionMembers(MEMBERS);

        // Acknowledging the message leaves the game for the campaign screen.
        loss.closeLossDialog();
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());
        campaign.verifyCampaignScreen();

        // No position was saved at the end, so the main menu offers no
        // Continue.
        campaign.back();
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(false);
    }

    @Test
    @Tag("local")
    void TC_02_a_saved_open_position_still_continues_and_the_end_is_not_repeated()
            throws Exception {
        RebellionLossFixture.prepare(getContext(), testFileName,
                "rebellion-loss-f192-saved.microcol", HAARLEM);
        final GamePage gamePage = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage loss = new RebellionLossPage(getContext());
        final CampaignScreenPage campaign = new CampaignScreenPage(
                getContext());

        // Save: leaving to the menu stores the open position, and Continue
        // opens it again.
        gamePage.exitToMenu();
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        new FreePlayAuditPage(getContext()).continueGame();
        loss.verifyRebellionHolds();
        loss.verifyColonyOwner(DELFT, DUTCH);

        // The player ends turns until the rebellion is lost.
        loss.endTurnsUntilTheGameEnds(MOST_TURNS);
        loss.verifyLossDialog(REASON_TEXT);
        loss.verifyGameOverReason(REASON_CODE);
        loss.closeLossDialog();
        campaign.verifyCampaignScreen();
        campaign.back();

        // The end itself was not saved. Continue opens the saved open position:
        // the rebellion holds, Delft is rebel-held, and no end is shown again.
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        new FreePlayAuditPage(getContext()).continueGame();
        loss.verifyRebellionHolds();
        loss.verifyColonyOwner(DELFT, DUTCH);
        loss.verifyRebellionMembers(MEMBERS);
        loss.verifyNoLossDialogShown(REASON_TEXT);
    }
}
