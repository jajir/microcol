package org.microcol.model;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * It's place where units stands before they sail to New World. Recruited units
 * appears here.
 */
public final class EuropePier {

    private final Model model;

    EuropePier(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    /**
     * Units waiting on the pier in departure order: the player's front-of-queue
     * choice first, then stable ID order as on a departing ship.
     *
     * @param player required owner of the pier units
     * @return ordered waiting units of the player
     */
    public List<Unit> getUnits(final Player player) {
        return model.getAllUnits().stream()
                .filter(unit -> unit.isAtEuropePier())
                .filter(unit -> unit.getOwner().equals(player))
                .sorted(Comparator
                        .comparing((Unit unit) -> !unit.isBoardingPriority())
                        .thenComparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .add("model", model).toString();
    }

}
