package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * F203: the Congress report shows the national support rounded down, the
 * recruited founders and the Crown's own roster. Cancelling changes nothing, a
 * turn and Continue refresh the figures, and the English and Czech dialogs fit
 * 900x750 with no clipped line.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_176_F203_congress_support_test extends AbstractMicroColTest {
    /** The turn count of the shared scenario: the year is 1747. */
    private static final int YEAR_1747 = 700;

    private static final int END_YEAR = 1900;

    /**
     * Eight residents in each of the four colonies, so 6400 points in all. 799
     * per colony is 3196 points, 49.9 percent, which still shows 49.
     */
    private static final long BELOW_HALF = 799L;

    /** 800 per colony is 3200 of 6400 points: exactly half, which shows 50. */
    private static final long HALF = 800L;

    private static final File INDEPENDENCE = new File(
            "src/test/scenarios/F03-independence.microcol");

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(), INDEPENDENCE);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_changes_nothing_and_a_turn_refreshes_support_and_crown()
            throws Exception {
        // No colony has support yet; the Crown holds 499 bells toward its next
        // unit.
        IndependenceFixture.prepareWithCrownRemainder(getContext(),
                INDEPENDENCE, "congress-support-zero.microcol", YEAR_1747,
                END_YEAR, new long[] { 0L, 0L, 0L, 0L }, 499);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        // Opening and cancelling the report neither recruits nor advances.
        congress.rememberWorld();
        congress.open();
        congress.verifySupport("National rebel support: 0% (32 residents)", 0);
        congress.verifyRoyalForce(6, 4, 2, 2, 499);
        congress.verifyDialogFits(900, 750);
        congress.close();
        congress.verifyWorldUnchanged();
        // The turn produces bells, and the reopened report shows them.
        congress.rememberBells();
        game.nextTurnAndCloseDialogs();
        congress.verifyBellsProducedSinceRemembered();
        congress.open();
        congress.verifyRoyalForceMatchesModel();
        congress.verifySupport("National rebel support: 0% (32 residents)", 0);
        congress.close();
        // Continue shows the same saved figures.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        congress.open();
        congress.verifyRoyalForceMatchesModel();
        congress.verifySupport("National rebel support: 0% (32 residents)", 0);
        congress.close();
    }

    @Test
    @Tag("local")
    void TC_02_just_below_half_shows_49_and_exactly_half_shows_50()
            throws Exception {
        IndependenceFixture.prepareWithCrownRemainder(getContext(),
                INDEPENDENCE, "congress-support-below.microcol", YEAR_1747,
                END_YEAR,
                new long[] { BELOW_HALF, BELOW_HALF, BELOW_HALF, BELOW_HALF },
                499);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.verifySupport("National rebel support: 49% (32 residents)",
                49);
        congress.verifyDialogFits(900, 750);
        congress.verifyTextNotClipped();
        congress.verifySupportVisible();
        congress.captureDialog("congressDialog", "r203-congress-below-en.png");
        // The end of the report shows the Crown roster.
        congress.scrollReportToEnd();
        congress.captureDialog("congressDialog", "r203-congress-roster-en.png");
        congress.verifyActionsAlwaysVisible();
        congress.verifyReportReachableByScrolling();
        congress.close();
        // Viewing the support does not offer or make a declaration.
        new IndependencePage(getContext()).verifyDeclareButtonVisible(false);
        // Exactly half is shown as 50 and meets the declaration threshold. The
        // second load reads the saved file, not the state of the first game.
        IndependenceFixture.prepareWithCrownRemainder(getContext(),
                INDEPENDENCE, "congress-support-half.microcol", YEAR_1747,
                END_YEAR, new long[] { HALF, HALF, HALF, HALF }, 499);
        game.exitToMenu().loadGame();
        congress.open();
        congress.verifySupport("National rebel support: 50% (32 residents)",
                50);
        congress.verifyDialogFits(900, 750);
        congress.verifyTextNotClipped();
        congress.verifyActionsAlwaysVisible();
        congress.verifyReportReachableByScrolling();
        congress.close();
        new IndependencePage(getContext()).verifyDeclareButtonVisible(true);
    }

    @Test
    @Tag("local")
    void TC_03_czech_support_label_shows_the_same_rounded_value()
            throws Exception {
        IndependenceFixture.prepareWithCrownRemainder(getContext(),
                INDEPENDENCE, "congress-support-czech.microcol", YEAR_1747,
                END_YEAR,
                new long[] { BELOW_HALF, BELOW_HALF, BELOW_HALF, BELOW_HALF },
                499);
        SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack().loadGame();
        CongressPage congress = new CongressPage(getContext());
        congress.open();
        congress.verifySupport(
                "Celostátní podpora povstání: 49 % (32 obyvatel)", 49);
        congress.verifyDialogFits(900, 750);
        congress.verifyTextNotClipped();
        congress.verifySupportVisible();
        congress.captureDialog("congressDialog", "r203-congress-below-cs.png");
        // The end of the report shows the Crown roster.
        congress.scrollReportToEnd();
        congress.captureDialog("congressDialog", "r203-congress-roster-cs.png");
        congress.verifyActionsAlwaysVisible();
        congress.verifyReportReachableByScrolling();
        congress.close();
    }
}
