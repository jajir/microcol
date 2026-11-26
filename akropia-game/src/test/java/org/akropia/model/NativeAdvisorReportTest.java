package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.akropia.model.NativeRelationsScene.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.akropia.model.game.GameModel;

class NativeAdvisorReportTest {
    private static NativeAdvisorReport report(final Model model,
            final String owner) {
        return model.getNativeAdvisorReport(model.getPlayerByName(owner));
    }

    private static Model learned() {
        return island(NativeNation.ARAWAK)
                .unit(DUTCH, UnitType.COLONIST, at(0, 1), UNARMED).toModel();
    }

    @Test
    void unknownNationsAndSettlementsStayHidden() {
        final Model model = island(NativeNation.ARAWAK).toModel();
        assertTrue(report(model, DUTCH).getKnownNations().isEmpty());
        assertTrue(report(model, DUTCH).getSettlements().isEmpty());
    }

    @Test
    void learnedSettlementNamesItsNationWithoutChiefTradeOrTeachingFacts() {
        final Model model = learned();
        final NativeAdvisorReport view = report(model, DUTCH);
        assertEquals(List.of(NativeNation.ARAWAK), view.getKnownNations());
        assertEquals(1, view.getSettlements().size());
        assertEquals(NativeNation.ARAWAK,
                view.getSettlements().get(0).getSettlement().getNation());
        assertTrue(view.getSettlements().get(0).getChiefFacts().isEmpty());
        assertEquals(NativeHostility.UNEASY,
                view.getSettlements().get(0).getRelation().getAttitude());
        assertEquals(5, model.getNativeFavour(NativeNation.ARAWAK,
                EuropeanNation.DUTCH));
    }

    @Test
    void discoveryIsNotSharedWithOtherPlayers() {
        final Model model = learned();
        assertEquals(1, report(model, DUTCH).getSettlements().size());
        assertTrue(report(model, FRANCE).getSettlements().isEmpty());
    }

    @Test
    void reopeningReflectsTheNationalFavourWithoutChangingIt() {
        final Model model = learned();
        model.applyNativeRelationOutcome(model.getPlayerByName(DUTCH),
                NativeRelationOutcome.settlementAttack(1, settlement(model),
                        false));
        final long randomBefore = model.save().getCombatRandomState();
        assertEquals(NativeHostility.HOSTILE, report(model, DUTCH)
                .getSettlements().get(0).getRelation().getAttitude());
        assertEquals(NativeHostility.HOSTILE, report(model, DUTCH)
                .getSettlements().get(0).getRelation().getAttitude());
        final Model restored = Model.make(model.save(),
                GameModel.gameOverEvaluators());
        assertEquals(2, restored.getNativeFavour(NativeNation.ARAWAK,
                EuropeanNation.DUTCH));
        assertEquals(randomBefore, restored.save().getCombatRandomState());
        assertEquals(1, report(restored, DUTCH).getSettlements().size());
    }
}
