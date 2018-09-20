package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class BuildingStatusTest {

    private @Mocked ColonyBuildingItem item;

    @Test
    public void test_constructor() throws Exception {
        mock(200, 100);
        final BuildingStatus<ColonyBuildingItem> stats = new BuildingStatus<ColonyBuildingItem>(
                item, 50, 10, 12, 3);

        assertEquals(100, stats.getTools().getRequired());
        assertEquals(12, stats.getTools().getAlreadyHave());
        assertEquals(3, stats.getTools().getBuildPerTurn());
        assertEquals(Integer.valueOf(30), stats.getTools().getTurnsToComplete().get());

        assertEquals(200, stats.getHammers().getRequired());
        assertEquals(50, stats.getHammers().getAlreadyHave());
        assertEquals(10, stats.getHammers().getBuildPerTurn());
        assertEquals(Integer.valueOf(15), stats.getHammers().getTurnsToComplete().get());
                
        assertEquals(Integer.valueOf(30), stats.getTurnsToComplete().get());
    }

    @Test
    public void test_constructor_neverFinish() throws Exception {
        mock(200, 100);
        final BuildingStatus<ColonyBuildingItem> stats = new BuildingStatus<ColonyBuildingItem>(
                item, 50, 10, 12, 0);

        assertEquals(100, stats.getTools().getRequired());
        assertEquals(12, stats.getTools().getAlreadyHave());
        assertEquals(0, stats.getTools().getBuildPerTurn());
        assertFalse(stats.getTools().getTurnsToComplete().isPresent());

        assertEquals(200, stats.getHammers().getRequired());
        assertEquals(50, stats.getHammers().getAlreadyHave());
        assertEquals(10, stats.getHammers().getBuildPerTurn());
        assertEquals(Integer.valueOf(15), stats.getHammers().getTurnsToComplete().get());
                
        assertFalse(stats.getTurnsToComplete().isPresent());
    }

    private void mock(final int requiredHammers, final int requiredTools) {
        new Expectations() {
            {
                item.getRequiredHammers();
                result = requiredHammers;
                item.getRequiredTools();
                result = requiredTools;
            }
        };
    }

}
