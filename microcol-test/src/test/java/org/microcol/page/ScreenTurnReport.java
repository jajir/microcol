package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.microcol.gui.screen.turnreport.TurnEventPanel;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;

/**
 * Object allows to test one unit shown in right panel.
 */
public class ScreenTurnReport extends AbstractScreen {

    public static ScreenTurnReport of(final TestContext context) {
	return new ScreenTurnReport(context);

    }

    private ScreenTurnReport(final TestContext context) {
	super(context);
	assertNotNull(getButtonOk());
    }

    public void close() {
	getRobot().clickOn(getButtonOk(), MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
    }

    public Button getButtonOk() {
	return getNodeFinder().lookup("#buttonOk").queryAs(Button.class);
    }

    public void verifyNumberOfEvents(final int expectedNumberOfEvents) {
	assertEquals(expectedNumberOfEvents, getEventPanels().size(),
		String.format("Number of events '%s' is different from expected number of events '%s'",
			getEventPanels().size(), expectedNumberOfEvents));
    }

    private List<TurnEventPanel> getEventPanels() {
	final String cssClass = ".turn-event";
	final Set<TurnEventPanel> panels = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<TurnEventPanel>(panels);
    }

    public void verifyThatAtLeastOneEventMessageContains(final String expectedMessagePart) {
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
	fail(String.format("No turn event contains text '%s'. Messages was: %s", expectedMessagePart, out.toString()));
    }

}
