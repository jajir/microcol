package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.lang.reflect.Field;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.WorldMapPo;

/**
 * Finite deposits under a real colony field. The worker of colony 0 is placed
 * on a deposit square prepared before loading, at full support where each field
 * job gains +2. Previews never spend a reserve; the committed update spends
 * exactly the extra output it credits, once.
 */
class MineralDepositColonyTest {

    private static final int COLONY_ID = 0;

    /** Worker of the prepared field, an ordinary colonist in the scenario. */
    private static final int WORKER_ID = 0;

    private final GameModelPoDao dao = new GameModelPoDao();

    /** Mission colony at full support, with corn so no update starves. */
    private GameModelPo data() {
        final GameModelPo data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        saved(data).setLiberty(1000L);
        saved(data).getColonyWarehouse().put("CORN", 100);
        return data;
    }

    private ColonyPo saved(final GameModelPo data) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == COLONY_ID).findFirst()
                .orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Set<Location> without(final Set<Location> locations,
            final Location at) {
        final Set<Location> out = new HashSet<>(locations);
        out.remove(at);
        return out;
    }

    /**
     * Put the worker's field on a square with the given terrain and finite
     * deposit, and make the worker produce the given goods there. The square
     * loses any forest, field, road and clearing, so only the deposit remains.
     *
     * @return the deposit square
     */
    private Location prepareDeposit(final GameModelPo data,
            final TerrainType terrainType, final ResourceOverlay overlay,
            final MineralReserve reserve, final GoodsType produced) {
        final ColonyPo colony = saved(data);
        final ColonyFieldPo field = colony.getColonyFields().stream()
                .filter(candidate -> Integer.valueOf(WORKER_ID)
                        .equals(candidate.getWorkerId()))
                .findFirst().orElseThrow();
        field.setProducedGoodsType(produced);
        final Location at = colony.getLocation().add(field.getDirection());
        final WorldMapPo map = data.getModel().getMap();
        final Map<Location, TerrainType> terrain = map.getTerrainMap();
        terrain.put(at, terrainType);
        map.setTerrainType(terrain);
        map.setTrees(without(map.getTreeSet(), at));
        map.setFields(without(map.getFieldSet(), at));
        map.setCleared(without(map.getClearedSet(), at));
        map.setRoads(without(map.getRoadSet(), at));
        final Map<Location, ResourceOverlay> resources = map.getResourceMap();
        resources.put(at, overlay);
        map.setResources(resources);
        final Map<Location, MineralReserve> reserves = new HashMap<>();
        reserves.put(at, reserve);
        map.setMineralReserves(reserves);
        return at;
    }

    /** Ordinary colonists are the default; the specialist is set here. */
    private void makeWorker(final GameModelPo data, final UnitType type) {
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == WORKER_ID).findFirst()
                .orElseThrow().setType(type);
    }

    private static ColonyField fieldOfWorker(final Colony colony) {
        return colony
                .getColonyFields().stream().filter(field -> field.getUnit()
                        .map(Unit::getId).orElse(-1) == WORKER_ID)
                .findFirst().orElseThrow();
    }

    private static int stored(final Colony colony, final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    @Test
    void previewShowsTheRemainingPartialSilverAndSpendsNothing() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(1, 0),
                GoodsType.SILVER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        // Silver 1 plus the deposit 2 is 3 before support; support adds 2, so
        // the job would give 5, but only one unit is left: 3 + 1 = 4.
        assertEquals(Goods.of(GoodsType.SILVER, 4),
                fieldOfWorker(colony).getProduction().orElseThrow());
        assertEquals(4, colony.getGoodsStats().getStatsByType(GoodsType.SILVER)
                .getRowProduction());
        // Repeated preview is read-only.
        colony.getGoodsStats();
        assertEquals(1,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }

    @Test
    void startTurnCreditsTheCappedSilverAndSpendsItOnce() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(1, 0),
                GoodsType.SILVER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        final int before = stored(colony, GoodsType.SILVER);
        colony.startTurn();
        assertEquals(before + 4, stored(colony, GoodsType.SILVER));
        assertEquals(0,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
        // The exhausted field keeps its baseline plus support: 1 + 2.
        assertEquals(Goods.of(GoodsType.SILVER, 3),
                fieldOfWorker(colony).getProduction().orElseThrow());
    }

    @Test
    void specialistLastPartialIsNotMultipliedByTheSpecialist() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(1, 0),
                GoodsType.SILVER);
        makeWorker(data, UnitType.EXPERT_SILVER_MINER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        // Expert without deposit: 1 doubled to 2, plus support 2 gives 4. With
        // deposit: 3 doubled to 6, plus 2 gives 8. The extra 4 is capped at the
        // one remaining unit, so the expert yields 5, not a doubled partial.
        assertEquals(Goods.of(GoodsType.SILVER, 5),
                fieldOfWorker(colony).getProduction().orElseThrow());
        final int before = stored(colony, GoodsType.SILVER);
        colony.startTurn();
        assertEquals(before + 5, stored(colony, GoodsType.SILVER));
        assertEquals(0,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }

    @Test
    void silverOfMixedDepositLeavesItsOreReserveUntouched() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.MIXED_MINERALS, MineralReserve.of(1, 200),
                GoodsType.SILVER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        assertEquals(Goods.of(GoodsType.SILVER, 4),
                fieldOfWorker(colony).getProduction().orElseThrow());
        colony.startTurn();
        assertEquals(0,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
        assertEquals(200,
                model.getMap().getMineralRemainingAt(at, GoodsType.ORE));
    }

    @Test
    void oreOfMixedDepositLeavesItsSilverReserveUntouched() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.MIXED_MINERALS, MineralReserve.of(0, 200),
                GoodsType.ORE);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        // Ore 3 plus deposit 2 is 5 before support, so 7 with support.
        assertEquals(Goods.of(GoodsType.ORE, 7),
                fieldOfWorker(colony).getProduction().orElseThrow());
        colony.startTurn();
        assertEquals(7, stored(colony, GoodsType.ORE));
        assertEquals(198,
                model.getMap().getMineralRemainingAt(at, GoodsType.ORE));
        assertEquals(0,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }

    @Test
    void nonMiningOutputOfADepositSquareSpendsNothing() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(1, 0), GoodsType.ORE);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        // Ore is the mountain's baseline 3 plus support 2; the silver deposit
        // gives no ore bonus.
        assertEquals(Goods.of(GoodsType.ORE, 5),
                fieldOfWorker(colony).getProduction().orElseThrow());
        colony.startTurn();
        assertEquals(5, stored(colony, GoodsType.ORE));
        assertEquals(1,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }

    @Test
    void exhaustedDepositGivesBaselineAndSpendsNothing() {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(0, 0),
                GoodsType.SILVER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        assertEquals(Goods.of(GoodsType.SILVER, 3),
                fieldOfWorker(colony).getProduction().orElseThrow());
        colony.startTurn();
        assertEquals(3, stored(colony, GoodsType.SILVER));
        assertEquals(0,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }

    @ParameterizedTest
    @CsvSource({ "false, 5, 98", "true, 0, 100" })
    void famineDoesNotChangeTheCommittedDepositTake(final boolean minerDies,
            final int expectedSilver, final int expectedReserve)
            throws Exception {
        final GameModelPo data = data();
        final Location at = prepareDeposit(data, TerrainType.MOUNTAIN,
                ResourceOverlay.SILVER, MineralReserve.of(100, 0),
                GoodsType.SILVER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(COLONY_ID).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertTrue(colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        final int residents = colony.getPopulation();
        final Unit victim = colony.getUnitsInsideColony().stream()
                .filter(unit -> (unit.getId() == WORKER_ID) == minerDies)
                .findFirst().orElseThrow();
        final int victimIndex = colony.getUnitsInsideColony().indexOf(victim);
        // Use the same deterministic famine setup as ColonyFamineTest.
        final Field randomField = Colony.class.getDeclaredField("random");
        randomField.setAccessible(true);
        long seed = 0;
        while (new Random(seed).nextInt(residents) != victimIndex) {
            seed++;
        }
        ((Random) randomField.get(colony)).setSeed(seed);
        colony.startTurn();
        assertEquals(!minerDies, model.getAllUnits().stream()
                .anyMatch(unit -> unit.getId() == WORKER_ID));
        assertEquals(residents - 1, colony.getPopulation());
        // Only a surviving miner produces and debits the deposit.
        assertEquals(expectedSilver, stored(colony, GoodsType.SILVER));
        assertEquals(expectedReserve,
                model.getMap().getMineralRemainingAt(at, GoodsType.SILVER));
    }
}
