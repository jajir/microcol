package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.CampaignScreenPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.HallOfFamePage;
import org.microcol.page.GamePage;
import org.microcol.page.ScoreBoundaryPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The free-play boundaries of R-0193 as the player meets them. The prepared
 * Dutch nation has not declared independence. Ending the last turn of winter
 * 1799 freezes its result at spring 1800 and opens a choice: continue without
 * scoring, or decide later. Deciding later leaves the game paused, and the
 * statistics offer the same choice again. Continuing keeps the frozen result
 * unchanged through a further round and through Save and Continue, and a saved
 * pending choice is asked again with the same identity and components. A second
 * prepared position, an authored unscored continuation one round before spring
 * 1850, ends all play with the earlier result kept.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_162_F193_score_boundary_test extends AbstractMicroColTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    private final static String AWAITING_TEXT = "Waiting for your choice";

    private final static String CONTINUED_TEXT = "You continued without scoring";

    private final static String END_TEXT = "free play is over";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_spring_1800_freezes_the_result_and_the_player_postpones_reopens_and_continues()
            throws Exception {
        // The prepared game stands in winter 1799, before any frozen result.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "score-boundary-f193.microcol");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyNoFrozenResult();
        boundary.verifyTurn(ScoreBoundaryFixture.WINTER_1799);

        // The player ends the turn. Spring 1800 freezes the result and asks
        // for the choice, showing the frozen result and the unscored limit.
        boundary.endTurnToBoundary();
        boundary.verifyTurn(ScoreBoundaryFixture.WINTER_1799 + 1);
        boundary.verifyDialogShown(true);
        boundary.verifyDialogShowsFrozenTotal();
        boundary.verifyDialogComponents();
        boundary.verifyDialogOffersChoices();
        boundary.verifyAwaitingChoice();
        boundary.rememberFrozenResult();

        // The player decides later. The game stays paused: no next turn, but
        // the statistics are still reachable.
        boundary.decideLater();
        boundary.verifyDialogShown(false);
        boundary.verifyAwaitingChoice();
        boundary.verifyNextTurnBlocked();
        boundary.verifyStatisticsReachable();

        // The statistics show the frozen result, labelled as such, and offer
        // the pending choice again.
        boundary.openStatistics();
        boundary.verifyFrozenDisplay(AWAITING_TEXT);
        boundary.verifyDecideActionOffered(true);
        boundary.decideFromStatistics();
        boundary.verifyDialogShown(true);
        boundary.verifyDialogShowsFrozenTotal();
        boundary.verifyRememberedResultKept();

        // The player continues without scoring. The new round reaches the
        // player's turn, the result is unchanged and the choice is gone.
        final Optional<ScreenTurnReport> firstReport = boundary
                .continueUnscored();
        boundary.verifyDialogShown(false);
        boundary.verifyContinuedUnscored();
        boundary.verifyRememberedResultKept();
        game = boundary.returnToMap(firstReport);

        // The statistics now show the continued frozen result and no choice.
        boundary.openStatistics();
        boundary.verifyFrozenDisplay(CONTINUED_TEXT);
        boundary.verifyDecideActionOffered(false);
        game = boundary.closeStatistics();

        // An unscored round passes without changing the frozen result.
        final Optional<ScreenTurnReport> report = game.nextTurn();
        boundary.closeReport(report);
        boundary.verifyTurn(ScoreBoundaryFixture.WINTER_1799 + 2);
        boundary.verifyContinuedUnscored();
        boundary.verifyRememberedResultKept();

        // Save and Continue keep the same result and ask nothing again.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        boundary.verifyDialogShown(false);
        boundary.verifyContinuedUnscored();
        boundary.verifyRememberedResultKept();
    }

    @Test
    @Tag("local")
    void TC_02_a_saved_pending_choice_is_asked_again_with_the_same_result()
            throws Exception {
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "score-boundary-f193-saved.microcol");
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());

        // The turn ends, the choice opens and the player decides later.
        boundary.endTurnToBoundary();
        boundary.verifyAwaitingChoice();
        boundary.rememberFrozenResult();
        boundary.decideLater();
        boundary.verifyDialogShown(false);

        // Save: leaving to the menu stores the paused position, and Continue
        // opens it again with the same pending choice.
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).verifyContinueButtonEnabled(true);
        new FreePlayAuditPage(getContext()).continueGame();
        boundary.waitForDialog();
        boundary.verifyDialogShown(true);
        boundary.verifyAwaitingChoice();
        boundary.verifyDialogShowsFrozenTotal();
        boundary.verifyRememberedResultKept();

        // Continuing now keeps the same identity and components.
        final Optional<ScreenTurnReport> report = boundary.continueUnscored();
        boundary.verifyDialogShown(false);
        boundary.verifyContinuedUnscored();
        boundary.verifyRememberedResultKept();
        boundary.closeReport(report);
    }

    @Test
    @Tag("local")
    void TC_03_spring_1850_ends_all_play_and_keeps_the_earlier_result()
            throws Exception {
        // The prepared game already continued without scoring and stands in
        // winter 1849; the choice is not asked again.
        ScoreBoundaryFixture.prepareBeforeEnd(getContext(), testFileName,
                "score-boundary-f193-end.microcol");
        WelcomePage.of(getContext()).loadGame();
        final ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyTurn(ScoreBoundaryFixture.WINTER_1849);
        boundary.verifyDialogShown(false);
        boundary.verifyContinuedUnscored();
        boundary.rememberFrozenResult();

        // The player ends the turn. Spring 1850 ends all play with a message,
        // and the earlier frozen result stays as it was.
        boundary.endTurnToFreePlayEnd(END_TEXT);
        boundary.verifyEndMessage(END_TEXT);
        boundary.verifyFreePlayEnded();
        boundary.verifyRememberedResultKept();

        // Acknowledging the message leaves the game for the campaign screen,
        // so play cannot continue.
        boundary.closeEndMessage();
        // The finished result follows the end message with its breakdown; it is
        // closed before the campaign screen.
        final HallOfFamePage result = new HallOfFamePage(getContext());
        result.awaitResultDialog();
        result.verifyResultDialogShown();
        result.verifyCurrentResultShown();
        result.closeResultDialog();
        new CampaignScreenPage(getContext()).verifyCampaignScreen();
    }
}
