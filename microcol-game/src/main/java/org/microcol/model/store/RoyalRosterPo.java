package org.microcol.model.store;

import java.util.List;

/**
 * Saved Crown roster: every member with its stable identity, the number of
 * growth units already added and the bell remainder toward the next one.
 */
public final class RoyalRosterPo {

    private Integer growthCount;

    private Integer remainder;

    private Integer dispatchCycle;

    private List<RoyalMemberPo> members;

    /**
     * @return family index of the next passenger in the cycle; absent means the
     *         start of the cycle
     */
    public Integer getDispatchCycle() {
        return dispatchCycle;
    }

    public void setDispatchCycle(final Integer dispatchCycle) {
        this.dispatchCycle = dispatchCycle;
    }

    /**
     * @return number of ground units added by bell growth
     */
    public Integer getGrowthCount() {
        return growthCount;
    }

    public void setGrowthCount(final Integer growthCount) {
        this.growthCount = growthCount;
    }

    /**
     * @return produced bells not yet converted into a unit
     */
    public Integer getRemainder() {
        return remainder;
    }

    public void setRemainder(final Integer remainder) {
        this.remainder = remainder;
    }

    /**
     * @return every member ever mustered, including shipped and destroyed ones
     */
    public List<RoyalMemberPo> getMembers() {
        return members;
    }

    public void setMembers(final List<RoyalMemberPo> members) {
        this.members = members;
    }
}
