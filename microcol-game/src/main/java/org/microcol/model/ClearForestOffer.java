package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable terms of one forest-clearing order, shown to the player before work
 * starts. Starting the work requires the offer the player confirmed to equal
 * the terms offered now.
 * <p>
 * The expected lumber is not a promise to a colony: the recipient is chosen,
 * and the warehouse capacity applied, only when the work completes.
 * </p>
 */
public final class ClearForestOffer {

    private final int unitId;

    private final Location location;

    private final int updates;

    private final int tools;

    private final int lumber;

    ClearForestOffer(final int unitId, final Location location,
            final int updates, final int tools, final int lumber) {
        this.unitId = unitId;
        this.location = Preconditions.checkNotNull(location);
        this.updates = updates;
        this.tools = tools;
        this.lumber = lumber;
    }

    /** @return the pioneer that would do the work */
    public int getUnitId() {
        return unitId;
    }

    /** @return the forested square */
    public Location getLocation() {
        return location;
    }

    /** @return owner updates until the forest is removed */
    public int getUpdates() {
        return updates;
    }

    /** @return tools spent once, when the work completes */
    public int getTools() {
        return tools;
    }

    /** @return lumber the completed work yields before warehouse limits */
    public int getLumber() {
        return lumber;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(unitId, location, updates, tools, lumber);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClearForestOffer)) {
            return false;
        }
        final ClearForestOffer other = (ClearForestOffer) obj;
        return unitId == other.unitId && location.equals(other.location)
                && updates == other.updates && tools == other.tools
                && lumber == other.lumber;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("unitId", unitId)
                .add("location", location).add("updates", updates)
                .add("tools", tools).add("lumber", lumber).toString();
    }
}
