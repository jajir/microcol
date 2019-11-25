package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.microcol.gui.screen.europe.EuropeButtonsPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * Object allows to test one unit shown in right panel.
 */
public class EuropePortScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory.getLogger(EuropePortScreen.class);

    private final PageComponentPanelDock pageComponentPanelDock;

    public static EuropePortScreen of(final TestContext context) {
	return new EuropePortScreen(context);
    }

    private EuropePortScreen(final TestContext context) {
	super(context);
	pageComponentPanelDock = PageComponentPanelDock.of(context);
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

    public void assertGold(final int expectedAmountOfGold) {
	final Labeled labeledGold = getLabeledById("labelGold");
	String gold = labeledGold.getText();
	gold = gold.substring(gold.lastIndexOf(" ") + 1);
	final int realGold = Integer.valueOf(gold);
	assertEquals(expectedAmountOfGold, realGold,
		String.format("Expected gold was '%s' but really is '%s'", expectedAmountOfGold, realGold));
    }

    public BuyUnitScreen openBuyUnitScreen() {
	final Button buttonNextTurn = getButtonById(EuropeButtonsPanel.BUY_BUTTON_ID);
	getRobot().clickOn(buttonNextTurn);
	WaitForAsyncUtils.waitForFxEvents();
	return BuyUnitScreen.of(getContext());
    }

    public EuropePortScreen selectUnitFromPort(final int unitIndex) {
	pageComponentPanelDock.selectUnitFromPort(unitIndex);
	return this;
    }

    public void verifyNumberOfShipsInPort(final int expectedNumberOfShipsInPort) {
	pageComponentPanelDock.verifyNumberOfShipsInPort(expectedNumberOfShipsInPort);
    }

    public ScreenMarketBuy buyGoodsAndMoveItToCargoSlotWithPressedControl(final int indexOfGoods,
	    final int indexOfCargoSlot) {
	getRobot().press(KeyCode.CONTROL);
	buyGoodsAndMoveItToCargoSlot(indexOfGoods, indexOfCargoSlot);
	getRobot().release(KeyCode.CONTROL);
	WaitForAsyncUtils.waitForFxEvents();
	return ScreenMarketBuy.of(getContext());
    }

    public void buyGoodsAndMoveItToCargoSlot(final int indexOfGoods, final int indexOfCargoSlot) {
	getRobot().drag(getListOfGoodsToBuy().get(indexOfGoods), MouseButton.PRIMARY)
		.dropTo(pageComponentPanelDock.getListOfCrates().get(indexOfCargoSlot)).release(MouseButton.PRIMARY);
    }

    public ScreenMarketSell sellGoodsWithPressedControl(final int indexOfCargoSlot, final int indexOfGoods) {
	getRobot().press(KeyCode.CONTROL);
	sellGoods(indexOfCargoSlot, indexOfGoods);
	getRobot().release(KeyCode.CONTROL);
	WaitForAsyncUtils.waitForFxEvents();
	return ScreenMarketSell.of(getContext());
    }

    public void sellGoods(final int indexOfCargoSlot, final int indexOfGoods) {
	getRobot().drag(pageComponentPanelDock.getListOfCrates().get(indexOfCargoSlot), MouseButton.PRIMARY)
		.dropTo(getListOfGoodsToBuy().get(indexOfGoods)).release(MouseButton.PRIMARY);
    }

    public List<VBox> getListOfGoodsToBuy() {
	return getListOfNodes(".panelGoods");
    }

    public GamePage close() {
	final Button buttonNextTurn = getButtonById(EuropeButtonsPanel.CLOSE_BUTTON_ID);
	getRobot().clickOn(buttonNextTurn);
	return GamePage.of(getContext());
    }

}
