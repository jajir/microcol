package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.microcol.gui.screen.europe.BuyUnitPanel;
import org.microcol.gui.screen.europe.EuropeButtonsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

/**
 * Object allows to test one unit shown in right panel.
 */
public class BuyUnitScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(BuyUnitScreen.class);

    public static BuyUnitScreen of(final TestContext context) {
	return new BuyUnitScreen(context);
    }

    private BuyUnitScreen(final TestContext context) {
	super(context);
    }

    public EuropePortScreen buyUnitByName(final String unitNameToBuy) {
	final List<BuyUnitPanel> buyUnitPanels = getListOfNodes(".unitPanel");
	final BuyUnitPanel buyUnitPanel = buyUnitPanels.stream().filter(node -> {
	    final String label = getNodeFinder().from(node).lookup(".name").queryLabeled().getText();
	    logger.debug("Comparing given unit name '{}' with found '{}'", unitNameToBuy, label);
	    return unitNameToBuy.equals(label);
	}).findFirst().orElseThrow(() -> new IllegalArgumentException(
		String.format("There is no unit to buy with label '%s'", unitNameToBuy)));
	final Button button = getNodeFinder().from(buyUnitPanel).lookup(".buttonBuy").queryButton();
	getRobot().clickOn(button, MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
	return EuropePortScreen.of(getContext());
    }

    public EuropePortScreen close() {
	final Button buttonNextTurn = getButtonById(EuropeButtonsPanel.CLOSE_BUTTON_ID);
	assertNotNull(buttonNextTurn);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return EuropePortScreen.of(getContext());
    }

}
