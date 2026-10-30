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
    void allGoodsKeepDistinctTransparentIconsAtNativeSize() {
        final ImageCache cache = new ImageCache(new StreamReader());
        new ImageLoaderTerrain().preload(cache);
        new ImageLoaderGoods().preload(cache);
        final GoodsImageProvider goods = new GoodsImageProvider(cache);
        final Set<Long> fingerprints = new HashSet<>();
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
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
        assertEquals(19, fingerprints.size());
    }

    @Test
    void productionGoodsHaveSelfContainedVectorSources() throws Exception {
        final var factory = javax.xml.parsers.DocumentBuilderFactory
                .newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        final var parser = factory.newDocumentBuilder();
        final Set<String> elements = Set.of("svg", "title", "desc", "g",
                "path");
        final Set<String> attributes = Set.of("xmlns", "width", "height",
                "viewBox", "aria-labelledby", "id", "stroke", "stroke-width",
                "stroke-linecap", "stroke-linejoin", "d", "fill", "transform");
        for (final String name : Set.of("bell", "cross", "hammer")) {
            final var source = java.nio.file.Path
                    .of("src/graphics/production-goods/good-" + name + ".svg");
            final var document = parser.parse(source.toFile());
            final var root = document.getDocumentElement();
            assertEquals("http://www.w3.org/2000/svg", root.getNamespaceURI());
            assertEquals("0 0 64 64", root.getAttribute("viewBox"));
            final var nodes = document.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                final var node = nodes.item(i);
                assertEquals(root.getNamespaceURI(), node.getNamespaceURI(),
                        name);
                assertTrue(elements.contains(node.getLocalName()), name);
                final var attrs = node.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    final var attribute = attrs.item(j);
                    assertTrue(attributes.contains(attribute.getNodeName()),
                            name);
                    assertFalse(attribute.getNodeValue().contains("url("),
                            name);
                }
            }
            final var paths = document
                    .getElementsByTagNameNS(root.getNamespaceURI(), "path");
            assertTrue(paths.getLength() > 0, name);
            for (int i = 0; i < paths.getLength(); i++) {
                final var path = (org.w3c.dom.Element) paths.item(i);
                assertFalse(path.getAttribute("d").isBlank(), name);
                assertTrue(path.getAttribute("fill")
                        .matches("none|#[0-9a-fA-F]{6}"), name);
            }
        }
    }
}
