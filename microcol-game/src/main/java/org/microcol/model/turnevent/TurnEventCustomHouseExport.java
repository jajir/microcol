package org.microcol.model.turnevent;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/** A persisted report of goods sold by a colony custom house. */
public final class TurnEventCustomHouseExport extends AbstractTurnEvent {
    private final int colonyId;
    private final String colonyName;
    private final Goods goods;
    private final int income;

    TurnEventCustomHouseExport(final String playerName, final int colonyId,
            final String colonyName, final Goods goods, final int income) {
        super(playerName);
        Preconditions.checkArgument(
                colonyId >= 0 && income >= 0 && goods != null
                        && goods.getAmount() > 0
                        && GoodsType.BUYABLE_GOOD_TYPES
                                .contains(goods.getType()),
                "Invalid custom-house export event");
        this.colonyId = colonyId;
        this.colonyName = Preconditions.checkNotNull(colonyName);
        this.goods = goods;
        this.income = income;
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

    public int getIncome() {
        return income;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setColonyId(colonyId);
        po.setColonyName(colonyName);
        po.setGoods(goods);
        po.setIncome(income);
        return po;
    }

    public static TurnEventCustomHouseExport tryLoad(final TurnEventPo po) {
        if (!TurnEventCustomHouseExport.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getColonyId() != null && po.getIncome() != null
                        && po.getGoods() != null,
                "Incomplete custom-house export event");
        return new TurnEventCustomHouseExport(po.getPlayerName(),
                po.getColonyId(), po.getColonyName(), po.getGoods(),
                po.getIncome());
    }
}
