package org.microcol.model.store;

import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlementClass;

/**
 * Saved native settlement. Every field is required in the current schema; a
 * missing field is rejected when the model is built, never guessed.
 */
public class NativeSettlementPo {

    private Integer id;

    private NativeNation nation;

    private NativeSettlementClass settlementClass;

    private Boolean capital;

    private Location location;

    private Integer treasury;

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public NativeNation getNation() {
        return nation;
    }

    public void setNation(final NativeNation nation) {
        this.nation = nation;
    }

    public NativeSettlementClass getSettlementClass() {
        return settlementClass;
    }

    public void setSettlementClass(
            final NativeSettlementClass settlementClass) {
        this.settlementClass = settlementClass;
    }

    public Boolean getCapital() {
        return capital;
    }

    public void setCapital(final Boolean capital) {
        this.capital = capital;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(final Location location) {
        this.location = location;
    }

    /**
     * @return treasury in gold, required; it starts at the agreed initial value
     *         and only grows by land payments in this schema
     */
    public Integer getTreasury() {
        return treasury;
    }

    public void setTreasury(final Integer treasury) {
        this.treasury = treasury;
    }

}
