package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.model.Location;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.screen.game.gamepanel.SelectedTileManager;
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

    public void selectUnit(final int id) {
        clickOnButtonWithId("end-turn-unit-" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyUnitWarning(final int id, final String label) {
        assertEquals(label, getButtonById("end-turn-unit-" + id).getText());
        assertTrue(getButtonById("end-turn-back").isDefaultButton());
        assertFalse(getButtonById("end-turn-proceed").isDefaultButton());
    }

    public void verifySelectedUnit(final int id, final Location location) {
        assertEquals(id,
                getContext().getClassFromGuice(SelectedUnitManager.class)
                        .getSelectedUnit().orElseThrow().getId());
        assertEquals(location,
                getContext().getClassFromGuice(SelectedTileManager.class)
                        .getSelectedTile().orElseThrow());
    }

    public void verifyFocusedWithoutGameplayChange(final String expected,
            final Location location) {
        final Gson gson = new Gson();
        final JsonObject focused = gson.fromJson(expected, JsonObject.class);
        focused.add("focusedField", gson.toJsonTree(location));
        assertEquals(focused, gson.fromJson(snapshot(), JsonObject.class));
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

    public void verifyTurnAdvancedWithoutWarning(final int previous)
            throws Exception {
        WaitForAsyncUtils.waitFor(60, java.util.concurrent.TimeUnit.SECONDS,
                () -> turn() > previous && getContext()
                        .getClassFromGuice(GameModelController.class)
                        .isHumanTurnReady());
        // Model readiness precedes the turn-start notification and its FX
        // toolbar update; wait for the actual player control as well.
        awaitTurnPresentation();
        WaitForAsyncUtils.waitFor(10, java.util.concurrent.TimeUnit.SECONDS,
                () -> WaitForAsyncUtils
                        .asyncFx(() -> !getButtonById("nextTurn").isDisabled())
                        .get());
        assertEquals(previous + 1, turn());
        assertTrue(getNodeFinder().lookup("#end-turn-warnings").tryQuery()
                .isEmpty());
    }
}
