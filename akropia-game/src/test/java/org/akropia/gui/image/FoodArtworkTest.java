package org.akropia.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.akropia.model.*;
import com.google.inject.Guice;

class FoodArtworkTest {
    @Test
    void stockAndProductionUseDifferentImagesAtTheirDisplaySizes() {
        final ImageProvider images = Guice.createInjector(new ModuleImages())
                .getInstance(ImageProvider.class);
        final Terrain land = new Terrain(Location.of(1, 1),
                TerrainType.GRASSLAND);
        final Terrain water = new Terrain(Location.of(1, 1), TerrainType.OCEAN);
        assertSame(images.getImage("good-food.png"),
                images.getGoodsTypeImage(GoodsType.GRAIN));
        assertSame(images.getImage("good-food-tiny.png"),
                images.getGoodsTypeImage(GoodsType.GRAIN, 15));
        assertSame(images.getImage("production-grain.png"),
                images.getProductionImage(GoodsType.GRAIN, land, 45));
        assertSame(images.getImage("production-fish.png"),
                images.getProductionImage(GoodsType.GRAIN, water, 45));
        assertSame(images.getImage("production-fish-small.png"),
                images.getProductionImage(GoodsType.GRAIN, water, 24));
        assertSame(images.getImage("production-grain-small.png"),
                images.getProductionImage(GoodsType.GRAIN, land, 25));
        land.setRiverSize(RiverSize.SMALL);
        assertSame(images.getImage("production-fish.png"),
                images.getProductionImage(GoodsType.GRAIN, land, 45));
        assertSame(images.getGoodsTypeImage(GoodsType.ORE),
                images.getProductionImage(GoodsType.ORE, water, 24));
        assertEquals(45, images.getImage("good-food.png").getWidth());
        assertEquals(24,
                images.getImage("production-fish-small.png").getWidth());
        assertEquals(15, images.getImage("good-food-tiny.png").getWidth());
    }
}
