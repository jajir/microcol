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
    private final org.microcol.gui.dialog.DialogNavalOutcome navalDialog;

    @Inject
    public UnitAttackedEventListener(final GamePanelPainter gamePanelView,
            final com.google.common.eventbus.EventBus eventBus,
            final org.microcol.i18n.I18n i18n,
            final org.microcol.gui.dialog.DialogNavalOutcome navalDialog) {
        this.navalDialog = navalDialog;
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
    }

    @Subscribe
    private void onUnitAttacked(final UnitAttackedEvent event) {
        final String lossMessage = outcomeMessage(event);
        final String promotionMessage = event.getPromotion().map(promotion -> {
            final org.microcol.model.Unit winner = promotion
                    .getUnitId() == event.getAttacker().getId()
                            ? event.getAttacker()
                            : event.getDefender();
            final String status = promotion.isVeteran()
                    ? i18n.get(
                            org.microcol.gui.dialog.Dialog.fight_veteranStatus)
                    : i18n.get(org.microcol.gui.UnitTypes
                            .getUnitName(promotion.getToType()));
            return i18n.get(org.microcol.gui.dialog.Dialog.fight_promoted,
                    winner.getOwner().getDisplayName(), promotion.getUnitId(),
                    status);
        }).orElse(null);
        final String message = lossMessage == null ? promotionMessage
                : promotionMessage == null ? lossMessage
                        : lossMessage + "\n" + promotionMessage;
        gamePanelView.addFightAnimation(
                event.getOriginalAttackerLocation()
                        .orElseGet(() -> event.getAttacker().getLocation()),
                event.getOriginalDefenderLocation().orElseGet(
                        () -> event.getDefender().getLocation()),
                () -> {
                    if (event.getNavalOutcome().isPresent() && (event
                            .getAttacker().getOwner().isHuman()
                            || event.getDefender().getOwner().isHuman())) {
                        javafx.application.Platform.runLater(() -> {
                            if (event.getModel().isGameRunning()) {
                                navalDialog.showOutcome(event);
                            }
                        });
                    }
                    if (message != null) {
                        javafx.application.Platform.runLater(() -> eventBus
                                .post(new org.microcol.gui.screen.game.components.StatusBarMessageEvent(
                                        message,
                                        org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source.GAME)));
                    }
                });
    }

    private String outcomeMessage(final UnitAttackedEvent event) {
        final java.util.Optional<org.microcol.model.ColonyBattleOutcome> colony = event
                .getColonyOutcome();
        if (colony.isEmpty()) {
            return equipmentMessage(event);
        }
        final org.microcol.model.ColonyBattleOutcome outcome = colony.get();
        if (outcome
                .getResult() == org.microcol.model.ColonyBattleOutcome.Result.COLONY_CAPTURED) {
            // Not an equipment loss: the colony and all its workers changed
            // hands.
            return i18n.get(org.microcol.gui.dialog.Dialog.fight_colonyCaptured,
                    event.getAttacker().getOwner().getDisplayName(),
                    outcome.getColony().getName());
        }
        if (outcome
                .getResult() == org.microcol.model.ColonyBattleOutcome.Result.WORKER_CAPTURED) {
            // One worker changed hands; the colony and its other jobs stay.
            return i18n.get(org.microcol.gui.dialog.Dialog.fight_workerCaptured,
                    event.getAttacker().getOwner().getDisplayName(),
                    outcome.getColony().getName());
        }
        final String equipment = equipmentMessage(event);
        if (!outcome.isMilitiaExited()) {
            return equipment;
        }
        final String exit = i18n.get(
                org.microcol.gui.dialog.Dialog.fight_revereExit,
                event.getDefender().getOwner().getDisplayName());
        return equipment == null ? exit : equipment + "\n" + exit;
    }

    private String equipmentMessage(final UnitAttackedEvent event) {
        if (event
                .getDefeated() instanceof org.microcol.model.unit.UnitArtillery) {
            return i18n.get(event.wasDestroyed()
                    ? org.microcol.gui.dialog.Dialog.fight_artilleryDestroyed
                    : org.microcol.gui.dialog.Dialog.fight_artilleryDamaged,
                    event.getDefeated().getOwner().getDisplayName());
        }
        if (event.wasDestroyed() && event
                .getDefeated() instanceof org.microcol.model.unit.UnitFreeColonist) {
            // A defeated Continental or royal soldier leaves nothing behind.
            return i18n.get(org.microcol.gui.dialog.Dialog.fight_roleDestroyed,
                    event.getDefeated().getOwner().getDisplayName());
        }
        if (!event.wasDestroyed() && event
                .getDefeated() instanceof org.microcol.model.unit.UnitFreeColonist) {
            final org.microcol.model.unit.UnitFreeColonist defeated = (org.microcol.model.unit.UnitFreeColonist) event
                    .getDefeated();
            return i18n.get(
                    defeated.isHoldingGuns()
                            ? org.microcol.gui.dialog.Dialog.fight_lostHorses
                            : org.microcol.gui.dialog.Dialog.fight_lostMuskets,
                    defeated.getOwner().getDisplayName());
        }
        return null;
    }
}
