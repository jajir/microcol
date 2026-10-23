package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.microcol.gui.util.ButtonBarOk;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.Player;
import org.microcol.model.ResultScore;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;

/**
 * The player's view of a won rebellion (R-0191): the battle that takes the last
 * Crown-held member ends the game at once, the game shows the victory reason
 * and leaves for the campaign screen. The page only reads what the player sees
 * and what the model recorded; the battle is made by {@link AttackPage}.
 */
public final class RebellionVictoryPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    public RebellionVictoryPage(final TestContext context) {
        super(context);
    }

    /**
     * Acknowledge the end-of-game message. The battle that won the rebellion
     * also captured a colony, so the capture notice is open beneath it; both
     * use the shared OK button, and only the button of the message's own window
     * is pressed. Closing it lets the game leave for the campaign screen.
     *
     * @param shownText required part of the shown victory reason
     */
    public void closeVictoryDialog(final String shownText) {
        clickOkOfDialogShowing(shownText);
    }

    /**
     * Acknowledge the notice that the player captured the colony, which stays
     * open beneath the victory message until the player closes it.
     *
     * @param colonyName required name of the captured colony
     */
    public void closeCaptureNotice(final String colonyName) {
        clickOkOfDialogShowing(captureText(colonyName));
    }

    /**
     * The notice that the player captured the colony is on screen.
     *
     * @param colonyName required name of the captured colony
     */
    public void verifyCaptureNotice(final String colonyName) throws Exception {
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(captureText(colonyName));
    }

    private static String captureText(final String colonyName) {
        return "You captured colony '" + colonyName + "'";
    }

    private void clickOkOfDialogShowing(final String shownText) {
        final Label label = getNodeFinder()
                .lookup((Node node) -> node instanceof Label
                        && ((Label) node).getText() != null
                        && ((Label) node).getText().contains(shownText))
                .queryAs(Label.class);
        final Button ok = (Button) label.getScene().getRoot()
                .lookup("#" + ButtonBarOk.BUTTON_OK_ID);
        getRobot().clickOn(ok, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The message that explains the victory is on screen.
     *
     * @param expectedText required part of the shown reason
     */
    public void verifyVictoryDialog(final String expectedText)
            throws Exception {
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(expectedText);
    }

    /** No end-of-game message is shown, such as after Continue. */
    public void verifyNoVictoryDialogShown(final String unexpectedText) {
        final Node found = getNodeFinder()
                .lookup((Node node) -> node instanceof Label
                        && ((Label) node).getText() != null
                        && ((Label) node).getText().contains(unexpectedText))
                .tryQuery().orElse(null);
        assertNull(found, "Unexpected dialog text: " + unexpectedText);
    }

    /**
     * The calculated result of the won rebellion (R-0196) has the expected
     * independence, destruction and difficulty terms, and its total follows
     * from the clamped base with a single floor after the difficulty factor.
     *
     * @param bonusPercent      expected order bonus in percent
     * @param earlyPoints       expected early declaration points
     * @param destroyed         expected destroyed native settlements
     * @param difficultyPercent expected difficulty factor in percent
     */
    public void verifySuccessfulResult(final long bonusPercent,
            final long earlyPoints, final long destroyed,
            final long difficultyPercent) {
        final ResultScore result = getModel().getPlayerByName(DUTCH)
                .getResultScore();
        assertEquals(bonusPercent, result.getOrderBonusPercent(),
                "Order bonus percent");
        assertEquals(earlyPoints, result.getEarlyPoints(), "Early points");
        assertEquals(destroyed, result.getDestroyedSettlements(),
                "Destroyed settlements");
        assertEquals(difficultyPercent, result.getDifficultyPercent(),
                "Difficulty percent");
        assertEquals(
                (result.getClampedBase() * (100 + bonusPercent)
                        + 100 * earlyPoints) * difficultyPercent / 10000,
                result.getTotal(), "Calculated result");
    }

    /**
     * The model recorded the one terminal result of the game: the victory, and
     * the first victory of the Dutch rebellion, which was never a loss.
     */
    public void verifyVictoryRecorded() {
        assertTrue(getModel().isGameFinished(), "The game must be finished");
        assertEquals(Optional.of(GameOverEvaluator.REASON_REBELLION_VICTORY),
                getModel().getGameOverResult()
                        .map(GameOverResult::getGameOverReason));
        final Player rebel = getModel().getPlayerByName(DUTCH);
        assertEquals(Optional.of(1), rebel.getRebellionVictoryOrder());
        assertTrue(rebel.getRebellionLossReason().isEmpty(),
                "A won rebellion is never also lost");
    }
}
