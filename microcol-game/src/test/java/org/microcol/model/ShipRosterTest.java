package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

class ShipRosterTest {
    private ModelPo scenario() {
        return new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel();
    }

    private Model load(ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void registeredShipsHaveSpecifiedMovementCargoAndPurchaseRules() {
        UnitType[] types = { UnitType.CARAVEL, UnitType.MERCHANTMAN,
                UnitType.GALLEON, UnitType.PRIVATEER, UnitType.FRIGATE,
                UnitType.MAN_OF_WAR };
        int[] moves = { 4, 5, 6, 8, 6, 6 };
        int[] holds = { 2, 4, 6, 2, 4, 6 };
        int[] prices = { 1000, 2000, 5000, 2000, 4000, 0 };
        for (int i = 0; i < types.length; i++) {
            UnitType type = types[i];
            assertSame(type, UnitType.valueOf(type.name()));
            assertTrue(type.isShip());
            assertEquals(moves[i], type.getSpeed());
            assertEquals(holds[i], type.getCargoCapacity());
            assertEquals(prices[i], type.getEuropePrice());
            assertEquals(i >= 3, type.canAttack());
            assertEquals(i >= 3,
                    type.getAttackableUnitType().contains(UnitType.CARAVEL));
            assertFalse(
                    type.getAttackableUnitType().contains(UnitType.COLONIST));
        }
        Model model = load(scenario());
        Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(10_000);
        int count = model.getAllUnits().size();
        int gold = buyer.getGold();
        assertFalse(buyer.canBuyUnit(UnitType.MAN_OF_WAR));
        assertThrows(IllegalArgumentException.class,
                () -> buyer.buy(UnitType.MAN_OF_WAR));
        assertEquals(count, model.getAllUnits().size());
        assertEquals(gold, buyer.getGold());
        for (UnitType type : types) {
            assertEquals(type == UnitType.GALLEON || type == UnitType.FRIGATE,
                    type.canBeBuildInColony());
        }
    }

    @Test
    void allShipClassesRestoreCargoAndDepartureIdentity() {
        for (UnitType type : new UnitType[] { UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.GALLEON, UnitType.PRIVATEER,
                UnitType.FRIGATE, UnitType.MAN_OF_WAR }) {
            ModelPo saved = scenario();
            UnitPo ship = saved.getUnits().stream().filter(u -> u.getId() == 3)
                    .findFirst().orElseThrow();
            ship.setType(type);
            saved.getUnits().removeIf(unit -> unit.getId() == 15
                    || unit.getId() == 14 || unit.getId() == 13);
            for (int slot = 1; slot < 4; slot++) {
                ship.getCargo().getSlots().get(slot).setUnitId(null);
            }
            CargoSlotPo cargo = ship.getCargo().getSlots().get(1);
            cargo.setGoodsType(GoodsType.ORE);
            cargo.setAmount(7);
            Model model = load(saved);
            Ship loaded = (Ship) model.getUnitById(3);
            assertSame(type, loaded.getType());
            assertEquals(type.getCargoCapacity(),
                    loaded.getCargo().getSlots().size());
            assertEquals(3,
                    ((PlaceHighSea) loaded.getPlace()).getRemainigTurns());
            assertEquals(Location.of(49, 15),
                    loaded.getDepartureLocation().orElseThrow());
            assertEquals(Direction.east,
                    loaded.getDepartureDirection().orElseThrow());
            assertEquals(16, loaded.getCargo().getSlotByIndex(0).getUnit()
                    .orElseThrow().getId());
            assertEquals(Goods.of(GoodsType.ORE, 7), loaded.getCargo()
                    .getSlotByIndex(1).getGoods().orElseThrow());
            Model restored = load(model.save());
            Ship roundTrip = (Ship) restored.getUnitById(3);
            assertSame(type, roundTrip.getType());
            assertEquals("Dutch", roundTrip.getOwner().getName());
            assertEquals(3,
                    ((PlaceHighSea) roundTrip.getPlace()).getRemainigTurns());
            assertEquals(Location.of(49, 15),
                    roundTrip.getDepartureLocation().orElseThrow());
            assertEquals(Direction.east,
                    roundTrip.getDepartureDirection().orElseThrow());
            assertEquals(16, roundTrip.getCargo().getSlotByIndex(0).getUnit()
                    .orElseThrow().getId());
            assertEquals(Goods.of(GoodsType.ORE, 7), roundTrip.getCargo()
                    .getSlotByIndex(1).getGoods().orElseThrow());
        }
    }

    @Test
    void newEuropeanShipsPurchaseAtPriceAndRejectInsufficientFunds() {
        for (UnitType type : new UnitType[] { UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.PRIVATEER }) {
            Model model = load(scenario());
            Player buyer = model.getPlayerByName("Dutch");
            buyer.setGold(type.getEuropePrice());
            int count = model.getAllUnits().size();
            int nextId = model.getAllUnits().stream().mapToInt(Unit::getId)
                    .max().orElseThrow() + 1;
            assertTrue(buyer.canBuyUnit(type));
            buyer.buy(type);
            assertEquals(0, buyer.getGold());
            assertEquals(count + 1, model.getAllUnits().size());
            Ship bought = (Ship) model.getUnitById(nextId);
            assertSame(type, bought.getType());
            assertSame(buyer, bought.getOwner());
            assertTrue(bought.isAtEuropePort());
            assertFalse(buyer.canBuyUnit(type));
            assertThrows(NotEnoughtGoldException.class, () -> buyer.buy(type));
            assertEquals(count + 1, model.getAllUnits().size());
            assertEquals(0, buyer.getGold());
        }
    }

    @Test
    void eachFullHoldRejectsAnotherPassengerWithoutMutation() {
        for (UnitType type : new UnitType[] { UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.GALLEON, UnitType.PRIVATEER,
                UnitType.FRIGATE, UnitType.MAN_OF_WAR }) {
            ModelPo saved = scenario();
            UnitPo ship = saved.getUnits().stream().filter(u -> u.getId() == 3)
                    .findFirst().orElseThrow();
            ship.setType(type);
            saved.getUnits()
                    .removeIf(unit -> unit.getId() == 16 || unit.getId() == 15
                            || unit.getId() == 14 || unit.getId() == 13);
            for (int slot = 0; slot < ship.getCargo().getSlots()
                    .size(); slot++) {
                CargoSlotPo stored = ship.getCargo().getSlots().get(slot);
                stored.setUnitId(null);
                if (slot < type.getCargoCapacity()) {
                    stored.setGoodsType(GoodsType.ORE);
                    stored.setAmount(1);
                }
            }
            while (ship.getCargo().getSlots().size() < type
                    .getCargoCapacity()) {
                CargoSlotPo stored = new CargoSlotPo();
                stored.setGoodsType(GoodsType.ORE);
                stored.setAmount(1);
                ship.getCargo().getSlots().add(stored);
            }
            Model model = load(saved);
            Ship full = (Ship) model.getUnitById(3);
            assertEquals(type.getCargoCapacity(),
                    full.getCargo().getSlots().size());
            assertTrue(full.getCargo().isFull());
            CargoSlot first = full.getCargo().getSlotByIndex(0);
            UnitFreeColonist passenger = (UnitFreeColonist) model
                    .getUnitById(0);
            Place place = passenger.getPlace();
            assertThrows(IllegalStateException.class,
                    () -> first.store(passenger));
            assertSame(place, passenger.getPlace());
            assertEquals(Goods.of(GoodsType.ORE, 1),
                    first.getGoods().orElseThrow());
        }
    }

    @Test
    void shipCannotMoveOntoLandOrBoardItself() {
        ModelPo saved = scenario();
        Map<Location, TerrainType> terrain = saved.getMap().getTerrainMap();
        terrain.put(Location.of(48, 16), TerrainType.OCEAN);
        terrain.put(Location.of(47, 16), TerrainType.GRASSLAND);
        saved.getMap().setTerrainType(terrain);
        UnitPo ship = saved.getUnits().stream().filter(u -> u.getId() == 3)
                .findFirst().orElseThrow();
        ship.setPlaceHighSeas(null);
        PlaceMapPo onMap = new PlaceMapPo();
        onMap.setLocation(Location.of(48, 16));
        onMap.setOrientation(Direction.west);
        ship.setPlaceMap(onMap);
        Model model = load(saved);
        Ship loaded = (Ship) model.getUnitById(3);
        Place place = loaded.getPlace();
        int moves = loaded.getActionPoints();
        assertFalse(model.isOwnedCoastalPort(loaded.getOwner(),
                Location.of(47, 16)));
        assertFalse(loaded.isPossibleToMoveAt(Location.of(47, 16)));
        assertThrows(IllegalArgumentException.class, () -> model
                .moveUnit(loaded, Path.of(List.of(Location.of(47, 16)))));
        assertSame(place, loaded.getPlace());
        assertEquals(moves, loaded.getActionPoints());
        CargoSlot empty = loaded.getCargo().getSlotByIndex(4);
        assertThrows(IllegalStateException.class, () -> empty.store(loaded));
        assertTrue(empty.isEmpty());
        assertSame(place, loaded.getPlace());
    }

    @Test
    void occupiedSlotBeyondCapacityRejectsLoad() {
        ModelPo saved = scenario();
        UnitPo ship = saved.getUnits().stream().filter(u -> u.getId() == 3)
                .findFirst().orElseThrow();
        ship.setType(UnitType.CARAVEL);
        assertThrows(IllegalArgumentException.class, () -> load(saved));
    }

    @Test
    void royalFactoryRejectsWrongOwnerBeforeAllocatingShip() {
        Model model = load(scenario());
        Player king = model.getPlayerByName("Dutch's King");
        int count = model.getAllUnits().size();
        assertThrows(IllegalArgumentException.class, () -> model
                .createCargoShipForKing(model.getPlayerByName("Dutch")));
        assertThrows(IllegalArgumentException.class,
                () -> model.createCargoShipForKing(
                        load(scenario()).getPlayerByName("Dutch's King")));
        assertEquals(count, model.getAllUnits().size());
        Ship royal = (Ship) model.createCargoShipForKing(king);
        assertSame(UnitType.MAN_OF_WAR, royal.getType());
        assertSame(king, royal.getOwner());
        assertEquals(count + 1, model.getAllUnits().size());
    }
}
