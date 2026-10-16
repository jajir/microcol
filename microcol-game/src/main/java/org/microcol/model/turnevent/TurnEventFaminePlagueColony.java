package org.microcol.model.turnevent;

import java.util.List;

import org.microcol.model.Goods;
import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.collect.ImmutableList;

import com.google.common.base.Preconditions;

public class TurnEventFaminePlagueColony extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    private final int victimId;

    private final UnitType victim;

    private final List<Goods> stocks;

    TurnEventFaminePlagueColony(final String playerName, final int colonyId,
            final String colonyName, final int victimId, final UnitType victim,
            final List<Goods> stocks) {
        super(playerName);
        Preconditions.checkArgument(colonyId >= 0, "Invalid colony id");
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
        Preconditions.checkArgument(victimId >= 0, "Invalid victim id");
        this.victimId = victimId;
        this.victim = Preconditions.checkNotNull(victim);
        this.stocks = ImmutableList.copyOf(stocks);
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public int getVictimId() {
        return victimId;
    }

    public UnitType getVictim() {
        return victim;
    }

    public List<Goods> getStocks() {
        return stocks;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setColonyId(colonyId);
        out.setColonyName(getColonyName());
        out.setFamineVictimId(victimId);
        out.setFamineVictim(victim);
        out.setFamineStocks(stocks);
        return out;
    }

    public static TurnEventFaminePlagueColony tryLoad(final TurnEventPo po) {
        if (TurnEventFaminePlagueColony.class.getSimpleName()
                .equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            return new TurnEventFaminePlagueColony(playerName, Preconditions
                    .checkNotNull(po.getColonyId(), "Missing colony id"),
                    colonyName,
                    Preconditions.checkNotNull(po.getFamineVictimId(),
                            "Missing famine victim id"),
                    Preconditions.checkNotNull(po.getFamineVictim(),
                            "Missing famine victim"),
                    Preconditions.checkNotNull(po.getFamineStocks(),
                            "Missing famine stocks"));
        }
        return null;
    }

}
