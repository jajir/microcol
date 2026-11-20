package org.akropia.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.akropia.model.store.LandOwnershipPo;

import com.google.common.base.Preconditions;

/**
 * Sparse colonial titles. Other land belongs to its native claimant, or nobody.
 */
final class LandOwnership {
    private final Map<Location, LandOwnershipPo> titles = new LinkedHashMap<>();

    static LandOwnership make(final List<LandOwnershipPo> saved,
            final Predicate<Location> land) {
        Preconditions.checkArgument(saved != null, "Missing land ownership");
        final LandOwnership result = new LandOwnership();
        for (final LandOwnershipPo row : saved) {
            Preconditions.checkArgument(
                    row != null && row.getLocation() != null
                            && land.test(row.getLocation())
                            && (row.getOwner() != null
                                    || row.getNativeClaim() != null)
                            && (!row.isPurchased() || row.getOwner() != null),
                    "Invalid land ownership");
            Preconditions.checkArgument(
                    result.titles.put(row.getLocation(), copy(row)) == null,
                    "Duplicate land owner");
        }
        return result;
    }

    Optional<String> owner(final Location at) {
        return Optional.ofNullable(titles.get(at))
                .map(LandOwnershipPo::getOwner);
    }

    Optional<NativeNation> nativeClaim(final Location at) {
        return Optional.ofNullable(titles.get(at))
                .map(LandOwnershipPo::getNativeClaim);
    }

    void acquire(final Location at, final String owner,
            final NativeNation claim, final boolean purchased) {
        Preconditions.checkArgument(
                owner(at).map(existing -> existing.equals(owner)).orElse(true),
                "Land belongs to another player");
        Preconditions.checkArgument(owner != null && !owner.isBlank(),
                "Missing land owner");
        final LandOwnershipPo existing = titles.get(at);
        if (existing != null) {
            existing.setOwner(owner);
            existing.setPurchased(existing.isPurchased() || purchased);
            return;
        }
        final LandOwnershipPo row = new LandOwnershipPo();
        row.setLocation(at);
        row.setOwner(owner);
        row.setNativeClaim(claim);
        row.setPurchased(purchased);
        titles.put(at, row);
    }

    void capture(final Location at, final String newOwner) {
        final LandOwnershipPo row = titles.get(at);
        if (row == null) {
            acquire(at, newOwner, null, false);
        } else {
            row.setOwner(newOwner);
        }
    }

    void releaseUnoccupied(final BiPredicate<NativeNation, String> friends,
            final BiPredicate<Location, String> occupied) {
        titles.values().stream()
                .filter(row -> row.getOwner() != null && !row.isPurchased()
                        && row.getNativeClaim() != null
                        && !friends.test(row.getNativeClaim(), row.getOwner())
                        && !occupied.test(row.getLocation(), row.getOwner()))
                .forEach(row -> row.setOwner(null));
    }

    List<LandOwnershipPo> save() {
        final List<LandOwnershipPo> result = new ArrayList<>();
        titles.values().forEach(row -> result.add(copy(row)));
        return result;
    }

    private static LandOwnershipPo copy(final LandOwnershipPo row) {
        final LandOwnershipPo copy = new LandOwnershipPo();
        copy.setLocation(row.getLocation());
        copy.setOwner(row.getOwner());
        copy.setNativeClaim(row.getNativeClaim());
        copy.setPurchased(row.isPurchased());
        return copy;
    }
}
