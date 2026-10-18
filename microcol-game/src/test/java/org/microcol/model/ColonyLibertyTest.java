package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Local liberty, population-weighted national support and the completed
 * resident-exit boundary of R-0100.
 */
class ColonyLibertyTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private GameModelPo data() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        return data;
    }

    private ColonyPo savedColony(final GameModelPo data, final int id) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void giveBolivar(final GameModelPo data) {
        giveBolivar(data, "Dutch");
    }

    private void giveBolivar(final GameModelPo data, final String nation) {
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> nation.equals(player.getName()))
                .findFirst().orElseThrow();
        // A fully valid active Congress: Bolivar is the only member and every
        // category offers exactly one other founder.
        final CongressPo congress = dutch.getCongress();
        congress.setActivated(true);
        congress.setGeneration(1);
        congress.setBells(0);
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        congress.setMembers(List.of(Founder.SIMON_BOLIVAR));
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder founder : Founder.values()) {
                if (founder.getCategory() == category
                        && founder != Founder.SIMON_BOLIVAR) {
                    candidates.add(founder);
                    break;
                }
            }
        }
        congress.setCandidates(candidates);
    }

    private long capacity(final Colony colony) {
        return 200L * colony.getPopulation();
    }

    @Test
    void unequalColoniesAreWeightedByPopulationNotByDisplayedPercent() {
        // 2 residents with 200 liberty (50 %) and 8 residents with 400 (25 %).
        final RebelSupport small = new RebelSupport(200, 400);
        final RebelSupport large = new RebelSupport(400, 1600);
        final RebelSupport national = small.plus(large);
        assertEquals(600, national.getNumerator());
        assertEquals(2000, national.getDenominator());
        assertEquals(10, national.getPopulation());
        assertEquals(30, national.getDisplayPercent());
        // One resident moves from the small to the large colony.
        final RebelSupport moved = new RebelSupport(200, 200)
                .plus(new RebelSupport(400, 1800));
        assertEquals(100, new RebelSupport(200, 200).getDisplayPercent());
        assertEquals(22, new RebelSupport(400, 1800).getDisplayPercent());
        assertEquals(30, moved.getDisplayPercent());
        assertEquals(national, moved);
    }

    @Test
    void noResidentsGiveZeroSupportAndMeetOnlyAZeroThreshold() {
        assertEquals(0, RebelSupport.NONE.getDisplayPercent());
        assertEquals(0, RebelSupport.NONE.getPopulation());
        assertTrue(RebelSupport.NONE.isAtLeastPercent(0));
        assertFalse(RebelSupport.NONE.isAtLeastPercent(1));
        assertFalse(RebelSupport.NONE.isAtLeastPercent(50));
        assertEquals(RebelSupport.NONE,
                RebelSupport.NONE.plus(RebelSupport.NONE));
    }

    @Test
    void thresholdComparisonIsExactAndNeverUsesTheRoundedDisplay() {
        final RebelSupport justBelow = new RebelSupport(399, 800);
        assertEquals(49, justBelow.getDisplayPercent());
        assertFalse(justBelow.isAtLeastPercent(50));
        final RebelSupport exactly = new RebelSupport(400, 800);
        assertEquals(50, exactly.getDisplayPercent());
        assertTrue(exactly.isAtLeastPercent(50));
        // 49.75 % displays as 49 and does not reach 50 %.
        final RebelSupport almost = new RebelSupport(199, 400);
        assertEquals(49, almost.getDisplayPercent());
        assertFalse(almost.isAtLeastPercent(50));
        assertTrue(almost.isAtLeastPercent(49));
        assertTrue(new RebelSupport(400, 400).isAtLeastPercent(100));
    }

    @Test
    void libertyAndAffiliationAreSavedAndRestored() {
        final GameModelPo data = data();
        savedColony(data, 0).setLiberty(123L);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(123L, colony.getLiberty());
        assertSame(model.getPlayerByName("Dutch"),
                colony.getLastColonialAffiliation());
        final ColonyPo saved = model.save().getColonies().stream()
                .filter(po -> po.getId() == 0).findFirst().orElseThrow();
        assertEquals(123L, saved.getLiberty());
        assertEquals("Dutch", saved.getLastColonialAffiliation());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(load(wrap(model.save())).save()));
    }

    private GameModelPo wrap(final org.microcol.model.store.ModelPo model) {
        final GameModelPo out = new GameModelPoDao().loadFromClassPath(MAP);
        out.setModel(model);
        return out;
    }

    static Stream<Consumer<ColonyPo>> invalidSavedData() {
        return Stream.of(colony -> colony.setLiberty(null),
                colony -> colony.setLiberty(-1L),
                // Colony 0 has five residents; 1001 exceeds 200 per resident.
                colony -> colony.setLiberty(1001L),
                colony -> colony.setLiberty(Long.MAX_VALUE),
                colony -> colony.setLastColonialAffiliation(null),
                colony -> colony.setLastColonialAffiliation("Nobody"),
                colony -> colony.setLastColonialAffiliation("Dutch's King"));
    }

    @ParameterizedTest
    @MethodSource("invalidSavedData")
    void invalidOrMissingSavedLibertyOrAffiliationIsRejected(
            final Consumer<ColonyPo> corruption) {
        final GameModelPo data = data();
        corruption.accept(savedColony(data, 0));
        assertThrows(IllegalArgumentException.class, () -> load(data));
    }

    @Test
    void localAndNationalSupportUseResidentsAndStoredLiberty() {
        final GameModelPo data = data();
        savedColony(data, 0).setLiberty(1000L);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final String before = new Gson().toJson(model.save());
        assertEquals(5, colony.getPopulation());
        assertEquals(new RebelSupport(1000, 1000), colony.getRebelSupport());
        long population = 0;
        for (final Colony own : model.getColonies(colony.getOwner())) {
            population += own.getPopulation();
        }
        final RebelSupport national = colony.getOwner()
                .getNationalRebelSupport();
        assertEquals(1000, national.getNumerator());
        assertEquals(200 * population, national.getDenominator());
        assertEquals((int) (100_000 / (200 * population)),
                national.getDisplayPercent());
        // Reading never mutates anything.
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void bolivarAddsTwentyPointsPerColonyAndCapsAtFull() {
        final GameModelPo data = data();
        giveBolivar(data);
        savedColony(data, 0).setLiberty(900L);
        final Model model = load(data);
        final Colony full = model.getColonyById(0).orElseThrow();
        final Colony bonusOnly = model.getColonyById(1).orElseThrow();
        // 900 + 40 per resident is clamped to full support.
        assertEquals(new RebelSupport(capacity(full), capacity(full)),
                full.getRebelSupport());
        assertEquals(new RebelSupport(40L * bonusOnly.getPopulation(),
                capacity(bonusOnly)), bonusOnly.getRebelSupport());
        assertEquals(20, bonusOnly.getRebelSupport().getDisplayPercent());
        // The bonus is not stored as liberty.
        assertEquals(0L, bonusOnly.getLiberty());
    }

    @Test
    void sameColonyReassignmentNeverCapsButCompletedExitAndRemovalDo() {
        final GameModelPo data = data();
        savedColony(data, 0).setLiberty(1000L);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Unit worker = colony.getUnitsInsideColony().get(0);
        // Change job inside the colony: transient detachment loses nothing.
        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(5, colony.getPopulation());
        assertEquals(1000L, colony.getLiberty());
        // A resident leaving for the outside garrison is a completed exit.
        worker.placeToLocation(colony.getLocation());
        assertEquals(4, colony.getPopulation());
        assertEquals(800L, colony.getLiberty());
        // Removing a resident (famine path) also applies the new cap.
        final Unit victim = colony.getUnitsInsideColony().get(0);
        victim.getPlace().destroy();
        model.destroyUnit(victim);
        assertEquals(3, colony.getPopulation());
        assertEquals(600L, colony.getLiberty());
        // A cap that is not exceeded changes nothing.
        final Unit other = colony.getUnitsInsideColony().get(0);
        other.placeToLocation(colony.getLocation());
        assertEquals(400L, colony.getLiberty());
    }

    @Test
    void libertyBelowTheNewCapIsKeptWhenAResidentLeaves() {
        final GameModelPo data = data();
        savedColony(data, 0).setLiberty(300L);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getUnitsInsideColony().get(0)
                .placeToLocation(colony.getLocation());
        assertEquals(300L, colony.getLiberty());
    }

    @Test
    void producedBellsAreAddedOnceAtTurnStartAndRepeatedReadsAddNothing() {
        final GameModelPo data = data();
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getUnitsInsideColony().get(0).placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        final int bells = colony.getGoodsStats().getStatsByType(GoodsType.BELL)
                .getRowProduction();
        assertTrue(bells > 0, "The town hall worker must make bells");
        final String before = new Gson().toJson(model.save());
        colony.getRebelSupport();
        colony.getOwner().getNationalRebelSupport();
        colony.getGoodsStats();
        assertEquals(before, new Gson().toJson(model.save()));
        // Stored warehouse bells are not new production.
        colony.getWarehouse().setGoods(Goods.of(GoodsType.BELL, 77));
        colony.startTurn();
        assertEquals(Math.min(bells, capacity(colony)), colony.getLiberty());
        final Model resumed = load(wrap(model.save()));
        assertEquals(colony.getLiberty(),
                resumed.getColonyById(0).orElseThrow().getLiberty());
    }

    @Test
    void crownOccupationKeepsAffiliationAndLibertyAndSubtractsProducedBells() {
        final GameModelPo data = data();
        giveBolivar(data);
        savedColony(data, 0).setLiberty(400L);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Player dutch = colony.getOwner();
        final Player king = model.getPlayerByName("Dutch's King");
        colony.getUnitsInsideColony().get(0).placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        final RebelSupport before = colony.getRebelSupport();
        final Unit force = model.addUnitOutSideColony(colony);
        force.takeOver(king);
        ColonyBattles.capture(model, colony, force);
        assertSame(king, colony.getOwner());
        assertSame(dutch, colony.getLastColonialAffiliation());
        assertEquals(400L, colony.getLiberty());
        // Dutch still count this colony and its Bolivar bonus.
        assertEquals(before, colony.getRebelSupport());
        assertEquals(dutch.getNationalRebelSupport().getNumerator(),
                before.getNumerator() + model.getColonies(dutch).stream()
                        .mapToLong(c -> c.getRebelSupport().getNumerator())
                        .sum());
        final Model saved = load(wrap(model.save()));
        assertSame(saved.getPlayerByName("Dutch"), saved.getColonyById(0)
                .orElseThrow().getLastColonialAffiliation());
        assertSame(saved.getPlayerByName("Dutch's King"),
                saved.getColonyById(0).orElseThrow().getOwner());
        final int bells = Math.max(0, colony.getGoodsStats()
                .getStatsByType(GoodsType.BELL).getRowProduction());
        colony.startTurn();
        assertEquals(Math.max(0, 400L - bells), colony.getLiberty());
        // Liberation by the Dutch keeps liberty and the same affiliation.
        final long remaining = colony.getLiberty();
        force.takeOver(dutch);
        ColonyBattles.capture(model, colony, force);
        assertSame(dutch, colony.getOwner());
        assertSame(dutch, colony.getLastColonialAffiliation());
        assertEquals(remaining, colony.getLiberty());
    }

    /** French Quebec (2 residents, liberty 200) at war with the Dutch. */
    private Model freePlayAtWar(final String bolivarNation) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        savedColony(data, 0).setLiberty(200L);
        giveBolivar(data, bolivarNation);
        data.getModel().getGameManager().setGameStarted(true);
        // France moves first so its own new resident can be given a job.
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Model model = load(data);
        final Colony quebec = model.getColonyById(0).orElseThrow();
        // Quebec starts with one working resident; a new colonist born on its
        // square takes a building job to make the two-resident colony.
        assertEquals(1, quebec.getPopulation());
        final Unit newcomer = model.addUnitOutSideColony(quebec);
        newcomer.placeToColonyStructureSlot(freeSlot(quebec, newcomer));
        assertEquals(2, quebec.getPopulation());
        model.declareHostilities(model.getPlayerByName("France"),
                model.getPlayerByName("Dutch"));
        // The armed French colonist guarding the square would block capture.
        model.getUnitById(6).placeToLocation(Location.of(18, 10));
        return model;
    }

    /** An armed Dutch colonist on the colony square, as after a march. */
    private Unit armedForce(final Model model, final Colony colony,
            final Player nation) {
        final Unit force = model.addUnitOutSideColony(colony);
        force.takeOver(nation);
        ((UnitFreeColonist) force).setHoldingGuns(true);
        return force;
    }

    private void conquerThenOccupy(final String bolivarNation,
            final int beforePercent, final int afterPercent) {
        final Model model = freePlayAtWar(bolivarNation);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final Player king = model.getPlayerByName("Dutch's King");
        final Colony quebec = model.getColonyById(0).orElseThrow();
        final Colony montreal = model.getColonyById(1).orElseThrow();
        assertSame(france, quebec.getOwner());
        assertEquals(2, quebec.getPopulation());
        assertEquals(beforePercent,
                quebec.getRebelSupport().getDisplayPercent());
        // A European conqueror keeps liberty and takes the affiliation, so
        // its own Bolivar membership now decides the bonus.
        final Unit invader = armedForce(model, quebec, dutch);
        ColonyBattles.capture(model, quebec, invader);
        assertSame(dutch, quebec.getOwner());
        assertSame(dutch, quebec.getLastColonialAffiliation());
        assertEquals(200L, quebec.getLiberty());
        assertEquals(2, quebec.getPopulation());
        assertEquals(afterPercent,
                quebec.getRebelSupport().getDisplayPercent());
        assertEquals(quebec.getRebelSupport(), dutch.getNationalRebelSupport());
        assertEquals(montreal.getRebelSupport(),
                france.getNationalRebelSupport());
        // The Crown then occupies it: affiliation, bonus and liberty remain.
        invader.getPlace().destroy();
        model.destroyUnit(invader);
        final Unit royal = model.addUnitOutSideColony(quebec);
        royal.takeOver(king);
        ColonyBattles.capture(model, quebec, royal);
        assertSame(king, quebec.getOwner());
        assertSame(dutch, quebec.getLastColonialAffiliation());
        assertEquals(200L, quebec.getLiberty());
        assertEquals(afterPercent,
                quebec.getRebelSupport().getDisplayPercent());
        assertEquals(quebec.getRebelSupport(), dutch.getNationalRebelSupport());
        assertEquals(montreal.getRebelSupport(),
                france.getNationalRebelSupport());
        // Save and load keep the Crown owner and the Dutch affiliation.
        final Model restored = load(wrap(model.save()));
        final Colony reloaded = restored.getColonyById(0).orElseThrow();
        assertSame(restored.getPlayerByName("Dutch's King"),
                reloaded.getOwner());
        assertSame(restored.getPlayerByName("Dutch"),
                reloaded.getLastColonialAffiliation());
        assertEquals(afterPercent,
                reloaded.getRebelSupport().getDisplayPercent());
    }

    @Test
    void conquerorWithBolivarGainsTheBonusAndTheCrownKeepsIt() {
        // France has no Bolivar: 200 of 400 is 50 %; the Dutch add 20 points.
        conquerThenOccupy("Dutch", 50, 70);
    }

    @Test
    void conquerorWithoutBolivarLosesTheFormerOwnersBonusAndTheCrownKeepsNone() {
        // France has Bolivar: 70 %; the Dutch have none, so 50 % remain.
        conquerThenOccupy("France", 70, 50);
    }

    @Test
    void movingOneResidentBetweenColoniesKeepsLibertyAndWeightsNationalSupport() {
        final GameModelPo data = data();
        savedColony(data, 0).setLiberty(200L);
        savedColony(data, 2).setLiberty(400L);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(2).orElseThrow();
        // Reach 2 and 8 residents with real operations: one resident of the
        // first colony and the single resident of the third leave, travel to
        // the second colony and take jobs there.
        assertEquals(5, first.getPopulation());
        assertEquals(6, second.getPopulation());
        final Colony third = model.getColonyById(1).orElseThrow();
        assertEquals(1, third.getPopulation());
        transfer(first.getUnitsInsideColony().get(0), second);
        transfer(third.getUnitsInsideColony().get(0), second);
        assertFalse(model.getColonyById(1).isPresent());
        for (int i = 0; i < 2; i++) {
            final Unit leaver = first.getUnitsInsideColony().get(0);
            leaver.getPlace().destroy();
            model.destroyUnit(leaver);
        }
        assertEquals(2, first.getPopulation());
        assertEquals(8, second.getPopulation());
        assertEquals(200L, first.getLiberty());
        assertEquals(400L, second.getLiberty());
        assertEquals(50, first.getRebelSupport().getDisplayPercent());
        assertEquals(25, second.getRebelSupport().getDisplayPercent());
        assertEquals(new RebelSupport(600, 2000),
                dutch.getNationalRebelSupport());
        assertEquals(30, dutch.getNationalRebelSupport().getDisplayPercent());
        // One resident leaves the first colony: while on the road only the
        // eight remaining workers of the second colony count with the one left.
        final Unit mover = first.getUnitsInsideColony().get(0);
        mover.placeToLocation(first.getLocation());
        assertEquals(1, first.getPopulation());
        assertEquals(8, second.getPopulation());
        assertEquals(new RebelSupport(600, 1800),
                dutch.getNationalRebelSupport());
        assertEquals(33, dutch.getNationalRebelSupport().getDisplayPercent());
        // It arrives and takes a job, so liberty did not travel with it.
        mover.placeToLocation(second.getLocation());
        mover.placeToColonyStructureSlot(freeSlot(second, mover));
        assertEquals(1, first.getPopulation());
        assertEquals(9, second.getPopulation());
        assertEquals(200L, first.getLiberty());
        assertEquals(400L, second.getLiberty());
        assertEquals(100, first.getRebelSupport().getDisplayPercent());
        assertEquals(22, second.getRebelSupport().getDisplayPercent());
        assertEquals(new RebelSupport(600, 2000),
                dutch.getNationalRebelSupport());
        assertEquals(30, dutch.getNationalRebelSupport().getDisplayPercent());
        assertTrue(dutch.getNationalRebelSupport().isAtLeastPercent(30));
        assertFalse(dutch.getNationalRebelSupport().isAtLeastPercent(31));
    }

    /**
     * A resident leaves its colony, stands on the target colony square and
     * takes a job there, as a player does with the exit, move and drag steps.
     */
    private void transfer(final Unit resident, final Colony target) {
        final Colony source = resident.isAtPlaceColonyField()
                ? resident.getPlaceColonyField().getColony()
                : resident.getPlaceConstruction().getColony();
        resident.placeToLocation(source.getLocation());
        resident.placeToLocation(target.getLocation());
        resident.placeToColonyStructureSlot(freeSlot(target, resident));
    }

    private ConstructionSlot freeSlot(final Colony colony, final Unit worker) {
        for (final Construction construction : colony.getConstructions()) {
            for (final ConstructionSlot slot : construction
                    .getConstructionSlots()) {
                if (slot.isEmpty() && slot.canAccept(worker)) {
                    return slot;
                }
            }
        }
        throw new AssertionError("No free job in " + colony.getName());
    }
}
