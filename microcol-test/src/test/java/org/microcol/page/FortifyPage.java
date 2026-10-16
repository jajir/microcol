package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitActionType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/** Real order controls with explicit model and player-visible observations. */
public final class FortifyPage extends AbstractScreen {
    public FortifyPage(final TestContext context) {
        super(context);
    }

    public void changeOrder() {
        clickOnButtonWithId("fortifyUnit");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void changeOrderWithKeyboard() {
        final Button button = getButtonById("fortifyUnit");
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyAction(final String text, final boolean enabled) {
        final Button button = getButtonById("fortifyUnit");
        assertEquals(text, button.getText());
        assertEquals(enabled, !button.isDisabled());
        assertTrue(button.getWidth() + 0.5 >= button.prefWidth(-1));
    }

    public void verifyLocation(final int id,
            final org.microcol.model.Location location) {
        assertEquals(location, getModel().getUnitById(id).getLocation());
    }

    public void verifyOrder(final int id, final boolean ordered,
            final boolean effective, final int moves) {
        final Unit unit = getModel().getUnitById(id);
        assertEquals(ordered ? UnitActionType.fortify : UnitActionType.noAction,
                unit.getAction().getType());
        assertEquals(effective, getModel().isFortified(unit));
        assertEquals(moves, unit.getActionPoints());
        if (ordered) {
            assertNotEquals(unit,
                    getModel().getFirstSelectableUnit().orElse(null));
        }
    }
}
