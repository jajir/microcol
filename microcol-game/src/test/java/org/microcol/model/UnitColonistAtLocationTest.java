package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;


import com.google.common.collect.Lists;

public class UnitColonistAtLocationTest extends AbstractUnitFreeColonistTest {

    protected PlaceLocation placeLocation = mock(PlaceLocation.class);

    @Test
    public void testInitialization() {
        makeColonist(model, 23, placeLocation, owner, 1);

        when(placeLocation.getLocation()).thenReturn(Location.of(4, 3));

        assertNotNull(unit);
        assertEquals(Location.of(4, 3), unit.getLocation());
        assertEquals(owner, unit.getOwner());
    }

    @Test
    public void testPlaceAtMap() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 1);
        
        assertTrue(unit.isAtPlaceLocation());
    }

    @Test
    public void testIsMovable() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 1);

        final Location loc = Location.of(10, 12);

        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(loc)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(loc)).thenReturn(TerrainType.GRASSLAND);
        when(owner.getEnemyUnitsAt(loc)).thenReturn(new ArrayList<>());

        assertTrue(unit.isPossibleToMoveAt(loc));
    }

    @Test
    public void testIsMovable_thereAreEnemyUnit() throws Exception {
        makeColonist(model, 23, placeLocation, owner, 1);

        final Location loc = Location.of(10, 12);

        when(model.getMap()).thenReturn(worldMap);
        when(worldMap.isValid(loc)).thenReturn(true);
        when(worldMap.getTerrainTypeAt(loc)).thenReturn(TerrainType.GRASSLAND);
        when(owner.getEnemyUnitsAt(loc)).thenReturn(Lists.newArrayList(unit));

        assertFalse(unit.isPossibleToMoveAt(loc));
    }

}