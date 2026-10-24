package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

class SpecialistProductionTest {

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private UnitPo worker(final GameModelPo data) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
    }

    private static Stream<Arguments> outdoorJobs() {
        return Stream.of(
                Arguments.of(UnitType.EXPERT_FARMER, GoodsType.CORN,
                        TerrainType.GRASSLAND),
                Arguments.of(UnitType.EXPERT_FISHERMAN, GoodsType.CORN,
                        TerrainType.OCEAN),
                Arguments.of(UnitType.EXPERT_FUR_TRAPPER, GoodsType.FUR,
                        TerrainType.GRASSLAND),
                Arguments.of(UnitType.EXPERT_SILVER_MINER, GoodsType.SILVER,
                        TerrainType.MOUNTAIN),
                Arguments.of(UnitType.EXPERT_LUMBERJACK, GoodsType.LUMBER,
                        TerrainType.GRASSLAND),
                Arguments.of(UnitType.EXPERT_ORE_MINER, GoodsType.ORE,
                        TerrainType.HILL),
                Arguments.of(UnitType.MASTER_SUGAR_PLANTER, GoodsType.SUGAR,
                        TerrainType.SAVANNAH),
                Arguments.of(UnitType.MASTER_COTTON_PLANTER, GoodsType.COTTON,
                        TerrainType.PRAIRIE),
                Arguments.of(UnitType.MASTER_TOBACCO_PLANTER, GoodsType.TOBACCO,
                        TerrainType.GRASSLAND));
    }

    private int fieldYield(final UnitType type, final GoodsType goods,
            final TerrainType terrain) {
        final GameModelPo data = scenario();
        worker(data).setType(type);
        final var savedColony = data.getModel().getColonies().get(0);
        if (terrain.isSea()) {
            final ConstructionPo dock = new ConstructionPo();
            dock.setType(ConstructionType.DOCK);
            savedColony.getConstructions().add(dock);
        }
        final var west = savedColony.getColonyFields().stream()
                .filter(field -> field.getDirection()
                        .equals(Direction.west.getVector()))
                .findFirst().orElseThrow();
        west.setProducedGoodsType(goods);
        final Location westSquare = savedColony.getLocation()
                .add(Direction.west.getVector());
        final Map<Location, TerrainType> map = data.getModel().getMap()
                .getTerrainMap();
        map.put(westSquare, terrain);
        data.getModel().getMap().setTerrainType(map);
        // Fur and lumber exist only on a forested square, so plant one there.
        data.getModel().getMap()
                .setTrees(goods == GoodsType.FUR || goods == GoodsType.LUMBER
                        ? Set.of(westSquare)
                        : Collections.emptySet());
        data.getModel().getMap().setFields(Collections.emptySet());
        final Model model = load(data);
        final ColonyField field = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.west);
        assertSame(type, field.getUnit().orElseThrow().getType());
        return field.getProduction().orElseThrow().getAmount();
    }

    @ParameterizedTest
    @MethodSource("outdoorJobs")
    void outdoorExpertDoublesOnlyMatchingPositiveFieldYield(final UnitType type,
            final GoodsType goods, final TerrainType terrain) {
        final int ordinary = fieldYield(UnitType.COLONIST, goods, terrain);
        assertTrue(ordinary > 0);
        assertEquals(ordinary * 2, fieldYield(type, goods, terrain));
        final TerrainType wrongTerrain = type == UnitType.EXPERT_FARMER
                ? TerrainType.OCEAN
                : TerrainType.GRASSLAND;
        assertEquals(
                fieldYield(UnitType.COLONIST, GoodsType.CORN, wrongTerrain),
                fieldYield(type, GoodsType.CORN, wrongTerrain));
    }

    @ParameterizedTest
    @MethodSource("outdoorJobs")
    void outdoorExpertCannotTurnZeroTerrainYieldIntoGoods(final UnitType type,
            final GoodsType goods, final TerrainType ignored) {
        final GameModelPo data = scenario();
        worker(data).setType(type);
        final var colony = data.getModel().getColonies().get(0);
        final Map<Location, TerrainType> map = data.getModel().getMap()
                .getTerrainMap();
        map.put(colony.getLocation().add(Direction.north.getVector()),
                TerrainType.ARCTIC);
        data.getModel().getMap().setTerrainType(map);
        final Model model = load(data);
        final ColonyField barren = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.north);
        assertEquals(0,
                barren.getPotentialProduction(model.getUnitById(0), goods)
                        .getAmount());
    }

    private static Stream<Arguments> indoorJobs() {
        return Stream.of(
                Arguments.of(UnitType.MASTER_CARPENTER,
                        ConstructionType.LUMBER_MILL),
                Arguments.of(UnitType.MASTER_DISTILLER,
                        ConstructionType.RUM_DISTILLERS_HOUSE),
                Arguments.of(UnitType.MASTER_WEAVER,
                        ConstructionType.WEAVERS_HOUSE),
                Arguments.of(UnitType.MASTER_TOBACCONIST,
                        ConstructionType.TOBACCONISTS_HOUSE),
                Arguments.of(UnitType.MASTER_FUR_TRADER,
                        ConstructionType.FUR_TRADERS_HOUSE),
                Arguments.of(UnitType.MASTER_BLACKSMITH,
                        ConstructionType.BLACKSMITHS_HOUSE),
                Arguments.of(UnitType.MASTER_GUNSMITH, ConstructionType.ARMORY),
                Arguments.of(UnitType.FIREBRAND_PREACHER,
                        ConstructionType.CHAPEL),
                Arguments.of(UnitType.ELDER_STATESMAN,
                        ConstructionType.TOWN_HALL));
    }

    private ConstructionTurnProduction indoorProduction(final UnitType type,
            final ConstructionType building, final int input) {
        final GameModelPo data = scenario();
        final var colony = data.getModel().getColonies().get(0);
        colony.getColonyFields().stream()
                .filter(field -> Integer.valueOf(0).equals(field.getWorkerId()))
                .forEach(field -> {
                    field.setWorkerId(null);
                    field.setProducedGoodsType(null);
                });
        worker(data).setPlaceColonyField(null);
        worker(data).setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        worker(data).setType(type);
        final ConstructionPo savedBuilding = colony.getConstructions().stream()
                .filter(value -> value.getType() == building).findFirst()
                .orElseGet(() -> {
                    final ConstructionPo created = new ConstructionPo();
                    created.setType(building);
                    colony.getConstructions().add(created);
                    return created;
                });
        savedBuilding.getSlots().stream()
                .filter(slot -> slot.getWorkerId() == null).findFirst()
                .orElseThrow().setWorkerId(0);
        final Model model = load(data);
        final ConstructionSlot slot = model.getColonyById(0).orElseThrow()
                .getConstructionByType(building).getConstructionSlots().stream()
                .filter(value -> !value.isEmpty()
                        && value.getUnit().getId() == 0)
                .findFirst().orElseThrow();
        assertSame(type, slot.getUnit().getType());
        return building.getConsumptionPerTurn().isPresent()
                ? slot.getProduction(Goods.of(building.getConsumptionPerTurn()
                        .orElseThrow().getType(), input))
                : slot.getProduction();
    }

    @ParameterizedTest
    @MethodSource("indoorJobs")
    void indoorExpertDoublesMatchingJobAndConsumesActualInputs(
            final UnitType type, final ConstructionType building) {
        final int ordinary = indoorProduction(UnitType.COLONIST, building, 100)
                .getProducedGoods().orElseThrow().getAmount();
        final ConstructionTurnProduction expert = indoorProduction(type,
                building, 100);
        assertTrue(ordinary > 0);
        assertEquals(ordinary * 2,
                expert.getProducedGoods().orElseThrow().getAmount());
        if (building.getConsumptionPerTurn().isPresent()) {
            final int ordinaryInput = indoorProduction(UnitType.COLONIST,
                    building, 100).getConsumedGoods().orElseThrow().getAmount();
            assertEquals(ordinaryInput * 2,
                    expert.getConsumedGoods().orElseThrow().getAmount());
            assertEquals(0, indoorProduction(type, building, 0)
                    .getProducedGoods().orElseThrow().getAmount());
        }
        final ConstructionType unrelated = type == UnitType.ELDER_STATESMAN
                ? ConstructionType.CHAPEL
                : ConstructionType.TOWN_HALL;
        assertEquals(
                indoorProduction(UnitType.COLONIST, unrelated, 100)
                        .getProducedGoods().orElseThrow().getAmount(),
                indoorProduction(type, unrelated, 100).getProducedGoods()
                        .orElseThrow().getAmount());
    }
}
