package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.gui.screen.colony.PanelColonyName;
import org.microcol.gui.screen.turnreport.TurnEventPanel;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Object allows to test one unit shown in right panel.
 */
public class ScreenTurnReport extends AbstractScreen {

    public static ScreenTurnReport of(final TestContext context) {
        return new ScreenTurnReport(context);

    }

    private Button retainedAction;

    private ScreenTurnReport(final TestContext context) {
        super(context);
    }

    public void verifyReady() {
        assertNotNull(getButtonOk());
    }

    public void close() {
        getRobot().clickOn(getButtonOk(), MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public Button getButtonOk() {
        return getNodeFinder().lookup("#closeButtonId").queryAs(Button.class);
    }

    public void verifyNumberOfEvents(final int expectedNumberOfEvents) {
        assertEquals(expectedNumberOfEvents, getEventPanels().size(),
                String.format(
                        "Number of events '%s' is different from expected number of events '%s'",
                        getEventPanels().size(), expectedNumberOfEvents));
    }

    public void verifyFamineLossCount(final int expectedCount) {
        final long actualCount = getEventPanels().stream()
                .map(panel -> (Label) panel.getChildren().get(1))
                .map(Label::getText)
                .filter(message -> message.startsWith("Famine in colony "))
                .count();
        assertEquals(expectedCount, actualCount);
    }

    public void verifyEventMessageCount(final String expectedPart,
            final int expectedCount) {
        final long count = getEventPanels().stream()
                .map(panel -> ((Label) panel.getChildren().get(1)).getText())
                .filter(message -> message.contains(expectedPart)).count();
        assertEquals(expectedCount, count,
                String.format(
                        "Unexpected number of turn events containing '%s'",
                        expectedPart));
    }

    public void verifyOpenColonyAction(final int colonyId,
            final String expectedText) {
        final Button button = getOpenColonyButton(colonyId);
        assertEquals(expectedText, button.getText());
        assertTrue(button.isVisible() && !button.isDisabled(),
                "Open colony action must be available");
        assertTrue(button.getWidth() >= button.prefWidth(-1),
                "Open colony label must not be truncated");
        final javafx.scene.layout.Region row = (javafx.scene.layout.Region) button
                .getParent();
        final javafx.scene.layout.Region report = (javafx.scene.layout.Region) row
                .getParent();
        assertTrue(row.getWidth() <= report.getMaxWidth(),
                String.format(
                        "Open colony row width %s must fit the report width %s",
                        row.getWidth(), report.getMaxWidth()));
    }

    /** The notice of the named colony offers Open colony. */
    public void verifyOpenColonyActionFor(final String colonyName,
            final String expectedText) {
        verifyOpenColonyAction(
                getModel().getColonyByName(colonyName).orElseThrow().getId(),
                expectedText);
    }

    /**
     * Reach the report's Open colony action with Tab and activate it with
     * Space.
     */
    public ColonyScreen openColonyWithKeyboard(final int colonyId,
            final String expectedColonyName) {
        final Button button = getOpenColonyButton(colonyId);
        for (int i = 0; i < 40 && !button.isFocused(); i++) {
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (!button.isFocused()) {
            throw new IllegalStateException(
                    "Open colony action is not keyboard reachable");
        }
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
        return ColonyScreen.of(getContext(), expectedColonyName);
    }

    public ColonyScreen openColonyWithMouse(final int colonyId,
            final String expectedColonyName) {
        getRobot().clickOn(getOpenColonyButton(colonyId), MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return ColonyScreen.of(getContext(), expectedColonyName);
    }

    /** Keep the shown action so it can be used after the game changes. */
    public void retainOpenColonyAction(final int colonyId) {
        retainedAction = getOpenColonyButton(colonyId);
    }

    public void clickRetainedOpenColonyAction() {
        getRobot().clickOn(retainedAction, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Fire the retained action even if its old report is no longer shown. */
    public void fireRetainedOpenColonyAction() {
        getRobot().interact(retainedAction::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyShown() {
        assertTrue(isButtonBackVisible(), "The turn report must stay shown");
    }

    public void verifyOpenColonyActionAbsent(final int colonyId) {
        assertTrue(
                getNodeFinder().lookup("#turnEventOpenColony-" + colonyId)
                        .tryQuery().isEmpty(),
                "Open colony action must not be offered");
    }

    public void verifyColonyScreenNotShown() {
        assertFalse(isCssIdVisible(PanelColonyName.COLONY_NAME_ID),
                "The inaccessible colony must not be opened");
    }

    /**
     * The arrival notice offers Open Europe as a visible, untruncated action.
     */
    public void verifyOpenEuropeAction(final String expectedText) {
        final Button button = getOpenEuropeButton();
        assertEquals(expectedText, button.getText());
        assertTrue(button.isVisible() && !button.isDisabled(),
                "Open Europe action must be available");
        assertTrue(button.getWidth() >= button.prefWidth(-1),
                "Open Europe label must not be truncated");
    }

    public void verifyOpenEuropeActionAbsent() {
        assertTrue(getNodeFinder().lookup("#turnEventOpenEurope").tryQuery()
                .isEmpty(), "Open Europe action must not be offered");
    }

    /** Activate the report's Open Europe action with the mouse. */
    public EuropePortScreen openEuropeWithMouse() {
        getRobot().clickOn(getOpenEuropeButton(), MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return EuropePortScreen.of(getContext());
    }

    /**
     * Close the report with its Close button. With the Europe opening
     * preference on, the arrival then shows Europe.
     */
    public EuropePortScreen closeIntoEurope() {
        close();
        return EuropePortScreen.of(getContext());
    }

    private Button getOpenEuropeButton() {
        return getNodeFinder().lookup("#turnEventOpenEurope")
                .queryAs(Button.class);
    }

    private Button getOpenColonyButton(final int colonyId) {
        return getNodeFinder().lookup("#turnEventOpenColony-" + colonyId)
                .queryAs(Button.class);
    }

    private List<TurnEventPanel> getEventPanels() {
        final String cssClass = ".turn-event";
        final Set<TurnEventPanel> panels = getNodeFinder().lookup(cssClass)
                .queryAll();
        return new ArrayList<TurnEventPanel>(panels);
    }

    public void verifyThatAtLeastOneEventMessageContains(
            final String expectedMessagePart) {
        final List<String> messages = new ArrayList<>();
        for (final TurnEventPanel eventPane : getEventPanels()) {
            final Label label = (Label) eventPane.getChildren().get(1);
            final String strLabel = label.getText();
            messages.add(strLabel);
            if (strLabel.contains(expectedMessagePart)) {
                return;
            }
        }
        final StringBuilder out = new StringBuilder();
        messages.forEach(msg -> {
            out.append(msg);
            out.append(", ");
        });
        fail(String.format("No turn event contains text '%s'. Messages was: %s",
                expectedMessagePart, out.toString()));
    }

}
