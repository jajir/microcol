package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.microcol.model.MicroColException;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.control.Slider;

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
        final Slider slider = getSlider();
        /*
         * Following code is not correct test. It get JavaFX component from
         * screen and in JavaFX Application Thread set desired value. It should
         * be set by mouse.
         */
        Platform.runLater(() -> slider.setValue(value));
        WaitForAsyncUtils.waitForFxEvents();
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
