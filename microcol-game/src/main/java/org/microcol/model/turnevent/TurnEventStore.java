package org.microcol.model.turnevent;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.ChainOfCommandStrategy;
import org.microcol.model.Player;
import org.microcol.model.PlayerStore;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Object storing and managing turn events. It store events to persistent
 * object. Class store turn event messages for multiple players.
 */
public final class TurnEventStore {

    private final ArrayList<TurnEvent> turnEvents = new ArrayList<>();

    private final ChainOfCommandStrategy<TurnEventPo, TurnEvent> turnEventResolver = new ChainOfCommandStrategy<>(
            Lists.newArrayList(TurnEventColonyWasDestroyed::tryLoad,
                    TurnEventColonyWasLost::tryLoad, TurnEventFaminePlagueColony::tryLoad,
                    TurnEventFamineWillPlagueColony::tryLoad, TurnEventGoodsWasThrownAway::tryLoad,
                    TurnEventNewUnitInColony::tryLoad, TurnEventNewUnitInEurope::tryLoad,
                    TurnEventShipComeToEuropePort::tryLoad, TurnEventShipComeToHighSeas::tryLoad));

    /**
     * Constructor that create event from persistent model.
     *
     * @param modelPo
     *            required persistent model
     * @param playerStore
     *            required players store
     */
    public TurnEventStore(final ModelPo modelPo, final PlayerStore playerStore) {
        modelPo.getTurnEvents()
                .forEach(turnEventPo -> turnEvents.add(turnEventResolver.apply(turnEventPo)));
    }

    /**
     * Save turn events to persistent model.
     *
     * @return list of objects to persistent model
     */
    public List<TurnEventPo> save() {
        return turnEvents.stream()
                .map(turnEvent -> Preconditions.checkNotNull(turnEvent.save(),
                        "Turn even {} was saved to null object", turnEvent))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Get list of turn events but event are not localized. In other words event
     * can't shown to human player.
     *
     * @param player
     *            required player
     * @return list of turn event for given player
     */
    public List<TurnEvent> getForPlayer(final Player player) {
        return turnEvents.stream()
                .filter(turnEvent -> turnEvent.getPlayerName().equals(player.getName()))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Allows to add new turn event object.
     *
     * @param turnEvent
     *            required turn event object.
     */
    public void add(final TurnEvent turnEvent) {
        Preconditions.checkNotNull(turnEvent, "Turn event is null");
        turnEvents.add(turnEvent);
    }

    /**
     * Get list of turn events for specific player.
     *
     * @param player
     *            required player
     * @return list of event messages for given player
     */
    public List<TurnEvent> getLocalizedMessages(final Player player) {
        return getForPlayer(player);
    }

    /**
     * Clear event for given player. Should be called when player press 'Next
     * turn'.
     *
     * @param player
     *            required payer whose turn event will be deleted
     */
    public void clearTurnEventsForPlayer(final Player player) {
        turnEvents.removeIf(event -> event.getPlayerName().equals(player.getName()));
    }

}
