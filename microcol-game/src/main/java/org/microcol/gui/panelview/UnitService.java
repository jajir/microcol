package org.microcol.gui.panelview;

import org.microcol.gui.event.model.GameModelController;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Provide basic unit operations and checks.
 *
 */
public class UnitService {

	private final GameModelController gameController;

	@Inject
	public UnitService(final GameModelController gameController) {
		this.gameController = Preconditions.checkNotNull(gameController);
	}

}
