package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.turnevent.TurnEvent;
import org.mockito.ArgumentCaptor;

public class ColonyFieldTest {

    private final Colony colony = mock(Colony.class);

    private final Direction direction = Direction.northWest;

    private final Location colonyLocation = Location.of(5, 7);

    private final Model model = mock(Model.class);

    private final Player player = mock(Player.class);

    private final WorldMap map = mock(WorldMap.class);

    private final Terrain terrain = mock(Terrain.class);

    private final ColonyWarehouse warehouse = mock(ColonyWarehouse.class);

    private final PlaceColonyField placeColonyField = mock(PlaceColonyField.class);

    private ColonyField field;

    @Test
    void test_countTurnProduction_warehouse_isNull() throws Exception {

        assertThrows(NullPointerException.class, () -> field.countTurnProduction(null));

        verify(placeColonyField, never()).getProducedGoodsType();
        verify(model, never()).getMap();
        verify(warehouse, never()).addGoods(any());
        verify(model, never()).addTurnEvent(any());
    }

    @Test
    void test_countTurnProduction_production_couldBeStored() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 0));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));

        field.countTurnProduction(warehouse);

        verify(warehouse).addGoods(Goods.of(GoodsType.CORN, 5));
        verify(model, never()).addTurnEvent(any());
    }

    @Test
    void test_countTurnProduction_part_of_production_couldBeStored() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 98));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));
        when(colony.getOwner()).thenReturn(player);
        when(colony.getName()).thenReturn("Prague");

        field.countTurnProduction(warehouse);

        final ArgumentCaptor<TurnEvent> acTurnEvent = ArgumentCaptor.forClass(TurnEvent.class);
        verify(model, times(1)).addTurnEvent(acTurnEvent.capture());

        final TurnEvent event = acTurnEvent.getValue();
        assertNotNull(event);
        assertEquals(3, event.getArgs().length);
        assertEquals(3, (Integer) event.getArgs()[0]);
        assertEquals(GoodsType.CORN, event.getArgs()[1]);
        assertEquals("Prague", event.getArgs()[2]);
        verify(warehouse).addGoods(Goods.of(GoodsType.CORN, 2));
    }

    @Test
    void test_countTurnProduction_produce_nothing_couldBeStored() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 100));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));
        when(colony.getOwner()).thenReturn(player);
        when(colony.getName()).thenReturn("Prague");

        field.countTurnProduction(warehouse);

        final ArgumentCaptor<TurnEvent> acTurnEvent = ArgumentCaptor.forClass(TurnEvent.class);
        verify(model, times(1)).addTurnEvent(acTurnEvent.capture());

        final TurnEvent event = acTurnEvent.getValue();
        assertNotNull(event);
        assertEquals(3, event.getArgs().length);
        assertEquals(5, (Integer) event.getArgs()[0]);
        assertEquals(GoodsType.CORN, event.getArgs()[1]);
        assertEquals("Prague", event.getArgs()[2]);

        verify(warehouse, never()).addGoods(any());
    }

    @Test
    void test_countTurnProduction_warehouse_is_overloded() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 150));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));
        when(colony.getOwner()).thenReturn(player);
        when(colony.getName()).thenReturn("Prague");

        field.countTurnProduction(warehouse);

        final ArgumentCaptor<TurnEvent> acTurnEvent = ArgumentCaptor.forClass(TurnEvent.class);
        verify(model, times(1)).addTurnEvent(acTurnEvent.capture());

        final TurnEvent event = acTurnEvent.getValue();
        assertNotNull(event);
        assertEquals(3, event.getArgs().length);
        assertEquals(5, (Integer) event.getArgs()[0]);
        assertEquals(GoodsType.CORN, event.getArgs()[1]);
        assertEquals("Prague", event.getArgs()[2]);

        verify(warehouse, never()).addGoods(any());
    }

    private void mockTurnProduction(final GoodsType goodsType, final int amount) {
        when(placeColonyField.getProducedGoodsType()).thenReturn(goodsType);
        when(model.getMap()).thenReturn(map);
        when(colony.getLocation()).thenReturn(colonyLocation);
        when(map.getTerrainAt(colonyLocation.add(direction.getVector()))).thenReturn(terrain);
        when(terrain.canProduceAmmount(goodsType)).thenReturn(amount);
    }

    @BeforeEach
    void setup() {
        field = new ColonyField(model, direction, colony);
        field.setPlaceColonyField(placeColonyField);
    }

    @AfterEach
    void tearDown() {
        field = null;
    }

}
