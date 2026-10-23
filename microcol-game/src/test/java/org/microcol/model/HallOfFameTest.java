package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.HallOfFameEntryPo;

/**
 * The persistent top ten (R-0197): order, ties, admission boundaries, duplicate
 * identities, restart and rejected stored rankings. Each entry goes through the
 * same loading check as a saved entry. A conquistador total is the servant
 * count times the servant points, so the expected totals are known without a
 * game.
 */
class HallOfFameTest {

    @Test
    void firstResultIsRankedFirstInAnEmptyRanking() {
        final HallOfFame hall = HallOfFame.empty().with(entry("a", 5));
        assertEquals(Optional.of(1), hall.getRank("a"));
        assertEquals(1, hall.getEntries().size());
    }

    @Test
    void emptyRankingHasNoPlaceForAnyResult() {
        assertEquals(Optional.empty(), HallOfFame.empty().getRank("missing"));
    }

    @Test
    void higherFinalScoreRanksFirst() {
        final HallOfFame hall = HallOfFame.empty().with(entry("low", 5))
                .with(entry("high", 9));
        assertEquals(Optional.of(1), hall.getRank("high"));
        assertEquals(Optional.of(2), hall.getRank("low"));
    }

    @Test
    void equalScoreKeepsTheEarlierRecordFirst() {
        final HallOfFame hall = HallOfFame.empty().with(entry("first", 7))
                .with(entry("second", 7));
        assertEquals(Optional.of(1), hall.getRank("first"));
        assertEquals(Optional.of(2), hall.getRank("second"));
    }

    @Test
    void resultEqualToTheTenthPlaceIsNotAdmitted() {
        final HallOfFame hall = tenRanked();
        final HallOfFame next = hall.with(entry("tie", 10));
        assertSame(hall, next);
        assertEquals(Optional.empty(), next.getRank("tie"));
        assertEquals(Optional.of(10), next.getRank("r1"));
    }

    @Test
    void resultAboveTheTenthPlaceDisplacesTheTenthEntry() {
        final HallOfFame next = tenRanked().with(entry("up", 11));
        assertEquals(10, next.getEntries().size());
        assertEquals(Optional.of(10), next.getRank("up"));
        assertEquals(Optional.empty(), next.getRank("r1"));
    }

    @Test
    void resultBelowTheTenthPlaceIsNotKept() {
        final HallOfFame hall = tenRanked();
        final HallOfFame next = hall.with(entry("low", 5));
        assertSame(hall, next);
        assertEquals(Optional.empty(), next.getRank("low"));
    }

    @Test
    void fifteenResultsKeepOnlyTheTenHighestTotalsInOrder() {
        final List<Long> totals = new ArrayList<>();
        HallOfFame hall = HallOfFame.empty();
        for (int index = 1; index <= 15; index++) {
            final int servants = index * 7 % 13 + 1;
            hall = hall.with(entry("x" + index, servants));
            totals.add((long) servants);
        }
        totals.sort(Comparator.reverseOrder());
        assertEquals(HallOfFame.CAPACITY, hall.getEntries().size());
        for (int index = 0; index < HallOfFame.CAPACITY; index++) {
            assertEquals(totals.get(index).longValue(),
                    hall.getEntries().get(index).getTotal());
        }
    }

    @Test
    void duplicateIdentityKeepsItsPlaceAndTieOrder() {
        final HallOfFame hall = HallOfFame.empty().with(entry("a", 7))
                .with(entry("b", 7));
        assertSame(hall, hall.with(entry("a", 7)));
        assertSame(hall, hall.with(entry("b", 9)));
        assertEquals(Optional.of(1), hall.getRank("a"));
        assertEquals(Optional.of(2), hall.getRank("b"));
    }

    @Test
    void frozenResultIsNotReplacedByLaterComponents() {
        final HallOfFame hall = HallOfFame.empty().with(entry("frozen", 3));
        final HallOfFame after = hall.with(entry("frozen", 50));
        assertSame(hall, after);
        assertEquals(3L, after.getEntries().get(0).getTotal());
    }

    @Test
    void restartKeepsOrderAndTieOrder() {
        final HallOfFame hall = HallOfFame.empty().with(entry("first", 7))
                .with(entry("second", 7)).with(entry("top", 9));
        final List<HallOfFameEntry> stored = new ArrayList<>();
        for (final HallOfFameEntry entry : hall.getEntries()) {
            stored.add(HallOfFameEntry.load(entry.save()));
        }
        final HallOfFame restarted = HallOfFame.of(stored);
        assertEquals(List.of("top", "first", "second"), identities(restarted));
        assertSame(restarted, restarted.with(entry("first", 7)));
    }

    @Test
    void storedRankingRejectsMoreThanTheCapacity() {
        final List<HallOfFameEntry> saved = new ArrayList<>();
        for (int index = 1; index <= 11; index++) {
            saved.add(entry("e" + index, 20 - index));
        }
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFame.of(saved));
    }

    @Test
    void storedRankingRejectsAnOrderThatIsNotRanked() {
        final List<HallOfFameEntry> saved = List.of(entry("low", 3),
                entry("high", 9));
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFame.of(saved));
    }

    @Test
    void storedRankingRejectsARepeatedIdentity() {
        final List<HallOfFameEntry> saved = List.of(entry("a", 9),
                entry("a", 3));
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFame.of(saved));
    }

    @Test
    void storedEntryRejectsATotalThatDoesNotMatchItsComponents() {
        final HallOfFameEntryPo po = po("a", 4, 5);
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFameEntry.load(po));
    }

    @Test
    void storedEntryRejectsAMissingComponent() {
        final HallOfFameEntryPo po = po("a", 4, 4);
        po.setServants(null);
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFameEntry.load(po));
    }

    @Test
    void storedEntryRejectsAnUnknownDifficulty() {
        final HallOfFameEntryPo po = po("a", 4, 4);
        po.setDifficulty("NOBODY");
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFameEntry.load(po));
    }

    @Test
    void storedEntryRejectsABlankIdentity() {
        final HallOfFameEntryPo po = po(" ", 4, 4);
        assertThrows(IllegalArgumentException.class,
                () -> HallOfFameEntry.load(po));
    }

    /** Ten results with totals 10, 20, ..., 100: r10 first, r1 tenth. */
    private static HallOfFame tenRanked() {
        HallOfFame hall = HallOfFame.empty();
        for (int index = 1; index <= HallOfFame.CAPACITY; index++) {
            hall = hall.with(entry("r" + index, index * 10));
        }
        return hall;
    }

    private static List<String> identities(final HallOfFame hall) {
        return hall.getEntries().stream().map(HallOfFameEntry::getIdentity)
                .collect(Collectors.toList());
    }

    private static HallOfFameEntry entry(final String identity,
            final int servants) {
        return HallOfFameEntry.load(po(identity, servants,
                (long) servants * BaseScore.SERVANT_POINTS));
    }

    private static HallOfFameEntryPo po(final String identity,
            final int servants, final long total) {
        final HallOfFameEntryPo po = new HallOfFameEntryPo();
        po.setIdentity(identity);
        po.setPlayerName("Dutch");
        po.setPlayerDisplayName("Player " + identity);
        po.setDifficulty(Difficulty.CONQUISTADOR.name());
        po.setServants(servants);
        po.setColonists(0);
        po.setExperts(0);
        po.setFathers(0);
        po.setGold(0L);
        po.setSupportPercent(0);
        po.setDestroyedSettlements(0);
        po.setBellsBeyondTarget(0L);
        po.setTotal(total);
        return po;
    }
}
