package org.microcol.model;

import java.util.List;
import java.util.Optional;

import org.microcol.model.unit.Ship;

import com.google.common.collect.ImmutableList;

/**
 * Immutable facts of a resolved naval battle, safe after a ship leaves the map.
 */
public final class NavalCombatOutcome {
    private final Location attackerLocation;
    private final Location defenderLocation;
    private final int winnerId;
    private final int loserId;
    private final boolean sunk;
    private final List<Goods> capturedGoods;
    private final List<Goods> remainingGoods;
    private final List<Goods> lostGoods;
    private final int preservedPassengers;
    private final int lostPassengers;
    private final Integer repairColonyId;
    private final String repairDestinationName;
    private final boolean europeRepairDestination;

    NavalCombatOutcome(final Model model, final NavalCombatPreview preview,
            final Ship winner, final Ship loser, final boolean sunk,
            final List<Goods> capturedGoods) {
        attackerLocation = preview.getAttackerLocation();
        defenderLocation = preview.getDefenderLocation();
        winnerId = winner.getId();
        loserId = loser.getId();
        this.sunk = sunk;
        this.capturedGoods = ImmutableList.copyOf(capturedGoods);
        final ImmutableList.Builder<Goods> remaining = ImmutableList.builder();
        int passengers = 0;
        for (final CargoSlot slot : loser.getCargo().getSlots()) {
            slot.getGoods().ifPresent(remaining::add);
            if (slot.isLoadedUnit()) {
                passengers++;
            }
        }
        final List<Goods> leftovers = remaining.build();
        remainingGoods = sunk ? List.of() : leftovers;
        lostGoods = sunk ? leftovers : List.of();
        preservedPassengers = sunk ? 0 : passengers;
        lostPassengers = sunk ? passengers : 0;
        final Optional<ShipRepairDestination> destination = sunk
                ? Optional.empty()
                : model.planShipRepair(loser);
        repairColonyId = destination.filter(value -> !value.isEurope())
                .map(ShipRepairDestination::getColonyId).orElse(null);
        europeRepairDestination = destination
                .map(ShipRepairDestination::isEurope).orElse(false);
        repairDestinationName = destination
                .map(value -> value.isEurope() ? "Europe"
                        : model.getColonyById(value.getColonyId()).orElseThrow()
                                .getName())
                .orElse(null);
    }

    public Location getAttackerLocation() {
        return attackerLocation;
    }

    public Location getDefenderLocation() {
        return defenderLocation;
    }

    public int getWinnerId() {
        return winnerId;
    }

    public int getLoserId() {
        return loserId;
    }

    public boolean isSunk() {
        return sunk;
    }

    public List<Goods> getCapturedGoods() {
        return capturedGoods;
    }

    public List<Goods> getRemainingGoods() {
        return remainingGoods;
    }

    public List<Goods> getLostGoods() {
        return lostGoods;
    }

    public int getPreservedPassengers() {
        return preservedPassengers;
    }

    public int getLostPassengers() {
        return lostPassengers;
    }

    public Integer getRepairColonyId() {
        return repairColonyId;
    }

    public boolean isEuropeRepairDestination() {
        return europeRepairDestination;
    }

    public String getRepairDestinationName() {
        return repairDestinationName;
    }
}
