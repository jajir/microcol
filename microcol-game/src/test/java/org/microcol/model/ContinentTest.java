package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ContinentTest {

    @Test
    void discoveryIncludesIsolatedAndConnectedStartingSquaresExactlyOnce() {
        final org.microcol.model.store.ModelPo data = new org.microcol.model.store.ModelPo();
        final var map = data.getMap();
        map.setMaxX(6);
        map.setMaxY(6);
        map.setSeed(1);
        final Location island = Location.of(2, 2);
        final Location first = Location.of(4, 4);
        final Location second = Location.of(4, 5);
        map.setTerrainType(java.util.Map.of(island, TerrainType.GRASSLAND,
                first, TerrainType.GRASSLAND, second, TerrainType.GRASSLAND));
        map.setTrees(java.util.Set.of());
        map.setFields(java.util.Set.of());
        final Model model = mock(Model.class);
        when(model.getMap()).thenReturn(new WorldMap(data));
        final Unit unit = mock(Unit.class);
        when(unit.getLocation()).thenReturn(island);

        final Continents continents = new ContinentTool().findContinents(model);

        assertEquals(2, continents.getContinents().size());
        assertEquals(java.util.List.of(island),
                continents.getContinentWhereIsUnitPlaced(unit).getLocations());
        final java.util.List<Location> connected = continents
                .getForLocation(first).orElseThrow().getLocations();
        assertEquals(2, connected.size());
        assertEquals(java.util.Set.of(first, second),
                java.util.Set.copyOf(connected));
    }

    @Test
    void containsAddedLocations() {
        final Continent continent = new Continent(mock(Model.class));
        final Location first = Location.of(22, 12);
        final Location second = Location.of(23, 17);
        continent.add(first);
        continent.add(second);
        assertTrue(continent.contains(first));
        assertTrue(continent.contains(second));
    }
}
