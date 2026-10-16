package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

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

class PrintingProductionTest {

    private static Stream<Arguments> bellCases() {
        return Stream.of(Arguments.of("", null, 1),
                Arguments.of("", ConstructionType.PRINTING_PRESS, 1),
                Arguments.of("", ConstructionType.NEWSPAPER, 2),
                Arguments.of("COLONIST", null, 4),
                Arguments.of("COLONIST", ConstructionType.PRINTING_PRESS, 6),
                Arguments.of("COLONIST", ConstructionType.NEWSPAPER, 8),
                Arguments.of("COLONIST,COLONIST",
                        ConstructionType.PRINTING_PRESS, 10),
                Arguments.of("COLONIST,COLONIST", ConstructionType.NEWSPAPER,
                        14),
                Arguments.of("ELDER_STATESMAN", ConstructionType.PRINTING_PRESS,
                        10),
                Arguments.of("ELDER_STATESMAN", ConstructionType.NEWSPAPER, 14),
                Arguments.of("INDENTURED_SERVANT",
                        ConstructionType.PRINTING_PRESS, 4),
                Arguments.of("INDENTURED_SERVANT", ConstructionType.NEWSPAPER,
                        6),
                Arguments.of("PETTY_CRIMINAL", ConstructionType.PRINTING_PRESS,
                        3),
                Arguments.of("PETTY_CRIMINAL", ConstructionType.NEWSPAPER, 4),
                Arguments.of("ELDER_STATESMAN,COLONIST",
                        ConstructionType.PRINTING_PRESS, 15));
    }

    private GameModelPo fixture(final String workers,
            final ConstructionType printing) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final var colony = data.getModel().getColonies().stream()
                .filter(saved -> saved.getId() == 0).findFirst().orElseThrow();
        final ConstructionPo hall = colony.getConstructions().stream()
                .filter(saved -> saved.getType() == ConstructionType.TOWN_HALL)
                .findFirst().orElseThrow();
        if (printing != null) {
            final ConstructionPo improvement = new ConstructionPo();
            improvement.setType(printing);
            colony.getConstructions().add(improvement);
        }
        if (!workers.isEmpty()) {
            final String[] names = workers.split(",");
            for (int index = 0; index < names.length; index++) {
                final int id = index;
                colony.getColonyFields().stream()
                        .filter(field -> Integer.valueOf(id)
                                .equals(field.getWorkerId()))
                        .findFirst().orElseThrow().setWorkerId(null);
                colony.getColonyFields().stream()
                        .filter(field -> field.getWorkerId() == null
                                && field.getProducedGoodsType() != null)
                        .forEach(field -> field.setProducedGoodsType(null));
                final UnitPo unit = data.getModel().getUnits().stream()
                        .filter(saved -> saved.getId() == id).findFirst()
                        .orElseThrow();
                unit.setType(workerType(names[index]));
                unit.setPlaceColonyField(null);
                unit.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
                hall.getSlots().get(index).setWorkerId(id);
            }
        }
        return data;
    }

    private UnitType workerType(final String name) {
        return UnitType.UNIT_TYPES.stream()
                .filter(type -> type.name().equals(name)).findFirst()
                .orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @ParameterizedTest
    @MethodSource("bellCases")
    void bonusAppliesOnceToWholeTownHallAndMatchesTurnAndReload(
            final String workers, final ConstructionType printing,
            final int expected) {
        final Model model = load(fixture(workers, printing));
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Construction hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL);
        assertEquals(expected, hall.getProduction().getProducedGoods()
                .orElseThrow().getAmount());
        assertEquals(expected, colony.getGoodsStats()
                .getStatsByType(GoodsType.BELL).getRowProduction());
        final int before = colony.getWarehouse().getGoods(GoodsType.BELL)
                .getAmount();
        colony.startTurn();
        assertEquals(before + expected,
                colony.getWarehouse().getGoods(GoodsType.BELL).getAmount());
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String saved = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(saved, new Gson().toJson(resumed.save()));
        assertEquals(expected,
                resumed.getColonyById(0).orElseThrow().getGoodsStats()
                        .getStatsByType(GoodsType.BELL).getRowProduction());
    }

    @Test
    void pressUpgradeCompletesAfterProductionAndRetainsHallWorkers() {
        final GameModelPo data = fixture("COLONIST",
                ConstructionType.PRINTING_PRESS);
        final var saved = data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        saved.getBuildingQueue().clear();
        saved.getColonyWarehouse().put("HAMMERS", 120);
        saved.getColonyWarehouse().put("TOOLS", 50);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getColonyBuildingQueue().addAtEnd(ConstructionType.NEWSPAPER);
        assertEquals(6, colony.getGoodsStats().getStatsByType(GoodsType.BELL)
                .getRowProduction());
        final int before = colony.getWarehouse().getGoods(GoodsType.BELL)
                .getAmount();
        colony.startTurn();
        assertEquals(before + 6,
                colony.getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertTrue(colony.wasConstructionBuilded(ConstructionType.NEWSPAPER));
        assertFalse(
                colony.wasConstructionBuilded(ConstructionType.PRINTING_PRESS));
        assertEquals(0,
                colony.getColonyBuildingQueue().getBuildingQueue().size());
        assertEquals(0, colony.getConstructionByType(ConstructionType.TOWN_HALL)
                .getConstructionSlots().get(0).getUnit().getId());
        assertEquals(8, colony.getGoodsStats().getStatsByType(GoodsType.BELL)
                .getRowProduction());
    }

    @Test
    void pressAndNewspaperCannotCoexistAsCompletedBuildings() {
        final GameModelPo data = fixture("COLONIST",
                ConstructionType.PRINTING_PRESS);
        final ConstructionPo newspaper = new ConstructionPo();
        newspaper.setType(ConstructionType.NEWSPAPER);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getConstructions().add(newspaper);
        assertThrows(IllegalStateException.class, () -> load(data));
    }
}
