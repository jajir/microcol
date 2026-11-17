package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.akropia.model.ModelListenerAdapter;
import org.akropia.model.event.TurnFinishedEvent;
import org.testfx.util.WaitForAsyncUtils;

import javafx.collections.ListChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/** Observes the real wait window without adding a delay to production AI. */
public final class TurnWaitingPage extends AbstractScreen {
    private volatile Stage waiting;
    private volatile long shown;
    private volatile long hidden;
    private final AtomicInteger frames = new AtomicInteger();
    private final CountDownLatch entered = new CountDownLatch(1);
    private final CountDownLatch release = new CountDownLatch(1);
    private ListChangeListener<Window> observer;

    public TurnWaitingPage(final TestContext context) {
        super(context);
    }

    public void observe() {
        observer = change -> {
            while (change.next()) {
                for (final Window window : change.getAddedSubList()) {
                    if (window.getScene() != null
                            && window.getScene().lookup("#turn-wait") != null) {
                        waiting = (Stage) window;
                        shown = System.nanoTime();
                        waiting.addEventHandler(WindowEvent.WINDOW_HIDDEN,
                                event -> hidden = System.nanoTime());
                        ((ImageView) window.getScene()
                                .lookup("#turn-wait-rowers")).viewportProperty()
                                .addListener((value, oldFrame,
                                        newFrame) -> frames.incrementAndGet());
                    }
                }
            }
        };
        getRobot().interact(() -> Window.getWindows().addListener(observer));
    }

    public void holdTurn(final boolean fail) {
        getModel().addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnFinished(final TurnFinishedEvent event) {
                if (fail ? event.getPlayer().isHuman()
                        : event.getPlayer().isComputer()
                                && entered.getCount() != 0) {
                    entered.countDown();
                    try {
                        if (!release.await(15, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Test hold expired");
                        }
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(interrupted);
                    }
                    if (fail) {
                        throw new IllegalStateException(
                                "Injected turn failure");
                    }
                }
            }
        });
    }

    public void requestTurn() {
        clickOnButtonWithId("nextTurn");
    }

    public void attemptDismissal() {
        getRobot().push(KeyCode.ESCAPE);
        getRobot().interact(() -> waiting.fireEvent(
                new WindowEvent(waiting, WindowEvent.WINDOW_CLOSE_REQUEST)));
        // The owner cannot receive a second turn through the blocked window.
        getRobot().clickOn(getPrimaryStage().getX() + 12,
                getPrimaryStage().getY() + 100);
        getRobot().push(KeyCode.ENTER);
    }

    public void releaseTurn() {
        release.countDown();
    }

    public void awaitClosed() throws Exception {
        WaitForAsyncUtils.waitFor(15, TimeUnit.SECONDS, () -> hidden != 0);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyHeldAndAnimated(final String expectedMessage)
            throws Exception {
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> System.nanoTime() - shown >= 1_300_000_000L
                        && frames.get() >= 3);
        getRobot().interact(() -> {
            assertTrue(waiting.isShowing());
            assertEquals(expectedMessage,
                    getLabeledById("turn-wait-message").getText());
            assertTrue(getNodeFinder().lookup("#turn-wait .button").queryAll()
                    .isEmpty());
        });
    }

    public void verifyClosedAfterMinimum() {
        assertNotNull(waiting);
        assertTrue(hidden - shown >= 1_000_000_000L,
                "The window must remain visible for at least one second");
        assertTrue(frames.get() >= 3, "Rowing must advance on the FX thread");
        assertFalse(waiting.isShowing());
    }

    public void verifyRound(final int expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getContext().getClassFromGuice(
                        org.akropia.gui.event.model.GameModelController.class)
                        .isHumanTurnReady());
        assertEquals(expected,
                getModel().getCalendar().getNumberOfPlayedTurns());
    }

    public void cleanup() {
        release.countDown();
        getRobot().interact(() -> Window.getWindows().removeListener(observer));
    }
}
