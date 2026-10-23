package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Preconditions;

/**
 * The persistent top ten of scored results (R-0197). Entries are ordered by the
 * higher total first and, for an equal total, by the earlier record first. One
 * result identity is kept at most once. The ranking never recalculates an
 * entry; it only inserts and trims. Storing it is outside this class.
 */
public final class HallOfFame {

    /** The number of ranked entries kept. */
    public static final int CAPACITY = 10;

    private final List<HallOfFameEntry> entries;

    private HallOfFame(final List<HallOfFameEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * @return a ranking without any recorded result
     */
    public static HallOfFame empty() {
        return new HallOfFame(List.of());
    }

    /**
     * Check a ranking read from storage. Its stored order is kept, which fixes
     * the tie order of equal totals. A ranking that is out of order, repeats an
     * identity or is longer than the capacity is rejected, never repaired.
     *
     * @param saved required entries in their stored order
     * @return immutable ranking
     * @throws IllegalArgumentException when the stored ranking is not valid
     */
    public static HallOfFame of(final List<HallOfFameEntry> saved) {
        Preconditions.checkNotNull(saved, "Saved entries are required");
        Preconditions.checkArgument(saved.size() <= CAPACITY,
                "Hall of Fame keeps at most %s results", CAPACITY);
        final Set<String> identities = new HashSet<>();
        for (int index = 0; index < saved.size(); index++) {
            final HallOfFameEntry entry = Preconditions
                    .checkNotNull(saved.get(index), "Saved entry is required");
            Preconditions.checkArgument(identities.add(entry.getIdentity()),
                    "Duplicate Hall of Fame identity");
            Preconditions.checkArgument(index == 0
                    || saved.get(index - 1).getTotal() >= entry.getTotal(),
                    "Hall of Fame is not in ranking order");
        }
        return new HallOfFame(saved);
    }

    /**
     * @return the ranked entries from the first place, unmodifiable
     */
    public List<HallOfFameEntry> getEntries() {
        return entries;
    }

    /**
     * @param identity required identity of a recorded result
     * @return its place from one, empty when the result is not ranked
     */
    public Optional<Integer> getRank(final String identity) {
        Preconditions.checkNotNull(identity, "Identity is required");
        for (int index = 0; index < entries.size(); index++) {
            if (entries.get(index).getIdentity().equals(identity)) {
                return Optional.of(index + 1);
            }
        }
        return Optional.empty();
    }

    /**
     * Record a scored result. A known identity is not recorded again, so its
     * place and tie order stay as they were. A total equal to ranked entries
     * goes after them. A result that does not reach the tenth place is not
     * kept.
     *
     * @param entry required scored result
     * @return the ranking after the record; this ranking when nothing changed
     */
    public HallOfFame with(final HallOfFameEntry entry) {
        Preconditions.checkNotNull(entry, "Entry is required");
        if (getRank(entry.getIdentity()).isPresent()) {
            return this;
        }
        int index = 0;
        while (index < entries.size()
                && entries.get(index).getTotal() >= entry.getTotal()) {
            index++;
        }
        if (index >= CAPACITY) {
            return this;
        }
        final List<HallOfFameEntry> next = new ArrayList<>(entries);
        next.add(index, entry);
        return new HallOfFame(next.subList(0, Math.min(next.size(), CAPACITY)));
    }

}
