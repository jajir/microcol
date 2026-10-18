package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;

/**
 * Rebel support bonus and Tory penalty of R-0101. Expected values come from the
 * hand-calculated tables in the rules, not from the production code.
 */
class PoliticalProductionTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    /** Tory count at which each difficulty starts the -1 penalty. */
    private static final Map<Difficulty, Integer> THRESHOLD = Map.of(
            Difficulty.DISCOVERER, 10, Difficulty.EXPLORER, 9,
            Difficulty.CONQUISTADOR, 8, Difficulty.GOVERNOR, 7,
            Difficulty.VICEROY, 6);

    private PoliticalProduction of(final long numerator, final int residents,
            final Difficulty difficulty) {
        return PoliticalProduction
                .of(new RebelSupport(numerator, 200L * residents), difficulty);
    }

    @Test
    void bonusBoundariesAreExactForTenResidents() {
        // {numerator out of 2000, bonus, Tory count}
        final long[][] table = { { 0, 0, 10 }, { 980, 0, 6 }, { 999, 0, 6 },
                { 1000, 1, 5 }, { 1980, 1, 1 }, { 1999, 1, 1 },
                { 2000, 2, 0 } };
        for (final long[] row : table) {
            final PoliticalProduction political = of(row[0], 10,
                    Difficulty.DISCOVERER);
            assertEquals(row[1], political.getBonus(), "bonus at " + row[0]);
            assertEquals(row[2], political.getToryCount(),
                    "Tories at " + row[0]);
        }
    }

    @Test
    void roundedDisplayNeverTriggersTheBonus() {
        // 1199 / 2400 is 49.96 %, displayed as 49 %, and gives no bonus.
        final RebelSupport almostHalf = new RebelSupport(1199, 2400);
        assertEquals(49, almostHalf.getDisplayPercent());
        assertEquals(0, PoliticalProduction.of(almostHalf, Difficulty.VICEROY)
                .getBonus());
        // 2399 / 2400 is displayed as 99 % and is below full support.
        assertEquals(1,
                PoliticalProduction
                        .of(new RebelSupport(2399, 2400), Difficulty.VICEROY)
                        .getBonus());
    }

    @Test
    void toryThresholdsDependOnDifficultyAndStartAtTheCount() {
        // Twelve residents; Tory count t means 12 - t rebels.
        for (final Difficulty difficulty : Difficulty.values()) {
            final int threshold = THRESHOLD.get(difficulty);
            assertEquals(threshold,
                    PoliticalProduction.getToryThreshold(difficulty));
            for (final int tories : List.of(threshold - 1, threshold,
                    threshold + 1)) {
                final int rebels = 12 - tories;
                final PoliticalProduction political = of(rebels * 200L, 12,
                        difficulty);
                assertEquals(tories, political.getToryCount());
                assertEquals(tories >= threshold ? -1 : 0,
                        political.getPenalty(),
                        difficulty + " with " + tories + " Tories");
                final int bonus = rebels == 12 ? 2 : rebels >= 6 ? 1 : 0;
                assertEquals(bonus, political.getBonus());
                // Combined once; there is no extra -2 step.
                assertEquals(bonus + political.getPenalty(),
                        political.getNetChange());
            }
        }
    }

    @Test
    void conquistadorExampleFromTheRules() {
        // N=10 and 20 % support: 8 Tories, -1. 50 % support: 5 Tories, +1.
        final PoliticalProduction low = of(400, 10, Difficulty.CONQUISTADOR);
        assertEquals(8, low.getToryCount());
        assertEquals(-1, low.getNetChange());
        final PoliticalProduction half = of(1000, 10, Difficulty.CONQUISTADOR);
        assertEquals(5, half.getToryCount());
        assertEquals(1, half.getNetChange());
    }

    @Test
    void bonusAndPenaltyCombineOnceAndOutputNeverGoesNegative() {
        // 20 residents, 50 %: bonus +1 and 10 Tories hit the Discoverer line.
        final PoliticalProduction both = of(2000, 20, Difficulty.DISCOVERER);
        assertEquals(1, both.getBonus());
        assertEquals(-1, both.getPenalty());
        assertEquals(0, both.getNetChange());
        assertEquals(5, both.apply(3, 5));
        // Penalty only: a servant job that yields nothing stays at zero.
        final PoliticalProduction penalty = of(0, 10, Difficulty.CONQUISTADOR);
        assertEquals(-1, penalty.getNetChange());
        assertEquals(0, penalty.apply(1, 0));
        assertEquals(1, penalty.apply(1, 2));
        // Full support gives +2 after the expert doubling: 3 -> 6 -> 8.
        assertEquals(8, of(2000, 10, Difficulty.VICEROY).apply(3, 6));
    }

    @Test
    void zeroBaseYieldNeverReceivesAChange() {
        final PoliticalProduction full = of(2000, 10, Difficulty.VICEROY);
        assertEquals(0, full.apply(0, 0));
        assertEquals(0, of(0, 10, Difficulty.VICEROY).apply(0, 0));
        assertEquals(Goods.of(GoodsType.ORE, 0), full
                .apply(Goods.of(GoodsType.ORE, 0), Goods.of(GoodsType.ORE, 0)));
    }

    @Test
    void emptyColonyHasNoBonusAndNoPenalty() {
        final PoliticalProduction none = PoliticalProduction
                .of(RebelSupport.NONE, Difficulty.VICEROY);
        assertEquals(0, none.getBonus());
        assertEquals(0, none.getToryCount());
        assertEquals(0, none.getNetChange());
    }

    @Test
    void scarceInputLimitsTheBoostedBudgetAndConsumesOnlyWhatIsUsed() {
        final ConstructionType type = ConstructionType.BLACKSMITHS_SHOP;
        final int output = type.getProductionPerTurn().orElseThrow()
                .getAmount();
        final int input = type.getConsumptionPerTurn().orElseThrow()
                .getAmount();
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit ordinary = model.getUnitById(0);
        final PoliticalProduction full = of(2000, 10, Difficulty.CONQUISTADOR);
        // An ordinary colonist makes base + 2 and needs the matching input.
        final int boosted = output + 2;
        final int needed = (boosted * input + output - 1) / output;
        assertEquals(needed, ConstructionSlot
                .getPotentialInput(type, ordinary, full).getAmount());
        // Plenty of input: the whole boosted output is produced.
        final ConstructionTurnProduction plenty = ConstructionSlot
                .productionForBudget(type, boosted, needed + 50);
        assertEquals(boosted,
                plenty.getProducedGoods().orElseThrow().getAmount());
        assertEquals(needed,
                plenty.getConsumedGoods().orElseThrow().getAmount());
        assertFalse(plenty.getBlockedGoods().isPresent());
        // One ore short: output follows the real input, the rest is blocked.
        final int available = needed - 1;
        final int possible = available * output / input;
        final ConstructionTurnProduction scarce = ConstructionSlot
                .productionForBudget(type, boosted, available);
        assertEquals(possible,
                scarce.getProducedGoods().orElseThrow().getAmount());
        assertEquals((possible * input + output - 1) / output,
                scarce.getConsumedGoods().orElseThrow().getAmount());
        assertEquals(boosted - possible,
                scarce.getBlockedGoods().orElseThrow().getAmount());
    }

    // --- real colonies ---

    private GameModelPo data(final Difficulty difficulty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().setDifficulty(difficulty);
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        return data;
    }

    private void liberty(final GameModelPo data, final int id,
            final long liberty) {
        final ColonyPo colony = data.getModel().getColonies().stream()
                .filter(po -> po.getId() == id).findFirst().orElseThrow();
        colony.setLiberty(liberty);
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Output a worked field must give: base, worker modifier, then change. */
    private int expectedFieldOutput(final ColonyField field, final int change) {
        final Unit worker = field.getUnit().orElseThrow();
        final GoodsType goods = worker.getPlaceColonyField()
                .getProducedGoodsType();
        final int base = field.getGoodsTypeProduction(goods);
        final int worked = (int) (base * worker.getType()
                .getFieldProductionModifier(goods, field.getTerrainType()));
        return base > 0 ? Math.max(0, worked + change) : worked;
    }

    private int checkFields(final Colony colony, final int change) {
        int checked = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            if (field.getUnit().isPresent()) {
                assertEquals(expectedFieldOutput(field, change),
                        field.getProduction().orElseThrow().getAmount(),
                        "field " + field.getDirection());
                checked++;
            }
        }
        return checked;
    }

    @Test
    void workedFieldsFollowTheSupportBoundariesOfARealColony() {
        // Colony 0 has five residents: 1000 points are full support.
        final long[][] table = { { 0, 0 }, { 490, 0 }, { 499, 0 }, { 500, 1 },
                { 990, 1 }, { 999, 1 }, { 1000, 2 } };
        int fieldWorkers = 0;
        for (final long[] row : table) {
            final GameModelPo data = data(Difficulty.CONQUISTADOR);
            liberty(data, 0, row[0]);
            final Colony colony = load(data).getColonyById(0).orElseThrow();
            assertEquals(5, colony.getPopulation());
            assertEquals(row[1], colony.getPoliticalProduction().getBonus());
            fieldWorkers = Math.max(fieldWorkers,
                    checkFields(colony, (int) row[1]));
        }
        assertTrue(fieldWorkers > 0, "Colony 0 must have a field worker");
    }

    @Test
    void toryPenaltyAppliesOnlyAtTheDifficultyThreshold() {
        // Colony 2 has six residents and no liberty: six Tories.
        for (final Difficulty difficulty : Difficulty.values()) {
            final Colony colony = load(data(difficulty)).getColonyById(2)
                    .orElseThrow();
            assertEquals(6, colony.getPopulation());
            final int penalty = THRESHOLD.get(difficulty) <= 6 ? -1 : 0;
            assertEquals(6, colony.getPoliticalProduction().getToryCount());
            assertEquals(penalty, colony.getPoliticalProduction().getPenalty());
            checkFields(colony, penalty);
        }
    }

    @Test
    void bellsCreditedInAnUpdateAffectOnlyTheNextUpdate() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR);
        // One liberty point below the 50 % line of five residents.
        liberty(data, 0, 499);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getUnitsInsideColony().get(0).placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(0, colony.getPoliticalProduction().getBonus());
        final int before = colony.getGoodsStats().getStatsByType(GoodsType.BELL)
                .getRowProduction();
        assertTrue(before > 0, "The town hall worker must make bells");
        colony.startTurn();
        // The update used the snapshot taken before crediting its own bells.
        assertEquals(499 + before, colony.getLiberty());
        assertEquals(1, colony.getPoliticalProduction().getBonus());
        assertTrue(colony.getGoodsStats().getStatsByType(GoodsType.BELL)
                .getRowProduction() > before);
    }

    @Test
    void outsideWorkerPreviewsTheNewResidentCountButAResidentDoesNot() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR);
        // 500 of 1000 is exactly 50 %; a sixth resident dilutes it to 41.7 %.
        liberty(data, 0, 500);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(1, colony.getPoliticalProduction().getBonus());
        final Unit resident = colony.getUnitsInsideColony().get(0);
        assertEquals(1, colony.getPoliticalProductionFor(resident).getBonus());
        final Unit outside = model.addUnitOutSideColony(colony);
        final PoliticalProduction prospective = colony
                .getPoliticalProductionFor(outside);
        assertEquals(6, prospective.getPopulation());
        assertEquals(0, prospective.getBonus());
        // Previewing does not change the colony.
        assertEquals(5, colony.getPopulation());
        assertEquals(1, colony.getPoliticalProduction().getBonus());
        // The job choices expose the same distinction.
        final Optional<Goods> residentBells = bellsChoice(model, colony,
                resident);
        final Optional<Goods> outsideBells = bellsChoice(model, colony,
                outside);
        assertTrue(residentBells.isPresent(), "resident town hall choice");
        assertTrue(outsideBells.isPresent(), "outside town hall choice");
        assertTrue(residentBells.get().getAmount() > outsideBells.get()
                .getAmount());
    }

    private Optional<Goods> bellsChoice(final Model model, final Colony colony,
            final Unit worker) {
        return ColonyJobChoice.forWorker(model, colony, worker).stream().filter(
                choice -> choice.getKind() == ColonyJobChoice.Kind.INDOOR
                        && choice.getGoodsType() == GoodsType.BELL)
                .findFirst().flatMap(ColonyJobChoice::getPotential);
    }

    @Test
    void residentLeavingChangesFreshInputsButNotACapturedSnapshot() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR);
        liberty(data, 0, 500);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        final PoliticalProduction snapshot = colony.getPoliticalProduction();
        assertEquals(1, snapshot.getBonus());
        // A completed exit caps liberty at 800 for four residents: 62.5 %.
        colony.getUnitsInsideColony().get(0)
                .placeToLocation(colony.getLocation());
        assertEquals(4, colony.getPopulation());
        assertEquals(5, snapshot.getPopulation());
        assertEquals(new RebelSupport(500, 1000), snapshot.getSupport());
        assertEquals(new RebelSupport(500, 800),
                colony.getPoliticalProduction().getSupport());
    }
}
