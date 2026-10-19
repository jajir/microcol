package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Identity of special resources: twelve kinds with distinct saved codes, the
 * output each one names, and the land versus water boundary.
 */
class ResourceOverlayTest {

    @Test
    void everyKindHasItsOwnCodeAndReadsBackFromIt() {
        final List<String> codes = Arrays.stream(ResourceOverlay.values())
                .map(ResourceOverlay::getCode).collect(Collectors.toList());
        assertEquals(codes.size(), codes.stream().distinct().count(),
                "Codes must be distinct");
        assertEquals(13, ResourceOverlay.values().length);
        for (final ResourceOverlay overlay : ResourceOverlay.values()) {
            assertEquals(overlay,
                    ResourceOverlay.valueOfCode(overlay.getCode()));
        }
        assertEquals(ResourceOverlay.NONE, ResourceOverlay.valueOfCode("-"));
        assertEquals(ResourceOverlay.SILVER, ResourceOverlay.valueOfCode("si"));
    }

    @Test
    void unknownCodeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> ResourceOverlay.valueOfCode("xx"));
        assertThrows(IllegalArgumentException.class,
                () -> ResourceOverlay.valueOfCode(""));
        assertThrows(NullPointerException.class,
                () -> ResourceOverlay.valueOfCode(null));
    }

    @Test
    void outputsNameTheGoodsOfEachKind() {
        assertEquals(List.of(GoodsType.SILVER),
                ResourceOverlay.SILVER.getOutputs());
        assertEquals(List.of(GoodsType.ORE, GoodsType.SILVER),
                ResourceOverlay.MIXED_MINERALS.getOutputs());
        assertEquals(List.of(GoodsType.CORN),
                ResourceOverlay.GAME.getOutputs());
        assertEquals(List.of(GoodsType.CORN),
                ResourceOverlay.FISHERY.getOutputs());
        assertEquals(List.of(GoodsType.LUMBER),
                ResourceOverlay.PRIME_TIMBER.getOutputs());
        assertTrue(ResourceOverlay.NONE.getOutputs().isEmpty());
    }

    @Test
    void bonusAppliesOnlyToMatchingGoods() {
        assertEquals(2, ResourceOverlay.BONUS);
        assertEquals(ResourceOverlay.BONUS,
                ResourceOverlay.MIXED_MINERALS.getBonusFor(GoodsType.ORE));
        assertEquals(ResourceOverlay.BONUS,
                ResourceOverlay.MIXED_MINERALS.getBonusFor(GoodsType.SILVER));
        assertEquals(0,
                ResourceOverlay.MIXED_MINERALS.getBonusFor(GoodsType.FUR));
        assertEquals(0, ResourceOverlay.NONE.getBonusFor(GoodsType.CORN));
    }

    @Test
    void landResourcesNeedLandAndFisheryNeedsOrdinaryWater() {
        for (final ResourceOverlay overlay : ResourceOverlay.values()) {
            if (overlay == ResourceOverlay.NONE
                    || overlay == ResourceOverlay.FISHERY) {
                continue;
            }
            assertTrue(overlay.canBeOn(TerrainType.GRASSLAND), overlay.name());
            assertFalse(overlay.canBeOn(TerrainType.HIGH_SEA), overlay.name());
        }
        assertTrue(ResourceOverlay.SILVER.canBeOn(TerrainType.MOUNTAIN));
        assertFalse(ResourceOverlay.SILVER.canBeOn(TerrainType.OCEAN));
        assertFalse(ResourceOverlay.SILVER.canBeOn(TerrainType.LAKE));
        assertTrue(ResourceOverlay.FISHERY.canBeOn(TerrainType.OCEAN));
        assertTrue(ResourceOverlay.FISHERY.canBeOn(TerrainType.LAKE));
        assertFalse(ResourceOverlay.FISHERY.canBeOn(TerrainType.GRASSLAND));
        assertTrue(ResourceOverlay.NONE.canBeOn(TerrainType.HIGH_SEA));
    }
}
