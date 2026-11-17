package org.akropia.gui.image;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;

class TurnRowersArtworkTest {
    @Test
    void sixDistinctFramesFitTheWaitingViewport() {
        final Image image = new Image(
                getClass().getResourceAsStream("/images/turn-rowers.png"));
        assertFalse(image.isError());
        assertEquals(1881, image.getWidth());
        assertEquals(836, image.getHeight());
        final Set<Long> frames = new HashSet<>();
        for (int frame = 0; frame < 6; frame++) {
            long fingerprint = 1;
            for (int y = 2; y < 416; y++) {
                for (int x = 2; x < 625; x++) {
                    final int argb = image.getPixelReader()
                            .getArgb(frame % 3 * 627 + x, frame / 3 * 418 + y);
                    assertEquals(255, argb >>> 24);
                    fingerprint = fingerprint * 31 + argb;
                }
            }
            assertTrue(frames.add(fingerprint), "Each rowing phase differs");
        }
    }

    @Test
    void sourceContainsOnlyEditablePathsAndFlatPalette() throws Exception {
        final var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl", true);
        final var document = factory.newDocumentBuilder()
                .parse(Path.of("src/graphics/turn-rowers.svg").toFile());
        final var elements = document.getElementsByTagName("*");
        final Set<String> palette = Set.of("#000000", "#F3AA32", "#4D7FB2",
                "#FAECD2", "#FFFFFF");
        for (int i = 0; i < elements.getLength(); i++) {
            final var element = (org.w3c.dom.Element) elements.item(i);
            assertTrue(Set.of("svg", "title", "path")
                    .contains(element.getTagName()));
            if (element.getTagName().equals("path")) {
                assertTrue(palette.contains(element.getAttribute("fill")));
                assertFalse(element.getAttribute("d").isBlank());
            }
        }
        assertTrue(elements.getLength() > 6);
    }
}
