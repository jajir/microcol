package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameResult;
import org.microcol.model.HallOfFame;
import org.microcol.model.RetirementQuote;
import org.microcol.model.campaign.po.HallOfFameDao;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * The retirement of R-0194 as the player meets it on the game panel: the Retire
 * control that scored free play offers, the preview of the score and its
 * consequence, cancelling without a record, and the confirmation that freezes
 * one result or keeps the earlier unscored one. The page reads the model and
 * the stored ranking only to check what the player saw.
 */
public final class RetirePage extends AbstractScreen {

    private static final String DIALOG = "retireDialog";

    /** Start of the end-of-game notice that a retirement of any kind opens. */
    private static final String RETIREMENT_NOTICE = "You retired";

    /** The previewed total as it was when it was last remembered. */
    private long rememberedTotal;

    /**
     * The frozen identity of the previewed result, empty for a live preview.
     */
    private String rememberedIdentity = "";

    public RetirePage(final TestContext context) {
        super(context);
    }

    private GameResult frozen() {
        return getModel().getFrozenResult().orElseThrow();
    }

    private RetirementQuote preview() {
        return getModel().getRetirementQuote().orElseThrow();
    }

    /** @return the number of ranked results in the stored Hall of Fame */
    public int rankingSize() {
        return getContext().getClassFromGuice(HallOfFameDao.class).load()
                .getEntries().size();
    }

    /** @return the rounds that the calendar has played so far */
    public int playedRounds() {
        return getModel().getCalendar().getNumberOfPlayedTurns();
    }

    // --- player gestures ---------------------------------------------------

    /** Remember the previewed score and frozen identity for later checks. */
    public void rememberPreview() {
        rememberedTotal = preview().getScore().getTotal();
        rememberedIdentity = preview().getIdentity().orElse("");
    }

    /** Open the Retire control and wait for its confirmation window. */
    public void openRetire() throws Exception {
        clickOnButtonWithId("retire");
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#" + DIALOG).tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Keep playing: the window closes and the game stays as it was. */
    public void cancelRetire() {
        clickOnButtonWithId("retireCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Retire and end the game; the result window follows the confirmation. */
    public void confirmRetire() {
        clickOnButtonWithId("retireConfirm");
        WaitForAsyncUtils.waitForFxEvents();
    }

    // --- explicit observations --------------------------------------------

    /** The calendar has played exactly this many rounds. */
    public void verifyTurn(final int played) {
        assertEquals(played, getModel().getCalendar().getNumberOfPlayedTurns());
    }

    /** The Retire control is shown only where the model offers a retirement. */
    public void verifyOffered(final boolean expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, getModel().getRetirementQuote().isPresent());
        assertEquals(expected, getButtonById("retire").isVisible());
    }

    public void verifyDialogShown(final boolean expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, isCssIdVisible(DIALOG));
    }

    /** The window states the exact total that the model previews. */
    public void verifyPreviewShown() {
        final String shown = getLabeledById("retireSummary").getText();
        assertTrue(shown.contains(preview().getScore().getTotal() + " points"),
                "Previewed total in: " + shown);
    }

    /** The preview is still the score the player saw when it was remembered. */
    public void verifyPreviewUnchanged() {
        assertEquals(rememberedTotal, preview().getScore().getTotal());
        assertEquals(rememberedIdentity, preview().getIdentity().orElse(""));
    }

    /**
     * The window says what the confirmation does to the recorded result: a live
     * retirement records a new one, a continuation keeps the one it has.
     *
     * @param continued whether the preview keeps a frozen result
     */
    public void verifyConsequenceShown(final boolean continued) {
        final String shown = getLabeledById("retireConsequence").getText();
        assertTrue(
                shown.contains(continued ? "keeps the result you already have"
                        : "recorded in the Hall of Fame"),
                "Consequence in: " + shown);
    }

    /** Both choices are offered with their own labels. */
    public void verifyChoicesOffered() {
        assertEquals("Retire and end the game",
                getButtonById("retireConfirm").getText());
        assertEquals("Keep playing", getButtonById("retireCancel").getText());
    }

    public void verifyNoFrozenResult() {
        assertTrue(getModel().getFrozenResult().isEmpty());
    }

    /**
     * Cancel, or Exit, leaves the live game running on the same turn with no
     * terminal result and the same stored ranking.
     *
     * @param played the number of rounds played before the action
     * @param ranked the stored ranking size before the action
     */
    public void verifyCanceled(final int played, final int ranked) {
        assertTrue(getModel().isGameRunning());
        assertFalse(getModel().isGameFinished());
        assertTrue(getModel().getGameOverResult().isEmpty());
        verifyNoFrozenResult();
        assertEquals(played, getModel().getCalendar().getNumberOfPlayedTurns());
        assertEquals(ranked, rankingSize());
    }

    /**
     * Exit records no new score and no new ranked result.
     *
     * @param ranked the stored ranking size before Exit
     */
    public void verifyExitRecordedNothing(final int ranked) {
        verifyNoFrozenResult();
        assertEquals(ranked, rankingSize());
    }

    /** A live retirement froze one final result at the previewed score. */
    public void verifyLiveRetirementFinished() {
        assertTrue(getModel().isGameFinished());
        assertFalse(getModel().isGameRunning());
        assertEquals(GameOverEvaluator.REASON_RETIRED, getModel()
                .getGameOverResult().orElseThrow().getGameOverReason());
        assertEquals(GameResult.Status.FINAL, frozen().getStatus());
        assertEquals(rememberedTotal, frozen().getScore().getTotal());
    }

    /**
     * An unscored continuation ended by retirement keeps the frozen identity
     * and the frozen total it had before the retirement.
     */
    public void verifyContinuedRetirementFinished() {
        assertTrue(getModel().isGameFinished());
        assertEquals(GameOverEvaluator.REASON_RETIRED, getModel()
                .getGameOverResult().orElseThrow().getGameOverReason());
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                frozen().getStatus());
        assertEquals(rememberedIdentity, frozen().getIdentity());
        assertEquals(rememberedTotal, frozen().getScore().getTotal());
    }

    /**
     * An open rebellion that the player retired keeps its frozen score with no
     * independence bonus and no early points, and the rebellion is not won.
     */
    public void verifyDeclaredRetirementFinished() {
        verifyLiveRetirementFinished();
        assertTrue(frozen().getVictoryOrder().isEmpty());
        assertEquals(0, frozen().getScore().getOrderBonusPercent());
        assertEquals(0L, frozen().getScore().getEarlyPoints());
    }

    /**
     * The stored ranking was empty before the confirmation, so it now holds
     * exactly this one result: as its only entry, in first place and with the
     * frozen total. A duplicate or a second record fails here.
     */
    public void verifyStoredAsOnlyEntry() {
        final HallOfFame ranking = getContext()
                .getClassFromGuice(HallOfFameDao.class).load();
        assertEquals(1, ranking.getEntries().size(), "Stored ranking size");
        assertEquals(Optional.of(1), ranking.getRank(frozen().getIdentity()),
                "Place of the retired result");
        assertEquals(frozen().getScore().getTotal(),
                ranking.getEntries().get(0).getTotal(),
                "Stored total of the retired result");
    }

    /**
     * The end-of-game notice of the retirement is on screen. It is waited for
     * because the model publishes it on the JavaFX thread after the
     * confirmation.
     */
    public void verifyRetirementNotice() throws Exception {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                () -> getNodeFinder()
                        .lookup((Node node) -> node instanceof Label
                                && ((Label) node).getText() != null
                                && ((Label) node).getText()
                                        .contains(RETIREMENT_NOTICE))
                        .tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(RETIREMENT_NOTICE);
    }

    /** Acknowledge the end-of-game notice; the result window follows it. */
    public void closeRetirementNotice() {
        DialogMessagePage.of(getContext()).close();
    }
}
