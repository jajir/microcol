package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.HallOfFamePage;
import org.microcol.page.RebellionLossPage;
import org.microcol.page.RetirePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The retirement of R-0194 as the player meets it. In scored free play before
 * spring 1800 the real menu Exit leaves the game and records nothing, and a
 * cancelled Retire changes nothing; a confirmed Retire freezes one score, shows
 * its notice, breakdown and ranking, and returns to the campaign screen. After
 * an unscored continuation a confirmed Retire keeps the earlier frozen result
 * and ranks it once. In a declared rebellion that is still open, cancelling
 * keeps the rebellion running, and a confirmed Retire records the score without
 * an independence bonus.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_164_F194_retirement_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** Shared scenario in which the Dutch can declare independence (R-0191). */
    private final static File rebellionFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    /** Colony of the independence scenario that the Crown holds. */
    private final static int DELFT = 1;

    /** Free land square beside Delft where the Dutch colonist stands. */
    private final static Location ATTACK_FROM = Location.of(23, 12);

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_exit_and_cancelled_retire_record_nothing_and_confirmed_retire_freezes_one_result()
            throws Exception {
        // The prepared Dutch game stands in winter 1799, before any frozen
        // result, with no saved ranking and an undeclared human nation.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "retire-f194-live.microcol");
        HallOfFameFixture.removeRanking();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RetirePage retire = new RetirePage(getContext());
        retire.verifyOffered(true);
        retire.verifyNoFrozenResult();
        retire.verifyTurn(ScoreBoundaryFixture.WINTER_1799);
        final int ranked = retire.rankingSize();

        // Exit is the real menu button: it stores the position and records
        // no score, so the same live game continues afterwards.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        retire.verifyOffered(true);
        retire.verifyExitRecordedNothing(ranked);

        // Retire, then cancel: the preview names the score, the consequence is
        // explained, and the live game stays exactly as it was.
        retire.openRetire();
        retire.verifyDialogShown(true);
        retire.verifyPreviewShown();
        retire.verifyConsequenceShown(false);
        retire.verifyChoicesOffered();
        retire.rememberPreview();
        retire.cancelRetire();
        retire.verifyDialogShown(false);
        retire.verifyCanceled(ScoreBoundaryFixture.WINTER_1799, ranked);

        // Retire again and confirm: the previewed score is frozen once and
        // ranked as the only entry. The end notice is acknowledged, then the
        // finished result with its breakdown is shown, and the campaign screen
        // follows.
        retire.openRetire();
        retire.verifyPreviewUnchanged();
        retire.confirmRetire();
        retire.verifyDialogShown(false);
        retire.verifyLiveRetirementFinished();
        retire.verifyStoredAsOnlyEntry();
        retire.verifyRetirementNotice();
        retire.closeRetirementNotice();
        final HallOfFamePage result = new HallOfFamePage(getContext());
        result.awaitResultDialog();
        result.verifyResultDialogShown();
        result.verifyRankedAt(1);
        result.verifyCurrentResultShown();
        result.verifyStoredEntries(1);
        result.closeResultDialog();
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
    }

    @Test
    @Tag("local")
    void TC_02_retire_after_unscored_continuation_keeps_the_original_result_and_adds_no_second_one()
            throws Exception {
        // The prepared game already continued without scoring and stands in
        // winter 1849 with its frozen result of spring 1800.
        ScoreBoundaryFixture.prepareBeforeEnd(getContext(), testFileName,
                "retire-f194-continued.microcol");
        HallOfFameFixture.removeRanking();
        WelcomePage.of(getContext()).loadGame();
        final RetirePage retire = new RetirePage(getContext());
        retire.verifyOffered(true);
        retire.verifyTurn(ScoreBoundaryFixture.WINTER_1849);
        retire.rememberPreview();

        // The preview keeps the frozen result; cancelling keeps it too.
        retire.openRetire();
        retire.verifyDialogShown(true);
        retire.verifyPreviewShown();
        retire.verifyConsequenceShown(true);
        retire.cancelRetire();
        retire.verifyDialogShown(false);
        retire.verifyOffered(true);
        retire.verifyPreviewUnchanged();

        // A confirmed retirement ends the game with the same identity and
        // total, and the stored ranking holds that result once.
        retire.openRetire();
        retire.confirmRetire();
        retire.verifyDialogShown(false);
        retire.verifyContinuedRetirementFinished();
        retire.verifyStoredAsOnlyEntry();
        retire.verifyRetirementNotice();
        retire.closeRetirementNotice();
        final HallOfFamePage result = new HallOfFamePage(getContext());
        result.awaitResultDialog();
        result.verifyResultDialogShown();
        result.verifyRankedAt(1);
        result.verifyCurrentResultShown();
        result.verifyStoredEntries(1);
        result.closeResultDialog();
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
    }

    @Test
    @Tag("local")
    void TC_03_declared_rebellion_cancel_keeps_it_open_and_confirmed_retire_records_it_without_bonus()
            throws Exception {
        // Scored free play in which the Dutch have declared independence and
        // the rebellion is still open on their own turn.
        RoyalVictoryFixture.prepareScoredFreePlay(getContext(),
                rebellionFileName, "retire-f194-declared.microcol", DELFT,
                ATTACK_FROM);
        HallOfFameFixture.removeRanking();
        WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RetirePage retire = new RetirePage(getContext());
        rebellion.verifyRebellionHolds();
        retire.verifyOffered(true);
        retire.verifyNoFrozenResult();
        final int played = retire.playedRounds();
        final int ranked = retire.rankingSize();

        // Cancelling leaves the rebellion running and records nothing.
        retire.openRetire();
        retire.verifyDialogShown(true);
        retire.verifyPreviewShown();
        retire.verifyConsequenceShown(false);
        retire.rememberPreview();
        retire.cancelRetire();
        retire.verifyDialogShown(false);
        rebellion.verifyRebellionHolds();
        retire.verifyCanceled(played, ranked);

        // The confirmed retirement freezes the current score, without any
        // independence bonus, as the only ranked entry.
        retire.openRetire();
        retire.verifyPreviewUnchanged();
        retire.confirmRetire();
        retire.verifyDialogShown(false);
        retire.verifyDeclaredRetirementFinished();
        retire.verifyStoredAsOnlyEntry();
        retire.verifyRetirementNotice();
        retire.closeRetirementNotice();
        final HallOfFamePage result = new HallOfFamePage(getContext());
        result.awaitResultDialog();
        result.verifyResultDialogShown();
        result.verifyRankedAt(1);
        result.verifyCurrentResultShown();
        result.verifyStoredEntries(1);
        result.closeResultDialog();
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
    }
}
