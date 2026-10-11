package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;

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

    public void clickOnOk() {
        clickOnButtonWithId("buttonOk");
    }

    public void cancel() {
        clickOnButtonWithId("buttonCancel");
    }

    public void pressEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
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

    public void selectAmountWithKeyboard(final int amount) {
        getRobot().clickOn(
                getNodeFinder().lookup(".slider").queryAs(Slider.class));
        getRobot().push(KeyCode.HOME);
        for (int i = 0; i < amount; i++) {
            getRobot().push(KeyCode.RIGHT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyZeroCannotBeConfirmed() {
        assertEquals(0, getNodeFinder().lookup(".slider").queryAs(Slider.class)
                .getValue());
        assertTrue(getButtonById("buttonOk").isDisabled());
    }

}
