package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.microcol.model.ResourceOverlay;

class ResourceOverlayNamesTest {

    @Test
    void everyResourceKindHasANameKey() {
        for (final ResourceOverlay overlay : ResourceOverlay.values()) {
            if (overlay != ResourceOverlay.NONE) {
                assertEquals(overlay.name() + "_name",
                        ResourceOverlayNames.getName(overlay).name());
            }
        }
    }

    @Test
    void emptySquareHasNoNameKey() {
        assertThrows(IllegalArgumentException.class,
                () -> ResourceOverlayNames.getName(ResourceOverlay.NONE));
    }
}
