package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.GamePage;
import org.microcol.page.HallOfFamePage;
import org.microcol.page.RebellionLossPage;
import org.microcol.page.RebellionVictoryPage;
import org.microcol.page.ScoreBoundaryPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The persistent Hall of Fame as the player meets it (R-0197). A scored result
 * is recorded once and shown with its breakdown and its place. The Dutch nation
 * reaches spring 1800, or wins a declared rebellion, and the result window or
 * the choice window shows it, whether or not it is ranked. A game left without
 * a frozen result records nothing. A storage failure is named where the result
 * is shown, while the saved bytes and the play stay unchanged.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_163_F197_hall_of_fame_test extends AbstractMicroColTest {

    /** Undeclared Dutch nation that freezes its score at spring 1800. */
    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** Dutch nation that has declared independence, for the won rebellion. */
    private final static File rebellionFileName = new File(
            "src/test/scenarios/F03-independence.microcol");

    /** Colony of the independence scenario that the Crown holds for the win. */
    private final static int DELFT = 1;

    private final static Location DELFT_AT = Location.of(22, 12);

    private final static String DELFT_NAME = "Delft";

    /** Free land square beside Delft where the Dutch colonist attacks from. */
    private final static Location ATTACK_FROM = Location.of(23, 12);

    private final static String REASON_TEXT = "every surviving rebel colony is yours";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        HallOfFameFixture.removeRanking();
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_unranked_result_is_shown_with_its_breakdown_and_the_full_ranking_is_kept()
            throws Exception {
        // The prepared game stands in winter 1799, and ten authored rivals are
        // already saved with totals above the Dutch result.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "hall-of-fame-f197-full.microcol");
        HallOfFameFixture.writeFullRanking();
        WelcomePage.of(getContext()).loadGame();
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());

        // Before spring 1800 nothing is frozen, so the ranking is only the
        // rivals.
        hall.openFromStatistics();
        hall.verifyRankingRows(HallOfFameFixture.RIVALS);
        hall.verifyNoCurrentResult();
        GamePage game = hall.closeStatistics();

        // Spring 1800 freezes the result and the player postpones the choice.
        boundary.endTurnToBoundary();
        boundary.verifyAwaitingChoice();
        boundary.decideLater();

        // The result is below the rivals: it is not ranked, but its breakdown
        // is shown next to the unchanged ranking.
        hall.openFromStatistics();
        hall.verifyRankingRows(HallOfFameFixture.RIVALS);
        hall.verifyNotRanked();
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
        game = hall.closeStatistics();

        // Reopening records nothing new: the same entries remain in the file.
        hall.openFromStatistics();
        hall.verifyRankingRows(HallOfFameFixture.RIVALS);
        hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
        game = hall.closeStatistics();

        // After leaving the game the main menu opens the saved ranking alone.
        game.exitToMenu();
        hall.openFromMenu();
        hall.verifyRankingRows(HallOfFameFixture.RIVALS);
        hall.verifyNoCurrentResult();
        hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
        hall.closeMenuDialog();
    }

    @Test
    @Tag("local")
    void TC_02_scored_result_is_the_only_entry_once_and_is_kept_after_leaving_the_game()
            throws Exception {
        // The prepared game stands in winter 1799 with no saved ranking.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "hall-of-fame-f197-first.microcol");
        HallOfFameFixture.removeRanking();
        WelcomePage.of(getContext()).loadGame();
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());

        // Spring 1800 records the frozen result, and it is the first entry.
        boundary.endTurnToBoundary();
        boundary.verifyRankingPlace(1);
        boundary.decideLater();
        hall.openFromStatistics();
        hall.verifyRankingRows(1);
        hall.verifyRankedAt(1);
        hall.verifyTopRowIsFrozenResult();
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(1);
        GamePage game = hall.closeStatistics();

        // Reopening keeps the single entry at the same place.
        hall.openFromStatistics();
        hall.verifyRankingRows(1);
        hall.verifyRankedAt(1);
        hall.verifyStoredEntries(1);
        game = hall.closeStatistics();

        // After leaving the game the main menu still shows the same entry.
        game.exitToMenu();
        hall.openFromMenu();
        hall.verifyRankingRows(1);
        hall.verifyStoredEntries(1);
        hall.closeMenuDialog();
    }

    @Test
    @Tag("local")
    void TC_03_leaving_before_a_frozen_result_records_nothing()
            throws Exception {
        // The prepared game stands in winter 1799: no score is frozen yet.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "hall-of-fame-f197-exit.microcol");
        HallOfFameFixture.removeRanking();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final HallOfFamePage hall = new HallOfFamePage(getContext());

        // Leaving the game without reaching spring 1800 writes no ranking file,
        // and the main menu shows an empty ranking.
        game.exitToMenu();
        hall.verifyNoStoredFile();
        hall.openFromMenu();
        hall.verifyRankingRows(0);
        hall.verifyNoCurrentResult();
        hall.verifyNoStoredFile();
        hall.closeMenuDialog();
    }

    @Test
    @Tag("local")
    void TC_04_a_won_rebellion_shows_its_unranked_result_after_the_end_messages()
            throws Exception {
        // The full ranking of rivals stays above the won rebellion's result.
        RoyalVictoryFixture.prepareScoredFreePlay(getContext(),
                rebellionFileName,
                "hall-of-fame-f197-victory-unranked.microcol", DELFT,
                ATTACK_FROM);
        HallOfFameFixture.writeFullRanking();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionLossPage rebellion = new RebellionLossPage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        rebellion.verifyRebellionHolds();
        rebellion.verifyColonyOwner(DELFT, RoyalVictoryFixture.CROWN);

        // The real battle for Delft wins the rebellion.
        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.verifyCaptureNotice(DELFT_NAME);
        victory.verifyVictoryRecorded();
        victory.closeVictoryDialog(REASON_TEXT);

        // The finished result is shown even though it is not ranked. The saved
        // ranking keeps its ten rivals.
        hall.awaitResultDialog();
        hall.verifyResultDialogShown();
        hall.verifyNotRanked();
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(HallOfFameFixture.RIVALS);
        hall.closeResultDialog();

        // The capture notice stayed open beneath the victory message and the
        // result window; the modal result window must be closed before it.
        victory.closeCaptureNotice(DELFT_NAME);
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
    }

    @Test
    @Tag("local")
    void TC_05_a_won_rebellion_with_no_saved_ranking_becomes_the_first_entry()
            throws Exception {
        RoyalVictoryFixture.prepareScoredFreePlay(getContext(),
                rebellionFileName, "hall-of-fame-f197-victory-first.microcol",
                DELFT, ATTACK_FROM);
        HallOfFameFixture.removeRanking();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final HallOfFamePage hall = new HallOfFamePage(getContext());

        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.closeVictoryDialog(REASON_TEXT);

        // The finished result is recorded and shown as the first place.
        hall.awaitResultDialog();
        hall.verifyResultDialogShown();
        hall.verifyRankedAt(1);
        hall.verifyCurrentResultShown();
        hall.verifyStoredEntries(1);
        hall.closeResultDialog();

        // The capture notice stayed open beneath the victory message and the
        // result window; the modal result window must be closed before it.
        victory.closeCaptureNotice(DELFT_NAME);
        new CampaignScreenPage(getContext()).verifyCampaignScreen();

        // The main menu shows that same single entry afterwards.
        new CampaignScreenPage(getContext()).back();
        hall.openFromMenu();
        hall.verifyRankingRows(1);
        hall.verifyStoredEntries(1);
        hall.closeMenuDialog();
    }

    @Test
    @Tag("local")
    void TC_06_invalid_saved_ranking_is_named_after_a_won_rebellion_and_its_bytes_stay()
            throws Exception {
        // The saved ranking is not valid. The result must still be shown, and
        // the file must keep its bytes.
        RoyalVictoryFixture.prepareScoredFreePlay(getContext(),
                rebellionFileName, "hall-of-fame-f197-victory-invalid.microcol",
                DELFT, ATTACK_FROM);
        HallOfFameFixture.writeInvalidRanking();
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        final HallOfFamePage hall = new HallOfFamePage(getContext());

        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(REASON_TEXT);
        victory.closeVictoryDialog(REASON_TEXT);

        // The storage failure is named with the result; closing leaves the
        // game.
        hall.awaitResultDialog();
        hall.verifyResultDialogShown();
        hall.verifyStorageFailure();
        hall.verifyStoredBytes(HallOfFameFixture.INVALID_RANKING);
        hall.closeResultDialog();

        // The capture notice stayed open beneath the victory message and the
        // result window; the modal result window must be closed before it.
        victory.closeCaptureNotice(DELFT_NAME);
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
        hall.verifyStoredBytes(HallOfFameFixture.INVALID_RANKING);
    }

    @Test
    @Tag("local")
    void TC_07_invalid_saved_ranking_is_named_at_spring_1800_and_continuing_still_works()
            throws Exception {
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "hall-of-fame-f197-invalid.microcol");
        HallOfFameFixture.writeInvalidRanking();
        WelcomePage.of(getContext()).loadGame();
        final HallOfFamePage hall = new HallOfFamePage(getContext());
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());

        // Spring 1800 freezes the result; the choice window names the failure.
        boundary.endTurnToBoundary();
        boundary.verifyRankingStorageFailure();
        boundary.verifyAwaitingChoice();

        // Continuing without scoring is still possible and keeps the bytes.
        final Optional<ScreenTurnReport> report = boundary.continueUnscored();
        boundary.verifyDialogShown(false);
        boundary.verifyContinuedUnscored();
        boundary.closeReport(report);
        hall.verifyStoredBytes(HallOfFameFixture.INVALID_RANKING);
    }
}
