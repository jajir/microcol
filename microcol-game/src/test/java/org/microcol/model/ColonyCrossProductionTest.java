package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

class ColonyCrossProductionTest {

    private Model model() {
        return Model.make(new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel(), MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Construction chapel(final Colony colony) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
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
        assertEquals(1, forecast(colony, GoodsType.CROSS));
        assertEquals(1, forecast(colony, GoodsType.CROSS));
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());

        colony.startTurn();
        assertEquals(1,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony resumed = restored.getColonyById(0).orElseThrow();
        assertEquals(1,
                resumed.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        restored.startGame();
        assertEquals(1,
                resumed.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(1, forecast(resumed, GoodsType.CROSS));
    }

    @Test
    void workerAndUpgradeRetainExactlyOneBaseContribution() {
        final Model model = model();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final Construction chapel = chapel(colony);
        final Unit worker = model.addUnitOutSideColony(colony);
        worker.placeToColonyStructureSlot(chapel.getSlotAt(0));
        final int staffed = forecast(colony, GoodsType.CROSS);
        assertEquals(2, staffed);
        colony.startTurn();
        assertEquals(2,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());

        worker.placeToColonyStructureSlot(
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getSlotAt(0));
        assertEquals(1, forecast(colony, GoodsType.CROSS));
        assertEquals(4, forecast(colony, GoodsType.BELL));
        chapel.upgrade();
        assertEquals(ConstructionType.CHURCH, chapel.getType());
        assertEquals(2, forecast(colony, GoodsType.CROSS));
        chapel.upgrade();
        assertEquals(ConstructionType.CATHEDRAL, chapel.getType());
        assertEquals(3, forecast(colony, GoodsType.CROSS));
        assertEquals(1, colony.getConstructions().stream().filter(
                building -> building.getType() == ConstructionType.CATHEDRAL)
                .count());
        assertTrue(colony.getConstructions().stream().noneMatch(
                building -> building.getType() == ConstructionType.CHAPEL
                        || building.getType() == ConstructionType.CHURCH));
        colony.startTurn();
        assertEquals(5,
                colony.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
    }

    @Test
    void twoColoniesProduceIndependentlyAndChapelsDoNotChangeBells() {
        final Model model = model();
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        final int firstBells = forecast(first, GoodsType.BELL);
        final int secondBells = forecast(second, GoodsType.BELL);
        assertEquals(1, firstBells);
        assertEquals(1, secondBells);
        final int firstBellStock = first.getWarehouse().getGoods(GoodsType.BELL)
                .getAmount();
        final int secondBellStock = second.getWarehouse()
                .getGoods(GoodsType.BELL).getAmount();
        chapel(first);
        chapel(second);
        assertEquals(1, forecast(first, GoodsType.CROSS));
        assertEquals(1, forecast(second, GoodsType.CROSS));
        assertEquals(firstBells, forecast(first, GoodsType.BELL));
        assertEquals(secondBells, forecast(second, GoodsType.BELL));

        first.startTurn();
        second.startTurn();
        assertEquals(1,
                first.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(1,
                second.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(firstBellStock + firstBells,
                first.getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertEquals(secondBellStock + secondBells,
                second.getWarehouse().getGoods(GoodsType.BELL).getAmount());
    }

    @Test
    void nationalRoundCreditsBothChapelsOnceAndResumeDoesNotReplay() {
        final Model model = model();
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        chapel(first);
        chapel(second);
        assertEquals(1, forecast(first, GoodsType.CROSS));
        assertEquals(1, forecast(second, GoodsType.CROSS));

        model.endTurn();
        model.endTurn();
        assertEquals(1,
                first.getWarehouse().getGoods(GoodsType.CROSS).getAmount());
        assertEquals(1,
                second.getWarehouse().getGoods(GoodsType.CROSS).getAmount());

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        resumed.startGame();
        assertEquals(1, resumed.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.CROSS).getAmount());
        assertEquals(1, resumed.getColonyById(1).orElseThrow().getWarehouse()
                .getGoods(GoodsType.CROSS).getAmount());
    }
}
