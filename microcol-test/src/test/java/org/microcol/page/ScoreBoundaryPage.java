package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.microcol.model.BaseScore;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.GameOverResult;
import org.microcol.model.GameResult;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.ResultScore;
import org.microcol.model.event.TurnStartedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Label;

/**
 * The frozen free-play result as the player meets it (R-0193): the choice that
 * a real end of turn opens at spring 1800, the postponed choice and its way
 * back from the statistics, the unscored continuation, Save and Continue, and
 * the end of all play at spring 1850. The page only reads what the player sees
 * and what the model recorded.
 */
public final class ScoreBoundaryPage extends AbstractScreen {

    private static final String DIALOG = "scoreBoundaryDialog";

    private static final Pattern POINTS = Pattern.compile("(\\d+) points");

    /** The frozen result as it was when it was last remembered. */
    private String remembered = "";

    private String rememberedIdentity = "";

    public ScoreBoundaryPage(final TestContext context) {
        super(context);
    }

    private GameResult frozen() {
        return getModel().getFrozenResult().orElseThrow();
    }

    /** Every named component of the frozen result, in one comparable text. */
    private static String describe(final GameResult result) {
        final BaseScore base = result.getBaseScore();
        final ResultScore score = result.getScore();
        return String.join("|", result.getIdentity(),
                String.valueOf(base.getServants()),
                String.valueOf(base.getColonists()),
                String.valueOf(base.getExperts()),
                String.valueOf(base.getFathers()),
                String.valueOf(base.getGold()),
                String.valueOf(base.getSupportPercent()),
                String.valueOf(score.getDestroyedSettlements()),
                String.valueOf(score.getBellsAfterTarget()),
                String.valueOf(score.getTotal()));
    }

    private long points(final String id) {
        final Matcher matcher = POINTS.matcher(getLabeledById(id).getText());
        if (!matcher.find()) {
            throw new IllegalStateException("No points in " + id);
        }
        return Long.parseLong(matcher.group(1));
    }

    // --- player gestures ---------------------------------------------------

    /** Remember the frozen result for later comparisons. */
    public void rememberFrozenResult() {
        remembered = describe(frozen());
        rememberedIdentity = frozen().getIdentity();
    }

    /** End the turn and wait for the choice that spring 1800 opens. */
    public void endTurnToBoundary() throws Exception {
        clickOnButtonWithId("nextTurn");
        waitForDialog();
    }

    /** End the turn and wait for the end-of-game message of spring 1850. */
    public void endTurnToFreePlayEnd(final String shownText) throws Exception {
        clickOnButtonWithId("nextTurn");
        WaitForAsyncUtils
                .waitFor(60, TimeUnit.SECONDS,
                        () -> getNodeFinder()
                                .lookup((Node node) -> node instanceof Label
                                        && ((Label) node).getText() != null
                                        && ((Label) node).getText()
                                                .contains(shownText))
                                .tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Wait for the choice window that the model asked for. */
    public void waitForDialog() throws Exception {
        WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#" + DIALOG).tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the window without choosing; the game stays paused. */
    public void decideLater() {
        clickOnButtonWithId("scoreBoundaryLater");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Choose to continue without scoring and wait until the human turn of the
     * new round has begun. A fresh turn with events opens the turn report,
     * which then replaces the screen the choice was made on.
     *
     * @return the report that the new turn opened, if it had events
     */
    public Optional<ScreenTurnReport> continueUnscored() throws Exception {
        final CountDownLatch started = new CountDownLatch(1);
        final ModelListenerAdapter listener = new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (event.getPlayer().isHuman()) {
                    started.countDown();
                }
            }
        };
        getModel().addListener(listener);
        try {
            clickOnButtonWithId("scoreBoundaryContinue");
            if (!started.await(60, TimeUnit.SECONDS)) {
                throw new TimeoutException(
                        "The unscored round did not reach the human turn");
            }
        } finally {
            getModel().removeListener(listener);
        }
        WaitForAsyncUtils.waitForFxEvents();
        if (getModel()
                .isTurnEventsMessagesEmpty(getModel().getCurrentPlayer())) {
            return Optional.empty();
        }
        WaitForAsyncUtils
                .waitFor(60, TimeUnit.SECONDS,
                        () -> WaitForAsyncUtils.asyncFx(() -> !getNodeFinder()
                                .lookup(".turn-event").queryAll().isEmpty())
                                .get(60, TimeUnit.SECONDS));
        WaitForAsyncUtils.waitForFxEvents();
        return Optional.of(ScreenTurnReport.of(getContext()));
    }

    /**
     * Return to the map from the screen left after continuing: close the turn
     * report if the new turn opened one, then the statistics if they are still
     * shown.
     *
     * @param report the report that the new turn opened, if any
     * @return the map
     */
    public GamePage returnToMap(final Optional<ScreenTurnReport> report) {
        report.ifPresent(ScreenTurnReport::close);
        if (isCssIdVisible("baseScoreTab")) {
            clickOnButtonWithId("closeButtonId");
        }
        return GamePage.of(getContext());
    }

    /** Open the base score tab of the statistics. */
    public void openStatistics() {
        new BaseScorePage(getContext()).open();
    }

    /** Leave the statistics for the map. */
    public GamePage closeStatistics() {
        return new BaseScorePage(getContext()).close();
    }

    /** Use the statistics action that opens the pending choice again. */
    public void decideFromStatistics() throws Exception {
        clickOnButtonWithId("decideFrozenScore");
        waitForDialog();
    }

    /** Acknowledge the end-of-game message. */
    public void closeEndMessage() {
        DialogMessagePage.of(getContext()).close();
    }

    public void closeReport(final Optional<ScreenTurnReport> report) {
        report.ifPresent(ScreenTurnReport::close);
    }

    // --- explicit observations --------------------------------------------

    public void verifyNoFrozenResult() {
        assertTrue(getModel().getFrozenResult().isEmpty());
    }

    /** The calendar has played exactly this many rounds. */
    public void verifyTurn(final int played) {
        assertEquals(played, getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void verifyDialogShown(final boolean expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, isCssIdVisible(DIALOG));
    }

    /** The window states the calculated result that the model froze. */
    public void verifyDialogShowsFrozenTotal() {
        final String shown = getLabeledById("scoreBoundarySummary").getText();
        assertTrue(
                shown.contains("frozen result is "
                        + frozen().getScore().getTotal() + " points"),
                "Frozen total in: " + shown);
    }

    /** The window names the components that the model froze. */
    public void verifyDialogComponents() {
        final String shown = getLabeledById("scoreBoundaryParts").getText();
        assertTrue(shown.contains(
                "base " + frozen().getBaseScore().getSubtotal() + " points"),
                "Base subtotal in: " + shown);
    }

    /** The two choices are offered and the unscored limit is explained. */
    public void verifyDialogOffersChoices() {
        assertEquals("Continue without scoring",
                getButtonById("scoreBoundaryContinue").getText());
        assertEquals("Decide later",
                getButtonById("scoreBoundaryLater").getText());
        assertFalse(getButtonById("scoreBoundaryContinue").isDisabled());
        final String note = getLabeledById("scoreBoundaryUnscored").getText();
        assertTrue(note.contains("until spring 1850"), "Limit in: " + note);
    }

    /**
     * The choice window names the storage failure of the Hall of Fame, and both
     * choices remain on it.
     */
    public void verifyRankingStorageFailure() {
        final String shown = getLabeledById("scoreBoundaryRanking").getText();
        assertTrue(shown.contains("could not be read or saved"),
                "Ranking status in: " + shown);
        assertFalse(getButtonById("scoreBoundaryContinue").isDisabled());
        assertFalse(getButtonById("scoreBoundaryLater").isDisabled());
    }

    /**
     * The choice window shows the place of the frozen result in the top ten.
     */
    public void verifyRankingPlace(final int rank) {
        assertEquals("Your result is ranked " + rank + " in the top ten.",
                getLabeledById("scoreBoundaryRanking").getText());
    }

    /** The game is paused at the boundary with the choice still open. */
    public void verifyAwaitingChoice() {
        assertTrue(getModel().isAwaitingScoreChoice());
        assertFalse(getModel().isGameRunning());
        assertEquals(GameResult.Status.AWAITING_CONTINUATION,
                frozen().getStatus());
    }

    public void verifyNextTurnBlocked() {
        assertTrue(getButtonById("nextTurn").isDisabled());
    }

    public void verifyStatisticsReachable() {
        assertFalse(getButtonById("statistics").isDisabled());
    }

    /**
     * The statistics show the frozen result, not the live preview: its
     * identity, its status and its recorded total.
     *
     * @param status required part of the status text
     */
    public void verifyFrozenDisplay(final String status) {
        assertFalse(isCssIdVisible("liveTitle"),
                "A frozen result is never labelled as the live preview");
        assertTrue(getLabeledById("frozenTitle").getText()
                .startsWith("Frozen result"));
        assertTrue(getLabeledById("frozenIdentity").getText()
                .contains(rememberedIdentity));
        assertTrue(getLabeledById("frozenStatus").getText().contains(status),
                "Status in: " + getLabeledById("frozenStatus").getText());
        assertTrue(getLabeledById("baseScoreTitle").getText()
                .startsWith("Frozen base score"));
        assertTrue(getLabeledById("resultTitle").getText()
                .startsWith("Frozen calculated result"));
        assertEquals(frozen().getScore().getTotal(), points("resultTotal"));
        assertEquals(frozen().getBaseScore().getSubtotal(),
                points("baseScoreSubtotal"));
    }

    /** The statistics offer, or do not offer, to decide the pending choice. */
    public void verifyDecideActionOffered(final boolean expected) {
        assertEquals(expected, isCssIdVisible("decideFrozenScore"));
    }

    /** The game continued unscored and plays on. */
    public void verifyContinuedUnscored() {
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                frozen().getStatus());
        assertFalse(getModel().isAwaitingScoreChoice());
        assertTrue(getModel().isGameRunning());
    }

    /** The frozen result has the remembered identity and every component. */
    public void verifyRememberedResultKept() {
        assertEquals(remembered, describe(frozen()));
    }

    /** All play ended with the free-play reason and the earlier result. */
    public void verifyFreePlayEnded() {
        assertTrue(getModel().isGameFinished());
        assertFalse(getModel().isGameRunning());
        assertEquals(Optional.of(GameOverEvaluator.REASON_FREE_PLAY_ENDED),
                getModel().getGameOverResult()
                        .map(GameOverResult::getGameOverReason));
        assertEquals(GameResult.Status.CONTINUED_UNSCORED,
                frozen().getStatus());
    }

    /** The end-of-game message is on screen. */
    public void verifyEndMessage(final String expectedText) throws Exception {
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(expectedText);
    }
}
