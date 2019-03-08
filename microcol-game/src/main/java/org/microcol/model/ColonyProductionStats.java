package org.microcol.model;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Colony production statistics. in current moment provide information about all
 * produces or consumed items and warehouse amount of goods.
 */
public final class ColonyProductionStats {

    private final Map<GoodsType, GoodsProductionStats> typeStats = new HashMap<>();

    public GoodsProductionStats getStatsByType(final GoodsType goodsType) {
        Preconditions.checkNotNull(goodsType);
        GoodsProductionStats out = typeStats.get(goodsType);
        if (out == null) {
            out = new GoodsProductionStats(goodsType);
            typeStats.put(goodsType, out);
        }
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("storedTypes", typeStats.size())
                .toString();
    }

}
