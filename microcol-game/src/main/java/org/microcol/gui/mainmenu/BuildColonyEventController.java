package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.panelview.ViewState;
import org.microcol.gui.util.AbstractEventController;
import org.microcol.model.Location;
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
	private final ViewState viewState;

	@Inject
	public BuildColonyEventController(final GameModelController game, final ViewState viewState) {
		this.gameModelController = Preconditions.checkNotNull(game);
		this.viewState = Preconditions.checkNotNull(viewState);
	}

	public void fireEvent() {
		final Player player = gameModelController.getCurrentPlayer();
		final Location location = viewState.getSelectedTile().orElseThrow(
				() -> new IllegalStateException("Build colony event can't be invoked when no tile is selected."));
		// TODO following hack provide selected unit.
		final Unit unit = gameModelController.getModel().getUnitsAt(location).get(0);
		super.fireEvent(new BuildColonyEvent(player, unit));
	}

}
