package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.microcol.model.ConstructionType;

class BuildingArtworkTest {
    @Test
    void everyBuildingHasASelfContainedVectorSource() throws Exception {
        final Path directory = Path.of("src/graphics/colony-buildings");
        final Set<String> expected = new HashSet<>();
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        final var parser = factory.newDocumentBuilder();
        final Set<String> elements = Set.of("svg", "title", "desc", "g",
                "path");
        final Set<String> attributes = Set.of("xmlns", "width", "height",
                "viewBox", "role", "aria-labelledby", "id", "transform", "d",
                "fill", "stroke", "stroke-width", "stroke-linecap",
                "stroke-linejoin");
        for (final ConstructionType type : ConstructionType.ALL) {
            final String name = type.name() + ".svg";
            expected.add(name);
            final Path source = directory.resolve(name);
            assertTrue(Files.isRegularFile(source), name);
            final var document = parser.parse(source.toFile());
            final var root = document.getDocumentElement();
            assertEquals("http://www.w3.org/2000/svg", root.getNamespaceURI(),
                    name);
            assertEquals("0 0 320 160", root.getAttribute("viewBox"), name);
            assertEquals("320", root.getAttribute("width"), name);
            assertEquals("160", root.getAttribute("height"), name);
            final var nodes = document.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                final var node = nodes.item(i);
                assertEquals(root.getNamespaceURI(), node.getNamespaceURI(),
                        name);
                assertTrue(elements.contains(node.getLocalName()),
                        name + ": unsupported element " + node.getNodeName());
                final var attrs = node.getAttributes();
                for (int j = 0; j < attrs.getLength(); j++) {
                    final var attribute = attrs.item(j);
                    assertTrue(attributes.contains(attribute.getNodeName()),
                            name + ": unsupported attribute "
                                    + attribute.getNodeName());
                }
            }
            final var paths = document
                    .getElementsByTagNameNS(root.getNamespaceURI(), "path");
            assertTrue(paths.getLength() > 0,
                    name + ": missing vector geometry");
            for (int i = 0; i < paths.getLength(); i++) {
                final var path = (org.w3c.dom.Element) paths.item(i);
                assertFalse(path.getAttribute("d").isBlank(), name);
                assertTrue(path.getAttribute("fill")
                        .matches("none|#[0-9a-fA-F]{6}"), name);
            }
        }
        try (var sources = Files.list(directory)) {
            assertEquals(expected,
                    sources.map(path -> path.getFileName().toString())
                            .filter(name -> name.endsWith(".svg"))
                            .collect(java.util.stream.Collectors.toSet()));
        }
    }

    @Test
    void everyBuildingLevelHasDistinctTransparentArtwork() throws Exception {
        final Set<Integer> pixels = new HashSet<>();
        for (final ConstructionType type : ConstructionType.ALL) {
            final var url = getClass().getResource(
                    "/images/colony-buildings/" + type.name() + ".png");
            assertNotNull(url, type.name());
            final BufferedImage image = ImageIO.read(url);
            assertEquals(320, image.getWidth(), type.name());
            assertEquals(160, image.getHeight(), type.name());
            assertTrue(image.getColorModel().hasAlpha(), type.name());
            assertEquals(0, image.getRGB(0, 0) >>> 24, type.name());
            for (int x = 0; x < image.getWidth(); x++) {
                assertEquals(0, image.getRGB(x, 0) >>> 24,
                        type.name() + ": artwork touches top edge");
                assertEquals(0, image.getRGB(x, 159) >>> 24,
                        type.name() + ": artwork touches bottom edge");
            }
            for (int y = 0; y < image.getHeight(); y++) {
                assertEquals(0, image.getRGB(0, y) >>> 24,
                        type.name() + ": artwork touches left edge");
                assertEquals(0, image.getRGB(319, y) >>> 24,
                        type.name() + ": artwork touches right edge");
            }
            final int[] data = image.getRGB(0, 0, 320, 160, null, 0, 320);
            assertTrue(java.util.Arrays.stream(data)
                    .anyMatch(pixel -> (pixel >>> 24) > 200), type.name());
            assertTrue(pixels.add(java.util.Arrays.hashCode(data)),
                    "Reused artwork: " + type.name());
        }
        assertEquals(41, pixels.size());
    }
}
