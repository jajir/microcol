package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFreeColonist.EquipmentProblem;

class ColonistEquipmentTest {
    private final Model model = mock(Model.class);
    private final Player player = mock(Player.class);
    private final Colony colony = mock(Colony.class);
    private final Location location = Location.of(24, 16);
    private ColonyWarehouse warehouse;
    private UnitFreeColonist unit;

    @BeforeEach
    void prepareOwnColonistAtColony() {
        when(colony.isValid()).thenReturn(true);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getWarehouseType())
                .thenReturn(ConstructionType.WAREHOUSE_BASIC);
        warehouse = new ColonyWarehouse(colony,
                Map.of("MUSKET", 100, "HORSE", 100, "TOOLS", 80));
        when(colony.getWarehouse()).thenReturn(warehouse);
        when(model.isGameRunning()).thenReturn(true);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(model.getColonyAt(location)).thenReturn(Optional.of(colony));
        unit = new UnitFreeColonist(model, 4,
                value -> new PlaceLocation(value, location, Direction.east),
                player, 1, new UnitActionNoAction(), 0, false, false);
        when(player.getAllUnits()).thenReturn(List.of(unit));
    }

    @Test
    void previewDoesNotMutateAndSwitchReturnsIncompatibleEquipment() {
        assertEquals(Map.of(GoodsType.MUSKET, -50),
                unit.getEquipmentGoodsChange(GoodsType.MUSKET, true));
        assertEquals(100, stock(GoodsType.MUSKET));
        assertFalse(unit.isHoldingGuns());
        unit.equipWithMuskets();
        unit.equipWithHorses();
        assertTrue(unit.isHoldingGuns());
        assertTrue(unit.isMounted());
        assertEquals(
                Map.of(GoodsType.TOOLS, -80, GoodsType.MUSKET, 50,
                        GoodsType.HORSE, 50),
                unit.getEquipmentGoodsChange(GoodsType.TOOLS, true));
        unit.equipWithTools();
        assertEquals(80, unit.getTools());
        assertFalse(unit.isHoldingGuns());
        assertFalse(unit.isMounted());
        assertEquals(100, stock(GoodsType.MUSKET));
        assertEquals(100, stock(GoodsType.HORSE));
        assertEquals(0, stock(GoodsType.TOOLS));
        assertEquals(4, unit.getId());
        assertEquals(UnitType.COLONIST, unit.getType());
        assertSame(player, unit.getOwner());
    }

    @ParameterizedTest
    @MethodSource("equipmentTypes")
    void repeatAndMissingEquipmentCannotCreateOrConsumeStock(
            final GoodsType type) {
        assertEquals(Optional.of(EquipmentProblem.ALREADY_SET),
                unit.getEquipmentProblem(type, false));
        assertThrows(IllegalStateException.class, () -> change(type, false));
        final int before = stock(type);
        change(type, true);
        final int after = stock(type);
        assertEquals(Optional.of(EquipmentProblem.ALREADY_SET),
                unit.getEquipmentProblem(type, true));
        assertThrows(IllegalStateException.class, () -> change(type, true));
        assertEquals(after, stock(type));
        change(type, false);
        assertEquals(before, stock(type));
    }

    @ParameterizedTest
    @MethodSource("equipmentTypes")
    void missingGoodsAreRejectedBeforeAnyEquipmentOrInventoryChange(
            final GoodsType type) {
        warehouse.setGoods(Goods.of(type, type == GoodsType.TOOLS ? 0 : 49));
        final int before = stock(type);
        assertEquals(Optional.of(EquipmentProblem.MISSING_GOODS),
                unit.getEquipmentProblem(type, true));
        assertThrows(IllegalStateException.class, () -> change(type, true));
        assertEquals(before, stock(type));
        assertFalse(unit.isHoldingGuns());
        assertFalse(unit.isMounted());
        assertEquals(0, unit.getTools());
    }

    @Test
    void secondReturnOverflowRejectsEntireSwitch() {
        unit.equipWithMuskets();
        unit.equipWithHorses();
        warehouse.addGoods(Goods.of(GoodsType.HORSE, 1));
        assertEquals(Optional.of(EquipmentProblem.STORAGE_FULL),
                unit.getEquipmentProblem(GoodsType.TOOLS, true));
        assertThrows(IllegalStateException.class, unit::equipWithTools);
        assertEquals(50, stock(GoodsType.MUSKET));
        assertEquals(51, stock(GoodsType.HORSE));
        assertEquals(80, stock(GoodsType.TOOLS));
        assertTrue(unit.isMounted());
        assertTrue(unit.isHoldingGuns());
    }

    @Test
    void returnedToolsMustFitBeforeConsumingHorses() {
        unit.equipWithTools();
        warehouse.addGoods(Goods.of(GoodsType.TOOLS, 21));
        assertThrows(IllegalStateException.class, unit::equipWithHorses);
        assertEquals(80, unit.getTools());
        assertEquals(21, stock(GoodsType.TOOLS));
        assertEquals(100, stock(GoodsType.HORSE));
        warehouse.removeGoods(Goods.of(GoodsType.TOOLS, 1));
        unit.equipWithHorses();
        assertEquals(100, stock(GoodsType.TOOLS));
        assertEquals(50, stock(GoodsType.HORSE));
        assertEquals(0, unit.getTools());
    }

    @Test
    void changedTurnRemovedUnitAndForeignOrDestroyedColonyRejectActions() {
        when(model.getCurrentPlayer()).thenReturn(mock(Player.class));
        assertUnavailable();
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getAllUnits()).thenReturn(List.of());
        assertUnavailable();
        when(player.getAllUnits()).thenReturn(List.of(unit));
        when(colony.getOwner()).thenReturn(mock(Player.class));
        assertUnavailable();
        when(colony.getOwner()).thenReturn(player);
        when(colony.isValid()).thenReturn(false);
        assertUnavailable();
        when(colony.isValid()).thenReturn(true);
        when(model.isGameRunning()).thenReturn(false);
        assertUnavailable();
    }

    @Test
    void automaticUnequipPreflightsAllReturnedEquipment() {
        unit.equipWithMuskets();
        unit.equipWithHorses();
        warehouse.addGoods(Goods.of(GoodsType.HORSE, 1));
        final ConstructionSlot empty = mock(ConstructionSlot.class);
        when(empty.isEmpty()).thenReturn(true);
        when(empty.isValid()).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> unit.placeToColonyStructureSlot(empty));
        assertEquals(50, stock(GoodsType.MUSKET));
        assertEquals(51, stock(GoodsType.HORSE));
        assertTrue(unit.isHoldingGuns());
        assertTrue(unit.isMounted());
        assertEquals(location, unit.getLocation());
    }

    @Test
    void previewIsImmutableAndToolsAreLimitedToOneHundred() {
        when(colony.getWarehouseType()).thenReturn(ConstructionType.WAREHOUSE);
        warehouse.setGoods(Goods.of(GoodsType.TOOLS, 150));
        final Map<GoodsType, Integer> preview = unit
                .getEquipmentGoodsChange(GoodsType.TOOLS, true);
        assertEquals(Map.of(GoodsType.TOOLS, -100), preview);
        assertThrows(UnsupportedOperationException.class,
                () -> preview.put(GoodsType.TOOLS, -150));
        unit.equipWithTools();
        assertEquals(100, unit.getTools());
        assertEquals(50, stock(GoodsType.TOOLS));
    }

    @ParameterizedTest
    @CsvSource({ "0,0", "1,0", "19,0", "20,20", "99,99", "100,100", "101,100" })
    void toolsBoundaryTransfersExactlyOrRejectsAtomically(final int stock,
            final int expected) {
        when(colony.getWarehouseType()).thenReturn(ConstructionType.WAREHOUSE);
        warehouse.setGoods(Goods.of(GoodsType.TOOLS, stock));
        unit.equipWithMuskets();
        unit.equipWithHorses();
        if (expected == 0) {
            assertEquals(Optional.of(EquipmentProblem.MISSING_GOODS),
                    unit.getEquipmentProblem(GoodsType.TOOLS, true));
            assertThrows(IllegalStateException.class, unit::equipWithTools);
            assertEquals(stock, stock(GoodsType.TOOLS));
            assertEquals(50, stock(GoodsType.MUSKET));
            assertEquals(50, stock(GoodsType.HORSE));
            assertTrue(unit.isHoldingGuns());
            assertTrue(unit.isMounted());
            assertEquals(0, unit.getTools());
        } else {
            assertTrue(
                    unit.getEquipmentProblem(GoodsType.TOOLS, true).isEmpty());
            assertEquals(
                    Map.of(GoodsType.TOOLS, -expected, GoodsType.MUSKET, 50,
                            GoodsType.HORSE, 50),
                    unit.getEquipmentGoodsChange(GoodsType.TOOLS, true));
            unit.equipWithTools();
            assertEquals(expected, unit.getTools());
            assertEquals(stock - expected, stock(GoodsType.TOOLS));
            assertEquals(100, stock(GoodsType.MUSKET));
            assertEquals(100, stock(GoodsType.HORSE));
            assertFalse(unit.isHoldingGuns());
            assertFalse(unit.isMounted());
            assertEquals(4, unit.getId());
            assertEquals(UnitType.COLONIST, unit.getType());
            unit.unequipWithTools();
            assertEquals(stock, stock(GoodsType.TOOLS));
            assertEquals(0, unit.getTools());
        }
    }

    @Test
    void invalidWorkDestinationCannotUnequipBeforeRejectingPlacement() {
        unit.equipWithMuskets();
        final ColonyField occupied = new ColonyField(model, Direction.west,
                colony);
        occupied.setPlaceColonyField(mock(PlaceColonyField.class));
        assertThrows(IllegalStateException.class,
                () -> unit.placeToColonyField(occupied, GoodsType.CORN));
        assertTrue(unit.isHoldingGuns());
        assertEquals(50, stock(GoodsType.MUSKET));
        final ConstructionSlot destroyed = mock(ConstructionSlot.class);
        when(destroyed.isEmpty()).thenReturn(true);
        when(destroyed.isValid()).thenReturn(false);
        assertThrows(IllegalStateException.class,
                () -> unit.placeToColonyStructureSlot(destroyed));
        assertTrue(unit.isHoldingGuns());
        assertEquals(50, stock(GoodsType.MUSKET));
    }

    @Test
    void invalidProductionCannotUnequipOrDetachUnit() {
        unit.equipWithMuskets();
        final WorldMap map = mock(WorldMap.class);
        when(model.getMap()).thenReturn(map);
        when(model.isExists(colony)).thenReturn(true);
        when(colony.getLocation()).thenReturn(location);
        when(map.isValid(location.add(Direction.west.getVector())))
                .thenReturn(true);
        when(map.getTerrainTypeAt(any(Location.class)))
                .thenReturn(TerrainType.GRASSLAND);
        when(map.getTerrainAt(any(Location.class))).thenReturn(
                new Terrain(location.add(Direction.west.getVector()),
                        TerrainType.GRASSLAND));
        final ColonyField field = new ColonyField(model, Direction.west,
                colony);
        assertThrows(IllegalArgumentException.class,
                () -> unit.placeToColonyField(field, GoodsType.MUSKET));
        assertEquals(location, unit.getLocation());
        assertTrue(unit.isHoldingGuns());
        assertEquals(50, stock(GoodsType.MUSKET));
        assertTrue(field.isEmpty());
    }

    private void assertUnavailable() {
        assertEquals(Optional.of(EquipmentProblem.UNIT_UNAVAILABLE),
                unit.getEquipmentProblem(GoodsType.MUSKET, true));
        assertThrows(IllegalStateException.class, unit::equipWithMuskets);
        assertEquals(100, stock(GoodsType.MUSKET));
        assertFalse(unit.isHoldingGuns());
    }

    private int stock(final GoodsType type) {
        return warehouse.getGoods(type).getAmount();
    }

    static java.util.stream.Stream<GoodsType> equipmentTypes() {
        return java.util.stream.Stream.of(GoodsType.MUSKET, GoodsType.HORSE,
                GoodsType.TOOLS);
    }

    private void change(final GoodsType type, final boolean equip) {
        if (type == GoodsType.MUSKET) {
            if (equip)
                unit.equipWithMuskets();
            else
                unit.unequipWithMuskets();
        } else if (type == GoodsType.HORSE) {
            if (equip)
                unit.equipWithHorses();
            else
                unit.unequipWithHorses();
        } else if (type == GoodsType.TOOLS) {
            if (equip)
                unit.equipWithTools();
            else
                unit.unequipWithTools();
        } else {
            throw new IllegalArgumentException();
        }
    }
}
