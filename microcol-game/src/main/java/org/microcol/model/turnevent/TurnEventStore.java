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

public class TurnEventStore {

    private final ArrayList<TurnEvent> turnEvents = new ArrayList<>();

    public TurnEventStore(final ModelPo modelPo, final PlayerStore playerStore) {
        modelPo.getTurnEvents().forEach(turnEventPo -> {
            final SimpleTurnEvent event = new SimpleTurnEvent(turnEventPo.getMessageKey(),
                    turnEventPo.getArgs().toArray(),
                    playerStore.getPlayerByName(turnEventPo.getPlayerName()));
            event.setSolved(turnEventPo.isSolved());
            turnEvents.add(event);
        });
    }

    public List<TurnEvent> getForPlayer(final Player player) {
        return turnEvents.stream().filter(turnEvent -> turnEvent.getPlayer().equals(player))
                .collect(ImmutableList.toImmutableList());
    }

    public void add(final TurnEvent turnEvent) {
        Preconditions.checkNotNull(turnEvent);
        turnEvents.add(turnEvent);
    }

    public List<TurnEvent> getLocalizedMessages(final Player player,
            final Function<String, String> messageProvider) {
        Preconditions.checkNotNull(messageProvider);
        final List<TurnEvent> out = getForPlayer(player);
        out.forEach(turnEvent -> setLocalizedMessage(turnEvent, messageProvider));
        return out;
    }

    public void setLocalizedMessage(final TurnEvent turnEvent,
            final Function<String, String> messageProvider) {
        final String template = messageProvider.apply(turnEvent.getMessageKey());
        turnEvent.setLocalizedMessage(String.format(template, turnEvent.getArgs()));
    }

    /**
     * Should be called when player press 'Next turn'.
     */
    public void clearAllTurnEvents() {
        turnEvents.clear();
    }

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
