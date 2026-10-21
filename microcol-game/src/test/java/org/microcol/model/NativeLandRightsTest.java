package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.NativeLandRecordPo;
import org.microcol.model.store.NativeSettlementPo;

class NativeLandRightsTest {

    private static final Predicate<Location> ANY_LAND = location -> true;

    private static NativeSettlementPo settlement(final int id,
            final NativeNation nation, final boolean capital, final int x,
            final int y) {
        final NativeSettlementPo po = new NativeSettlementPo();
        po.setId(id);
        po.setNation(nation);
        po.setSettlementClass(nation.getSettlementClass());
        po.setCapital(capital);
        po.setTreasury(capital ? 2000 : 1000);
        po.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        po.setOfferedGoods(List.of());
        po.setWantedGoods(List.of());
        po.setTrade(List.of());
        po.setLocation(Location.of(x, y));
        return po;
    }

    private static NativeSettlements registry(
            final NativeSettlementPo... saved) {
        return NativeSettlements.make(List.of(saved), ANY_LAND, Set.of());
    }

    private static NativeLandRecordPo record(final EuropeanNation european,
            final int x, final int y) {
        final NativeLandRecordPo po = new NativeLandRecordPo();
        po.setEuropean(european);
        po.setLocation(Location.of(x, y));
        return po;
    }

    private static NativeSettlementPo villageAt10() {
        return settlement(0, NativeNation.ARAWAK, true, 10, 10);
    }

    private static NativeSettlementPo cityAt20() {
        return settlement(1, NativeNation.AZTEC, true, 20, 10);
    }

    @Test
    void villageClaimsOneSquareAndCityClaimsTwo() {
        final NativeSettlements registry = registry(villageAt10(), cityAt20());
        assertEquals(0,
                registry.getClaimOwner(Location.of(11, 11)).get().getId());
        assertFalse(registry.getClaimOwner(Location.of(12, 10)).isPresent());
        assertEquals(1,
                registry.getClaimOwner(Location.of(18, 8)).get().getId());
        assertFalse(registry.getClaimOwner(Location.of(17, 8)).isPresent());
    }

    @Test
    void campClaimsOneSquare() {
        final NativeSettlements registry = registry(
                settlement(0, NativeNation.TUPI, true, 10, 10));
        assertTrue(registry.getClaimOwner(Location.of(9, 9)).isPresent());
        assertFalse(registry.getClaimOwner(Location.of(8, 10)).isPresent());
    }

    @Test
    void settlementSquareIsNeverClaimed() {
        final NativeSettlements registry = registry(villageAt10(), cityAt20());
        assertFalse(registry.getClaimOwner(Location.of(10, 10)).isPresent());
        assertFalse(registry.getClaimOwner(Location.of(20, 10)).isPresent());
    }

    @Test
    void nearestSettlementBeatsLowerIdAndLowestIdBreaksTies() {
        // The village (id 4) is nearer to the shared square than the city
        // (id 0), so the higher ID wins on distance.
        final NativeSettlements nearer = registry(
                settlement(4, NativeNation.ARAWAK, true, 10, 10),
                settlement(0, NativeNation.AZTEC, true, 13, 10));
        assertEquals(4,
                nearer.getClaimOwner(Location.of(11, 10)).get().getId());
        // Equal distance: the lower ID wins.
        final NativeSettlements tie = registry(
                settlement(4, NativeNation.ARAWAK, true, 10, 10),
                settlement(2, NativeNation.TUPI, true, 12, 10));
        assertEquals(2, tie.getClaimOwner(Location.of(11, 10)).get().getId());
    }

    @Test
    void rightOnSquareWithoutAClaimantIsKeptBecauseItBelongsToTheEuropean() {
        // The square (30, 30) has no claiming settlement, as after a claim
        // change; the permanent right must still load and save unchanged.
        final NativeLandRights rights = NativeLandRights.make(ANY_LAND,
                List.of(record(EuropeanNation.FRENCH, 30, 30)), List.of());
        assertTrue(rights.isPaid(EuropeanNation.FRENCH, Location.of(30, 30)));
        assertEquals(1, rights.savePaid().size());
    }

    @Test
    void rightOnSeaOrOffMapSquareIsRejected() {
        final Location sea = Location.of(11, 11);
        assertThrows(IllegalArgumentException.class,
                () -> NativeLandRights.make(location -> !location.equals(sea),
                        List.of(record(EuropeanNation.FRENCH, 11, 11)),
                        List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeLandRights.make(location -> !location.equals(sea),
                        List.of(),
                        List.of(record(EuropeanNation.FRENCH, 11, 11))));
    }

    @Test
    void incompleteRecordsAreRejected() {
        final NativeLandRecordPo noEuropean = record(EuropeanNation.FRENCH, 11,
                11);
        noEuropean.setEuropean(null);
        final NativeLandRecordPo noLocation = record(EuropeanNation.FRENCH, 11,
                11);
        noLocation.setLocation(null);
        assertThrows(IllegalArgumentException.class, () -> NativeLandRights
                .make(ANY_LAND, List.of(noEuropean), List.of()));
        assertThrows(IllegalArgumentException.class, () -> NativeLandRights
                .make(ANY_LAND, List.of(), List.of(noLocation)));
    }

    @Test
    void missingListsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> NativeLandRights.make(ANY_LAND, null, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeLandRights.make(ANY_LAND, List.of(), null));
    }

    @Test
    void duplicateRecordIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> NativeLandRights.make(ANY_LAND,
                        List.of(record(EuropeanNation.FRENCH, 11, 11),
                                record(EuropeanNation.FRENCH, 11, 11)),
                        List.of()));
    }

    @Test
    void savedRightsAndTakingsKeepEachEuropeanSeparate() {
        final NativeLandRights first = NativeLandRights.make(ANY_LAND,
                List.of(record(EuropeanNation.FRENCH, 11, 11)),
                List.of(record(EuropeanNation.DUTCH, 11, 11),
                        record(EuropeanNation.FRENCH, 11, 10)));
        final NativeLandRights loaded = NativeLandRights.make(ANY_LAND,
                first.savePaid(), first.saveTaken());
        assertTrue(loaded.isPaid(EuropeanNation.FRENCH, Location.of(11, 11)));
        assertFalse(loaded.isPaid(EuropeanNation.DUTCH, Location.of(11, 11)));
        assertTrue(loaded.isTaken(EuropeanNation.DUTCH, Location.of(11, 11)));
        assertTrue(loaded.isTaken(EuropeanNation.FRENCH, Location.of(11, 10)));
        assertFalse(loaded.isTaken(EuropeanNation.FRENCH, Location.of(11, 11)));
        assertEquals(1, loaded.savePaid().size());
        assertEquals(2, loaded.saveTaken().size());
        assertEquals(Set.of(Location.of(11, 10)),
                loaded.takenBy(EuropeanNation.FRENCH));
    }

}
