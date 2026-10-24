package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

class IndustrialProductionTest {

    /**
     * Colony 0 residents of the fixture: ids 0-2 work fields, 4-5 the
     * tobacconist house.
     */
    private static final List<Integer> RESIDENT_IDS = List.of(0, 1, 2, 4, 5);

    /**
     * Residents who do not staff the tested building wait in the chapel's three
     * slots first, then on these fixture fields, which were worked before.
     */
    private static final List<Integer> FIELD_ORDER = List.of(4, 5);

    /**
     * Literal expectations per budget shape, one row per cell:
     * {@code shape,crew,workers,input,consumed,produced,blocked,inputAfter}.
     * The shape is input:output per ordinary worker. An expert doubles that
     * worker's budget. A mixed crew is one ordinary and one expert worker. In
     * the 6:9 and 3:6 shapes the input needed for an output is rounded up, so
     * those rows are written out cell by cell rather than derived in code.
     */
    private static final List<String> SHAPE_ROWS = List.of(
            "3:3,ORDINARY,0,4,0,0,0,4", "3:3,ORDINARY,1,2,2,2,1,0",
            "3:3,ORDINARY,1,3,3,3,0,0", "3:3,ORDINARY,1,7,3,3,0,4",
            "3:3,ORDINARY,2,5,5,5,1,0", "3:3,ORDINARY,2,6,6,6,0,0",
            "3:3,ORDINARY,2,10,6,6,0,4", "3:3,ORDINARY,3,8,8,8,1,0",
            "3:3,ORDINARY,3,9,9,9,0,0", "3:3,ORDINARY,3,13,9,9,0,4",
            "3:3,EXPERT,1,5,5,5,1,0", "3:3,EXPERT,1,6,6,6,0,0",
            "3:3,EXPERT,1,10,6,6,0,4", "3:3,EXPERT,2,11,11,11,1,0",
            "3:3,EXPERT,2,12,12,12,0,0", "3:3,EXPERT,2,16,12,12,0,4",
            "3:3,EXPERT,3,17,17,17,1,0", "3:3,EXPERT,3,18,18,18,0,0",
            "3:3,EXPERT,3,22,18,18,0,4", "6:6,ORDINARY,0,4,0,0,0,4",
            "6:6,ORDINARY,1,5,5,5,1,0", "6:6,ORDINARY,1,6,6,6,0,0",
            "6:6,ORDINARY,1,10,6,6,0,4", "6:6,ORDINARY,2,11,11,11,1,0",
            "6:6,ORDINARY,2,12,12,12,0,0", "6:6,ORDINARY,2,16,12,12,0,4",
            "6:6,ORDINARY,3,17,17,17,1,0", "6:6,ORDINARY,3,18,18,18,0,0",
            "6:6,ORDINARY,3,22,18,18,0,4", "6:6,EXPERT,1,11,11,11,1,0",
            "6:6,EXPERT,1,12,12,12,0,0", "6:6,EXPERT,1,16,12,12,0,4",
            "6:6,EXPERT,2,23,23,23,1,0", "6:6,EXPERT,2,24,24,24,0,0",
            "6:6,EXPERT,2,28,24,24,0,4", "6:6,EXPERT,3,35,35,35,1,0",
            "6:6,EXPERT,3,36,36,36,0,0", "6:6,EXPERT,3,40,36,36,0,4",
            "6:9,ORDINARY,0,4,0,0,0,4", "6:9,ORDINARY,1,5,5,7,2,0",
            "6:9,ORDINARY,1,6,6,9,0,0", "6:9,ORDINARY,1,10,6,9,0,4",
            "6:9,ORDINARY,2,11,11,16,2,0", "6:9,ORDINARY,2,12,12,18,0,0",
            "6:9,ORDINARY,2,16,12,18,0,4", "6:9,ORDINARY,3,17,17,25,2,0",
            "6:9,ORDINARY,3,18,18,27,0,0", "6:9,ORDINARY,3,22,18,27,0,4",
            "6:9,EXPERT,1,11,11,16,2,0", "6:9,EXPERT,1,12,12,18,0,0",
            "6:9,EXPERT,1,16,12,18,0,4", "6:9,EXPERT,2,23,23,34,2,0",
            "6:9,EXPERT,2,24,24,36,0,0", "6:9,EXPERT,2,28,24,36,0,4",
            "6:9,EXPERT,3,35,35,52,2,0", "6:9,EXPERT,3,36,36,54,0,0",
            "6:9,EXPERT,3,40,36,54,0,4", "6:9,MIXED,2,17,17,25,2,0",
            "6:9,MIXED,2,18,18,27,0,0", "6:9,MIXED,2,22,18,27,0,4",
            "3:6,ORDINARY,0,4,0,0,0,4", "3:6,ORDINARY,1,2,2,4,2,0",
            "3:6,ORDINARY,1,3,3,6,0,0", "3:6,ORDINARY,1,7,3,6,0,4",
            "3:6,ORDINARY,2,5,5,10,2,0", "3:6,ORDINARY,2,6,6,12,0,0",
            "3:6,ORDINARY,2,10,6,12,0,4", "3:6,ORDINARY,3,8,8,16,2,0",
            "3:6,ORDINARY,3,9,9,18,0,0", "3:6,ORDINARY,3,13,9,18,0,4",
            "3:6,EXPERT,1,5,5,10,2,0", "3:6,EXPERT,1,6,6,12,0,0",
            "3:6,EXPERT,1,10,6,12,0,4", "3:6,EXPERT,2,11,11,22,2,0",
            "3:6,EXPERT,2,12,12,24,0,0", "3:6,EXPERT,2,16,12,24,0,4",
            "3:6,EXPERT,3,17,17,34,2,0", "3:6,EXPERT,3,18,18,36,0,0",
            "3:6,EXPERT,3,22,18,36,0,4", "3:6,MIXED,2,8,8,16,2,0",
            "3:6,MIXED,2,9,9,18,0,0", "3:6,MIXED,2,13,9,18,0,4");

    /**
     * Every manufacturing building of the seven chains, with its budget shape
     * and its expert: {@code building,shape,expert}. Shop and factory tiers are
     * included, and the lumber chain also has its carpenter's stand.
     */
    private static final List<String> BUILDINGS = List.of(
            "CARPENTERS_STAND,3:3,MASTER_CARPENTER",
            "LUMBER_MILL,6:6,MASTER_CARPENTER",
            "BLACKSMITHS_HOUSE,3:3,MASTER_BLACKSMITH",
            "BLACKSMITHS_SHOP,6:6,MASTER_BLACKSMITH",
            "IRON_WORKS,6:9,MASTER_BLACKSMITH",
            "TOBACCONISTS_HOUSE,3:3,MASTER_TOBACCONIST",
            "TOBACCONISTS_SHOP,6:6,MASTER_TOBACCONIST",
            "CIGAR_FACTORY,6:9,MASTER_TOBACCONIST",
            "WEAVERS_HOUSE,3:3,MASTER_WEAVER", "WEAVERS_SHOP,6:6,MASTER_WEAVER",
            "TEXTILE_MILL,6:9,MASTER_WEAVER",
            "RUM_DISTILLERS_HOUSE,3:3,MASTER_DISTILLER",
            "RUM_DISTILLERY,6:6,MASTER_DISTILLER",
            "RUM_FACTORY,6:9,MASTER_DISTILLER",
            "FUR_TRADERS_HOUSE,3:3,MASTER_FUR_TRADER",
            "FUR_TRADING_POST,6:6,MASTER_FUR_TRADER",
            "FUR_FACTORY,6:9,MASTER_FUR_TRADER", "ARMORY,3:3,MASTER_GUNSMITH",
            "MAGAZINE,6:6,MASTER_GUNSMITH", "ARSENAL,3:6,MASTER_GUNSMITH");

    private static Stream<Arguments> industryTiers() {
        return Stream.of(Arguments.of(ConstructionType.CARPENTERS_STAND, 3, 3),
                Arguments.of(ConstructionType.LUMBER_MILL, 6, 6),
                Arguments.of(ConstructionType.BLACKSMITHS_HOUSE, 3, 3),
                Arguments.of(ConstructionType.BLACKSMITHS_SHOP, 6, 6),
                Arguments.of(ConstructionType.IRON_WORKS, 6, 9),
                Arguments.of(ConstructionType.TOBACCONISTS_HOUSE, 3, 3),
                Arguments.of(ConstructionType.TOBACCONISTS_SHOP, 6, 6),
                Arguments.of(ConstructionType.CIGAR_FACTORY, 6, 9),
                Arguments.of(ConstructionType.WEAVERS_HOUSE, 3, 3),
                Arguments.of(ConstructionType.WEAVERS_SHOP, 6, 6),
                Arguments.of(ConstructionType.TEXTILE_MILL, 6, 9),
                Arguments.of(ConstructionType.RUM_DISTILLERS_HOUSE, 3, 3),
                Arguments.of(ConstructionType.RUM_DISTILLERY, 6, 6),
                Arguments.of(ConstructionType.RUM_FACTORY, 6, 9),
                Arguments.of(ConstructionType.FUR_TRADERS_HOUSE, 3, 3),
                Arguments.of(ConstructionType.FUR_TRADING_POST, 6, 6),
                Arguments.of(ConstructionType.FUR_FACTORY, 6, 9),
                Arguments.of(ConstructionType.ARMORY, 3, 3),
                Arguments.of(ConstructionType.MAGAZINE, 6, 6),
                Arguments.of(ConstructionType.ARSENAL, 3, 6));
    }

    @ParameterizedTest
    @MethodSource("industryTiers")
    void everyIndustryUsesExactBudgetsAndConservesScarceInput(
            final ConstructionType type, final int input, final int output) {
        assertEquals(input,
                type.getConsumptionPerTurn().orElseThrow().getAmount());
        assertEquals(output,
                type.getProductionPerTurn().orElseThrow().getAmount());
        final UnitType expertType = UnitType.EUROPEAN_SPECIALISTS.stream()
                .filter(candidate -> candidate.getProductionModifier(type
                        .getProductionPerTurn().orElseThrow().getType()) == 2)
                .findFirst().orElseThrow();
        final GameModelPo workers = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        workers.getModel().getUnits().stream().filter(unit -> unit.getId() == 1)
                .findFirst().orElseThrow().setType(expertType);
        final Model workersModel = Model.make(workers.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit ordinary = workersModel.getUnitById(0);
        final Unit expert = workersModel.getUnitById(1);
        assertEquals(input, ConstructionSlot
                .getPotentialInput(type, ordinary, PoliticalProduction.NONE)
                .getAmount());
        assertEquals(input * 2, ConstructionSlot
                .getPotentialInput(type, expert, PoliticalProduction.NONE)
                .getAmount());
        verify(type, output, input, input, output, 0);
        verify(type, output * 2, input * 2, input * 2, output * 2, 0);
        verify(type, output, 0, 0, 0, output);
        final int scarceInput = input - 1;
        final int scarceOutput = scarceInput * output / input;
        final int consumed = (scarceOutput * input + output - 1) / output;
        verify(type, output, scarceInput, consumed, scarceOutput,
                output - scarceOutput);
    }

    private void verify(final ConstructionType type, final int budget,
            final int available, final int expectedInput,
            final int expectedOutput, final int expectedBlocked) {
        final ConstructionTurnProduction produced = ConstructionSlot
                .productionForBudget(type, budget, available);
        assertEquals(expectedInput,
                produced.getConsumedGoods().orElseThrow().getAmount());
        assertEquals(expectedOutput,
                produced.getProducedGoods().orElseThrow().getAmount());
        assertEquals(expectedBlocked,
                produced.getBlockedGoods().map(Goods::getAmount).orElse(0));
    }

    @Test
    void oreBecomesToolsAndThenMusketsInOneUpdateWithoutDoubleSpend() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final var saved = data.getModel().getColonies().stream()
                .filter(c -> c.getId() == 0).findFirst().orElseThrow();
        saved.getColonyFields().stream()
                .filter(field -> Integer.valueOf(0).equals(field.getWorkerId()))
                .findFirst().orElseThrow().setProducedGoodsType(GoodsType.ORE);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(saved.getLocation().add(Direction.west.getVector()),
                TerrainType.HILL);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        final ConstructionPo smith = saved.getConstructions().stream()
                .filter(c -> c.getType() == ConstructionType.BLACKSMITHS_HOUSE)
                .findFirst().orElseThrow();
        final ConstructionPo armory = new ConstructionPo();
        armory.setType(ConstructionType.ARMORY);
        saved.getConstructions().add(armory);
        for (final int id : new int[] { 1, 2 }) {
            final var vacatedField = saved.getColonyFields().stream().filter(
                    field -> Integer.valueOf(id).equals(field.getWorkerId()))
                    .findFirst().orElseThrow();
            vacatedField.setWorkerId(null);
            vacatedField.setProducedGoodsType(null);
            final UnitPo worker = data.getModel().getUnits().stream()
                    .filter(unit -> unit.getId() == id).findFirst()
                    .orElseThrow();
            worker.setPlaceColonyField(null);
            worker.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        }
        smith.getSlots().get(0).setWorkerId(1);
        armory.getSlots().get(0).setWorkerId(2);
        saved.getColonyWarehouse().put("ORE", 3);
        saved.getColonyWarehouse().put("TOOLS", 0);
        saved.getColonyWarehouse().put("MUSKET", 0);

        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyProductionStats preview = colony.getGoodsStats();
        assertEquals(3,
                preview.getStatsByType(GoodsType.TOOLS).getRowProduction());
        assertEquals(3, preview.getStatsByType(GoodsType.TOOLS).getConsumed());
        assertEquals(3,
                preview.getStatsByType(GoodsType.MUSKET).getRowProduction());
        final int oreAfter = preview.getStatsByType(GoodsType.ORE)
                .getInWarehouseAfter();
        colony.startTurn();
        assertEquals(oreAfter,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(3,
                colony.getWarehouse().getGoods(GoodsType.MUSKET).getAmount());
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String savedState = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(savedState, new Gson().toJson(resumed.save()));
    }

    /**
     * Colony 0 of the fixture with no second town commodity ({@code NONE}) and
     * its residents staged. The first {@code workers} residents staff the
     * tested building with the crew's unit types; the rest wait in the chapel
     * or on corn fields. Every other goods-consuming building is removed, so
     * the tested chain is the only consumer, and its input and output stocks
     * are explicit.
     */
    private Model staffedColony(final String building, final String expert,
            final String crew, final int workers, final int input,
            final int outputStock, final ConstructionType warehouse) {
        final ConstructionType type = ConstructionType.valueOf(building);
        final GoodsType inputType = type.getConsumptionPerTurn().orElseThrow()
                .getType();
        final GoodsType outputType = type.getProductionPerTurn().orElseThrow()
                .getType();
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final var saved = data.getModel().getColonies().stream()
                .filter(c -> c.getId() == 0).findFirst().orElseThrow();
        saved.setTownCommonsGoods(TownCommons.NONE_CODE);
        saved.getColonyFields().forEach(field -> {
            field.setWorkerId(null);
            field.setProducedGoodsType(null);
        });
        saved.getConstructions().forEach(construction -> construction.getSlots()
                .forEach(slot -> slot.setWorkerId(null)));
        saved.getConstructions().removeIf(construction -> construction.getType()
                .getConsumptionPerTurn().isPresent());
        final ConstructionPo tested = new ConstructionPo();
        tested.setType(type);
        saved.getConstructions().add(tested);
        saved.getConstructions().stream()
                .filter(construction -> ConstructionType.WAREHOUSES
                        .contains(construction.getType()))
                .findFirst().orElseThrow().setType(warehouse);
        final var chapel = saved.getConstructions().stream()
                .filter(construction -> construction
                        .getType() == ConstructionType.CHAPEL)
                .findFirst().orElseThrow().getSlots();
        for (int index = 0; index < RESIDENT_IDS.size(); index++) {
            final int id = RESIDENT_IDS.get(index);
            final UnitPo resident = data.getModel().getUnits().stream()
                    .filter(unit -> unit.getId() == id).findFirst()
                    .orElseThrow();
            if (index < workers) {
                final boolean specialist = "EXPERT".equals(crew)
                        || "MIXED".equals(crew) && index > 0;
                resident.setType(
                        UnitType.valueOf(specialist ? expert : "COLONIST"));
                resident.setPlaceColonyField(null);
                resident.setPlaceConstructionSlot(
                        new PlaceConstructionSlotPo());
                tested.getSlots().get(index).setWorkerId(id);
            } else {
                final int idle = index - workers;
                if (idle < chapel.size()) {
                    resident.setPlaceColonyField(null);
                    resident.setPlaceConstructionSlot(
                            new PlaceConstructionSlotPo());
                    chapel.get(idle).setWorkerId(id);
                } else {
                    resident.setPlaceConstructionSlot(null);
                    resident.setPlaceColonyField(new PlaceColonyFieldPo());
                    final var field = saved.getColonyFields()
                            .get(FIELD_ORDER.get(idle - chapel.size()));
                    field.setWorkerId(id);
                    field.setProducedGoodsType(GoodsType.CORN);
                }
            }
        }
        final var warehouseGoods = saved.getColonyWarehouse();
        warehouseGoods.put(GoodsType.CORN.name(), 100);
        warehouseGoods.put(inputType.name(), input);
        warehouseGoods.put(outputType.name(), outputStock);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Stream<Arguments> manufacturingCells() {
        return BUILDINGS.stream().flatMap(building -> {
            final String[] tier = building.split(",");
            return SHAPE_ROWS.stream()
                    .filter(row -> row.startsWith(tier[1] + ",")).map(row -> {
                        final String[] cell = row.split(",");
                        return Arguments.of(tier[0], tier[2], cell[1],
                                Integer.parseInt(cell[2]),
                                Integer.parseInt(cell[3]),
                                Integer.parseInt(cell[4]),
                                Integer.parseInt(cell[5]),
                                Integer.parseInt(cell[6]),
                                Integer.parseInt(cell[7]));
                    });
        });
    }

    /**
     * Every manufacturing building through a real owner update: preview and
     * actual consumed input, output and blocked output, then the input left and
     * the output stored after {@link Colony#startTurn()}. Expected values are
     * the literal rows of {@link #SHAPE_ROWS}.
     */
    @ParameterizedTest(name = "{0}: {2} crew of {3}, input {4}")
    @MethodSource("manufacturingCells")
    void manufacturingBuildingsUseTheStaffedBudgetOnceInTheRealUpdate(
            final String building, final String expert, final String crew,
            final int workers, final int input, final int consumed,
            final int produced, final int blocked, final int inputAfter) {
        final ConstructionType type = ConstructionType.valueOf(building);
        final GoodsType inputType = type.getConsumptionPerTurn().orElseThrow()
                .getType();
        final GoodsType outputType = type.getProductionPerTurn().orElseThrow()
                .getType();
        final Model model = staffedColony(building, expert, crew, workers,
                input, 0, ConstructionType.WAREHOUSE_BASIC);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyProductionStats preview = colony.getGoodsStats();
        assertEquals(consumed, preview.getStatsByType(inputType).getConsumed());
        assertEquals(produced,
                preview.getStatsByType(outputType).getRowProduction());
        assertEquals(blocked,
                preview.getStatsByType(outputType).getBlockedProduction());
        colony.startTurn();
        assertEquals(inputAfter,
                colony.getWarehouse().getGoods(inputType).getAmount());
        assertEquals(produced,
                colony.getWarehouse().getGoods(outputType).getAmount());
    }

    /**
     * Save and Continue keep the actual stocks of one real update, and the
     * resumed game does not run the update again. Two ordinary workers, with an
     * input excess of four.
     */
    @ParameterizedTest(name = "{0}: stocks after save and continue")
    @CsvSource({ "CARPENTERS_STAND,MASTER_CARPENTER,10,6,4",
            "LUMBER_MILL,MASTER_CARPENTER,16,12,4",
            "BLACKSMITHS_HOUSE,MASTER_BLACKSMITH,10,6,4",
            "BLACKSMITHS_SHOP,MASTER_BLACKSMITH,16,12,4",
            "IRON_WORKS,MASTER_BLACKSMITH,16,18,4",
            "TOBACCONISTS_HOUSE,MASTER_TOBACCONIST,10,6,4",
            "TOBACCONISTS_SHOP,MASTER_TOBACCONIST,16,12,4",
            "CIGAR_FACTORY,MASTER_TOBACCONIST,16,18,4",
            "WEAVERS_HOUSE,MASTER_WEAVER,10,6,4",
            "WEAVERS_SHOP,MASTER_WEAVER,16,12,4",
            "TEXTILE_MILL,MASTER_WEAVER,16,18,4",
            "RUM_DISTILLERS_HOUSE,MASTER_DISTILLER,10,6,4",
            "RUM_DISTILLERY,MASTER_DISTILLER,16,12,4",
            "RUM_FACTORY,MASTER_DISTILLER,16,18,4",
            "FUR_TRADERS_HOUSE,MASTER_FUR_TRADER,10,6,4",
            "FUR_TRADING_POST,MASTER_FUR_TRADER,16,12,4",
            "FUR_FACTORY,MASTER_FUR_TRADER,16,18,4",
            "ARMORY,MASTER_GUNSMITH,10,6,4", "MAGAZINE,MASTER_GUNSMITH,16,12,4",
            "ARSENAL,MASTER_GUNSMITH,10,12,4" })
    void actualStocksSurviveSaveAndContinueWithoutReplay(final String building,
            final String expert, final int input, final int produced,
            final int inputAfter) {
        final ConstructionType type = ConstructionType.valueOf(building);
        final GoodsType inputType = type.getConsumptionPerTurn().orElseThrow()
                .getType();
        final GoodsType outputType = type.getProductionPerTurn().orElseThrow()
                .getType();
        final Model model = staffedColony(building, expert, "ORDINARY", 2,
                input, 0, ConstructionType.WAREHOUSE_BASIC);
        model.getColonyById(0).orElseThrow().startTurn();

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony restored = resumed.getColonyById(0).orElseThrow();
        assertEquals(inputAfter,
                restored.getWarehouse().getGoods(inputType).getAmount());
        assertEquals(produced,
                restored.getWarehouse().getGoods(outputType).getAmount());
        final String savedState = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(savedState, new Gson().toJson(resumed.save()));
        assertEquals(inputAfter,
                restored.getWarehouse().getGoods(inputType).getAmount());
        assertEquals(produced,
                restored.getWarehouse().getGoods(outputType).getAmount());
    }

    /**
     * Warehouse limits through a real update: two ordinary blacksmiths make six
     * tools, which keep only what fits the warehouse's 100, 200 or 300. The
     * excess is thrown away by the update, not refused.
     */
    @ParameterizedTest(name = "{0}: tools {1} before, {2} after")
    @CsvSource({ "BASIC_WAREHOUSE,94,100", "BASIC_WAREHOUSE,96,100",
            "BASIC_WAREHOUSE,100,100", "WAREHOUSE,194,200", "WAREHOUSE,196,200",
            "WAREHOUSE,200,200", "WAREHOUSE_EXPANSION,294,300",
            "WAREHOUSE_EXPANSION,296,300", "WAREHOUSE_EXPANSION,300,300" })
    void warehouseLimitKeepsOnlyWhatFitsAfterTheRealUpdate(
            final String warehouse, final int before, final int after) {
        final Model model = staffedColony("BLACKSMITHS_HOUSE",
                "MASTER_BLACKSMITH", "ORDINARY", 2, 6, before,
                ConstructionType.valueOf(warehouse));
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.startTurn();
        assertEquals(after,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
    }
}
