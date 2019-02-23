package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.util.Listener;
import org.microcol.model.event.UnitAttackedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Listen when some unit attack another one. When this occurs than it schedule
 * attack animation.
 */
@Listener
public final class UnitAttackedEventListener {
    
    private final GamePanelView gamePanelView;

    @Inject
    public UnitAttackedEventListener(final GamePanelView gamePanelView) {
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
    }

    @Subscribe
    private void onUnitAttacked(final UnitAttackedEvent event) {
        gamePanelView.addFightAnimation(event.getAttacker(), event.getDefender());
    }

}
