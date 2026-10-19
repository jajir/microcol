package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.Location;
import org.microcol.model.Unit;

import javafx.scene.control.Label;

/**
 * Terrain movement results on the game screen: where a colonist stands, how
 * much movement it keeps, which squares it can still reach and the rejection
 * shown for an unaffordable step.
 */
public final class TerrainMovementPage extends AbstractScreen {

    public TerrainMovementPage(TestContext context) {
        super(context);
    }

    public void verifyUnitAt(final Unit unit, final Location expectedLocation,
            final int expectedMovementThirds) {
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(expectedLocation, unit.getLocation());
        assertEquals(expectedMovementThirds, unit.getMovementThirds());
    }

    public void verifyReach(final Unit unit, final Location reachable,
            final Location unreachable) {
        assertTrue(unit.getAvailableLocations().contains(reachable),
                "Expected reachable square " + reachable);
        assertFalse(unit.getAvailableLocations().contains(unreachable),
                "Expected unreachable square " + unreachable);
    }

    public void verifyNoReach(final Unit unit) {
        assertTrue(unit.getAvailableLocations().isEmpty(),
                "Unit without movement should reach no square");
    }

    public void verifyMovementUnavailable() {
        assertFalse(getContext().getClassFromGuice(
                org.microcol.gui.screen.game.gamepanel.ModeController.class)
                .isMoveMode());
        assertFalse(isCssIdVisible("buttonOk"));
    }

    public void verifyCannotMoveHere() {
        assertTrue(isCssIdVisible("buttonOk"));
        assertTrue(getNodeFinder().lookup("I't not possible to move unit here.")
                .queryAs(Label.class).isVisible());
    }

    public void closeRejection() {
        clickOnButtonWithId("buttonOk");
    }

}
