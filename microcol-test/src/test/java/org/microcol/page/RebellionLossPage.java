package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.model.GameOverResult;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;
import javafx.scene.Node;

/**
 * The player's view of a lost rebellion (R-0192): ordinary turns pass until a
 * Crown action ends the rebellion, then the game shows the reason and leaves
 * for the campaign screen. The page only presses the real next-turn button; the
 * Crown's own turn ends the rebellion, and nothing here changes the model.
 */
public final class RebellionLossPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    public RebellionLossPage(final TestContext context) {
        super(context);
    }

    /**
     * Press the next-turn button until the game ends, closing the turn report
     * of every turn that does not end it. Each press waits for the real
     * outcome: the next human turn or the end of the game.
     *
     * @param maxTurns required most turns to press
     */
    public void endTurnsUntilTheGameEnds(final int maxTurns) throws Exception {
        final GamePage game = GamePage.of(getContext());
        for (int turn = 1; turn <= maxTurns
                && !getModel().isGameFinished(); turn++) {
            final int previousRound = getModel().getCalendar()
                    .getNumberOfPlayedTurns();
            final CountDownLatch settled = new CountDownLatch(1);
            final ModelListenerAdapter listener = new ModelListenerAdapter() {
                @Override
                public void onTurnStarted(final TurnStartedEvent event) {
                    if (event.getPlayer().isHuman() && getModel().getCalendar()
                            .getNumberOfPlayedTurns() > previousRound) {
                        settled.countDown();
                    }
                }

                @Override
                public void onGameFinished(final GameFinishedEvent event) {
                    settled.countDown();
                }
            };
            getModel().addListener(listener);
            try {
                getRobot().clickOn(
                        getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID));
                if (!settled.await(60, TimeUnit.SECONDS)) {
                    throw new TimeoutException(
                            "Neither the next human turn nor the end of the game came");
                }
            } finally {
                getModel().removeListener(listener);
            }
            if (!getModel().isGameFinished()) {
                game.waitWhileMoving();
                closeTurnReportIfShown();
            }
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void closeTurnReportIfShown() throws Exception {
        if (!getModel()
                .isTurnEventsMessagesEmpty(getModel().getCurrentPlayer())) {
            WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils.asyncFx(this::isButtonBackVisible)
                            .get(60, TimeUnit.SECONDS));
            WaitForAsyncUtils.waitForFxEvents();
            ScreenTurnReport.of(getContext()).close();
        }
    }

    /** Acknowledge the end-of-game message of the original game screen. */
    public void closeLossDialog() {
        DialogMessagePage.of(getContext()).close();
    }

    /**
     * The message that explains the loss is on screen.
     *
     * @param expectedText required part of the shown reason
     */
    public void verifyLossDialog(final String expectedText) throws Exception {
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(expectedText);
    }

    /** No end-of-game message is shown, such as after Continue. */
    public void verifyNoLossDialogShown(final String unexpectedText) {
        final Node found = getNodeFinder()
                .lookup((Node node) -> node instanceof Label
                        && ((Label) node).getText() != null
                        && ((Label) node).getText().contains(unexpectedText))
                .tryQuery().orElse(null);
        assertNull(found, "Unexpected dialog text: " + unexpectedText);
    }

    /**
     * The model recorded the one terminal result of the game.
     *
     * @param expectedReason required reason code
     */
    public void verifyGameOverReason(final String expectedReason) {
        assertTrue(getModel().isGameFinished(), "The game must be finished");
        assertEquals(Optional.of(expectedReason), getModel().getGameOverResult()
                .map(GameOverResult::getGameOverReason));
        assertEquals(Optional.of(expectedReason),
                getModel().getPlayerByName(DUTCH).getRebellionLossReason());
    }

    /** The game is open: no terminal result and no lost rebellion. */
    public void verifyRebellionHolds() {
        assertFalse(getModel().isGameFinished(), "The game must still be open");
        assertTrue(getModel().getGameOverResult().isEmpty());
        assertTrue(getModel().getPlayerByName(DUTCH).getRebellionLossReason()
                .isEmpty());
    }

    /**
     * The saved rebellion members, whoever holds them now.
     *
     * @param expectedColonyIds required member colony IDs
     */
    public void verifyRebellionMembers(final Set<Integer> expectedColonyIds) {
        assertEquals(expectedColonyIds,
                getModel().getPlayerByName(DUTCH).getRebellionColonyIds());
    }

    /**
     * @param colonyId      required colony
     * @param expectedOwner required name of its current owner
     */
    public void verifyColonyOwner(final int colonyId,
            final String expectedOwner) {
        assertEquals(expectedOwner, getModel().getColonyById(colonyId)
                .orElseThrow().getOwner().getName());
    }
}
