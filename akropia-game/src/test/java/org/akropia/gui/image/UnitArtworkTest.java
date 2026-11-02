package org.akropia.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.akropia.gui.util.StreamReader;
import org.akropia.model.Direction;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.Place;
import org.akropia.model.unit.UnitActionType;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;
import org.akropia.model.unit.UnitArtillery;
import org.akropia.model.unit.UnitFreeColonist;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;

class UnitArtworkTest {
    private final ImageCache cache = new ImageCache(new StreamReader());
    private final UnitImageProvider images;

    UnitArtworkTest() {
        new ImageLoaderUnit().preload(cache);
        images = new UnitImageProvider(cache);
    }

    @Test
    void allTypesAndStatesHaveDistinctBoldArtworkInsideTransparentMargins() {
        final Set<Long> fingerprints = new HashSet<>();
        for (final UnitType type : UnitType.UNIT_TYPES) {
            verifyArtwork(images.getUnitImage(type), fingerprints, type.name());
        }
        for (final String key : List.of(
                ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED,
                ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_TOOLS,
                ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_WEAPONS,
                ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED_WEAPONS,
                "artillery-damaged.png")) {
            verifyArtwork(cache.getImage(key), fingerprints, key);
        }
        assertEquals(40, fingerprints.size());
    }

    private void verifyArtwork(final Image image, final Set<Long> fingerprints,
            final String label) {
        assertEquals(45, image.getWidth(), label);
        assertEquals(45, image.getHeight(), label);
        long fingerprint = 1;
        int ink = 0;
        for (int y = 0; y < 45; y++) {
            for (int x = 0; x < 45; x++) {
                final Color color = image.getPixelReader().getColor(x, y);
                fingerprint = 31 * fingerprint
                        + image.getPixelReader().getArgb(x, y);
                if (x < 2 || y < 2 || x >= 43 || y >= 43) {
                    assertEquals(0, color.getOpacity(),
                            label + " transparent margin");
                }
                if (color.getOpacity() > .86 && color.getRed() < .26
                        && color.getGreen() < .26 && color.getBlue() < .26) {
                    ink++;
                }
            }
        }
        assertTrue(ink >= 100,
                label + " retains substantial opaque ink at 45 px");
        assertTrue(fingerprints.add(fingerprint),
                label + " has its own artwork");
    }

    @Test
    void actualEquipmentOverridesProfessionAndRemovalRestoresIt() {
        final UnitFreeColonist unit = mock(UnitFreeColonist.class);
        when(unit.getType()).thenReturn(UnitType.EXPERT_FARMER);
        assertSame(images.getUnitImage(UnitType.EXPERT_FARMER),
                images.getUnitImage(unit, Direction.east));
        when(unit.isHoldingTools()).thenReturn(true);
        assertSame(cache.getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_TOOLS),
                images.getUnitImage(unit, Direction.east));
        when(unit.isHoldingWeapons()).thenReturn(true);
        assertSame(
                cache.getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_WEAPONS),
                images.getUnitImage(unit, Direction.east));
        when(unit.isMounted()).thenReturn(true);
        assertSame(
                cache.getImage(
                        ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED_WEAPONS),
                images.getUnitImage(unit, Direction.east));
        when(unit.isHoldingWeapons()).thenReturn(false);
        assertSame(
                cache.getImage(ImageLoaderUnit.IMG_UNIT_FREE_COLONIST_MOUNTED),
                images.getUnitImage(unit, Direction.east));
        when(unit.isMounted()).thenReturn(false);
        when(unit.isHoldingTools()).thenReturn(false);
        assertSame(images.getUnitImage(UnitType.EXPERT_FARMER),
                images.getUnitImage(unit, Direction.east));
        final Unit invalid = mock(Unit.class);
        when(invalid.getType()).thenReturn(UnitType.COLONIST);
        assertThrows(IllegalStateException.class,
                () -> images.getUnitImage(invalid, Direction.east));
        assertThrows(NullPointerException.class,
                () -> images.getUnitImage(unit, null));
    }

    private UnitArtillery artillery(final boolean damaged) {
        return new UnitArtillery(mock(Model.class), 1,
                unit -> mock(Place.class), mock(Player.class), 3,
                UnitActionType.noAction.make(), damaged);
    }

    @Test
    void damageAndExistingWestwardShipOrientationRemainVisible() {
        final UnitArtillery gun = artillery(false);
        assertSame(images.getUnitImage(UnitType.ARTILLERY),
                images.getUnitImage(gun, Direction.east));
        assertSame(cache.getImage("artillery-damaged.png"),
                images.getUnitImage(artillery(true), Direction.east));
        for (final UnitType type : List.of(UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.GALLEON)) {
            final Unit ship = mock(Unit.class);
            when(ship.getType()).thenReturn(type);
            final Image east = images.getUnitImage(ship, Direction.east);
            final Image west = images.getUnitImage(ship, Direction.west);
            for (int y = 0; y < 45; y++) {
                for (int x = 0; x < 45; x++) {
                    assertEquals(east.getPixelReader().getArgb(x, y),
                            west.getPixelReader().getArgb(44 - x, y),
                            type.name());
                }
            }
        }
    }
}
