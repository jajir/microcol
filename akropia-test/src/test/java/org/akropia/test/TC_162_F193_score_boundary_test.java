package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.page.CampaignScreenPage;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.HallOfFamePage;
import org.akropia.page.GamePage;
import org.akropia.page.ScoreBoundaryPage;
import org.akropia.page.ScreenTurnReport;
import org.akropia.page.WelcomePage;
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
 * 1850, ends all play with the earlier result kept. On the shipped free-play
 * map the human nation owns no colony, so the round that ends winter 1599 ends
 * play at spring 1600 with one final result and one Hall entry.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_162_F193_score_boundary_test extends AbstractAkropiaTest {

    private final static File testFileName = new File(
            "src/test/scenarios/F22-expert-purchase.akropia");

    private final static String AWAITING_TEXT = "Waiting for your choice";

    private final static String CONTINUED_TEXT = "You continued without scoring";

    private final static String END_TEXT = "free play is over";

    /** The whole message of the loss of every colony, as the game words it. */
    private final static String NO_COLONY_TEXT = "You lost all colonies.";

    @Start
    private void start(final Stage primaryStage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_spring_1800_keeps_scored_play_running() throws Exception {
        // The player crosses the former scoring boundary without a modal
        // interruption.
        ScoreBoundaryFixture.prepareBeforeBoundary(getContext(), testFileName,
                "untimed-1800.akropia");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyNoFrozenResult();
        game.nextTurnAndCloseDialogs();
        boundary.verifyTurn(ScoreBoundaryFixture.WINTER_1799 + 1);
        boundary.verifyUntimedPlay();
    }

    @Test
    @Tag("local")
    void TC_02_save_and_continue_past_1800_preserve_active_scored_play()
            throws Exception {
        // Continuing a saved game after 1800 must not create a frozen result.
        ScoreBoundaryFixture.prepareUntimed(getContext(), testFileName,
                "untimed-reload.akropia", 920);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyUntimedPlay();
        game.exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        boundary.verifyUntimedPlay();
        boundary.verifyTurn(920);
    }

    @Test
    @Tag("local")
    void TC_03_spring_1850_does_not_end_free_play() throws Exception {
        // The player crosses the former final deadline and can keep playing.
        ScoreBoundaryFixture.prepareUntimed(getContext(), testFileName,
                "untimed-1850.akropia", 1109);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
        ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyTurn(1110);
        boundary.verifyUntimedPlay();
    }

    @Test
    @Tag("local")
    void TC_04_spring_1600_does_not_eliminate_a_nation_before_its_first_colony()
            throws Exception {
        // An arriving expedition is not defeated just because the calendar
        // changed.
        ScoreBoundaryFixture.prepareNoColonyBeforeBoundary(getContext(),
                "untimed-1600.akropia");
        GamePage game = WelcomePage.of(getContext()).loadGame();
        game.nextTurnAndCloseDialogs();
        ScoreBoundaryPage boundary = new ScoreBoundaryPage(getContext());
        boundary.verifyTurn(ScoreBoundaryFixture.SPRING_1600);
        boundary.verifyUntimedPlay();
    }
}
