package org.microcol.model.turnevent;

import org.microcol.model.Goods;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

public class TurnEventGoodsWasThrownAway extends AbstractTurnEvent {

    private final int colonyId;

    private final String colonyName;

    private final Goods goods;

    TurnEventGoodsWasThrownAway(final String playerName, final int colonyId,
            final String colonyName, final Goods goods) {
        super(playerName);
        Preconditions.checkArgument(colonyId >= 0, "Invalid colony id");
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
        this.goods = Preconditions.checkNotNull(goods);
    }

    public int getColonyId() {
        return colonyId;
    }

    public String getColonyName() {
        return colonyName;
    }

    public Goods getGoods() {
        return goods;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo out = new TurnEventPo();
        out.setType(getClass().getSimpleName());
        out.setPlayerName(getPlayerName());
        out.setColonyId(colonyId);
        out.setColonyName(getColonyName());
        out.setGoods(getGoods());
        return out;
    }

    public static TurnEventGoodsWasThrownAway tryLoad(final TurnEventPo po) {
        if (TurnEventGoodsWasThrownAway.class.getSimpleName()
                .equals(po.getType())) {
            final String playerName = po.getPlayerName();
            final String colonyName = po.getColonyName();
            final Goods goods = po.getGoods();
            return new TurnEventGoodsWasThrownAway(playerName, Preconditions
                    .checkNotNull(po.getColonyId(), "Missing colony id"),
                    colonyName, goods);
        }
        return null;
    }

}
