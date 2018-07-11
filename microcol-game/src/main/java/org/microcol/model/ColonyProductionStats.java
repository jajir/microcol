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

    private final Map<GoodType, GoodProductionStats> typeStats = new HashMap<>();

    public GoodProductionStats getStatsByType(final GoodType goodType) {
        Preconditions.checkNotNull(goodType);
        GoodProductionStats out = typeStats.get(goodType);
        if (out == null) {
            out = new GoodProductionStats(goodType);
            typeStats.put(goodType, out);
        }
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("storedTypes", typeStats.size())
                .toString();
    }

}
