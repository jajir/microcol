package org.akropia.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

/**
 * Object allows to test one unit shown in right panel.
 */
public class ScreenMarketBuy extends AbstractScreenMarket {

    private static final String[] TEXT_IDS = { "marketHowMuch", "marketLimit",
            "marketUnitPriceCaption", "marketUnitPrice", "marketAmountCaption",
            "marketGross", "marketTaxCaption", "marketTax",
            "marketTotalCaption", "marketTotal", "marketRemainingCaption",
            "marketRemaining", "marketQuickBuyHint" };

    public static ScreenMarketBuy of(final TestContext context) {
        return new ScreenMarketBuy(context);
    }

    private ScreenMarketBuy(final TestContext context) {
        super(context);
    }

    public void clickOnBuy() {
        clickOnButtonWithId("buttonYes");
    }

    public void clickOnCancel() {
        clickOnButtonWithId("buttonNo");
    }

    /** Escape returns to the harbor without a purchase. */
    public void cancelWithEscape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Reach the quantity slider with Tab only and lower it with the left arrow.
     */
    public void reduceQuantityWithKeyboard(final int steps) {
        focusWithTab("marketSlider");
        for (int step = 0; step < steps; step++) {
            getRobot().push(KeyCode.LEFT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Reach Buy with Tab only and press it with the space bar. */
    public void buyWithKeyboard() {
        focusWithTab("buttonYes");
        getRobot().push(KeyCode.SPACE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    private void focusWithTab(final String id) {
        final Node target = getNodeFinder().lookup("#" + id).query();
        for (int step = 0; step < 20; step++) {
            if (target.isFocused()) {
                return;
            }
            getRobot().push(KeyCode.TAB);
            WaitForAsyncUtils.waitForFxEvents();
        }
        throw new IllegalStateException(
                "Tab does not reach the purchase control " + id);
    }

    public void resize(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * @param expectedText why no more than the preselected maximum is offered
     */
    public void verifyLimitExplanation(final String expectedText) {
        assertEquals(expectedText, getLabeledById("marketLimit").getText());
    }

    /**
     * Every purchase detail of the selected amount, as shown.
     */
    public void verifyPurchaseDetails(final String amountCaption,
            final String unitPrice, final String gross, final String taxCaption,
            final String tax, final String total, final String goldLeft) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(amountCaption,
                getLabeledById("marketAmountCaption").getText());
        assertEquals(unitPrice, getLabeledById("marketUnitPrice").getText());
        assertEquals(gross, getLabeledById("marketGross").getText());
        assertEquals(taxCaption, getLabeledById("marketTaxCaption").getText());
        assertEquals(tax, getLabeledById("marketTax").getText());
        assertEquals(total, getLabeledById("marketTotal").getText());
        assertEquals(goldLeft, getLabeledById("marketRemaining").getText());
    }

    /**
     * The end of the slider names the numeric maximum, drawn inside the window.
     */
    public void verifyMaximumAtSliderEnd(final String expectedText) {
        WaitForAsyncUtils.waitForFxEvents();
        final Node slider = getNodeFinder().lookup("#marketSlider").query();
        final Predicate<Node> endLabel = node -> node instanceof Text
                && expectedText.equals(((Text) node).getText());
        final List<Text> labels = new ArrayList<>(
                getNodeFinder().from(slider).lookup(endLabel).queryAll());
        assertEquals(1, labels.size(), "Slider end label " + expectedText);
        final Text label = labels.get(0);
        assertTrue(label.isVisible(), "Slider end label is visible");
        final Bounds bounds = label.localToScene(label.getBoundsInLocal());
        assertTrue(
                bounds.getMinX() >= 0
                        && bounds.getMaxX() <= label.getScene().getWidth(),
                "Slider end label inside the window: " + bounds);
        // The label also stays inside the purchase panel that holds the slider.
        final Node panel = slider.getParent();
        final Bounds panelBounds = panel.localToScene(panel.getLayoutBounds());
        assertTrue(
                bounds.getMinX() >= panelBounds.getMinX() - 0.5
                        && bounds.getMaxX() <= panelBounds.getMaxX() + 0.5,
                "Slider end label inside the purchase panel: " + bounds + " in "
                        + panelBounds);
    }

    /**
     * @param expectedMac   hint shown on macOS
     * @param expectedOther hint shown on Linux and Windows
     * @param spokenMac     hint for assistive technologies on macOS
     */
    public void verifyQuickBuyHint(final String expectedMac,
            final String expectedOther, final String spokenMac) {
        final Labeled hint = getLabeledById("marketQuickBuyHint");
        assertEquals(isMacPlatform() ? expectedMac : expectedOther,
                hint.getText());
        assertEquals(isMacPlatform() ? spokenMac : expectedOther,
                hint.getAccessibleText());
    }

    public void verifyBuyEnabled(final boolean expected) {
        assertEquals(expected, !getButtonById("buttonYes").isDisabled(),
                "Buy is enabled");
    }

    /**
     * Each caption, value, hint and action is drawn in full inside the window,
     * not shortened with an ellipsis.
     */
    public void verifyTextShownInFull() {
        WaitForAsyncUtils.waitForFxEvents();
        final List<Labeled> shown = new ArrayList<>();
        for (final String id : TEXT_IDS) {
            shown.add(getLabeledById(id));
        }
        shown.add(getButtonById("buttonYes"));
        shown.add(getButtonById("buttonNo"));
        for (final Labeled labeled : shown) {
            final Text drawn = labeled.getChildrenUnmodifiable().stream()
                    .filter(child -> child instanceof Text
                            && child.getStyleClass().contains("text"))
                    .map(child -> (Text) child).findFirst().orElseThrow();
            assertEquals(labeled.getText(), drawn.getText(),
                    "Truncated text of " + labeled.getId());
            final Bounds bounds = labeled
                    .localToScene(labeled.getLayoutBounds());
            final double sceneWidth = labeled.getScene().getWidth();
            final double sceneHeight = labeled.getScene().getHeight();
            assertTrue(bounds.getMinX() >= 0 && bounds.getMaxX() <= sceneWidth
                    && bounds.getMinY() >= 0 && bounds.getMaxY() <= sceneHeight,
                    "Text outside the window: " + labeled.getId());
            final Bounds text = labeled.localToScene(drawn.getBoundsInParent());
            assertTrue(
                    text.getMinX() >= bounds.getMinX() - 0.5
                            && text.getMaxX() <= bounds.getMaxX() + 0.5,
                    "Text drawn outside its control: " + labeled.getId()
                            + " text=" + text + " control=" + bounds);
        }
        assertTrue(shown.stream().allMatch(Node::isVisible),
                "Every purchase text is visible");
    }

}
