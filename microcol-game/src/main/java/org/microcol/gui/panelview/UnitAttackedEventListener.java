package org.microcol.gui.panelview;

import org.microcol.gui.event.model.UnitAttackedEventController;
import org.microcol.model.event.UnitAttackedEvent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Listen when some unit attack another one. When this occurs than it schedule
 * attack animation.
 */
public class UnitAttackedEventListener {
	private final GamePanelView gamePanelView;

	@Inject
	public UnitAttackedEventListener(final GamePanelView gamePanelView,
			final UnitAttackedEventController unitAttackedEventController) {
		this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
		unitAttackedEventController.addListener(this::onUnitAttacked);
	}

	private void onUnitAttacked(UnitAttackedEvent event) {
		gamePanelView.addFightAnimation(event.getAttacker(), event.getDefender());
	}

}
