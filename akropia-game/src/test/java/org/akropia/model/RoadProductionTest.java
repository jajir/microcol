package org.akropia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.UnitPo;

/**
 * Road output: a road adds {@link #ROAD_BONUS} once to a positive ore, clay or
 * lumber output, and to mountain silver only with a silver deposit. It is one
 * flat terrain addition, applied before the river modifier and before expert
 * doubling.
 */
class RoadProductionTest {

    private static final int ROAD_BONUS = 1;

    private static final Location AT = Location.of(1, 1);

    private static Terrain terrain(final TerrainType type,
            final ResourceOverlay overlay, final boolean road) {
        final Terrain terrain = new Terrain(AT, type);
        terrain.setResourceOverlay(overlay);
        terrain.setHasRoad(road);
        return terrain;
    }

    private static int output(final Terrain terrain, final GoodsType goods) {
        return terrain.getTerrainProduction(goods).getProduction();
    }

    @Test
    void roadAddsOnceToPositiveOreClayAndLumber() {
        // Hill ore 4, bare grassland clay pit clay 0 + 2, forested prime timber
        // lumber 6 + 2 and mountain ore 3 each gain exactly one from the road.
        assertEquals(4 + ROAD_BONUS,
                output(terrain(TerrainType.HILL, ResourceOverlay.NONE, true),
                        GoodsType.ORE));
        assertEquals(3 + ROAD_BONUS, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.NONE, true),
                GoodsType.ORE));
        assertEquals(2 + ROAD_BONUS, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.CLAY_PIT, true),
                GoodsType.CLAY));
        final Terrain forest = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_TIMBER, true);
        forest.setHasTrees(true);
        assertEquals(6 + ResourceOverlay.BONUS + ROAD_BONUS,
                output(forest, GoodsType.LUMBER));
        // Without the road the same squares keep their output.
        assertEquals(2, output(
                terrain(TerrainType.GRASSLAND, ResourceOverlay.CLAY_PIT, false),
                GoodsType.CLAY));
    }

    @Test
    void mountainSilverNeedsAnExplicitSilverDeposit() {
        // A deposit-free mountain already yields silver 1, which the road must
        // not raise; the silver deposit adds its own bonus and the road adds 1.
        assertEquals(1, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.NONE, true),
                GoodsType.SILVER));
        assertEquals(1 + ResourceOverlay.BONUS + ROAD_BONUS, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.SILVER, true),
                GoodsType.SILVER));
        assertEquals(1 + ResourceOverlay.BONUS + ROAD_BONUS,
                output(terrain(TerrainType.MOUNTAIN,
                        ResourceOverlay.MIXED_MINERALS, true),
                        GoodsType.SILVER));
        // Silver on a hill is not mountain silver, so the road adds nothing.
        assertEquals(ResourceOverlay.BONUS,
                output(terrain(TerrainType.HILL, ResourceOverlay.SILVER, true),
                        GoodsType.SILVER));
    }

    @Test
    void zeroBaseAndUnrelatedOutputsGetNoRoadBonus() {
        // Bare grassland has no ore, clay or lumber, so a road cannot create
        // any.
        final Terrain grass = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.NONE, true);
        assertEquals(0, output(grass, GoodsType.CLAY));
        assertEquals(0, output(grass, GoodsType.ORE));
        assertEquals(0, output(grass, GoodsType.LUMBER));
        // Grain and olives are outside the road list, even with a road.
        assertEquals(4, output(grass, GoodsType.GRAIN));
        assertEquals(1, output(grass, GoodsType.OLIVES));
        // A prime olives field gets its overlay and plow bonus, never the road.
        final Terrain olivesField = terrain(TerrainType.GRASSLAND,
                ResourceOverlay.PRIME_OLIVES, true);
        olivesField.setHasField(true);
        assertEquals(1 + ResourceOverlay.BONUS + 1,
                output(olivesField, GoodsType.OLIVES));
    }

    @Test
    void squareWithoutRoadIsUnchangedByResourceChecks() {
        final Terrain plain = terrain(TerrainType.HILL, ResourceOverlay.NONE,
                false);
        assertEquals(4, output(plain, GoodsType.ORE));
        assertEquals(1, output(
                terrain(TerrainType.MOUNTAIN, ResourceOverlay.NONE, false),
                GoodsType.SILVER));
    }

    @Test
    void roadLeavesRiverDepositsUnchangedAndStillImprovesOrdinaryLand() {
        final Terrain ore = terrain(TerrainType.HILL, ResourceOverlay.ORE,
                true);
        ore.setRiverSize(RiverSize.LARGE);
        assertEquals(1, output(ore, GoodsType.ORE));
        assertEquals(5,
                output(terrain(TerrainType.HILL, ResourceOverlay.NONE, true),
                        GoodsType.ORE));
    }

    private static GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
    }

    private static UnitPo worker(final GameModelPo data) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
    }

    /**
     * Yield of the field west of the colony, worked by the given unit type, on
     * bare grassland with a clay pit resource and an optional road.
     */
    private static int fieldYield(final UnitType type, final boolean road) {
        final GameModelPo data = scenario();
        worker(data).setType(type);
        final var savedColony = data.getModel().getColonies().get(0);
        final var west = savedColony.getColonyFields().stream()
                .filter(field -> field.getDirection()
                        .equals(Direction.west.getVector()))
                .findFirst().orElseThrow();
        west.setProducedGoodsType(GoodsType.CLAY);
        final Location westSquare = savedColony.getLocation()
                .add(Direction.west.getVector());
        final Map<Location, TerrainType> map = data.getModel().getMap()
                .getTerrainMap();
        map.put(westSquare, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(map);
        data.getModel().getMap().setTrees(Collections.emptySet());
        data.getModel().getMap().setFields(Collections.emptySet());
        data.getModel().getMap()
                .setResources(Map.of(westSquare, ResourceOverlay.CLAY_PIT));
        data.getModel().getMap()
                .setRoads(road ? Set.of(westSquare) : Collections.emptySet());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ColonyField field = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.west);
        return field.getProduction().orElseThrow().getAmount();
    }

    @Test
    void matchingExpertDoublesTheRoadOutputAsOrdinaryWorkDoes() {
        // The road lands in the ordinary output before the expert doubles it,
        // so the expert gets twice the road-improved yield, not road plus
        // double.
        final int ordinaryWithoutRoad = fieldYield(UnitType.COLONIST, false);
        final int ordinary = fieldYield(UnitType.COLONIST, true);
        assertTrue(ordinary > ordinaryWithoutRoad);
        assertEquals(ordinary * 2,
                fieldYield(UnitType.EXPERT_CLAY_DIGGER, true));
    }
}
