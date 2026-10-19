package org.microcol.page;

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
        final BuyUnitPanel buyUnitPanel = buyUnitPanels.stream()
                .filter(node -> {
                    final String label = getNodeFinder().from(node)
                            .lookup(".name").queryLabeled().getText();
                    logger.debug(
                            "Comparing given unit name '{}' with found '{}'",
                            unitNameToBuy, label);
                    return unitNameToBuy.equals(label);
                }).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("There is no unit to buy with label '%s'",
                                unitNameToBuy)));
        final Button button = getNodeFinder().from(buyUnitPanel)
                .lookup(".buttonBuy").queryButton();
        new ExpertPurchasePage(getContext()).revealSpecialist(
                button.getId().substring("buy-unit-".length()));
        getRobot().clickOn(button, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
        return EuropePortScreen.of(getContext());
    }

    public void verifyShipOffer(org.microcol.model.UnitType type, String name,
            int price, boolean affordable) {
        Button button = getButtonById("buy-unit-" + type.name());
        org.junit.jupiter.api.Assertions.assertEquals(!affordable,
                button.isDisabled());
        org.junit.jupiter.api.Assertions
                .assertTrue(button.getText().endsWith(" " + price));
        org.junit.jupiter.api.Assertions.assertTrue(
                getNodeFinder().lookup(name).queryLabeled().isVisible());
        javafx.scene.Parent panel = button.getParent();
        javafx.scene.image.ImageView image = panel.getChildrenUnmodifiable()
                .stream()
                .filter(node -> node instanceof javafx.scene.image.ImageView)
                .map(node -> (javafx.scene.image.ImageView) node).findFirst()
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(image.getImage());
        org.junit.jupiter.api.Assertions
                .assertFalse(image.getImage().isError());
    }

    public void verifyRoyalShipNotOffered() {
        org.junit.jupiter.api.Assertions.assertTrue(getNodeFinder()
                .lookup("#buy-unit-MAN_OF_WAR").queryAll().isEmpty());
    }

    public EuropePortScreen close() {
        final Button buttonNextTurn = getButtonById(
                EuropeButtonsPanel.CLOSE_BUTTON_ID);
        getRobot().clickOn(buttonNextTurn);
        WaitForAsyncUtils.waitForFxEvents();
        return EuropePortScreen.of(getContext());
    }

}
