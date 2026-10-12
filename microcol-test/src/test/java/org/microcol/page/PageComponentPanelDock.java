package org.microcol.page;

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
    }

    public boolean isUnitInPortSelected(final int unitIndex) {
        return getUnitsInPort().get(unitIndex).isSelected();
    }

    public List<ToggleButton> getUnitsInPort() {
        return getListOfNodes("." + PanelDockView.SHIP_IN_PORT_STYLE);
    }

    public int getNumberOfShipsInPort() {
        return getUnitsInPort().size();
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
