package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.PlaceConstructionSlotPo;
import org.microcol.model.store.UnitPo;

/**
 * Real colonies at nonzero support: exclusions, percentage order and the
 * consistency of previews with the committed update. Colony 0 has five
 * residents, so 500 liberty is exactly 50 % and 1000 is full support.
 */
class PoliticalProductionIntegrationTest {

    private GameModelPo data(final long liberty) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies().forEach(colony -> {
            colony.setLiberty(0L);
            colony.setLastColonialAffiliation(colony.getOwnerName());
        });
        saved(data).setLiberty(liberty);
        return data;
    }

    private ColonyPo saved(final GameModelPo data) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Move a saved resident from its field into a building slot. */
    private void moveToBuilding(final GameModelPo data, final int unitId,
            final ConstructionType building, final int slot,
            final UnitType type) {
        final ColonyPo colony = saved(data);
        colony.getColonyFields().stream().filter(
                field -> Integer.valueOf(unitId).equals(field.getWorkerId()))
                .forEach(field -> {
                    field.setWorkerId(null);
                    field.setProducedGoodsType(null);
                });
        final UnitPo unit = data.getModel().getUnits().stream()
                .filter(saved -> saved.getId() == unitId).findFirst()
                .orElseThrow();
        unit.setType(type);
        unit.setPlaceColonyField(null);
        unit.setPlaceConstructionSlot(new PlaceConstructionSlotPo());
        final ConstructionPo target = colony.getConstructions().stream()
                .filter(saved -> saved.getType() == building).findFirst()
                .orElseThrow();
        target.getSlots().get(slot).setWorkerId(unitId);
    }

    private void addBuilding(final GameModelPo data,
            final ConstructionType type) {
        final ConstructionPo improvement = new ConstructionPo();
        improvement.setType(type);
        saved(data).getConstructions().add(improvement);
    }

    private int stored(final Colony colony, final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    private int planned(final Colony colony, final GoodsType type) {
        return colony.getGoodsStats().getStatsByType(type).getRowProduction();
    }

    @ParameterizedTest
    @CsvSource({ "0,1,1", "500,1,1", "1000,1,1" })
    void unattendedBellsAndCrossesNeverReceiveTheSupportBonus(
            final long liberty, final int bells, final int crosses) {
        // The town hall base is 1 bell and the empty chapel 1 cross.
        final Model model = load(data(liberty));
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(bells, planned(colony, GoodsType.BELL));
        assertEquals(crosses, planned(colony, GoodsType.CROSS));
        final int bellsBefore = stored(colony, GoodsType.BELL);
        final int crossesBefore = stored(colony, GoodsType.CROSS);
        colony.startTurn();
        assertEquals(bellsBefore + bells, stored(colony, GoodsType.BELL));
        assertEquals(crossesBefore + crosses, stored(colony, GoodsType.CROSS));
    }

    @ParameterizedTest
    @CsvSource({ "0,10", "500,12", "1000,13" })
    void specialistThenPoliticalThenPrintingPressPercentage(final long liberty,
            final int expectedBells) {
        // Elder statesman: 3 doubled to 6, +0/+1/+2 for support, plus the
        // hall's
        // unattended bell, then the press adds half: (6 + p + 1) * 3 / 2.
        final GameModelPo data = data(liberty);
        addBuilding(data, ConstructionType.PRINTING_PRESS);
        moveToBuilding(data, 0, ConstructionType.TOWN_HALL, 0,
                UnitType.ELDER_STATESMAN);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        assertEquals(5, colony.getPopulation());
        assertEquals(expectedBells, planned(colony, GoodsType.BELL));
        assertEquals(expectedBells,
                colony.getConstructionByType(ConstructionType.TOWN_HALL)
                        .getProduction().getProducedGoods().orElseThrow()
                        .getAmount());
        final int before = stored(colony, GoodsType.BELL);
        colony.startTurn();
        assertEquals(before + expectedBells, stored(colony, GoodsType.BELL));
    }

    /** A valid active Congress whose only member is the given founder. */
    private void giveFounder(final GameModelPo data, final Founder founder) {
        final CongressPo congress = data.getModel().getGameManager()
                .getPlayers().stream()
                .filter(player -> saved(data).getOwnerName()
                        .equals(player.getName()))
                .findFirst().orElseThrow().getCongress();
        congress.setActivated(true);
        congress.setGeneration(1);
        congress.setBells(0);
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        congress.setMembers(List.of(founder));
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            for (final Founder other : Founder.values()) {
                if (other.getCategory() == category && other != founder) {
                    candidates.add(other);
                    break;
                }
            }
        }
        congress.setCandidates(candidates);
    }

    @ParameterizedTest
    @CsvSource({ "CHURCH,0,12", "CHURCH,500,13", "CHURCH,1000,15",
            "CATHEDRAL,0,22", "CATHEDRAL,500,24", "CATHEDRAL,1000,25" })
    void pennMultipliesTheBoostedPreacherAndUnattendedBase(
            final ConstructionType tier, final long liberty,
            final int expectedCrosses) {
        // Firebrand preacher doubles the tier's base (3 or 6); the political
        // change (+0/+1/+2) is added to that job only, the unattended base
        // (2 or 3) is added without it, and Penn then takes floor(total * 3 /
        // 2).
        final GameModelPo data = data(liberty);
        giveFounder(data, Founder.WILLIAM_PENN);
        final ConstructionPo religious = saved(data).getConstructions().stream()
                .filter(value -> value.getType() == ConstructionType.CHAPEL)
                .findFirst().orElseThrow();
        religious.setType(tier);
        moveToBuilding(data, 0, tier, 0, UnitType.FIREBRAND_PREACHER);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        assertEquals(5, colony.getPopulation());
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CROSS, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        assertEquals(expectedCrosses, planned(colony, GoodsType.CROSS));
        colony.startTurn();
        assertEquals(expectedCrosses, stored(colony, GoodsType.CROSS));
    }

    @Test
    void newspaperDoublesTheBoostedHall() {
        final GameModelPo data = data(1000);
        addBuilding(data, ConstructionType.NEWSPAPER);
        moveToBuilding(data, 0, ConstructionType.TOWN_HALL, 0,
                UnitType.ELDER_STATESMAN);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        // (6 + 2 + 1) * 2
        assertEquals(18, planned(colony, GoodsType.BELL));
    }

    @ParameterizedTest
    @CsvSource({ "0", "1000" })
    void horseBreedingIsNotAJobOutput(final long liberty) {
        final GameModelPo data = data(liberty);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(24, 14), TerrainType.TUNDRA);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(java.util.Collections.emptySet());
        saved(data).getColonyWarehouse().put("HORSE", 2);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        // Leave exactly 3 food after the residents: one foal is born.
        final int net = colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 3 - net));
        assertEquals(3, colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter());
        assertEquals(1, planned(colony, GoodsType.HORSE));
        colony.startTurn();
        assertEquals(3, stored(colony, GoodsType.HORSE));
    }

    @ParameterizedTest
    @CsvSource({ "0,0,3,0", "1000,2,3,2" })
    void scarceOreLimitsTheBoostedBlacksmithInPreviewAndUpdate(
            final long liberty, final int bonus, final int tools,
            final int blocked) {
        final GameModelPo data = data(liberty);
        moveToBuilding(data, 0, ConstructionType.BLACKSMITHS_HOUSE, 0,
                UnitType.COLONIST);
        saved(data).getColonyWarehouse().put("ORE", 3);
        saved(data).getColonyWarehouse().put("CORN", 100);
        // Start below the storage cap so new tools are not trimmed.
        saved(data).getColonyWarehouse().put("TOOLS", 0);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        assertEquals(bonus, colony.getPoliticalProduction().getBonus());
        // The house could make 3 + bonus tools but only 3 ore are stored.
        final GoodsProductionStats toolStats = colony.getGoodsStats()
                .getStatsByType(GoodsType.TOOLS);
        assertEquals(tools, toolStats.getRowProduction());
        assertEquals(blocked, toolStats.getBlockedProduction());
        assertEquals(3, colony.getGoodsStats().getStatsByType(GoodsType.ORE)
                .getConsumed());
        final int toolsBefore = stored(colony, GoodsType.TOOLS);
        colony.startTurn();
        assertEquals(toolsBefore + tools, stored(colony, GoodsType.TOOLS));
        assertEquals(0, stored(colony, GoodsType.ORE));
    }

    @Test
    void stockedOreLetsTheBonusRaiseTheBlacksmithOutput() {
        final GameModelPo data = data(500);
        moveToBuilding(data, 0, ConstructionType.BLACKSMITHS_HOUSE, 0,
                UnitType.COLONIST);
        saved(data).getColonyWarehouse().put("ORE", 40);
        saved(data).getColonyWarehouse().put("CORN", 100);
        // Start below the storage cap so new tools are not trimmed.
        saved(data).getColonyWarehouse().put("TOOLS", 0);
        final Colony colony = load(data).getColonyById(0).orElseThrow();
        assertEquals(1, colony.getPoliticalProduction().getBonus());
        assertEquals(4, planned(colony, GoodsType.TOOLS));
        final int tools = stored(colony, GoodsType.TOOLS);
        final int ore = stored(colony, GoodsType.ORE);
        colony.startTurn();
        assertEquals(tools + 4, stored(colony, GoodsType.TOOLS));
        assertEquals(ore - 4, stored(colony, GoodsType.ORE));
    }

    @Test
    void positiveBaseFieldsFollowPreviewAndUpdateWhileZeroYieldStaysZero() {
        final GameModelPo data = data(1000);
        final Model model = load(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        // Keep the three field workers: corn 0 and tobacco 1 and 2.
        for (final Unit unit : List.copyOf(colony.getUnitsInsideColony())) {
            if (!Set.of(0, 1, 2).contains(unit.getId())) {
                unit.getPlace().destroy();
                model.destroyUnit(unit);
            }
        }
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOBACCO, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        // Liberty was capped to full support of three residents: +2.
        assertEquals(2, colony.getPoliticalProduction().getBonus());
        int expectedTobacco = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            if (field.getUnit().isEmpty()
                    || field.getUnit().get().getPlaceColonyField()
                            .getProducedGoodsType() != GoodsType.TOBACCO) {
                continue;
            }
            final Unit worker = field.getUnit().get();
            final int base = field.getGoodsTypeProduction(GoodsType.TOBACCO);
            assertTrue(base > 0);
            final int expected = (int) (base
                    * worker.getType().getFieldProductionModifier(
                            GoodsType.TOBACCO, field.getTerrainType()))
                    + 2;
            assertEquals(expected,
                    field.getProduction().orElseThrow().getAmount());
            expectedTobacco += expected;
        }
        assertEquals(expectedTobacco, planned(colony, GoodsType.TOBACCO));
        colony.startTurn();
        assertEquals(expectedTobacco, stored(colony, GoodsType.TOBACCO));
        // A good the terrain cannot yield gets nothing from the bonus.
        final Unit anyone = colony.getUnitsInsideColony().get(0);
        int zeroYield = 0;
        for (final ColonyField field : colony.getColonyFields()) {
            for (final GoodsType goods : GoodsType.GOOD_TYPES) {
                if (field.getGoodsTypeProduction(goods) == 0) {
                    assertEquals(0,
                            field.getPotentialProduction(anyone, goods)
                                    .getAmount(),
                            field.getDirection() + " " + goods);
                    zeroYield++;
                }
            }
        }
        assertTrue(zeroYield > 0, "Some field and good must be zero-yield");
    }
}
