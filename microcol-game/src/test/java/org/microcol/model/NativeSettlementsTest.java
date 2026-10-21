package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.NativeSettlementPo;

/**
 * Native settlement identity: the agreed nation classes, the capital bonus and
 * treasury, and the saved identity that is rejected when it is not valid.
 */
class NativeSettlementsTest {

    private static final Set<Location> SEA = Set.of(Location.of(9, 9));

    private static NativeSettlementPo po(final int id,
            final NativeNation nation, final boolean capital, final int x,
            final int y) {
        final NativeSettlementPo po = new NativeSettlementPo();
        po.setId(id);
        po.setNation(nation);
        po.setSettlementClass(nation.getSettlementClass());
        po.setCapital(capital);
        po.setTreasury(capital ? 2000 : 1000);
        po.setLocation(Location.of(x, y));
        return po;
    }

    private static NativeSettlements make(final List<NativeSettlementPo> saved,
            final Set<Location> colonySites) {
        return NativeSettlements.make(saved,
                location -> !SEA.contains(location), colonySites);
    }

    private static NativeSettlements make(
            final List<NativeSettlementPo> saved) {
        return make(saved, Set.of());
    }

    @Test
    void eightNationsMapToAgreedClasses() {
        assertEquals(NativeSettlementClass.VILLAGE,
                NativeNation.ARAWAK.getSettlementClass());
        assertEquals(NativeSettlementClass.CITY,
                NativeNation.AZTEC.getSettlementClass());
        assertEquals(NativeSettlementClass.CITY,
                NativeNation.INCA.getSettlementClass());
        assertEquals(NativeSettlementClass.CAMP,
                NativeNation.TUPI.getSettlementClass());
        assertEquals(NativeSettlementClass.VILLAGE,
                NativeNation.CHEROKEE.getSettlementClass());
        assertEquals(NativeSettlementClass.VILLAGE,
                NativeNation.IROQUOIS.getSettlementClass());
        assertEquals(NativeSettlementClass.CAMP,
                NativeNation.SIOUX.getSettlementClass());
        assertEquals(NativeSettlementClass.CAMP,
                NativeNation.APACHE.getSettlementClass());
        assertEquals(8, NativeNation.values().length);
    }

    @Test
    void defenseBonusAddsCapitalToClassBonus() {
        assertEquals(0, NativeSettlementClass.CAMP.getDefenseBonusPercent());
        assertEquals(25,
                NativeSettlementClass.VILLAGE.getDefenseBonusPercent());
        assertEquals(50, NativeSettlementClass.CITY.getDefenseBonusPercent());
        final NativeSettlements registry = make(
                List.of(po(0, NativeNation.TUPI, true, 1, 1),
                        po(1, NativeNation.CHEROKEE, false, 2, 2),
                        po(2, NativeNation.CHEROKEE, true, 3, 3),
                        po(3, NativeNation.AZTEC, true, 4, 4)));
        assertEquals(25, registry.getAll().get(0).getDefenseBonusPercent());
        assertEquals(25, registry.getAll().get(1).getDefenseBonusPercent());
        assertEquals(50, registry.getAll().get(2).getDefenseBonusPercent());
        assertEquals(75, registry.getAll().get(3).getDefenseBonusPercent());
    }

    @Test
    void capitalStartsWithDoubleTreasury() {
        final NativeSettlements registry = make(
                List.of(po(0, NativeNation.INCA, true, 1, 1),
                        po(1, NativeNation.INCA, false, 2, 2)));
        assertEquals(2000, registry.getAll().get(0).getInitialTreasury());
        assertEquals(1000, registry.getAll().get(1).getInitialTreasury());
    }

    @Test
    void eachNationAcceptsOrdinaryAndCapitalPair() {
        final List<NativeSettlementPo> saved = new ArrayList<>();
        final NativeNation[] nations = NativeNation.values();
        for (int i = 0; i < nations.length; i++) {
            saved.add(po(2 * i, nations[i], true, 1 + 2 * i, 1));
            saved.add(po(2 * i + 1, nations[i], false, 1 + 2 * i, 2));
        }
        final NativeSettlements registry = make(saved);
        assertEquals(16, registry.getAll().size());
        for (int i = 0; i < nations.length; i++) {
            final NativeSettlement capital = registry
                    .getAt(Location.of(1 + 2 * i, 1)).orElseThrow();
            final NativeSettlement ordinary = registry
                    .getAt(Location.of(1 + 2 * i, 2)).orElseThrow();
            assertEquals(nations[i], capital.getNation());
            assertTrue(capital.isCapital());
            assertFalse(ordinary.isCapital());
            assertEquals(nations[i].getSettlementClass(),
                    capital.getSettlementClass());
            assertTrue(capital.getDefenseBonusPercent() > ordinary
                    .getDefenseBonusPercent());
        }
    }

    @Test
    void emptyWorldIsValid() {
        assertTrue(make(List.of()).getAll().isEmpty());
        assertTrue(make(List.of()).getAt(Location.of(1, 1)).isEmpty());
    }

    @Test
    void missingListIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> make(null));
    }

    @Test
    void missingOrDuplicateIdentityIsRejected() {
        final NativeSettlementPo noId = po(0, NativeNation.TUPI, true, 1, 1);
        noId.setId(null);
        assertThrows(IllegalArgumentException.class, () -> make(List.of(noId)));
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(-1, NativeNation.TUPI, true, 1, 1))));
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.TUPI, true, 1, 1),
                        po(0, NativeNation.SIOUX, false, 2, 2))));
    }

    @Test
    void missingNationOrCapitalFlagIsRejected() {
        final NativeSettlementPo noNation = po(0, NativeNation.TUPI, true, 1,
                1);
        noNation.setNation(null);
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(noNation)));
        final NativeSettlementPo noCapital = po(0, NativeNation.TUPI, true, 1,
                1);
        noCapital.setCapital(null);
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(noCapital)));
    }

    @Test
    void classThatDoesNotMatchNationIsRejected() {
        final NativeSettlementPo wrong = po(0, NativeNation.AZTEC, true, 1, 1);
        wrong.setSettlementClass(NativeSettlementClass.CAMP);
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(wrong)));
    }

    @Test
    void nationWithSettlementsNeedsExactlyOneCapital() {
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.AZTEC, false, 1, 1))));
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.AZTEC, true, 1, 1),
                        po(1, NativeNation.AZTEC, true, 2, 2))));
    }

    @Test
    void siteMustBeLandDistinctFromColoniesAndOtherSettlements() {
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.TUPI, true, 9, 9))));
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.TUPI, true, 4, 4)),
                        Set.of(Location.of(4, 4))));
        assertThrows(IllegalArgumentException.class,
                () -> make(List.of(po(0, NativeNation.TUPI, true, 4, 4),
                        po(1, NativeNation.SIOUX, false, 4, 4))));
    }

    @Test
    void savedIdentityRoundTripsUnchanged() {
        final NativeSettlements original = make(
                List.of(po(7, NativeNation.IROQUOIS, false, 3, 4),
                        po(9, NativeNation.IROQUOIS, true, 5, 6)));
        final NativeSettlements restored = make(original.toPo());
        assertEquals(original.getAll().size(), restored.getAll().size());
        for (int i = 0; i < original.getAll().size(); i++) {
            final NativeSettlement before = original.getAll().get(i);
            final NativeSettlement after = restored.getAll().get(i);
            assertEquals(before.getId(), after.getId());
            assertEquals(before.getNation(), after.getNation());
            assertEquals(before.getSettlementClass(),
                    after.getSettlementClass());
            assertEquals(before.isCapital(), after.isCapital());
            assertEquals(before.getLocation(), after.getLocation());
        }
    }

}
