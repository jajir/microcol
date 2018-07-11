package org.microcol.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class PlayerGoodsStatistics {

    private final Map<GoodType, Integer> goodAmounts = new HashMap<>();

    void addColonyData(final Colony colony) {
        colony.getColonyWarehouse().saveStatisticsTo(this);
    }

    /**
     * Economy value is counted as simple sum of all goods.
     *
     * TODO create something meaningful
     *
     * @return economy value of players
     */
    public int getEconomyValue() {
        return goodAmounts.entrySet().stream().mapToInt(entry -> entry.getValue()).sum();
    }

    public int getGoodsAmount(final GoodType goodType) {
        final Integer val = goodAmounts.get(goodType);
        if (val == null) {
            return 0;
        } else {
            return val;
        }
    }

    void addUnitData(final Unit unit) {
        unit.getCargo().getSlots().forEach(slot -> {
            if (slot.isLoadedGood()) {
                final GoodsAmount amount = slot.getGoods().get();
                addGoods(amount.getGoodType(), amount.getAmount());
            }
        });
    }

    void addEntry(final Entry<GoodType, Integer> entry) {
        addGoods(entry.getKey(), entry.getValue());
    }

    private void addGoods(final GoodType goodType, final Integer amount) {
        Integer val = goodAmounts.get(goodType);
        if (val == null) {
            goodAmounts.put(goodType, amount);
        } else {
            val += amount;
            goodAmounts.put(goodType, val);
        }
    }

}
