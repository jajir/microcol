package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.microcol.model.MicroColException;
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

    public void verifyNumberOfSliders(final int expectedCount) {
        assertEquals(expectedCount,
                getNodeFinder().lookup(".slider").queryAll().size());
    }

    private Slider getSlider() {
        final String cssClass = ".slider";
        final Set<Slider> sliders = getNodeFinder().lookup(cssClass).queryAll();
        return sliders.stream().findAny().orElseThrow(
                () -> new MicroColException("There is no such slider"));
    }
}
