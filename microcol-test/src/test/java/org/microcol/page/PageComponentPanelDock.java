package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.microcol.gui.dock.PanelCrateView;
import org.microcol.gui.dock.PanelDockView;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.ToggleButton;
import javafx.scene.layout.StackPane;

public class PageComponentPanelDock extends AbstractPageComponent {

    public static PageComponentPanelDock of(final TestContext context) {
        return new PageComponentPanelDock(context);
    }

    private PageComponentPanelDock(TestContext context) {
        super(context);
    }

    public void selectUnitFromPort(final int unitIndex) {
        final ToggleButton ship = getUnitsInPort().get(unitIndex);
        if (!ship.isSelected()) {
            getRobot().clickOn(ship);
        }
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(ship.isSelected(), "The requested ship must be selected");
    }

    public List<ToggleButton> getUnitsInPort() {
        return getListOfNodes("." + PanelDockView.SHIP_IN_PORT_STYLE);
    }

    public void verifyNumberOfShipsInPort(
            final int expectedNumberOfShipsInPort) {
        assertEquals(expectedNumberOfShipsInPort, getUnitsInPort().size(),
                String.format(
                        "Expected numeber of ships is '%s' but really there is '%s' ships.",
                        expectedNumberOfShipsInPort, getUnitsInPort().size()));
    }

    /**
     * Return list of crates where user could put some goods or units.
     *
     * @return list of crates
     */
    public List<StackPane> getListOfCrates() {
        return getListOfNodes("." + PanelCrateView.CRATE_CLASS);
    }

}
