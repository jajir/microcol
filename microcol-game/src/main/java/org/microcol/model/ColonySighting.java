package org.microcol.model;

import org.microcol.model.store.ColonySightingPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable snapshot of a colony as one player last observed it. It never reads
 * the live colony, so it cannot disclose later hidden changes.
 */
public final class ColonySighting {

    private final Location location;

    private final String colonyName;

    private final Player owner;

    private final int observedTurn;

    private final int observedYear;

    ColonySighting(final Location location, final String colonyName,
            final Player owner, final int observedTurn,
            final int observedYear) {
        this.location = Preconditions.checkNotNull(location);
        this.colonyName = Preconditions.checkNotNull(colonyName);
        this.owner = Preconditions.checkNotNull(owner);
        this.observedTurn = observedTurn;
        this.observedYear = observedYear;
    }

    static ColonySighting of(final Colony colony, final Calendar calendar) {
        return new ColonySighting(colony.getLocation(), colony.getName(),
                colony.getOwner(), calendar.getNumberOfPlayedTurns(),
                calendar.getCurrentYear());
    }

    static ColonySighting restore(final ColonySightingPo po,
            final Model model) {
        Preconditions.checkArgument(
                po != null && po.getLocation() != null
                        && po.getColonyName() != null
                        && po.getOwnerName() != null
                        && po.getObservedTurn() != null
                        && po.getObservedTurn() >= 0
                        && po.getObservedTurn() <= model.getCalendar()
                                .getNumberOfPlayedTurns(),
                "Invalid remembered colony");
        Preconditions.checkArgument(model.isValid(po.getLocation()),
                "Remembered colony is outside the map");
        return new ColonySighting(po.getLocation(), po.getColonyName(),
                model.getPlayerByName(po.getOwnerName()), po.getObservedTurn(),
                model.getCalendar().getYearForTurnNo(po.getObservedTurn()));
    }

    ColonySightingPo save() {
        final ColonySightingPo out = new ColonySightingPo();
        out.setLocation(location);
        out.setColonyName(colonyName);
        out.setOwnerName(owner.getName());
        out.setObservedTurn(observedTurn);
        return out;
    }

    public Location getLocation() {
        return location;
    }

    public String getColonyName() {
        return colonyName;
    }

    public Player getOwner() {
        return owner;
    }

    /** @return the played-turn counter when the colony was last observed */
    public int getObservedTurn() {
        return observedTurn;
    }

    /** @return the calendar year when the colony was last observed */
    public int getObservedYear() {
        return observedYear;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("location", location)
                .add("colonyName", colonyName).add("owner", owner)
                .add("observedTurn", observedTurn).toString();
    }
}
