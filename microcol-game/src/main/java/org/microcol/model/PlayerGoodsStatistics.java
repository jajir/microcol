package org.microcol.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.microcol.model.unit.UnitWithCargo;

public final class PlayerGoodsStatistics {

    private final Map<GoodsType, Integer> goods = new HashMap<>();

    void addColonyData(final Colony colony) {
        colony.getWarehouse().saveStatisticsTo(this);
    }

    /**
     * Economy value is counted as simple sum of all goods.
     *
     * TODO create something meaningful
     *
     * @return economy value of players
     */
    public int getEconomyValue() {
        return goods.entrySet().stream().mapToInt(entry -> entry.getValue()).sum();
    }

    public int getGoods(final GoodsType goodsType) {
        final Integer val = goods.get(goodsType);
        if (val == null) {
            return 0;
        } else {
            return val;
        }
    }

    void addUnitData(final Unit unit) {
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            unitWithCargo.getCargo().getSlots().forEach(slot -> {
                if (slot.isLoadedGood()) {
                    final Goods amount = slot.getGoods().get();
                    addGoods(amount.getType(), amount.getAmount());
                }
            });
        }
    }

    void addEntry(final Entry<GoodsType, Integer> entry) {
        addGoods(entry.getKey(), entry.getValue());
    }

    private void addGoods(final GoodsType goodsType, final Integer amount) {
        Integer val = goods.get(goodsType);
        if (val == null) {
            goods.put(goodsType, amount);
        } else {
            val += amount;
            goods.put(goodsType, val);
        }
    }

}
