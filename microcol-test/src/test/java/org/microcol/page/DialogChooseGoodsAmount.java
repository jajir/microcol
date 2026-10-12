package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.stage.Window;

/**
 * Object allows to test one unit shown in right panel.
 */
public class DialogChooseGoodsAmount extends AbstractScreenMarket {

    public static DialogChooseGoodsAmount of(final TestContext context) {
        return new DialogChooseGoodsAmount(context);
    }

    private DialogChooseGoodsAmount(final TestContext context) {
        super(context);
    }

    public void clickOnOk() throws TimeoutException {
        final Window window = getButtonById("buttonOk").getScene().getWindow();
        clickOnButtonWithId("buttonOk");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !window.isShowing());
    }

    public void cancel() throws TimeoutException {
        final Window window = getButtonById("buttonCancel").getScene()
                .getWindow();
        clickOnButtonWithId("buttonCancel");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !window.isShowing());
    }

    public void pressEscape() throws TimeoutException {
        final Window window = getButtonById("buttonOk").getScene().getWindow();
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> !window.isShowing());
    }

    public void verifyOfferedAmount(final int expected) {
        final Slider slider = getNodeFinder().lookup(".slider")
                .queryAs(Slider.class);
        assertEquals(expected, slider.getMax());
        assertEquals(expected, slider.getValue());
    }

    public void verifyExplanation(final String expectedText) {
        assertEquals(expectedText,
                getLabeledById("goodsTransferExplanation").getText());
    }

    public void focusAmountSlider() {
        getRobot().clickOn(".slider .thumb");
    }

    public void verifyAmountSliderFocused() {
        assertTrue(
                getNodeFinder().lookup(".slider").queryAs(Slider.class)
                        .isFocused(),
                "The amount slider must receive keyboard input");
    }

    public void selectAmountWithKeyboard(final int amount) {
        getRobot().push(KeyCode.HOME);
        for (int step = 0; step < amount; step++) {
            getRobot().push(KeyCode.RIGHT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySelectedAmount(final int amount) {
        final Slider slider = getNodeFinder().lookup(".slider")
                .queryAs(Slider.class);
        assertEquals(Math.min(amount, slider.getMax()), slider.getValue());
    }

    public void verifyZeroCannotBeConfirmed() {
        assertEquals(0, getNodeFinder().lookup(".slider").queryAs(Slider.class)
                .getValue());
        assertTrue(getButtonById("buttonOk").isDisabled());
    }

}
