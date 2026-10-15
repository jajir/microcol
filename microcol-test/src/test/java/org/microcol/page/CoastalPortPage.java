package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.microcol.gui.screen.game.gamepanel.MoveModeSupport;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

/** Observes coastal sailing separately from passenger landing. */
public final class CoastalPortPage extends AbstractScreen {
    private final Gson gson = new Gson();
    private JsonElement before;
    private boolean canceledBeforeRelease;
    private String cancellationContext;

    public CoastalPortPage(final TestContext context) {
        super(context);
    }

    public void cancelPreview() {
        getRobot().press(javafx.scene.input.KeyCode.ESCAPE)
                .release(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(() -> {
            canceledBeforeRelease = !getContext().getClassFromGuice(
                    org.microcol.gui.screen.game.gamepanel.ModeController.class)
                    .isMoveMode();
            cancellationContext = "Focus: "
                    + getPrimaryStage().getScene().getFocusOwner()
                    + "; stage focused: " + getPrimaryStage().isFocused();
        });
        getRobot().release(javafx.scene.input.MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPreviewCanceled() {
        assertTrue(canceledBeforeRelease, cancellationContext);
        assertFalse(getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.ModeController.class)
                .isMoveMode());
        assertFalse(isCssIdVisible("passenger-confirm"));
    }

    public void verifyColoniesMenuShowing(final boolean expected) {
        assertEquals(expected, getNodeFinder().lookup("#coloniesMenu")
                .queryAs(javafx.scene.control.MenuButton.class).isShowing());
    }

    public void verifyColoniesMenuFocused() {
        assertSame(getNodeFinder().lookup("#coloniesMenu").query(),
                getPrimaryStage().getScene().getFocusOwner());
    }

    public void rememberWorld() {
        getRobot().interact(() -> before = gson.toJsonTree(getModel().save()));
    }

    public void verifyWorldUnchanged() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(before, gson.toJsonTree(getModel().save()));
    }

    public void verifyPort(final Location at, final boolean expected) {
        assertTrue(getModel().getColonyAt(at).isPresent());
        assertSame(getModel().getCurrentPlayer(),
                getModel().getColonyAt(at).orElseThrow().getOwner());
        assertEquals(expected, getModel()
                .isOwnedCoastalPort(getModel().getCurrentPlayer(), at));
    }

    public void verifyPreview(final Location destination, final String mode) {
        final MoveModeSupport preview = getContext()
                .getClassFromGuice(MoveModeSupport.class);
        assertEquals(List.of(destination), preview.getMoveLocations());
        assertEquals(mode, String.valueOf(preview.getMoveMode()));
    }

    public void verifyNoSailingRoute(final Location destination) {
        final Unit ship = getModel().getUnitById(3);
        assertFalse(ship.isPossibleToMoveAt(destination));
        assertTrue(ship.getPath(destination).isEmpty());
        assertFalse(ship.getAvailableLocations().contains(destination));
    }

    public void verifyEmptyShip() {
        final UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(3);
        assertTrue(ship.getCargo().getSlots().stream()
                .allMatch(slot -> slot.isEmpty()));
    }
}
