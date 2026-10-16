package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

class IndustrialProductionTest {

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
        assertEquals(input,
                ConstructionSlot.getPotentialInput(type, ordinary).getAmount());
        assertEquals(input * 2,
                ConstructionSlot.getPotentialInput(type, expert).getAmount());
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
}
