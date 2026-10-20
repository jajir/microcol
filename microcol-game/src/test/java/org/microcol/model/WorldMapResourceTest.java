package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.WorldMapPo;

import com.google.gson.Gson;

/**
 * Saved special resources: a present block must be complete and use known codes
 * on squares that allow them, an absent block means none, and Save keeps each
 * identity without changing the output of its square.
 */
class WorldMapResourceTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive"
            + ".microcol";

    private final GameModelPoDao dao = new GameModelPoDao();

    /** The first land square found on the map. */
    private Location landSquare(final GameModelPo data) {
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        for (final Location at : types.keySet()) {
            if (types.get(at).isLand()) {
                return at;
            }
        }
        throw new IllegalStateException("No land square in the map");
    }

    /** One square without a saved terrain, which is ocean. */
    private Location seaSquare(final GameModelPo data) {
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        for (int y = 1; y <= data.getModel().getMap().getMaxY(); y++) {
            for (int x = 1; x <= data.getModel().getMap().getMaxX(); x++) {
                final Location at = Location.of(x, y);
                if (!types.containsKey(at)) {
                    return at;
                }
            }
        }
        throw new IllegalStateException("No sea square in the map");
    }

    private WorldMap resourceMap(final GameModelPo data,
            final Map<Location, ResourceOverlay> resources) {
        data.getModel().getMap().setResources(resources);
        data.getModel().getMap().setMineralReserves(initialReserves(resources));
        return new WorldMap(data.getModel());
    }

    /** A new deposit starts with every unit of its overlay. */
    private static Map<Location, MineralReserve> initialReserves(
            final Map<Location, ResourceOverlay> resources) {
        final Map<Location, MineralReserve> out = new HashMap<>();
        resources.forEach((at, overlay) -> {
            if (overlay.isFinite()) {
                out.put(at, MineralReserve.initial(overlay));
            }
        });
        return out;
    }

    private WorldMap reserveMap(final GameModelPo data, final Location at,
            final ResourceOverlay overlay, final MineralReserve reserve) {
        data.getModel().getMap().setResources(Map.of(at, overlay));
        data.getModel().getMap().setMineralReserves(Map.of(at, reserve));
        return new WorldMap(data.getModel());
    }

    private static Map<GoodsType, Integer> outputs(final Terrain terrain) {
        final Map<GoodsType, Integer> out = new HashMap<>();
        for (final GoodsType goods : GoodsType.GOOD_TYPES) {
            out.put(goods, terrain.canProduceAmmount(goods));
        }
        return out;
    }

    @Test
    void mapWithoutResourceBlockHasNoResource() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final WorldMap map = new WorldMap(data.getModel());
        final Location land = landSquare(data);
        assertEquals(ResourceOverlay.NONE, map.getResourceOverlayAt(land));
        assertEquals(ResourceOverlay.NONE,
                map.getTerrainAt(land).getResourceOverlay());
    }

    @Test
    void savedResourcesLoadWithTheirIdentityAndUnchangedOutput() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Location land = landSquare(data);
        final Location sea = seaSquare(data);
        final WorldMap map = resourceMap(data, Map.of(land,
                ResourceOverlay.SILVER, sea, ResourceOverlay.FISHERY));
        final ModelPo saved = new ModelPo();
        map.save(saved);
        final WorldMap reloaded = new WorldMap(saved);
        assertEquals(ResourceOverlay.SILVER,
                reloaded.getResourceOverlayAt(land));
        assertEquals(ResourceOverlay.FISHERY,
                reloaded.getResourceOverlayAt(sea));
        assertEquals(ResourceOverlay.SILVER,
                reloaded.getTerrainAt(land).getResourceOverlay());
        // Loading adds no output: the reloaded square yields what it yielded.
        assertEquals(outputs(map.getTerrainAt(land)),
                outputs(reloaded.getTerrainAt(land)));
        assertEquals(outputs(map.getTerrainAt(sea)),
                outputs(reloaded.getTerrainAt(sea)));
    }

    @Test
    void illegalPlacementIsRefusedBeforeTheMapIsUsable() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Location land = landSquare(data);
        final Location sea = seaSquare(data);
        // Fishery needs ordinary water, and land resources need land.
        assertThrows(IllegalArgumentException.class,
                () -> resourceMap(data, Map.of(land, ResourceOverlay.FISHERY)));
        assertThrows(IllegalArgumentException.class,
                () -> resourceMap(data, Map.of(sea, ResourceOverlay.SILVER)));
        assertThrows(IllegalArgumentException.class,
                () -> resourceMap(data, Map.of(sea, ResourceOverlay.GAME)));
    }

    @Test
    void malformedSavedResourceBlockIsRefused() {
        // A two by one map whose resource block is damaged in three ways.
        final String header = "column  :1,2";
        final WorldMapPo unknownCode = new Gson()
                .fromJson("{\"maxX\":2,\"maxY\":1,\"resources\":[\"" + header
                        + "\",\"row-0001:xx,-\"]}", WorldMapPo.class);
        assertThrows(IllegalArgumentException.class,
                unknownCode::getResourceMap);
        final WorldMapPo missingRow = new Gson().fromJson(
                "{\"maxX\":2,\"maxY\":1,\"resources\":[\"" + header + "\"]}",
                WorldMapPo.class);
        assertThrows(IllegalArgumentException.class,
                missingRow::getResourceMap);
        final WorldMapPo shortRow = new Gson()
                .fromJson("{\"maxX\":2,\"maxY\":1,\"resources\":[\"" + header
                        + "\",\"row-0001:si\"]}", WorldMapPo.class);
        assertThrows(IllegalArgumentException.class, shortRow::getResourceMap);
    }

    @Test
    void savedRemainingUnitsSurviveSaveIncludingZeroAndPartialReserves() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Location land = landSquare(data);
        final WorldMap map = resourceMap(data,
                Map.of(land, ResourceOverlay.MIXED_MINERALS));
        // Silver is used up while the ore reserve stays full.
        map.extractMinerals(Map.of(land, Goods.of(GoodsType.SILVER, 200)));
        final ModelPo saved = new ModelPo();
        map.save(saved);
        final WorldMap reloaded = new WorldMap(saved);
        assertEquals(0, reloaded.getMineralRemainingAt(land, GoodsType.SILVER));
        assertEquals(200, reloaded.getMineralRemainingAt(land, GoodsType.ORE));
        // A partial reserve survives too, and the loaded square is unchanged.
        final WorldMap partial = reserveMap(data, land, ResourceOverlay.SILVER,
                MineralReserve.of(37, 0));
        final ModelPo partialSaved = new ModelPo();
        partial.save(partialSaved);
        assertEquals(37, new WorldMap(partialSaved).getMineralRemainingAt(land,
                GoodsType.SILVER));
    }

    @Test
    void rejectedExtractionPlanChangesNoReserve() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Location land = landSquare(data);
        final Location sea = seaSquare(data);
        final WorldMap map = resourceMap(data,
                Map.of(land, ResourceOverlay.MIXED_MINERALS));
        // The first entry is valid, but the second square has no deposit.
        assertThrows(IllegalArgumentException.class,
                () -> map.extractMinerals(
                        Map.of(land, Goods.of(GoodsType.SILVER, 10), sea,
                                Goods.of(GoodsType.ORE, 1))));
        assertEquals(200, map.getMineralRemainingAt(land, GoodsType.SILVER));
        // More than the reserve, and goods the deposit does not yield.
        assertThrows(IllegalArgumentException.class, () -> map.extractMinerals(
                Map.of(land, Goods.of(GoodsType.SILVER, 201))));
        assertThrows(IllegalArgumentException.class, () -> map
                .extractMinerals(Map.of(land, Goods.of(GoodsType.FUR, 1))));
        assertEquals(200, map.getMineralRemainingAt(land, GoodsType.SILVER));
        assertEquals(200, map.getMineralRemainingAt(land, GoodsType.ORE));
    }

    @Test
    void mineralReserveMustFitItsDepositAndSquare() {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Location land = landSquare(data);
        // A pure silver deposit holds at most 100 units and no ore.
        assertThrows(IllegalArgumentException.class, () -> reserveMap(data,
                land, ResourceOverlay.SILVER, MineralReserve.of(101, 0)));
        assertThrows(IllegalArgumentException.class, () -> reserveMap(data,
                land, ResourceOverlay.SILVER, MineralReserve.of(-1, 0)));
        assertThrows(IllegalArgumentException.class, () -> reserveMap(data,
                land, ResourceOverlay.SILVER, MineralReserve.of(100, 1)));
        // A mixed deposit holds at most 200 of each.
        assertThrows(IllegalArgumentException.class,
                () -> reserveMap(data, land, ResourceOverlay.MIXED_MINERALS,
                        MineralReserve.of(201, 0)));
        // A finite deposit must have its reserve, and a reserve needs one.
        data.getModel().getMap()
                .setResources(Map.of(land, ResourceOverlay.SILVER));
        data.getModel().getMap().setMineralReserves(Map.of());
        assertThrows(IllegalArgumentException.class,
                () -> new WorldMap(data.getModel()));
        data.getModel().getMap().setResources(Map.of());
        data.getModel().getMap()
                .setMineralReserves(Map.of(land, MineralReserve.of(1, 0)));
        assertThrows(IllegalArgumentException.class,
                () -> new WorldMap(data.getModel()));
    }

    @Test
    void malformedSavedMineralReserveBlockIsRefused() {
        assertTrue(new Gson().fromJson("{}", WorldMapPo.class)
                .getMineralReserveMap().isEmpty());
        // An entry without its ore count is incomplete.
        final WorldMapPo missingOre = new Gson().fromJson(
                "{\"maxX\":2,\"maxY\":1,\"mineralReserves\":[{\"location\":"
                        + "{\"x\":1,\"y\":1},\"silver\":100}]}",
                WorldMapPo.class);
        assertThrows(IllegalArgumentException.class,
                missingOre::getMineralReserveMap);
        // The same square twice is ambiguous.
        final WorldMapPo duplicate = new Gson().fromJson(
                "{\"maxX\":2,\"maxY\":1,\"mineralReserves\":[{\"location\":"
                        + "{\"x\":1,\"y\":1},\"silver\":100,\"ore\":0},"
                        + "{\"location\":{\"x\":1,\"y\":1},\"silver\":5,"
                        + "\"ore\":0}]}",
                WorldMapPo.class);
        assertThrows(IllegalArgumentException.class,
                duplicate::getMineralReserveMap);
    }
}
