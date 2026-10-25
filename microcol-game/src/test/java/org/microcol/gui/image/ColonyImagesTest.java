package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.microcol.model.EuropeanNation;

class ColonyImagesTest {
    @Test
    void populationBoundariesAreIndependentOfDefense() {
        final int[] populations = { 0, 1, 3, 4, 7, 8, 30 };
        final int[] rows = { 0, 0, 0, 1, 1, 2, 2 };
        for (int defense = 0; defense <= 3; defense++) {
            for (int i = 0; i < populations.length; i++) {
                assertEquals("colony_" + defense + "_" + rows[i],
                        ImageLoaderColonies.colonyKey(populations[i], defense));
            }
        }
        assertThrows(IllegalArgumentException.class,
                () -> ImageLoaderColonies.colonyKey(-1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> ImageLoaderColonies.colonyKey(1, 4));
    }

    @Test
    void flagsMatchTheAtlasOrder() {
        assertEquals("colony_0_3",
                ImageLoaderColonies.flagKey(EuropeanNation.DUTCH));
        assertEquals("colony_1_3",
                ImageLoaderColonies.flagKey(EuropeanNation.FRENCH));
        assertEquals("colony_2_3",
                ImageLoaderColonies.flagKey(EuropeanNation.ENGLISH));
        assertEquals("colony_3_3",
                ImageLoaderColonies.flagKey(EuropeanNation.SPANISH));
    }
}
