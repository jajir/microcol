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

    private final GamePanelPainter gamePanelView;

    private final com.google.common.eventbus.EventBus eventBus;
    private final org.microcol.i18n.I18n i18n;

    @Inject
    public UnitAttackedEventListener(final GamePanelPainter gamePanelView,
            final com.google.common.eventbus.EventBus eventBus,
            final org.microcol.i18n.I18n i18n) {
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
    }

    @Subscribe
    private void onUnitAttacked(final UnitAttackedEvent event) {
        gamePanelView.addFightAnimation(event.getAttacker(),
                event.getDefender());
        if (!event.wasDestroyed() && event
                .getDefeated() instanceof org.microcol.model.unit.UnitFreeColonist) {
            final org.microcol.model.unit.UnitFreeColonist defeated = (org.microcol.model.unit.UnitFreeColonist) event
                    .getDefeated();
            final String message = i18n.get(
                    defeated.isHoldingGuns()
                            ? org.microcol.gui.dialog.Dialog.fight_lostHorses
                            : org.microcol.gui.dialog.Dialog.fight_lostMuskets,
                    defeated.getOwner().getDisplayName());
            javafx.application.Platform.runLater(() -> eventBus.post(
                    new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                            message,
                            org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME)));
        }
    }

}
