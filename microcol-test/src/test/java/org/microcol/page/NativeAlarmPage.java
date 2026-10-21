package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.TurnStartedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/**
 * Player gestures and observations for the native settlement details: the
 * status text of a hovered settlement, the keyboard dialog opened with N and
 * closed with Escape, and the screenshots kept as review evidence.
 */
public final class NativeAlarmPage extends AbstractScreen {

    public static NativeAlarmPage of(final TestContext context) {
        return new NativeAlarmPage(context);
    }

    private NativeAlarmPage(final TestContext context) {
        super(context);
    }

    /** Press N on the game screen, the shortcut of the details dialog. */
    public void openWithNKey() throws Exception {
        getRobot().push(KeyCode.N);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> isCssIdVisible(NativeSettlementsDialog.CONTENT_ID));
    }

    /**
     * End the turn with the real Next turn button while the pointer stays on
     * the hovered tile, so the status text can only change by the live refresh.
     * The button is fired, not clicked, because a click would move the pointer.
     *
     * @return the turn report, when the new turn has messages
     * @throws Exception if the human turn is not prepared in time
     */
    public Optional<ScreenTurnReport> nextTurnKeepingPointer()
            throws Exception {
        final int previousRound = getModel().getCalendar()
                .getNumberOfPlayedTurns();
        final CountDownLatch prepared = new CountDownLatch(1);
        final ModelListenerAdapter listener = new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (event.getPlayer().isHuman() && getModel().getCalendar()
                        .getNumberOfPlayedTurns() > previousRound) {
                    prepared.countDown();
                }
            }
        };
        getModel().addListener(listener);
        try {
            final Button nextTurn = getButtonById(
                    ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
            getRobot().interact(nextTurn::fire);
            if (!prepared.await(60, TimeUnit.SECONDS)) {
                throw new TimeoutException(
                        "Human turn preparation did not finish");
            }
        } finally {
            getModel().removeListener(listener);
        }
        WaitForAsyncUtils.waitForFxEvents();
        if (!getModel()
                .isTurnEventsMessagesEmpty(getModel().getCurrentPlayer())) {
            WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils.asyncFx(this::isButtonBackVisible)
                            .get(60, TimeUnit.SECONDS));
            WaitForAsyncUtils.waitForFxEvents();
            return Optional.of(ScreenTurnReport.of(getContext()));
        }
        return Optional.empty();
    }

    /** Escape closes the details; the dialog keeps no changes. */
    public void closeWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Keep the open dialog as review evidence under target/warehouse-qa. */
    public void captureDialog(final String fileName) throws IOException {
        captureDialog(NativeSettlementsDialog.CONTENT_ID, fileName);
    }

    /**
     * The hover text of the status bar must name the three meanings: attitude,
     * signed alarm change with its direction, and the provoker.
     */
    public void verifyStatusBarShows(final String attitude,
            final String alarmChange, final String provoker) {
        // The live refresh is queued on the JavaFX thread after the model's
        // turn callback, so wait for it before comparing.
        try {
            WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                    () -> WaitForAsyncUtils.asyncFx(() -> {
                        final String shown = getLabeledById(
                                StatusBarView.STATUS_BAR_LABEL_ID).getText();
                        return shown.contains(attitude)
                                && shown.contains(alarmChange)
                                && shown.contains(provoker);
                    }).get(10, TimeUnit.SECONDS));
        } catch (TimeoutException notShown) {
            // The assertions below report the text actually shown.
        }
        final String text = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID)
                .getText();
        assertTrue(text.contains(attitude), () -> "Status bar should contain '"
                + attitude + "' but was: " + text);
        assertTrue(text.contains(alarmChange),
                () -> "Status bar should contain '" + alarmChange
                        + "' but was: " + text);
        assertTrue(text.contains(provoker), () -> "Status bar should contain '"
                + provoker + "' but was: " + text);
    }

    public void verifyDialogOpen() {
        assertTrue(isCssIdVisible(NativeSettlementsDialog.CONTENT_ID),
                "Native settlement details are not shown");
        assertTrue(isCssIdVisible(NativeSettlementsDialog.LIST_ID),
                "Native settlement list is not shown");
    }

    /** The three meanings as separate lines of the selected settlement. */
    public void verifyDialogRelation(final String attitude,
            final String alarmChange, final String provoker) {
        assertEquals(attitude,
                getLabeledById(NativeSettlementsDialog.ATTITUDE_ID).getText(),
                "Unexpected attitude line");
        assertEquals(alarmChange,
                getLabeledById(NativeSettlementsDialog.ALARM_CHANGE_ID)
                        .getText(),
                "Unexpected alarm change line");
        assertEquals(provoker,
                getLabeledById(NativeSettlementsDialog.RIVAL_ID).getText(),
                "Unexpected provoker line");
    }

    /** Keyboard users reach the settlement list as soon as the dialog opens. */
    public void verifyListHasFocus() throws Exception {
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(
                WaitForAsyncUtils.asyncFx(() -> getNodeFinder()
                        .lookup("#" + NativeSettlementsDialog.LIST_ID).query()
                        .isFocused()).get(5, TimeUnit.SECONDS),
                "The settlement list must have keyboard focus when opened");
    }

    /**
     * The dialog content lies inside its window at the test viewport, so no
     * line is cut off.
     */
    public void verifyDialogFitsWindow() throws Exception {
        final Node root = getNodeFinder()
                .lookup("#" + NativeSettlementsDialog.CONTENT_ID).query();
        final boolean fits = WaitForAsyncUtils.asyncFx(() -> {
            final Bounds bounds = root.localToScene(root.getBoundsInLocal());
            return bounds.getMinX() >= 0 && bounds.getMinY() >= 0
                    && bounds.getMaxX() <= root.getScene().getWidth()
                    && bounds.getMaxY() <= root.getScene().getHeight();
        }).get(5, TimeUnit.SECONDS);
        assertTrue(fits, "Native settlement details must fit their window");
    }

    public void verifyDialogClosed() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !isCssIdVisible(NativeSettlementsDialog.CONTENT_ID));
        assertFalse(isCssIdVisible(NativeSettlementsDialog.CONTENT_ID),
                "Native settlement details should be closed");
    }

}
