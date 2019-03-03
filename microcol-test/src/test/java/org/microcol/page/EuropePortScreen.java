package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.microcol.gui.screen.europe.EuropeButtonsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;

/**
 * Object allows to test one unit shown in right panel.
 */
public class EuropePortScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(EuropePortScreen.class);

    public static EuropePortScreen of(final TestContext context) {
	return new EuropePortScreen(context);
    }

    private EuropePortScreen(final TestContext context) {
	super(context);
	verifyEuropePortName();
    }

    private void verifyEuropePortName() {
	final String expectedNamePart = "Europe";
	final String cssClass = ".label-title";
	final List<Labeled> list = getListOfNodes(cssClass);
	final Labeled labeled = list.get(0);
	logger.info("Colony name: " + labeled.getText());
	assertTrue(labeled.getText().contains(expectedNamePart),
		String.format("Text '%s' should appear in Europe port name '%s'", expectedNamePart, labeled.getText()));
    }

    public GamePage close() {
	final Button buttonNextTurn = getButtoonById(EuropeButtonsPanel.CLOSE_BUTTON_ID);
	getRobot().clickOn(buttonNextTurn);
	return GamePage.of(getContext());
    }

}
