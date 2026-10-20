package org.microcol.model.store;

import java.util.HashMap;
import java.util.Map;

/**
 * Saved national European markets. Each nation maps every tradable goods name
 * to {@code [bid, pressure in eighths, traded-since-update as 0 or 1]}.
 * Cumulative trade accounts map a nation and a traded goods name to
 * {@code [bought units, bought gold, sold units, sold gold, revolutionary
 * units, revolutionary gold]}; only nonempty accounts are written.
 */
public final class EuropePo {

    private Map<String, Map<String, int[]>> markets;

    /** Absent in a save without settled trades, which is an empty state. */
    private Map<String, Map<String, int[]>> accounts = new HashMap<>();

    public Map<String, Map<String, int[]>> getMarkets() {
        return markets;
    }

    public void setMarkets(final Map<String, Map<String, int[]>> markets) {
        this.markets = markets;
    }

    public Map<String, Map<String, int[]>> getAccounts() {
        return accounts;
    }

    public void setAccounts(final Map<String, Map<String, int[]>> accounts) {
        this.accounts = accounts;
    }

}
