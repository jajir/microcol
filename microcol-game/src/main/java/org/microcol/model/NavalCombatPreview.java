package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.Ship;

import com.google.common.collect.ImmutableList;

/**
 * Frozen naval participants, cargo, market, and repair choices for
 * confirmation.
 */
public final class NavalCombatPreview {
    static final class Transfer {
        final int source;
        final int destination;
        final Goods goods;

        Transfer(int source, int destination, Goods goods) {
            this.source = source;
            this.destination = destination;
            this.goods = goods;
        }
    }

    private final Location attackerLocation;
    private final Location defenderLocation;
    private final String inputs;
    private final Optional<ShipRepairDestination> attackerRepair;
    private final Optional<ShipRepairDestination> defenderRepair;
    private final List<Transfer> attackerLoot;
    private final List<Transfer> defenderLoot;

    NavalCombatPreview(final Model model, final Ship attacker,
            final Ship defender) {
        attackerLocation = attacker.getLocation();
        defenderLocation = defender.getLocation();
        attackerRepair = model.planShipRepair(attacker);
        defenderRepair = model.planShipRepair(defender);
        attackerLoot = ImmutableList
                .copyOf(planLoot(model, attacker, defender));
        defenderLoot = ImmutableList
                .copyOf(planLoot(model, defender, attacker));
        inputs = cargoSignature(attacker) + ':' + cargoSignature(defender) + ':'
                + marketSignature(model, attacker, defender) + ':'
                + repairSignature(attackerRepair) + ':'
                + repairSignature(defenderRepair);
    }

    private static String cargoSignature(final Ship ship) {
        final StringBuilder out = new StringBuilder();
        for (final CargoSlot slot : ship.getCargo().getSlots()) {
            if (slot.isLoadedUnit()) {
                out.append('U').append(slot.getUnit().orElseThrow().getId());
            } else if (slot.isLoadedGood()) {
                final Goods goods = slot.getGoods().orElseThrow();
                out.append('G').append(goods.getType().name()).append('=')
                        .append(goods.getAmount());
            } else {
                out.append('E');
            }
            out.append(';');
        }
        return out.toString();
    }

    private static String repairSignature(
            final Optional<ShipRepairDestination> destination) {
        return destination
                .map(value -> value.isEurope() ? "Europe"
                        : value.getColonyId() + "/" + value.getSeaDistance()
                                + "/" + value.getRepairDuration())
                .orElse("None");
    }

    private static String marketSignature(final Model model,
            final Ship attacker, final Ship defender) {
        final StringBuilder out = new StringBuilder();
        for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            out.append(type.name()).append('=')
                    .append(model.getEurope()
                            .getGoodsTradeForType(type,
                                    attacker.getOwner().getEuropeanNation())
                            .getSellPrice())
                    .append('/')
                    .append(model.getEurope()
                            .getGoodsTradeForType(type,
                                    defender.getOwner().getEuropeanNation())
                            .getSellPrice())
                    .append(';');
        }
        return out.toString();
    }

    private static List<Transfer> planLoot(final Model model, final Ship winner,
            final Ship loser) {
        final List<CargoSlot> source = loser.getCargo().getSlots();
        final List<CargoSlot> target = winner.getCargo().getSlots();
        final List<Integer> ordered = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            if (source.get(index).isLoadedGood()) {
                ordered.add(index);
            }
        }
        ordered.sort(Comparator
                .<Integer>comparingInt(index -> model.getEurope()
                        .getGoodsTradeForType(
                                source.get(index).getGoods().orElseThrow()
                                        .getType(),
                                winner.getOwner().getEuropeanNation())
                        .getSellPrice())
                .reversed()
                .thenComparing(index -> source.get(index).getGoods()
                        .orElseThrow().getType().name())
                .thenComparingInt(Integer::intValue));
        final GoodsType[] targetTypes = new GoodsType[target.size()];
        final int[] targetAmounts = new int[target.size()];
        for (int index = 0; index < target.size(); index++) {
            targetTypes[index] = target.get(index).getGoods()
                    .map(Goods::getType).orElse(null);
            targetAmounts[index] = target.get(index).getGoods()
                    .map(Goods::getAmount).orElse(0);
            if (target.get(index).isLoadedUnit()) {
                targetAmounts[index] = CargoSlot.MAX_CARGO_SLOT_CAPACITY;
            }
        }
        final List<Transfer> result = new ArrayList<>();
        for (final int sourceIndex : ordered) {
            final Goods sourceGoods = source.get(sourceIndex).getGoods()
                    .orElseThrow();
            int remaining = sourceGoods.getAmount();
            for (int pass = 0; pass < 2; pass++) {
                for (int index = 0; index < target.size()
                        && remaining > 0; index++) {
                    if (pass == 0 ? targetTypes[index] != sourceGoods.getType()
                            : targetTypes[index] != null) {
                        continue;
                    }
                    final int free = CargoSlot.MAX_CARGO_SLOT_CAPACITY
                            - targetAmounts[index];
                    final int amount = Math.min(remaining, free);
                    if (amount > 0) {
                        result.add(new Transfer(sourceIndex, index,
                                Goods.of(sourceGoods.getType(), amount)));
                        targetTypes[index] = sourceGoods.getType();
                        targetAmounts[index] += amount;
                        remaining -= amount;
                    }
                }
            }
        }
        return result;
    }

    public Location getAttackerLocation() {
        return attackerLocation;
    }

    public Location getDefenderLocation() {
        return defenderLocation;
    }

    public Optional<ShipRepairDestination> getAttackerRepairDestination() {
        return attackerRepair;
    }

    public Optional<ShipRepairDestination> getDefenderRepairDestination() {
        return defenderRepair;
    }

    public List<Goods> getPotentialLootIfAttackerWins() {
        return lootGoods(attackerLoot);
    }

    public List<Goods> getPotentialLootIfDefenderWins() {
        return lootGoods(defenderLoot);
    }

    private static List<Goods> lootGoods(final List<Transfer> transfers) {
        final List<Goods> result = new ArrayList<>();
        transfers.forEach(transfer -> result.add(transfer.goods));
        return ImmutableList.copyOf(result);
    }

    List<Transfer> loot(final boolean winnerAttacker) {
        return winnerAttacker ? attackerLoot : defenderLoot;
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof NavalCombatPreview
                && inputs.equals(((NavalCombatPreview) other).inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inputs);
    }
}
