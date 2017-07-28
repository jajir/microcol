package org.microcol.gui.panelview;

import org.microcol.gui.event.model.GameController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Provide basic unit operations and checks.
 *
 */
public class UnitService {

	private final GameController gameController;

	@Inject
	public UnitService(final GameController gameController) {
		this.gameController = Preconditions.checkNotNull(gameController);
	}

}
