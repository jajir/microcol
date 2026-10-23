package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;

/**
 * Native advisor report (R-0201): the nations and learned settlements of one
 * viewer, chief facts only after the chief has told them, isolation between
 * European viewers, relation refresh, pure reads and reload.
 */
class NativeAdvisorReportTest {

    private static final String DUTCH = NativeRelationsScene.DUTCH;

    private static final String FRANCE = NativeRelationsScene.FRANCE;

    private static NativeAdvisorReport report(final Model model,
            final String owner) {
        return model.getNativeAdvisorReport(model.getPlayerByName(owner));
    }

    /** Island settlement learned by a Dutch colonist, with a market to read. */
    private static NativeRelationsScene learnedByDutch() {
        return NativeRelationsScene.island(NativeNation.ARAWAK)
                .unit(DUTCH, UnitType.COLONIST, NativeRelationsScene.at(0, 1),
                        NativeRelationsScene.UNARMED)
                .market(1, List.of(GoodsType.CORN), List.of(GoodsType.MUSKET));
    }

    @Test
    void unknownNationsAndSettlementsStayHidden() {
        final Model model = NativeRelationsScene.island(NativeNation.ARAWAK)
                .toModel();
        final NativeAdvisorReport report = report(model, DUTCH);
        assertTrue(report.getKnownNations().isEmpty());
        assertTrue(report.getSettlements().isEmpty());
        final NativeSettlement settlement = NativeRelationsScene
                .settlement(model);
        assertTrue(
                model.getNativeSettlementKnownTo(model.getPlayerByName(DUTCH),
                        settlement.getLocation()).isEmpty());
    }

    @Test
    void learnedSettlementNamesItsNationAndRelationBeforeAnyChiefVisit() {
        final Model model = learnedByDutch().toModel();
        final NativeAdvisorReport report = report(model, DUTCH);
        assertEquals(List.of(NativeNation.ARAWAK), report.getKnownNations());
        assertEquals(1, report.getSettlements().size());
        final NativeAdvisorReport.Settlement row = report.getSettlements()
                .get(0);
        assertEquals(NativeNation.ARAWAK, row.getSettlement().getNation());
        assertEquals(NativeRelationsScene.settlement(model).getLocation(),
                row.getSettlement().getLocation());
        assertEquals(NativeHostility.CONTENT, row.getRelation().getAttitude());
        assertTrue(row.getChiefFacts().isEmpty());
    }

    @Test
    void landContactNamesANationWithoutAnyLearnedSettlement() {
        final Model model = NativeRelationsScene.island(NativeNation.ARAWAK)
                .contact(EuropeanNation.DUTCH, NativeNation.TUPI).toModel();
        final NativeAdvisorReport report = report(model, DUTCH);
        assertEquals(List.of(NativeNation.TUPI), report.getKnownNations());
        assertTrue(report.getSettlements().isEmpty());
    }

    @Test
    void chiefFactsAppearOnlyAfterTheInformationOutcome() {
        final Model before = learnedByDutch().toModel();
        assertTrue(report(before, DUTCH).getSettlements().get(0).getChiefFacts()
                .isEmpty());

        final Model informed = learnedByDutch().chiefOfFirst(
                EuropeanNation.DUTCH, NativeChiefOutcome.INFORMATION).toModel();
        final NativeChiefResult.Information facts = report(informed, DUTCH)
                .getSettlements().get(0).getChiefFacts().orElseThrow();
        assertEquals(List.of(GoodsType.CORN), facts.getOfferedGoods());
        assertEquals(List.of(GoodsType.MUSKET), facts.getWantedGoods());
        assertEquals(UnitType.EXPERT_FARMER, facts.getTeachableSkill());

        final Model gold = learnedByDutch()
                .chiefOfFirst(EuropeanNation.DUTCH, NativeChiefOutcome.GOLD)
                .toModel();
        assertTrue(report(gold, DUTCH).getSettlements().get(0).getChiefFacts()
                .isEmpty());
    }

    @Test
    void anotherEuropeanSeesNeitherTheDiscoveryNorTheChiefFacts() {
        final Model model = learnedByDutch()
                .unit(FRANCE, UnitType.COLONIST, NativeRelationsScene.at(1, 0),
                        NativeRelationsScene.UNARMED)
                .chiefOfFirst(EuropeanNation.DUTCH,
                        NativeChiefOutcome.INFORMATION)
                .toModel();
        final NativeSettlement settlement = NativeRelationsScene
                .settlement(model);
        assertTrue(report(model, DUTCH).getSettlements().get(0).getChiefFacts()
                .isPresent());

        // The French colonist sees the same settlement, yet the Dutch chief
        // told nothing to France.
        final NativeAdvisorReport french = report(model, FRANCE);
        assertEquals(List.of(NativeNation.ARAWAK), french.getKnownNations());
        assertTrue(french.getSettlements().get(0).getChiefFacts().isEmpty());

        final Model onlyDutch = learnedByDutch().toModel();
        assertTrue(report(onlyDutch, FRANCE).getKnownNations().isEmpty());
        assertTrue(report(onlyDutch, FRANCE).getSettlements().isEmpty());
        assertTrue(onlyDutch
                .getNativeSettlementKnownTo(onlyDutch.getPlayerByName(FRANCE),
                        settlement.getLocation())
                .isEmpty());
    }

    @Test
    void reopeningReflectsAChangedAttitude() {
        final Model model = learnedByDutch().toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        assertEquals(NativeHostility.CONTENT, report(model, DUTCH)
                .getSettlements().get(0).getRelation().getAttitude());
        model.addNativeTribalAnger(NativeNation.ARAWAK, dutch, 30);
        assertEquals(NativeHostility.UNEASY, report(model, DUTCH)
                .getSettlements().get(0).getRelation().getAttitude());
    }

    @Test
    void repeatedReadsChangeNoStateAndSurviveReload() {
        final Model model = learnedByDutch().chiefOfFirst(EuropeanNation.DUTCH,
                NativeChiefOutcome.INFORMATION).toModel();
        final Player dutch = model.getPlayerByName(DUTCH);
        final long randomBefore = model.save().getCombatRandomState()
                .longValue();
        final int learnedBefore = model.getNativeSettlementsKnownTo(dutch)
                .size();
        final NativeAdvisorReport first = report(model, DUTCH);
        final NativeAdvisorReport second = report(model, DUTCH);
        assertEquals(first.getKnownNations(), second.getKnownNations());
        assertEquals(
                first.getSettlements().get(0).getChiefFacts().orElseThrow()
                        .getOfferedGoods(),
                second.getSettlements().get(0).getChiefFacts().orElseThrow()
                        .getOfferedGoods());
        assertEquals(randomBefore,
                model.save().getCombatRandomState().longValue());
        assertEquals(learnedBefore,
                model.getNativeSettlementsKnownTo(dutch).size());
        // Reading the report spends no chief visit and records no outcome.
        assertEquals(NativeChiefOutcome.INFORMATION,
                NativeRelationsScene.settlement(model)
                        .findChiefEncounter(EuropeanNation.DUTCH)
                        .orElseThrow());

        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeAdvisorReport restored = report(reloaded, DUTCH);
        assertEquals(List.of(NativeNation.ARAWAK), restored.getKnownNations());
        final NativeAdvisorReport.Settlement row = restored.getSettlements()
                .get(0);
        assertEquals(first.getSettlements().get(0).getRelation().getAttitude(),
                row.getRelation().getAttitude());
        assertEquals(List.of(GoodsType.CORN),
                row.getChiefFacts().orElseThrow().getOfferedGoods());
    }

}
