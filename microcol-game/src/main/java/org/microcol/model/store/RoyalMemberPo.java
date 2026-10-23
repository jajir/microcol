package org.microcol.model.store;

import org.microcol.model.RoyalRoster;

/**
 * One saved member of a Crown roster with its identity, family and state. A
 * shipped member names its carrying unit; a shipped man-of-war may name its
 * colony target and its retry count. Absent values mean none or zero.
 */
public final class RoyalMemberPo {

    private Integer id;

    private RoyalRoster.Family family;

    private RoyalRoster.State state;

    private Integer unitId;

    private Integer targetColonyId;

    private Integer blockedTurns;

    public Integer getId() {
        return id;
    }

    /**
     * @return identity of the carrying unit, present only when shipped
     */
    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(final Integer unitId) {
        this.unitId = unitId;
    }

    /**
     * @return colony chosen as the landing target, if any
     */
    public Integer getTargetColonyId() {
        return targetColonyId;
    }

    public void setTargetColonyId(final Integer targetColonyId) {
        this.targetColonyId = targetColonyId;
    }

    /**
     * @return unsuccessful royal turns of the current route
     */
    public Integer getBlockedTurns() {
        return blockedTurns;
    }

    public void setBlockedTurns(final Integer blockedTurns) {
        this.blockedTurns = blockedTurns;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public RoyalRoster.Family getFamily() {
        return family;
    }

    public void setFamily(final RoyalRoster.Family family) {
        this.family = family;
    }

    public RoyalRoster.State getState() {
        return state;
    }

    public void setState(final RoyalRoster.State state) {
        this.state = state;
    }
}
