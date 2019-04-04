package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.function.Consumer;

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

    @SuppressWarnings("unchecked")
    @Test
    void test_countTurnProduction_all_production_could_be_stored() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 0));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));

        field.countTurnProduction(warehouse);

        final ArgumentCaptor<Goods> acGoods = ArgumentCaptor.forClass(Goods.class);
        final ArgumentCaptor<Consumer<Goods>> acConsumerGoods = ArgumentCaptor
                .forClass(Consumer.class);
        verify(warehouse, times(1)).addGoodsWithThrowingAway(acGoods.capture(),
                acConsumerGoods.capture());
        assertEquals(Goods.of(GoodsType.CORN, 5), acGoods.getValue());
        verify(model, never()).addTurnEvent(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void test_countTurnProduction_part_of_production_could_be_stored() throws Exception {
        mockTurnProduction(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 98));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));

        field.countTurnProduction(warehouse);

        final ArgumentCaptor<Goods> acGoods = ArgumentCaptor.forClass(Goods.class);
        final ArgumentCaptor<Consumer<Goods>> acConsumerGoods = ArgumentCaptor
                .forClass(Consumer.class);
        verify(warehouse, times(1)).addGoodsWithThrowingAway(acGoods.capture(),
                acConsumerGoods.capture());
        assertEquals(Goods.of(GoodsType.CORN, 5), acGoods.getValue());

        final ArgumentCaptor<TurnEvent> acTurnEvent = ArgumentCaptor.forClass(TurnEvent.class);
        /*
         * Following code is not called because method addGoodsWithThrowingAway
         * is mocked.
         */
        verify(model, never()).addTurnEvent(acTurnEvent.capture());
    }

    @Test
    void test_countTurnProduction_produce_nothing_couldBeStored() throws Exception {
        mockTurnProduction_old(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 100));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));
        when(colony.getOwner()).thenReturn(player);
        when(colony.getName()).thenReturn("Prague");

        field.countTurnProduction(warehouse);

        verify(warehouse, never()).addGoods(any());
    }

    @Test
    void test_countTurnProduction_warehouse_is_overloded() throws Exception {
        mockTurnProduction_old(GoodsType.CORN, 5);
        when(warehouse.getGoods(GoodsType.CORN)).thenReturn(Goods.of(GoodsType.CORN, 150));
        when(warehouse.getStorageCapacity(GoodsType.CORN))
                .thenReturn(Goods.of(GoodsType.CORN, 100));
        when(colony.getOwner()).thenReturn(player);
        when(colony.getName()).thenReturn("Prague");

        field.countTurnProduction(warehouse);

        verify(warehouse, never()).addGoods(any());
    }

    @Test
    void test_getProduction() throws Exception {
        mockTurnProduction(GoodsType.COTTON, 10);
        Optional<Goods> ret = field.getProduction();

        assertNotNull(ret);
        assertTrue(ret.isPresent());
        assertEquals(GoodsType.COTTON, ret.get().getType());
        assertEquals(10, ret.get().getAmount());
    }

    private void mockTurnProduction(final GoodsType goodsType, final int amount) {
        field.setPlaceColonyField(placeColonyField);
        when(placeColonyField.getProducedGoodsType()).thenReturn(goodsType);
        when(model.getMap()).thenReturn(map);
        when(colony.getLocation()).thenReturn(colonyLocation);
        when(map.getTerrainAt(colonyLocation.add(direction.getVector()))).thenReturn(terrain);
        when(terrain.canProduceAmmount(goodsType)).thenReturn(amount);
    }

    private void mockTurnProduction_old(final GoodsType goodsType, final int amount) {
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
