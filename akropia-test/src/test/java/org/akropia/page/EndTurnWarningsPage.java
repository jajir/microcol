package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import javafx.scene.input.KeyCode;
import org.testfx.util.WaitForAsyncUtils;

/** Player actions and explicit assertions for the common end-turn warning. */
public final class EndTurnWarningsPage extends AbstractScreen {
    public EndTurnWarningsPage(final TestContext context) {
        super(context);
    }

    public String snapshot() {
        return new Gson().toJson(getModel().save());
    }

    public int turn() {
        return getModel().getCalendar().getNumberOfPlayedTurns();
    }

    public void requestTurn() {
        getRobot().clickOn("#nextTurn");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void back() {
        clickOnButtonWithId("end-turn-back");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openColony(final int id) {
        clickOnButtonWithId("end-turn-colony-" + id);
    }

    public void verifyWarning(final int colonyId, final String label) {
        assertEquals(label,
                getButtonById("end-turn-colony-" + colonyId).getText());
        assertTrue(getButtonById("end-turn-back").isDefaultButton());
        assertFalse(getButtonById("end-turn-proceed").isDefaultButton());
    }

    public void verifyUnchanged(final String expected) {
        assertEquals(expected, snapshot());
    }

    public void verifyClosed() {
        assertTrue(getNodeFinder().lookup("#end-turn-warnings").tryQuery()
                .isEmpty());
        assertFalse(getButtonById("nextTurn").isDisabled());
    }

    public void verifyTurn(final int expected) {
        assertEquals(expected, turn());
    }
}
