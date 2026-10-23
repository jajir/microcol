package org.microcol.test;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AttackPage;
import org.microcol.page.BaseScorePage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.ExpertPurchasePage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RebellionVictoryPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * The calculated result preview (R-0196) reads the live nation from the
 * statistics screen. The prepared Dutch nation has not declared independence,
 * so no successful-independence term applies and the total is the base subtotal
 * scaled by the difficulty factor. Hiring a specialist in Europe moves the
 * population by four points and the treasury by two, so the clamped base moves
 * by two. Reopening and Save and Continue keep the same values. The preview is
 * labelled as not recorded. A second case starts from a valid open rebellion
 * that destroyed one native settlement: the preview and Save and Continue show
 * no independence term, then the real battle that retakes Delft wins the
 * rebellion and the recorded result gains the first-order bonus and the early
 * points of the 1747 declaration.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_161_F196_result_score_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F22-expert-purchase.microcol");

    /** The specialist bought in Europe is on the pier with this unit ID. */
    private static final int HIRED_ID = 103;

    /** Four population points minus the two treasury points that were lost. */
    private static final long CLAMPED_CHANGE = 2;

    /**
     * Shared independence scenario, whose calendar is in 1747. The prepared
     * copy declares the Dutch in that year with the Crown holding Delft, as
     * TC158.
     */
    private static final File SCENARIO_SOURCE = new File(
            "src/test/scenarios/F03-independence.microcol");

    private static final String PREPARED_HISTORY = "result-history-f196.microcol";

    /** Colony of the scenario that the Crown holds, retaken in the battle. */
    private static final int DELFT = 1;

    private static final Location DELFT_AT = Location.of(22, 12);

    /** Free land square beside Delft where the Dutch colonist attacks from. */
    private static final Location ATTACK_FROM = Location.of(23, 12);

    private static final String DELFT_NAME = "Delft";

    private static final String VICTORY_TEXT = "every surviving rebel colony is yours";

    /** The only rebel to win takes the first order: bonus is 100 percent. */
    private static final long FIRST_BONUS_PERCENT = 100;

    /** The declaration in 1747 is 33 years before 1780. */
    private static final long EARLY_POINTS = 33;

    private static final long DESTROYED_SETTLEMENTS = ResultHistoryFixture.DESTROYED_SETTLEMENTS;

    /** The Conquistador price of a destroyed settlement is three points. */
    private static final long POINTS_PER_SETTLEMENT = 3;

    private static final long CONQUISTADOR_PERCENT = 100;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_hiring_a_specialist_changes_the_result_preview_and_continue_keeps_it()
            throws Exception {
        // The calculated block opens below the base score and names itself as
        // not recorded. Reopening after closing shows the same values.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final BaseScorePage base = new BaseScorePage(getContext());
        base.open();
        base.verifyTabSelected();
        base.verifyCalculatedPreviewNotRecorded();
        base.verifyNoIndependenceTerms();
        base.verifyCalculatedResultFromComponents();
        base.rememberCalculatedResult();
        game = base.close();
        base.open();
        base.verifyRememberedCalculatedResultKept();
        game = base.close();

        // The player hires a specialist in Europe with a legal purchase.
        final EuropePortScreen europe = game.openEuroperPort();
        final ExpertPurchasePage purchase = new ExpertPurchasePage(
                getContext());
        europe.openBuyUnitScreen();
        purchase.verifyOffer(true, "");
        purchase.confirm();
        purchase.verifyTreasuryAndPierExperts(2200, List.of(HIRED_ID));
        game = europe.close();

        // The clamped base follows the components and the total is recomputed
        // from the same factors.
        base.open();
        base.verifyCalculatedChangeFromRemembered(CLAMPED_CHANGE);
        base.verifyCalculatedResultFromComponents();
        base.rememberCalculatedResult();
        game = base.close();

        // Save and Continue keep the same calculated values without any new
        // payment or unit.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        base.open();
        base.verifyRememberedCalculatedResultKept();
        base.close();
    }

    @Test
    @Tag("local")
    void TC_02_a_successful_independence_history_shows_its_terms_and_continue_keeps_them()
            throws Exception {
        // The prepared Dutch rebellion declared in 1747, the saved year, and
        // has destroyed one native settlement. The Crown holds Delft and no
        // victory is recorded yet, so the preview shows no independence terms,
        // the Conquistador price of three points for the settlement and the
        // 100 percent difficulty factor.
        ResultHistoryFixture.prepare(getContext(), SCENARIO_SOURCE,
                PREPARED_HISTORY, DELFT, ATTACK_FROM);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final BaseScorePage base = new BaseScorePage(getContext());
        final RebellionVictoryPage victory = new RebellionVictoryPage(
                getContext());
        final AttackPage attack = new AttackPage(getContext());
        base.open();
        base.verifyCalculatedPreviewNotRecorded();
        base.verifyNoIndependenceTerms();
        base.verifyDestructionTerms(DESTROYED_SETTLEMENTS,
                POINTS_PER_SETTLEMENT);
        base.verifyDifficultyPercent(CONQUISTADOR_PERCENT);
        base.verifyCalculatedResultFromComponents();
        base.rememberCalculatedResult();
        game = base.close();

        // Reopening shows the same calculated values, so the preview changed
        // nothing.
        base.open();
        base.verifyRememberedCalculatedResultKept();
        game = base.close();

        // Save and Continue keep the same history and the same calculated
        // values.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        base.open();
        base.verifyNoIndependenceTerms();
        base.verifyDestructionTerms(DESTROYED_SETTLEMENTS,
                POINTS_PER_SETTLEMENT);
        base.verifyRememberedCalculatedResultKept();
        game = base.close();

        // The player's colonist attacks Delft and wins the real battle, which
        // ends the rebellion with the first victory.
        game.panToTile(DELFT_AT);
        attack.chooseExistingOutcome(RoyalVictoryFixture.ATTACKER, true);
        attack.chooseAttack(ATTACK_FROM, DELFT_AT);
        attack.confirm();
        victory.verifyVictoryDialog(VICTORY_TEXT);
        victory.verifyVictoryRecorded();

        // The recorded result of the won rebellion adds the full first-order
        // bonus and the early points of the 1747 declaration, keeps the
        // destroyed settlement, and floors only once after the difficulty.
        victory.verifySuccessfulResult(FIRST_BONUS_PERCENT, EARLY_POINTS,
                DESTROYED_SETTLEMENTS, CONQUISTADOR_PERCENT);
        victory.closeVictoryDialog(VICTORY_TEXT);
        victory.closeCaptureNotice(DELFT_NAME);
    }
}
