package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.gui.Tile.TILE_CENTER;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.TurnFinishedEvent;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/** Observes completed national turns and saved action boundaries. */
public final class NationalTurnPage extends AbstractScreen {
    private CountDownLatch paused;
    private CountDownLatch release;
    private CompletableFuture<Void> failedTransition;
    private final List<String> finished = new CopyOnWriteArrayList<>();

    public NationalTurnPage(final TestContext context) {
        super(context);
    }

    public void observeTurnOrder() {
        finished.clear();
        getRobot().interact(
                () -> getModel().addListener(new ModelListenerAdapter() {
                    @Override
                    public void onTurnFinished(final TurnFinishedEvent event) {
                        finished.add(event.getPlayer().getName());
                    }
                }));
    }

    public void verifyTurnOrder(final List<String> expected) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> finished.size() >= expected.size()
                        && getModel().isGameRunning()
                        && getModel().getCurrentPlayer().isHuman());
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, finished);
        assertEquals("Dutch", getModel().getCurrentPlayer().getName());
    }

    public JsonElement readTurnState() {
        return new Gson().toJsonTree(getModel().save());
    }

    public void verifyTurnState(final JsonElement expected) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, readTurnState(),
                "Continue must preserve the already prepared turn exactly");
    }

    public void pauseBeforeNextHumanFinish() {
        paused = new CountDownLatch(1);
        release = new CountDownLatch(1);
        getModel().addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnFinished(final TurnFinishedEvent event) {
                if (event.getPlayer().isHuman()) {
                    paused.countDown();
                    try {
                        if (!release.await(30, TimeUnit.SECONDS)) {
                            throw new IllegalStateException(
                                    "Test did not release turn preparation");
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(exception);
                    }
                }
            }
        });
    }

    public void clickNextTurnWithoutWaiting() {
        getRobot().clickOn("#nextTurn");
    }

    public void clickMapTile(final org.microcol.model.Location location) {
        final org.microcol.gui.Point point = getContext().getArea()
                .convertToCanvasPoint(location)
                .add(getPrimaryStage().getX(), getPrimaryStage().getY())
                .add(TILE_CENTER);
        getRobot().clickOn(point.getX(), point.getY());
    }

    public void releasePreparation() {
        release.countDown();
    }

    public JsonElement readUnitState(final int unitId) {
        return new Gson().toJsonTree(getModel().getUnitById(unitId).save());
    }

    public void verifyPreparationGate() throws Exception {
        assertTrue(paused.await(5, TimeUnit.SECONDS));
        assertTrue(getModel().getCurrentPlayer().isHuman());
        assertFalse(getModel().isGameRunning());
        assertFalse(getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.GamePanelController.class)
                .isMouseEnabled());
    }

    public void verifyUnitAndMoveMode(final int unitId,
            final JsonElement expected, final boolean expectedMoveMode) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expected, readUnitState(unitId));
        assertEquals(expectedMoveMode, getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.ModeController.class)
                .isMoveMode());
    }

    public void failNextPreparation() {
        final org.microcol.model.Model model = getModel();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnFinished(final TurnFinishedEvent event) {
                throw new IllegalStateException("Injected UI recovery failure");
            }
        });
        failedTransition = CompletableFuture.runAsync(model::endTurn);
    }

    public void verifyFailedPreparation() {
        final ExecutionException failure = assertThrows(
                ExecutionException.class,
                () -> failedTransition.get(5, TimeUnit.SECONDS));
        assertEquals("Injected UI recovery failure",
                failure.getCause().getMessage());
        assertFalse(getModel().isGameRunning());
    }

    public void verifyRecoveryMessage(final String expectedMessage) {
        final String expectedText = expectedMessage + "\n\n"
                + new FreePlayAuditPage(getContext()).diagnosticFolderLine();
        assertEquals(expectedText, getNodeFinder().lookup((
                javafx.scene.Node node) -> node instanceof javafx.scene.control.Label
                        && expectedText.equals(
                                ((javafx.scene.control.Label) node).getText()))
                .queryAs(javafx.scene.control.Label.class).getText());
    }

}
