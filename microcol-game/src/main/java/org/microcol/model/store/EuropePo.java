package org.microcol.model.store;

import java.util.Map;

/**
 * Saved national European markets. Each nation maps every tradable goods name
 * to {@code [bid, pressure in eighths, traded-since-update as 0 or 1]}.
 */
public final class EuropePo {

    private Map<String, Map<String, int[]>> markets;

    public Map<String, Map<String, int[]>> getMarkets() {
        return markets;
    }

    public void setMarkets(final Map<String, Map<String, int[]>> markets) {
        this.markets = markets;
    }

}
