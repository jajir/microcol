package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyBuildingQueueItemPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.QueueItemType;

import com.google.gson.Gson;

class ColonyBreedingTest {

    private Model model(final int horses, final ConstructionType stable,
            final ConstructionType storage, final boolean queueStable) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final ColonyPo saved = data.getModel().getColonies().stream()
                .filter(c -> c.getId() == 0).findFirst().orElseThrow();
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(Location.of(24, 14), TerrainType.TUNDRA);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        saved.getColonyWarehouse().put("HORSE", horses);
        if (storage != ConstructionType.WAREHOUSE_BASIC) {
            saved.getConstructions().stream().filter(
                    c -> c.getType() == ConstructionType.WAREHOUSE_BASIC)
                    .findFirst().orElseThrow().setType(storage);
        }
        if (stable != null) {
            final ConstructionPo completed = new ConstructionPo();
            completed.setType(stable);
            saved.getConstructions().add(completed);
        }
        if (queueStable) {
            saved.getBuildingQueue().clear();
            final ColonyBuildingQueueItemPo queued = new ColonyBuildingQueueItemPo();
            queued.setId(100);
            queued.setType(QueueItemType.CONSTRUCTION);
            queued.setConstructionType(ConstructionType.STABLES);
            queued.setBuildHammers(0);
            saved.getBuildingQueue().add(queued);
            saved.getColonyWarehouse().put("HAMMERS", 64);
        }
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Colony withFoodAfterResidents(final Model model, final int food) {
        final Colony colony = model.getColonyById(0).orElseThrow();
        final GoodsProductionStats raw = colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN,
                Math.subtractExact(food, raw.getNetProduction())));
        assertEquals(food, colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter());
        return colony;
    }

    @ParameterizedTest
    @CsvSource({ "0,3,0", "1,3,0", "2,3,1", "3,3,1", "4,3,2", "2,0,0", "2,1,1",
            "4,1,1" })
    void ordinaryBreedingUsesExistingPairsAndRemainingFood(final int horses,
            final int food, final int born) {
        final Model model = model(horses, null,
                ConstructionType.WAREHOUSE_BASIC, false);
        final Colony colony = withFoodAfterResidents(model, food);
        verifyPreviewAndUpdate(colony, horses, food - born, born);
    }

    @ParameterizedTest
    @CsvSource({ "STABLES,2,3,2", "STABLES,3,3,2", "STABLES,4,3,3",
            "LARGE_STABLES,4,3,3", "LARGE_STABLES,4,4,4" })
    void completedStableDoublesRateSubjectToFood(final String stableName,
            final int horses, final int food, final int born) {
        final Model model = model(horses,
                "STABLES".equals(stableName) ? ConstructionType.STABLES
                        : ConstructionType.LARGE_STABLES,
                ConstructionType.WAREHOUSE_BASIC, false);
        verifyPreviewAndUpdate(withFoodAfterResidents(model, food), horses,
                food - born, born);
    }

    @ParameterizedTest
    @CsvSource({ "BASIC,99,1", "BASIC,100,0", "WAREHOUSE,199,1",
            "WAREHOUSE,200,0", "EXPANSION,299,1", "EXPANSION,300,0" })
    void breedingRespectsHorseStorageCapacity(final String storageName,
            final int horses, final int born) {
        final ConstructionType storage = "BASIC".equals(storageName)
                ? ConstructionType.WAREHOUSE_BASIC
                : "WAREHOUSE".equals(storageName) ? ConstructionType.WAREHOUSE
                        : ConstructionType.WAREHOUSE_EXPANSION;
        final Model model = model(horses, ConstructionType.STABLES, storage,
                false);
        verifyPreviewAndUpdate(withFoodAfterResidents(model, 4), horses,
                4 - born, born);
    }

    @ParameterizedTest
    @CsvSource({ "200,0,0,1", "201,1,0,1", "204,4,0,1", "400,0,0,2" })
    void birthsTakeFoodBeforeBreeding(final int food, final int bornHorses,
            final int remainingFood, final int bornColonists) {
        final Model model = model(4, ConstructionType.STABLES,
                ConstructionType.WAREHOUSE_BASIC, false);
        final Colony colony = withFoodAfterResidents(model, food);
        final int units = model.getAllUnits().size();
        verifyPreviewAndUpdate(colony, 4, remainingFood, bornHorses);
        assertEquals(units + bornColonists, model.getAllUnits().size());
    }

    @Test
    void newlyCompletedStableFirstAffectsFollowingTurn() {
        final Model model = model(4, null, ConstructionType.WAREHOUSE_BASIC,
                true);
        final Colony colony = withFoodAfterResidents(model, 4);
        verifyPreviewAndUpdate(colony, 4, 2, 2);
        assertTrue(colony.wasConstructionBuilded(ConstructionType.STABLES));
        withFoodAfterResidents(model, 4);
        verifyPreviewAndUpdate(colony, 6, 0, 4);
    }

    @Test
    void famineDoesNotBreedHorsesEvenWhenStableAndStockExist() {
        final Model model = model(4, ConstructionType.STABLES,
                ConstructionType.WAREHOUSE_BASIC, false);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertTrue(colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        assertEquals(0, colony.getGoodsStats().getStatsByType(GoodsType.HORSE)
                .getRowProduction());
        colony.startTurn();
        assertEquals(4,
                colony.getWarehouse().getGoods(GoodsType.HORSE).getAmount());
    }

    @Test
    void finalResidentFamineLossCannotBreed() {
        final Model model = model(4, ConstructionType.STABLES,
                ConstructionType.WAREHOUSE_BASIC, false);
        final Colony colony = model.getColonyById(0).orElseThrow();
        for (final Unit resident : colony.getUnitsInsideColony()) {
            if (resident.getId() != 1) {
                resident.getPlace().destroy();
                model.destroyUnit(resident);
            }
        }
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        assertTrue(colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() < 0);
        assertEquals(0, colony.getGoodsStats().getStatsByType(GoodsType.HORSE)
                .getRowProduction());
        colony.startTurn();
        assertFalse(colony.isValid());
    }

    @Test
    void previewAndResumeDoNotProduceHorses() {
        final Model model = model(4, ConstructionType.STABLES,
                ConstructionType.WAREHOUSE_BASIC, false);
        final Colony colony = withFoodAfterResidents(model, 3);
        final String before = new Gson().toJson(model.save());
        assertEquals(3, colony.getGoodsStats().getStatsByType(GoodsType.HORSE)
                .getRowProduction());
        assertEquals(before, new Gson().toJson(model.save()));
        colony.startTurn();
        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String after = new Gson().toJson(resumed.save());
        resumed.startGame();
        assertEquals(after, new Gson().toJson(resumed.save()));
        assertEquals(7, resumed.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.HORSE).getAmount());
    }

    private void verifyPreviewAndUpdate(final Colony colony, final int horses,
            final int foodAfter, final int born) {
        final ColonyProductionStats preview = colony.getGoodsStats();
        assertEquals(born,
                preview.getStatsByType(GoodsType.HORSE).getRowProduction());
        assertEquals(horses + born,
                preview.getStatsByType(GoodsType.HORSE).getInWarehouseAfter());
        assertEquals(foodAfter,
                preview.getStatsByType(GoodsType.CORN).getInWarehouseAfter());
        colony.startTurn();
        assertEquals(horses + born,
                colony.getWarehouse().getGoods(GoodsType.HORSE).getAmount());
        assertEquals(foodAfter,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
    }
}
