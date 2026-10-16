package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitEmbarkedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.gson.Gson;

/** Verify individual map transfers against live ownership and cargo links. */
public class UnitColonistEmbarkTest {
    private static final Location WATER = Location.of(49, 15);
    private static final Location SHORE = Location.of(48, 15);
    private static final Location FAR = Location.of(46, 15);
    private GameModelPo data;
    private Model model;
    private Unit unit;
    private Ship ship;
    private CargoSlot slot;
    private ModelListener listener;

    @BeforeEach
    void prepare() {
        data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        UnitPo carrier = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 3).findFirst().orElseThrow();
        carrier.setPlaceHighSeas(null);
        carrier.setDepartureLocation(null);
        carrier.setDepartureDirection(null);
        carrier.setPlaceMap(at(WATER));
        data.getModel().getUnits().stream().filter(u -> u.getId() == 16)
                .findFirst().orElseThrow().setAvailableMoves(2);
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(WATER, TerrainType.OCEAN);
        terrain.put(SHORE, TerrainType.GRASSLAND);
        terrain.put(FAR, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        restore();
    }

    private PlaceMapPo at(Location location) {
        PlaceMapPo place = new PlaceMapPo();
        place.setLocation(location);
        place.setOrientation(Direction.west);
        return place;
    }

    private void restore() {
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        unit = model.getUnitById(16);
        ship = (Ship) model.getUnitById(3);
        slot = ship.getCargo().getSlotByIndex(0);
        listener = mock(ModelListener.class);
        model.addListener(listener);
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private void rejected(Runnable action) {
        String before = snapshot();
        assertThrows(IllegalStateException.class, action::run);
        assertEquals(before, snapshot());
        verify(listener, never()).onUnitMovedStepStarted(any());
    }

    @Test
    public void test_embarkFromLocation_cargoSlot_is_null() {
        unit.placeToLocation(SHORE);
        String before = snapshot();
        assertThrows(NullPointerException.class,
                () -> unit.embarkFromLocation(null));
        assertEquals(before, snapshot());
    }

    @Test
    public void test_embarkFromLocation_cargoSlot_is_not_empty() {
        unit.placeToLocation(SHORE);
        rejected(() -> unit
                .embarkFromLocation(ship.getCargo().getSlotByIndex(1)));
    }

    @Test
    public void test_embarkFromLocation_unit_isNotAtMapLocation() {
        ship.placeToEuropePort(model.getEurope().getPort());
        unit.placeToEuropePortPier();
        rejected(() -> unit.embarkFromLocation(slot));
    }

    @Test
    public void test_embarkFromLocation_target_unit_isNotAtMapLocation() {
        unit.placeToLocation(SHORE);
        ship.placeToEuropePort(model.getEurope().getPort());
        rejected(() -> unit.embarkFromLocation(slot));
    }

    @Test
    public void test_embarkFromLocation_cargoSlow_belongs_to_different_owner() {
        unit.placeToLocation(SHORE);
        UnitPo foreign = new UnitPo();
        foreign.setId(100);
        foreign.setType(UnitType.GALLEON);
        foreign.setOwnerId("Dutch's King");
        foreign.setAvailableMoves(3);
        foreign.setAction(new UnitActionNoAction().save());
        foreign.setPlaceMap(at(Location.of(49, 16)));
        Ship other = (Ship) model.createUnit(model.save(), foreign);
        rejected(() -> unit
                .embarkFromLocation(other.getCargo().getSlotByIndex(0)));
    }

    @Test
    public void test_embarkFromLocation_embark_location_is_too_far() {
        unit.placeToLocation(FAR);
        rejected(() -> unit.embarkFromLocation(slot));
    }

    @Test
    public void test_embarkFromLocation_move_was_stoped_by_ui() {
        unit.placeToLocation(SHORE);
        cancelMovement();
        String before = snapshot();
        unit.embarkFromLocation(slot);
        assertEquals(before, snapshot());
        verify(listener, never()).onUnitMovedStepStarted(any());
    }

    @Test
    public void test_embarkFromLocation() {
        unit.placeToLocation(SHORE);
        int shipPoints = ship.getActionPoints();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitEmbarked(UnitEmbarkedEvent event) {
                assertSame(unit, event.getUnit());
                assertSame(slot, event.getSlot());
                assertSame(unit, slot.getUnit().orElseThrow());
                assertSame(slot, unit.getPlaceCargoSlot().getCargoSlot());
            }
        });
        unit.embarkFromLocation(slot);
        assertEquals(1, unit.getActionPoints());
        assertEquals(shipPoints, ship.getActionPoints());
        assertSame(unit, slot.getUnit().orElseThrow());
        assertSame(slot, unit.getPlaceCargoSlot().getCargoSlot());
        verify(listener).onUnitEmbarked(any());
        verify(listener).onUnitMovedStepStarted(
                argThat(event -> event.getStart().equals(SHORE)
                        && event.getEnd().equals(WATER)
                        && event.getUnit() == unit));
        verify(listener).onUnitMoveFinished(any());
    }

    @Test
    public void test_disembarkToLocation_cargoSlot_is_null() {
        String before = snapshot();
        assertThrows(NullPointerException.class,
                () -> unit.disembarkToLocation(null));
        assertEquals(before, snapshot());
    }

    @Test
    public void test_disembarkToLocation_not_enought_action_points() {
        data.getModel().getUnits().stream().filter(u -> u.getId() == 16)
                .findFirst().orElseThrow().setAvailableMoves(0);
        restore();
        rejected(() -> unit.disembarkToLocation(SHORE));
    }

    @Test
    public void test_disembarkToLocation_unit_not_in_cargo_slot() {
        unit.placeToLocation(SHORE);
        rejected(() -> unit.disembarkToLocation(SHORE));
    }

    @Test
    public void test_disembarkToLocation_unit_cant_move_at_target_location() {
        rejected(() -> unit.disembarkToLocation(WATER));
    }

    @Test
    public void test_disembarkToLocation_target_location_is_too_far() {
        rejected(() -> unit.disembarkToLocation(FAR));
    }

    @Test
    public void test_disembarkToLocation_target_location_is_colony() {
        Location port = Location.of(25, 14);
        // An owned coastal colony is now valid; a foreign one remains
        // forbidden.
        data.getModel().getColonies().stream()
                .filter(c -> c.getLocation().equals(port)).findFirst()
                .orElseThrow().setOwnerName("Dutch's King");
        // The foreign colony and its residents must have the same owner.
        data.getModel().getUnits().stream().filter(
                u -> java.util.Set.of(0, 1, 2, 4, 5).contains(u.getId()))
                .forEach(u -> u.setOwnerId("Dutch's King"));
        Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(Location.of(26, 14), TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getUnits().stream().filter(u -> u.getId() == 3)
                .findFirst().orElseThrow().setPlaceMap(at(Location.of(26, 14)));
        restore();
        assertFalse(unit.canUnitDisembarkAt(port));
        rejected(() -> unit.disembarkToLocation(port));
    }

    private void cancelMovement() {
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMoveStarted(UnitMoveStartedEvent event) {
                event.stopEventExecution();
            }
        });
    }

    @Test
    public void test_disembarkToLocation_move_was_stoped_by_ui() {
        cancelMovement();
        String before = snapshot();
        unit.disembarkToLocation(SHORE);
        assertEquals(before, snapshot());
        verify(listener, never()).onUnitMovedStepStarted(any());
    }

    @Test
    public void test_disembarkToLocation() {
        int shipPoints = ship.getActionPoints();
        unit.disembarkToLocation(SHORE);
        assertEquals(1, unit.getActionPoints());
        assertEquals(shipPoints, ship.getActionPoints());
        assertTrue(slot.isEmpty());
        assertEquals(SHORE, unit.getLocation());
        verify(listener).onUnitMovedStepStarted(
                argThat(event -> event.getStart().equals(WATER)
                        && event.getEnd().equals(SHORE)
                        && event.getUnit() == unit));
        verify(listener).onUnitMoveFinished(any());
    }

    @Test
    void rejectsSlotFilledDuringMovementConfirmationBeforeAnimation() {
        unit.placeToLocation(SHORE);
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMoveStarted(UnitMoveStartedEvent event) {
                slot.addGoods(Goods.of(GoodsType.ORE, 7));
            }
        });
        assertThrows(IllegalStateException.class,
                () -> unit.embarkFromLocation(slot));
        assertEquals(SHORE, unit.getLocation());
        assertEquals(2, unit.getActionPoints());
        assertEquals(Goods.of(GoodsType.ORE, 7), slot.getGoods().orElseThrow());
        verify(listener, never()).onUnitMovedStepStarted(any());
        verify(listener, never()).onUnitEmbarked(any());
    }

}
