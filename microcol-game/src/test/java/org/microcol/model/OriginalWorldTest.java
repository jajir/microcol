package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;

/**
 * Fixed authored geography and legal natural features, independently of
 * rendering.
 */
class OriginalWorldTest {
    private ModelPo source() {
        return new GameModelPoDao()
                .loadFromClassPath(FreePlayMap.ORIGINAL.getResource())
                .getModel();
    }

    @Test
    void completeNaturalInventoryWithSeparatePolarRegions() {
        final ModelPo saved = source();
        final WorldMap map = new WorldMap(saved);
        final Set<TerrainType> types = new HashSet<>();
        final Set<ResourceOverlay> resources = new HashSet<>();
        final Set<RiverSize> rivers = new HashSet<>();
        for (int y = 1; y <= 30; y++) {
            for (int x = 1; x <= 50; x++) {
                final Location at = Location.of(x, y);
                final TerrainType type = map.getTerrainTypeAt(at);
                types.add(type);
                resources.add(map.getResourceOverlayAt(at));
                rivers.add(map.getRiverSizeAt(at));
                assertTrue(map.getResourceOverlayAt(at).canBeOn(type));
                // Constructing each terrain also validates its forest
                // placement.
                assertNotNull(map.getTerrainAt(at));
                if (y >= 26)
                    assertNotEquals(TerrainType.TUNDRA, type);
            }
        }
        assertEquals(new HashSet<>(TerrainType.TERRAINS), types);
        assertEquals(Set.of(ResourceOverlay.values()), resources);
        assertEquals(Set.of(RiverSize.values()), rivers);
        assertEquals(TerrainType.TUNDRA,
                map.getTerrainTypeAt(Location.of(16, 3)));
        assertEquals(TerrainType.ARCTIC,
                map.getTerrainTypeAt(Location.of(20, 29)));
        assertFalse(saved.getMap().getTreeSet().isEmpty());
    }

    @Test
    void directDutchApproachReachesRiverAndFoundableLand() {
        final WorldMap map = new WorldMap(source());
        for (int x = 27; x <= 45; x++) {
            assertTrue(map.getTerrainTypeAt(Location.of(x, 15)).isSea());
        }
        assertTrue(map.getTerrainTypeAt(Location.of(26, 14)).isSea());
        assertTrue(map.getTerrainTypeAt(Location.of(25, 14)).isCanHaveField());
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(25, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(24, 13), Location.of(25, 13)));
        assertEquals(9, source().getMap().getRiverSegments().size());
    }

    @Test
    void freshLoadsAndSavePreserveFeaturesWithoutChangingReference() {
        final ModelPo saved = source();
        final WorldMap map = new WorldMap(saved);
        map.save(saved);
        final WorldMap restored = new WorldMap(saved);
        final WorldMap fresh = new WorldMap(source());
        for (int y = 1; y <= 30; y++) {
            for (int x = 1; x <= 50; x++) {
                final Location at = Location.of(x, y);
                assertEquals(fresh.getTerrainTypeAt(at),
                        restored.getTerrainTypeAt(at));
                assertEquals(fresh.getResourceOverlayAt(at),
                        restored.getResourceOverlayAt(at));
                assertEquals(fresh.getRiverSizeAt(at),
                        restored.getRiverSizeAt(at));
                assertEquals(fresh.getRiverLinksAt(at),
                        restored.getRiverLinksAt(at));
            }
        }
        assertNotEquals(FreePlayMap.REFERENCE.getResource(),
                FreePlayMap.ORIGINAL.getResource());
        final ModelPo reference = new GameModelPoDao()
                .loadFromClassPath(FreePlayMap.REFERENCE.getResource())
                .getModel();
        assertTrue(reference.getMap().getRiverMap().isEmpty());
        assertTrue(reference.getMap().getResourceMap().isEmpty());
    }
}
