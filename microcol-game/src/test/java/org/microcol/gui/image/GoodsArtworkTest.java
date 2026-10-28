package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.microcol.gui.util.StreamReader;
import org.microcol.model.GoodsType;
import javafx.scene.image.Image;

class GoodsArtworkTest {
    @Test
    void allTradeGoodsKeepDistinctTransparentIconsAtNativeSize() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrain().preload(cache);
        new ImageLoaderGoods().preload(cache);
        final GoodsImageProvider goods = new GoodsImageProvider(cache);
        final Set<Long> fingerprints = new HashSet<>();
        for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
            final Image image = goods.getGoodsTypeImage(type);
            assertEquals(45, image.getWidth(), type.name());
            assertEquals(45, image.getHeight(), type.name());
            int ink = 0;
            long fingerprint = 1;
            for (int y = 0; y < 45; y++) {
                for (int x = 0; x < 45; x++) {
                    final int pixel = image.getPixelReader().getArgb(x, y);
                    fingerprint = fingerprint * 31 + pixel;
                    if ((pixel >>> 24) > 192) {
                        ink++;
                    }
                    if (x == 0 || y == 0 || x == 44 || y == 44) {
                        assertEquals(0, pixel >>> 24,
                                type + " transparent edge");
                    }
                }
            }
            assertTrue(ink > 300, type + " remains legible at game size");
            assertTrue(fingerprints.add(fingerprint), type + " is distinct");
        }
        assertEquals(16, fingerprints.size());
        assertSame(cache.getImage("type_9_5"),
                goods.getGoodsTypeImage(GoodsType.BELL));
        assertSame(cache.getImage("type_0_7"),
                goods.getGoodsTypeImage(GoodsType.HAMMERS));
        assertSame(cache.getImage("type_10_5"),
                goods.getGoodsTypeImage(GoodsType.CROSS));
    }
}
