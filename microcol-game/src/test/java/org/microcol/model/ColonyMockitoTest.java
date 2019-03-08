package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In test is created colony. Colony doesn't have any units.
 */
public class ColonyMockitoTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Model model;

    private WorldMap map;

    private Player owner;

    @Test
    public void verify_that_passed_costructions_are_builded() throws Exception {
        final Colony colony = makeColony(col -> {
            List<Construction> list = new ArrayList<>();
            list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
            list.add(Construction.build(model, col, ConstructionType.CATHEDRAL));
            return list;
        });

        assertNotNull(colony);
        assertTrue(colony.isContainsConstructionByType(ConstructionType.BLACKSMITHS_SHOP));
        assertTrue(colony.isContainsConstructionByType(ConstructionType.CATHEDRAL));
        assertTrue(colony.isContainsConstructionByType(ConstructionType.BLACKSMITHS_HOUSE));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.IRON_WORKS));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DOCK));
    }

    @Test
    public void verify_that_colony_is_created_at_correct_location() throws Exception {
        final Colony colony = makeDefaultColony();

        assertEquals(25, colony.getLocation().getX());
        assertEquals(35, colony.getLocation().getY());
    }

    @Test
    public void verify_terrain_in_colony_fields() throws Exception {
        final Colony colony = makeDefaultColony();
        final ColonyField colonyField = colony.getColonyFields().get(0);
        mockFileds();

        assertEquals(3, colonyField.getGoodsTypeProduction(GoodsType.CORN));
        assertEquals(TerrainType.OCEAN, colonyField.getTerrainType());
    }

    @Test
    public void verify_that_unit_placed_at_filed_produce_corn() throws Exception {
        final Unit unit = mock(Unit.class);
        final Colony colony = makeDefaultColony();
        mockFileds();
        final ColonyField colonyField = colony
                .getColonyFieldInDirection(Direction.north.getVector());

        verifyCornProduction(colony, 0, 0);

        // place unit at ocean field, and define unit type
        when(unit.getType()).thenReturn(UnitType.COLONIST);
        PlaceColonyField place = new PlaceColonyField(unit, colonyField, GoodsType.CORN);
        colonyField.setPlaceColonyField(place);

        assertFalse(colonyField.isEmpty());
        verifyCornProduction(colony, 3, 2);
    }

    @Test
    public void verify_getEmptyFieldsWithMaxCornProduction_noUnits() throws Exception {
        final Colony colony = makeDefaultColony();
        mockFileds();

        final List<ColonyField> out = colony.getEmptyFieldsWithMaxCornProduction();

        assertEquals(2, out.size());
        assertEquals(TerrainType.GRASSLAND, out.get(0).getTerrainType());
        assertEquals(TerrainType.GRASSLAND, out.get(1).getTerrainType());
    }

    @Test
    public void verify_getEmptyFieldsWithMaxCornProduction_oneUnitAtGrassLand() throws Exception {
        final Unit unit = mock(Unit.class);
        final Colony colony = makeDefaultColony();
        mockFileds();

        final ColonyField colonyField = colony
                .getColonyFieldInDirection(Direction.east.getVector());

        // place unit at ocean field, and define unit type
        when(unit.getType()).thenReturn(UnitType.COLONIST);
        PlaceColonyField place = new PlaceColonyField(unit, colonyField, GoodsType.CORN);
        colonyField.setPlaceColonyField(place);

        final List<ColonyField> out = colony.getEmptyFieldsWithMaxCornProduction();

        assertEquals(1, out.size());
        assertEquals(TerrainType.GRASSLAND, out.get(0).getTerrainType());
    }

    @Test
    public void verify_getEmptyFieldsWithMaxCornProduction_twoUnitAtGrassLand() throws Exception {
        final Unit unit = mock(Unit.class);
        final Colony colony = makeDefaultColony();
        mockFileds();

        final ColonyField colonyFieldEast = colony
                .getColonyFieldInDirection(Direction.east.getVector());
        final ColonyField colonyFieldWest = colony
                .getColonyFieldInDirection(Direction.west.getVector());

        // place unit at ocean field, and define unit type
        when(unit.getType()).thenReturn(UnitType.COLONIST);
        PlaceColonyField place = new PlaceColonyField(unit, colonyFieldEast, GoodsType.CORN);
        colonyFieldEast.setPlaceColonyField(place);
        colonyFieldWest.setPlaceColonyField(place);

        final List<ColonyField> out = colony.getEmptyFieldsWithMaxCornProduction();

        assertEquals(3, out.size());
        assertEquals(TerrainType.OCEAN, out.get(0).getTerrainType());
        assertEquals(TerrainType.OCEAN, out.get(1).getTerrainType());
        assertEquals(TerrainType.OCEAN, out.get(2).getTerrainType());
    }

    @Test
    public void verify_getEmptyFieldsWithMaxCornProduction_allFieldsAreOccupied() throws Exception {
        final Unit unit = mock(Unit.class);
        final Colony colony = makeDefaultColony();
        mockFileds();

        // place unit at all fields
        when(unit.getType()).thenReturn(UnitType.COLONIST);
        Direction.getAll().forEach(direction -> {
            final ColonyField field = colony.getColonyFieldInDirection(direction.getVector());
            PlaceColonyField place = new PlaceColonyField(unit, field, GoodsType.CORN);
            field.setPlaceColonyField(place);
            assertFalse(field.isEmpty());
        });

        final List<ColonyField> out = colony.getEmptyFieldsWithMaxCornProduction();

        assertEquals(0, out.size(), "There should not be any field.");
    }

    /**
     * <pre>
     *   24   25   26
     * +----+----+----+
     * |    |    |    | 34
     * +----+----+----+
     * |    |    |    | 35
     * +----+----+----+
     * |    |    |    | 36
     * +----+----+----+
     * </pre>
     */
    private void mockFileds() {
        when(model.getMap()).thenReturn(map);
        recordTerranAtPlace(Location.of(24, 34), TerrainType.OCEAN);
        recordTerranAtPlace(Location.of(25, 34), TerrainType.OCEAN);
        recordTerranAtPlace(Location.of(26, 34), TerrainType.OCEAN);

        recordTerranAtPlace(Location.of(24, 35), TerrainType.GRASSLAND);
        // recordTerranAtPlace(Location.of(25, 35), TerrainType.GRASSLAND);
        recordTerranAtPlace(Location.of(26, 35), TerrainType.GRASSLAND);

        recordTerranAtPlace(Location.of(24, 36), TerrainType.SWAMP);
        recordTerranAtPlace(Location.of(25, 36), TerrainType.SWAMP);
        recordTerranAtPlace(Location.of(26, 36), TerrainType.SWAMP);
    }

    private void recordTerranAtPlace(final Location loc, final TerrainType terrainType) {
        when(map.getTerrainAt(loc)).thenReturn(new Terrain(loc, terrainType));
        when(map.getTerrainTypeAt(loc)).thenReturn(terrainType);
    }

    private void verifyCornProduction(final Colony colony, final int expectedRowProduction,
            final int expectedConsumed) {
        final GoodsProductionStats cornStats = colony.getGoodsStats().getStatsByType(GoodsType.CORN);
        logger.debug("Corn stats: " + cornStats);
        assertEquals(expectedRowProduction, cornStats.getRowProduction(),
                String.format("Corn row production was expected '%s' but is '%s'.",
                        expectedRowProduction, cornStats.getRowProduction()));
        assertEquals(expectedConsumed, cornStats.getConsumed(),
                String.format("Corn consumed was expected '%s' but is '%s'.", expectedConsumed,
                        cornStats.getConsumed()));
    }

    @BeforeEach
    public void before() {
        model = mock(Model.class);
        map = mock(WorldMap.class);
        owner = mock(Player.class);
    }

    @AfterEach
    public void after() {
        model = null;
        owner = null;
    }

    private Colony makeDefaultColony() {
        return makeColony(col -> ConstructionType.NEW_COLONY_CONSTRUCTIONS.stream()
                .map(type -> Construction.build(model, col, type)).collect(Collectors.toList()));
    }

    private Colony makeColony(final Function<Colony, List<Construction>> constructionsBuilder) {
        return new Colony(model, "Prague", owner, Location.of(25, 35), constructionsBuilder,
                new HashMap<String, Integer>(), new ArrayList<>());
    }
}
