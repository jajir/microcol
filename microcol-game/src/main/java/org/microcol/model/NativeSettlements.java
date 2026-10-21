package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.microcol.model.store.NativeSettlementPo;

import com.google.common.base.Preconditions;

/**
 * Registry of native settlements. It validates saved identity before the model
 * accepts it: unique IDs, land sites distinct from European colonies and other
 * settlements, the nation's own class and exactly one capital per nation that
 * has settlements.
 */
public final class NativeSettlements {

    private final List<NativeSettlement> settlements;

    private NativeSettlements(final List<NativeSettlement> settlements) {
        this.settlements = List.copyOf(settlements);
    }

    /**
     * Build the registry from saved entries. Missing or invalid identity is
     * rejected; an empty list is a valid world.
     *
     * @param saved       required saved entries, possibly empty
     * @param landSite    required test for a square where a settlement may
     *                    stand
     * @param colonySites required squares of European colonies
     * @return validated registry
     */
    public static NativeSettlements make(final List<NativeSettlementPo> saved,
            final Predicate<Location> landSite,
            final Set<Location> colonySites) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native settlements");
        Preconditions.checkNotNull(landSite);
        Preconditions.checkNotNull(colonySites);
        final Set<Integer> ids = new HashSet<>();
        final Set<Location> sites = new HashSet<>();
        final EnumMap<NativeNation, Integer> capitals = new EnumMap<>(
                NativeNation.class);
        final List<NativeSettlement> out = new ArrayList<>();
        for (final NativeSettlementPo entry : saved) {
            Preconditions.checkArgument(entry != null,
                    "Unsupported save: missing native settlement");
            Preconditions.checkArgument(
                    entry.getId() != null && entry.getId() >= 0
                            && ids.add(entry.getId()),
                    "Unsupported save: missing or duplicate native settlement id");
            Preconditions.checkArgument(entry.getNation() != null,
                    "Unsupported save: missing native nation");
            Preconditions.checkArgument(
                    entry.getSettlementClass() == entry.getNation()
                            .getSettlementClass(),
                    "Unsupported save: native settlement class does not match its nation");
            Preconditions.checkArgument(entry.getCapital() != null,
                    "Unsupported save: missing native capital flag");
            Preconditions.checkArgument(
                    entry.getTreasury() != null && entry.getTreasury() >= 0,
                    "Unsupported save: missing or negative native treasury");
            final Location location = entry.getLocation();
            Preconditions.checkArgument(location != null,
                    "Unsupported save: missing native settlement location");
            Preconditions.checkArgument(landSite.test(location),
                    "Unsupported save: native settlement is not on land");
            Preconditions.checkArgument(!colonySites.contains(location),
                    "Unsupported save: native settlement shares a European colony square");
            Preconditions.checkArgument(sites.add(location),
                    "Unsupported save: two native settlements share a square");
            if (entry.getCapital()) {
                capitals.merge(entry.getNation(), 1, Integer::sum);
            }
            out.add(new NativeSettlement(entry.getId(), entry.getNation(),
                    entry.getCapital(), location, entry.getTreasury()));
        }
        for (final NativeSettlement settlement : out) {
            Preconditions.checkArgument(
                    capitals.getOrDefault(settlement.getNation(), 0) == 1,
                    "Unsupported save: a native nation with settlements needs exactly one capital");
        }
        return new NativeSettlements(out);
    }

    /**
     * @return every settlement in the saved order, unmodifiable
     */
    public List<NativeSettlement> getAll() {
        return settlements;
    }

    /**
     * @param location required square
     * @return settlement on the square, if any
     */
    public Optional<NativeSettlement> getAt(final Location location) {
        Preconditions.checkNotNull(location);
        return settlements.stream()
                .filter(settlement -> settlement.getLocation().equals(location))
                .findAny();
    }

    /**
     * Owner of the land claim on a square: the nearest settlement whose claim
     * covers it, then the lowest stable ID. A settlement's own square is never
     * claimed. The caller decides whether the square is land.
     *
     * @param location required square
     * @return claiming settlement, if any
     */
    public Optional<NativeSettlement> getClaimOwner(final Location location) {
        Preconditions.checkNotNull(location);
        if (getAt(location).isPresent()) {
            return Optional.empty();
        }
        return settlements.stream()
                .filter(settlement -> settlement.claims(location))
                .min(Comparator.comparingInt(
                        (NativeSettlement settlement) -> settlement
                                .getLocation().getDistanceChebyshev(location))
                        .thenComparingInt(NativeSettlement::getId));
    }

    /**
     * @return saved form of every settlement, in the same order
     */
    public List<NativeSettlementPo> toPo() {
        final List<NativeSettlementPo> out = new ArrayList<>();
        for (final NativeSettlement settlement : settlements) {
            final NativeSettlementPo po = new NativeSettlementPo();
            po.setId(settlement.getId());
            po.setNation(settlement.getNation());
            po.setSettlementClass(settlement.getSettlementClass());
            po.setCapital(settlement.isCapital());
            po.setLocation(settlement.getLocation());
            po.setTreasury(settlement.getTreasury());
            out.add(po);
        }
        return out;
    }

}
