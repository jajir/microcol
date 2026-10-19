package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
