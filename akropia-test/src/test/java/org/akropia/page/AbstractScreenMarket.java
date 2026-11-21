package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.akropia.model.AkropiaException;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;

/**
 * Object allows to test one unit shown in right panel.
 */
public class AbstractScreenMarket extends AbstractScreen {

    public static AbstractScreenMarket of(final TestContext context) {
        return new AbstractScreenMarket(context);
    }

    protected AbstractScreenMarket(final TestContext context) {
        super(context);
    }

    public void selectValueAtSlider(final int value) {
        getRobot().clickOn(".slider .thumb").push(KeyCode.HOME);
        for (int step = 0; step < value; step++) {
            getRobot().push(KeyCode.RIGHT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySelectedMarketQuantity(final int expected) {
        assertEquals(expected, getSlider().getValue());
    }

    public void verifyQuotedPrice(final String expectedUnitPrice,
            final String expectedTotal) {
        assertEquals(expectedUnitPrice,
                getLabeledById("marketUnitPrice").getText());
        assertEquals(expectedTotal, getLabeledById("marketTotal").getText());
    }

    public void verifyModal(final int expectedGold) {
        final javafx.scene.Node root = getNodeFinder().lookup("#marketDialog")
                .query();
        final javafx.stage.Stage dialog = (javafx.stage.Stage) root.getScene()
                .getWindow();
        assertEquals(javafx.stage.Modality.WINDOW_MODAL, dialog.getModality());
        assertEquals(getContext().getPrimaryStage(), dialog.getOwner());
        assertEquals(expectedGold,
                getModel().getPlayers().stream()
                        .filter(org.akropia.model.Player::isHuman).findFirst()
                        .orElseThrow().getGold());
        org.junit.jupiter.api.Assertions.assertNotNull(getContext()
                .getPrimaryStage().getScene().lookup("#europeMarketGoods"));
    }

    public void verifyNumberOfSliders(final int expectedCount) {
        assertEquals(expectedCount,
                getNodeFinder().lookup(".slider").queryAll().size());
    }

    private Slider getSlider() {
        final String cssClass = ".slider";
        final Set<Slider> sliders = getNodeFinder().lookup(cssClass).queryAll();
        return sliders.stream().findAny().orElseThrow(
                () -> new AkropiaException("There is no such slider"));
    }
}
