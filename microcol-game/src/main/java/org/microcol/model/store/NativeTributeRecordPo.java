package org.microcol.model.store;

import org.microcol.model.EuropeanNation;

/**
 * Last tribute demand of a visiting European nation at a native settlement
 * (R-0162). The settlement holding the record gives its identity. Both fields
 * are required; an absent list of these records means no demand yet.
 */
public class NativeTributeRecordPo {

    private EuropeanNation european;

    private Integer turn;

    public EuropeanNation getEuropean() {
        return european;
    }

    public void setEuropean(final EuropeanNation european) {
        this.european = european;
    }

    /**
     * @return saved game turn of the demand, required and nonnegative
     */
    public Integer getTurn() {
        return turn;
    }

    public void setTurn(final Integer turn) {
        this.turn = turn;
    }

}
