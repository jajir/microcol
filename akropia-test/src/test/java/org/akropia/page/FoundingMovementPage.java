package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Player paths for the exhausted founder; inspection never changes the model.
 */
public final class FoundingMovementPage extends AbstractScreen {
    private String before;

    public FoundingMovementPage(final TestContext context) {
        super(context);
    }

    public void rememberWorld() {
        before = new Gson().toJson(getModel().save());
    }

    public void verifyWaitingAction() {
        verifyWaitingAction("no movement remaining");
    }

    public void verifyWaitingAction(final String explanation) {
        openUnitActions();
        final Button button = getButtonById("buildColony");
        assertTrue(button.isVisible());
        assertTrue(button.isDisabled());
        assertTrue(button.getAccessibleHelp().contains(explanation));
        assertTrue(getNodeFinder().lookup(".unit-action-explanation")
                .queryAllAs(Label.class).stream()
                .anyMatch(label -> label.isVisible()
                        && label.getText().contains(explanation)));
    }

    public void pressFoundingShortcut() {
        getRobot().push(KeyCode.B);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyInformation() {
        verifyInformation("no movement remaining");
    }

    public void verifyInformation(final String explanation) {
        assertEquals(1, getNodeFinder().lookup("#buttonOk").queryAll().size());
        assertTrue(getNodeFinder().lookup(
                (java.util.function.Predicate<javafx.scene.Node>) node -> node instanceof Label
                        && ((Label) node).getText().contains(explanation))
                .tryQuery().isPresent());
        assertFalse(getNodeFinder().lookup("#colonyNameInput").tryQuery()
                .isPresent());
    }

    public void dismissWithOk() {
        clickOnButtonWithId("buttonOk");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyFoundingAvailable() {
        openUnitActions();
        final Button button = getButtonById("buildColony");
        assertTrue(button.isVisible());
        assertFalse(button.isDisabled());
    }

    public void dismiss(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyWorldUnchanged() {
        assertEquals(before, new Gson().toJson(getModel().save()));
    }

    public void verifyInformationClosed() {
        assertTrue(getNodeFinder().lookup("#buttonOk").tryQuery().isEmpty());
    }
}
