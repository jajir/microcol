package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.panelview.SelectedUnitManager;
import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Control build colony event.
 * <p>
 * It could be difficult to instantiate {@link BuildColonyEvent}. Because of
 * that there special method {@link #fireEvent()} which can do it.
 * </p>
 */
public class BuildColonyEventController extends AbstractEventController<BuildColonyEvent> {

	private final GameModelController gameModelController;
	private final SelectedUnitManager selectedUnitManager;

	@Inject
	public BuildColonyEventController(final GameModelController game, final SelectedUnitManager selectedUnitManager) {
		this.gameModelController = Preconditions.checkNotNull(game);
		this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
	}

	public void fireEvent() {
		final Player player = gameModelController.getCurrentPlayer();
		final Unit unit = selectedUnitManager.getSelectedUnit().orElseThrow(
				() -> new IllegalStateException("Build colony event can't be invoked when no unit is selected."));
		super.fireEvent(new BuildColonyEvent(player, unit));
	}

}
