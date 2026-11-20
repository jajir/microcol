package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.akropia.model.NativeRelationsScene.*;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The minimal native interaction surface; removed actions cannot be offered.
 */
class NativeVisitChoicesTest {
    @Test
    void missionaryHasOnlyOneShotDiplomacy() {
        final Model model = island(NativeNation.AZTEC).unit(DUTCH,
                UnitType.COLONIST, at(1, 0), unit -> unit.setMissionary(true))
                .toModel();
        assertEquals(List.of(NativeVisitAction.MISSION),
                model.getNativeVisitChoices(model.getUnitById(200),
                        settlement(model)).stream()
                        .map(NativeVisitChoice::getAction)
                        .collect(java.util.stream.Collectors.toList()));
    }

    @Test
    void anEmptySettlementOffersNoBattleAndNoRemovedActions() {
        final Model armed = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SOLDIER).toModel();
        assertEquals(List.of(),
                armed.getNativeVisitChoices(armed.getUnitById(200),
                        settlement(armed)).stream()
                        .map(NativeVisitChoice::getAction)
                        .collect(java.util.stream.Collectors.toList()));
        final Model plain = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED).toModel();
        assertTrue(plain.getNativeVisitChoices(plain.getUnitById(200),
                settlement(plain)).isEmpty());
    }

    @Test
    void chiefTradeTributeContestAndIncitementAreNotOffered() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        final Unit scout = model.getUnitById(200);
        final NativeSettlement site = settlement(model);
        assertTrue(model.getNativeChiefPreview(scout, site).isEmpty());
        assertTrue(model.getNativeMarketView(scout, site).isEmpty());
        assertTrue(model.getNativeTributeQuote(scout, site).isEmpty());
        assertTrue(model.getNativeMissionContestPreview(scout, site).isEmpty());
        assertTrue(model.getNativeIncitementTargets(scout, site).isEmpty());
        assertThrows(IllegalStateException.class, () -> model
                .startNativeTeaching(scout, site, site.getTeachableSkill()));
    }
}
