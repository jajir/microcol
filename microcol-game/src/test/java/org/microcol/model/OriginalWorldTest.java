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
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(24, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(23, 13), Location.of(24, 13)));
        assertEquals(RiverSize.LARGE, map.getRiverSizeAt(Location.of(25, 13)));
        assertTrue(
                map.hasRiverSegment(Location.of(24, 13), Location.of(25, 13)));
        assertEquals(20, source().getMap().getRiverSegments().size());
    }

    @Test
    void forestsAndBonusesAreDistributedAcrossTheContinent() {
        final ModelPo saved = source();
        final WorldMap map = new WorldMap(saved);
        assertTrue(saved.getMap().getTreeSet().size() >= 70);
        assertTrue(saved.getMap().getResourceMap().size() >= 35);
        for (final ResourceOverlay resource : ResourceOverlay.values()) {
            if (resource != ResourceOverlay.NONE) {
                assertTrue(saved.getMap().getResourceMap().values().stream()
                        .filter(value -> value == resource).count() >= 2,
                        resource.name());
            }
        }
        for (final int startY : new int[] { 3, 12, 22 }) {
            assertTrue(saved.getMap().getTreeSet().stream()
                    .filter(at -> at.getY() >= startY && at.getY() < startY + 4)
                    .count() >= 10);
        }
        assertEquals(5, saved.getMap().getRumorSet().size());
        for (final Location reserved : new Location[] { Location.of(17, 10),
                Location.of(16, 10), Location.of(17, 21), Location.of(16, 21),
                Location.of(25, 14) }) {
            assertFalse(map.getTerrainAt(reserved).isHasTrees());
            assertFalse(map.hasLostCityRumorAt(reserved));
        }
        assertEquals(ResourceOverlay.PRIME_FOOD,
                map.getResourceOverlayAt(Location.of(25, 14)));
    }

    @Test
    void freshwaterLakeIsEnclosedAndRiversStayInland() {
        final WorldMap map = new WorldMap(source());
        assertEquals(TerrainType.LAKE,
                map.getTerrainTypeAt(Location.of(19, 9)));
        assertEquals(TerrainType.GRASSLAND,
                map.getTerrainTypeAt(Location.of(20, 9)));
        assertEquals(TerrainType.OCEAN,
                map.getTerrainTypeAt(Location.of(21, 9)));
        assertEquals(ResourceOverlay.FISHERY,
                map.getResourceOverlayAt(Location.of(19, 8)));
        for (final Location end : new Location[] { Location.of(16, 6),
                Location.of(24, 13), Location.of(19, 24) }) {
            assertNotEquals(RiverSize.NONE, map.getRiverSizeAt(end));
            assertFalse(map.getRiverLinksAt(end).isEmpty());
            assertTrue(end.getNeighbors().stream()
                    .noneMatch(at -> map.getTerrainTypeAt(at).isSea()));
        }
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
                assertEquals(fresh.getTerrainAt(at).isHasTrees(),
                        restored.getTerrainAt(at).isHasTrees());
                assertEquals(fresh.hasLostCityRumorAt(at),
                        restored.hasLostCityRumorAt(at));
                for (final GoodsType mineral : new GoodsType[] {
                        GoodsType.SILVER, GoodsType.ORE }) {
                    assertEquals(fresh.getMineralRemainingAt(at, mineral),
                            restored.getMineralRemainingAt(at, mineral));
                }
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

    @Test
    void riverCorridorsStayOpenWithMoreSilverThanOre() {
        final ModelPo saved = source();
        final WorldMap map = new WorldMap(saved);
        final Set<Location> rivers = saved.getMap().getRiverMap().keySet();
        for (final Location at : rivers) {
            assertFalse(map.getTerrainAt(at).isHasTrees());
            assertFalse(map.getTerrainAt(at).isHasField());
            assertTrue(map.getResourceOverlayAt(at)
                    .canBeOn(map.getTerrainTypeAt(at), map.getRiverSizeAt(at)));
            assertTrue(saved.getColonies().stream()
                    .noneMatch(colony -> colony.getLocation().equals(at)));
        }
        assertEquals(4, rivers.stream().filter(
                at -> map.getResourceOverlayAt(at) == ResourceOverlay.SILVER)
                .count());
        assertEquals(1, rivers.stream().filter(
                at -> map.getResourceOverlayAt(at) == ResourceOverlay.ORE)
                .count());
    }
}
