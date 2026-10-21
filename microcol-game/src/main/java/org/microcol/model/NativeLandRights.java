package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import org.microcol.model.store.NativeLandRecordPo;

import com.google.common.base.Preconditions;

/**
 * Native-land state of each colonial European: paid rights and one-time unpaid
 * takings. Both are keyed by European and square, never by the claimant, so a
 * payment survives a later change of claim owner and a taking is never charged
 * again. Saved order follows the European order, then the order of first
 * record, so Save and Continue write the same file.
 */
final class NativeLandRights {

    private final Map<EuropeanNation, Set<Location>> paid;

    private final Map<EuropeanNation, Set<Location>> taken;

    private NativeLandRights(final Map<EuropeanNation, Set<Location>> paid,
            final Map<EuropeanNation, Set<Location>> taken) {
        this.paid = paid;
        this.taken = taken;
    }

    /**
     * Build the state from the saved lists. Both lists are required; an empty
     * list means nothing has been paid or taken. Each record must name a
     * European and a square of real map land, without duplicates; it does not
     * need a current claimant.
     *
     * @param landSquare required test for a valid land square of the map
     * @param savedPaid  required paid rights, possibly empty
     * @param savedTaken required unpaid takings, possibly empty
     * @return validated state
     */
    static NativeLandRights make(final Predicate<Location> landSquare,
            final List<NativeLandRecordPo> savedPaid,
            final List<NativeLandRecordPo> savedTaken) {
        Preconditions.checkNotNull(landSquare);
        Preconditions.checkArgument(savedPaid != null,
                "Unsupported save: missing native land rights");
        Preconditions.checkArgument(savedTaken != null,
                "Unsupported save: missing native land takings");
        return new NativeLandRights(read(landSquare, savedPaid),
                read(landSquare, savedTaken));
    }

    /**
     * @return saved form of every paid right, in a fixed order
     */
    List<NativeLandRecordPo> savePaid() {
        return save(paid);
    }

    /**
     * @return saved form of every unpaid taking, in a fixed order
     */
    List<NativeLandRecordPo> saveTaken() {
        return save(taken);
    }

    boolean isPaid(final EuropeanNation european, final Location at) {
        return paid.getOrDefault(european, Set.of()).contains(at);
    }

    boolean isTaken(final EuropeanNation european, final Location at) {
        return taken.getOrDefault(european, Set.of()).contains(at);
    }

    /**
     * @param european required European
     * @return every square this European has taken unpaid, as a copy
     */
    Set<Location> takenBy(final EuropeanNation european) {
        return Set.copyOf(taken.getOrDefault(european, Set.of()));
    }

    void addPaid(final EuropeanNation european, final Location at) {
        add(paid, european, at);
    }

    void addTaken(final EuropeanNation european, final Location at) {
        add(taken, european, at);
    }

    private static Map<EuropeanNation, Set<Location>> read(
            final Predicate<Location> landSquare,
            final List<NativeLandRecordPo> saved) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native land record list");
        final Map<EuropeanNation, Set<Location>> out = new EnumMap<>(
                EuropeanNation.class);
        for (final NativeLandRecordPo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getEuropean() != null
                            && entry.getLocation() != null,
                    "Unsupported save: incomplete native land record");
            Preconditions.checkArgument(landSquare.test(entry.getLocation()),
                    "Unsupported save: native land record is off the map or at sea");
            // A paid right belongs to the European and the square, so it stays
            // valid even when no settlement claims the square any more.
            Preconditions.checkArgument(
                    add(out, entry.getEuropean(), entry.getLocation()),
                    "Unsupported save: duplicate native land record");
        }
        return out;
    }

    private static List<NativeLandRecordPo> save(
            final Map<EuropeanNation, Set<Location>> table) {
        final List<NativeLandRecordPo> out = new ArrayList<>();
        for (final EuropeanNation european : EuropeanNation.values()) {
            for (final Location at : table.getOrDefault(european, Set.of())) {
                final NativeLandRecordPo po = new NativeLandRecordPo();
                po.setEuropean(european);
                po.setLocation(at);
                out.add(po);
            }
        }
        return out;
    }

    private static boolean add(final Map<EuropeanNation, Set<Location>> table,
            final EuropeanNation european, final Location at) {
        Preconditions.checkNotNull(european);
        Preconditions.checkNotNull(at);
        return table.computeIfAbsent(european, key -> new LinkedHashSet<>())
                .add(at);
    }

}
