package org.microcol.model.store;

import java.util.List;

/**
 * Native settlement identities one player has learned by sight. Settlement ids
 * refer to {@link NativeSettlementPo#getId()} in the same save.
 */
public class NativeSettlementKnowledgePo {

    private String playerName;

    private List<Integer> settlementIds;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(final String playerName) {
        this.playerName = playerName;
    }

    public List<Integer> getSettlementIds() {
        return settlementIds;
    }

    public void setSettlementIds(final List<Integer> settlementIds) {
        this.settlementIds = settlementIds;
    }

}
