package org.microcol.model.turnevent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    /**
     * Constructor that create event from persistent model.
     *
     * @param modelPo
     *            required persistent model
     * @param playerStore
     *            required players store
     */
    public TurnEventStore(final ModelPo modelPo, final PlayerStore playerStore) {
        modelPo.getTurnEvents().forEach(turnEventPo -> {
            final SimpleTurnEvent event = new SimpleTurnEvent(turnEventPo.getMessageKey(),
                    turnEventPo.getArgs().toArray(),
                    playerStore.getPlayerByName(turnEventPo.getPlayerName()));
            event.setSolved(turnEventPo.isSolved());
            turnEvents.add(event);
        });
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
        return turnEvents.stream().filter(turnEvent -> turnEvent.getPlayer().equals(player))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Allows to add new turn event object.
     *
     * @param turnEvent
     *            required turn event object.
     */
    public void add(final TurnEvent turnEvent) {
        Preconditions.checkNotNull(turnEvent);
        turnEvents.add(turnEvent);
    }

    /**
     * Get list of localized turn event for specific player.
     *
     * @param player
     *            required player
     * @param messageProvider
     *            required localized message provider
     * @return list of localized turn event messages for given player
     */
    public List<TurnEvent> getLocalizedMessages(final Player player,
            final Function<String, String> messageProvider) {
        Preconditions.checkNotNull(messageProvider);
        final List<TurnEvent> out = getForPlayer(player);
        out.forEach(turnEvent -> setLocalizedMessage(turnEvent, messageProvider));
        return out;
    }

    /**
     * Utility method that combine message key, message arguments and message
     * provider.
     *
     * @param turnEvent
     *            required turn event
     * @param messageProvider
     *            required localized message provider
     */
    public void setLocalizedMessage(final TurnEvent turnEvent,
            final Function<String, String> messageProvider) {
        final String template = messageProvider.apply(turnEvent.getMessageKey());
        turnEvent.setLocalizedMessage(String.format(template, turnEvent.getArgs()));
    }

    /**
     * Clear all event. Should be called when player press 'Next turn'.
     */
    public void clearAllTurnEvents() {
        turnEvents.clear();
    }

    /**
     * Save turn events to persistent model.
     *
     * @return list of objects to persistent model
     */
    public List<TurnEventPo> save() {
        return turnEvents.stream().map(turnEvent -> {
            final TurnEventPo po = new TurnEventPo();
            po.setArgs(Lists.newArrayList(turnEvent.getArgs()));
            po.setMessageKey(turnEvent.getMessageKey());
            po.setPlayerName(turnEvent.getPlayer().getName());
            po.setSolved(turnEvent.isSolved());
            return po;
        }).collect(ImmutableList.toImmutableList());
    }

}
