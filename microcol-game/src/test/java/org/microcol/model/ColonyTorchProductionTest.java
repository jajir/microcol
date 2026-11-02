package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

class ColonyTorchProductionTest {

    private Model model() {
        return Model.make(new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Construction chapel(final Colony colony) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.GRAIN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TORCHES, 0));
        return colony.getConstructionByType(ConstructionType.CHAPEL);
    }

    private int forecast(final Colony colony, final GoodsType type) {
        return colony.getGoodsStats().getStatsByType(type).getRowProduction();
    }

    @Test
    void unstaffedChapelForecastMatchesCommittedWarehouseAndResume() {
        final Model model = model();
        final Colony colony = model.getColonyById(0).orElseThrow();
        chapel(colony);
        assertEquals(1, forecast(colony, GoodsType.TORCHES));
        assertEquals(1, forecast(colony, GoodsType.TORCHES));
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());

        colony.startTurn();
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony resumed = restored.getColonyById(0).orElseThrow();
        assertEquals(1,
                resumed.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
        restored.startGame();
        assertEquals(1,
                resumed.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
        assertEquals(1, forecast(resumed, GoodsType.TORCHES));
    }

    @Test
    void workerAndUpgradeRetainExactlyOneBaseContribution() {
        final Model model = model();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Construction chapel = chapel(colony);
        final Unit worker = model.addUnitOutSideColony(colony);
        worker.placeToColonyStructureSlot(chapel.getSlotAt(0));
        final int staffed = forecast(colony, GoodsType.TORCHES);
        assertEquals(2, staffed);
        colony.startTurn();
        assertEquals(2,
                colony.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());

        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(1, forecast(colony, GoodsType.TORCHES));
        assertEquals(4, forecast(colony, GoodsType.OWLS));
        chapel.upgrade();
        assertEquals(ConstructionType.CHURCH, chapel.getType());
        assertEquals(2, forecast(colony, GoodsType.TORCHES));
        chapel.upgrade();
        assertEquals(ConstructionType.CATHEDRAL, chapel.getType());
        assertEquals(3, forecast(colony, GoodsType.TORCHES));
        assertEquals(1, colony.getConstructions().stream().filter(
                building -> building.getType() == ConstructionType.CATHEDRAL)
                .count());
        assertTrue(colony.getConstructions().stream().noneMatch(
                building -> building.getType() == ConstructionType.CHAPEL
                        || building.getType() == ConstructionType.CHURCH));
        colony.startTurn();
        assertEquals(5,
                colony.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
    }

    @Test
    void twoColoniesProduceIndependentlyAndChapelsDoNotChangeOwls() {
        final Model model = model();
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        final int firstOwls = forecast(first, GoodsType.OWLS);
        final int secondOwls = forecast(second, GoodsType.OWLS);
        assertEquals(1, firstOwls);
        assertEquals(1, secondOwls);
        final int firstOwlStock = first.getWarehouse().getGoods(GoodsType.OWLS)
                .getAmount();
        final int secondOwlStock = second.getWarehouse()
                .getGoods(GoodsType.OWLS).getAmount();
        chapel(first);
        chapel(second);
        assertEquals(1, forecast(first, GoodsType.TORCHES));
        assertEquals(1, forecast(second, GoodsType.TORCHES));
        assertEquals(firstOwls, forecast(first, GoodsType.OWLS));
        assertEquals(secondOwls, forecast(second, GoodsType.OWLS));

        first.startTurn();
        second.startTurn();
        assertEquals(1,
                first.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
        assertEquals(1,
                second.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
        assertEquals(firstOwlStock + firstOwls,
                first.getWarehouse().getGoods(GoodsType.OWLS).getAmount());
        assertEquals(secondOwlStock + secondOwls,
                second.getWarehouse().getGoods(GoodsType.OWLS).getAmount());
    }

    @Test
    void nationalRoundCreditsBothChapelsOnceAndResumeDoesNotReplay() {
        final Model model = model();
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        chapel(first);
        chapel(second);
        assertEquals(1, forecast(first, GoodsType.TORCHES));
        assertEquals(1, forecast(second, GoodsType.TORCHES));

        model.endTurn();
        model.endTurn();
        assertEquals(1,
                first.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());
        assertEquals(1,
                second.getWarehouse().getGoods(GoodsType.TORCHES).getAmount());

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        resumed.startGame();
        assertEquals(1, resumed.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.TORCHES).getAmount());
        assertEquals(1, resumed.getColonyById(1).orElseThrow().getWarehouse()
                .getGoods(GoodsType.TORCHES).getAmount());
    }
}
