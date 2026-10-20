package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.control.Label;

/**
 * Real quick load and unload controls of the colony port, with their
 * confirmation and refusal feedback.
 */
public final class QuickCargoPage extends AbstractScreen {

    private static final String PLAYER_NAME = "Dutch";

    public static QuickCargoPage of(final TestContext context) {
        return new QuickCargoPage(context);
    }

    private QuickCargoPage(final TestContext context) {
        super(context);
    }

    /** Use the visible quick load control for the selected carrier. */
    public void quickLoad() {
        clickOnButtonWithId("quickLoad");
    }

    /** Use the visible quick unload control for the selected carrier. */
    public void quickUnload() {
        clickOnButtonWithId("quickUnload");
    }

    public void confirm() {
        clickOnButtonWithId("quickCargoConfirm");
    }

    public void cancel() {
        clickOnButtonWithId("quickCargoCancel");
    }

    public void closeRefusal() {
        clickOnButtonWithId("buttonOk");
    }

    public void verifyPreview(final String expectedQuestion) {
        assertEquals(expectedQuestion, getNodeFinder()
                .lookup("#quickCargoQuestion").queryAs(Label.class).getText());
    }

    public void verifyNoPreview() {
        assertTrue(
                getNodeFinder().lookup("#quickCargoQuestion").tryQuery()
                        .isEmpty(),
                "The quick cargo confirmation must be closed");
    }

    public void verifyRefusal(final String expectedMessage) {
        assertEquals(expectedMessage,
                getLabeledById("warehouseTransferMessage").getText());
    }

    public void verifyTreasury(final int expectedGold) {
        assertEquals(expectedGold,
                getModel().getPlayerByName(PLAYER_NAME).getGold(),
                "Quick cargo must not change the treasury");
    }
}
