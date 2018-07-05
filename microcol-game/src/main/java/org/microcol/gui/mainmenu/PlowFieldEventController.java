package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedUnitManager;
import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Plow field event controller.
 */
public class PlowFieldEventController extends AbstractEventController<PlowFieldEvent> {

    private final GameModelController gameModelController;
    private final SelectedUnitManager selectedUnitManager;

    @Inject
    public PlowFieldEventController(final GameModelController game, final SelectedUnitManager selectedUnitManager) {
	this.gameModelController = Preconditions.checkNotNull(game);
	this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
    }

    public void fireEvent() {
	final Player player = gameModelController.getCurrentPlayer();
	final Unit unit = selectedUnitManager.getSelectedUnit().orElseThrow(
		() -> new IllegalStateException("Plow field event can't be invoked when no unit is selected."));
	Preconditions.checkArgument(unit.canPlowFiled(), "Unit can't plow field.");
	super.fireEvent(new PlowFieldEvent(player, unit));
    }

}
