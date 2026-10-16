package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.microcol.model.*;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

class CoastalPortMovementTest {
    private static final Location PORT = Location.of(25, 14);
    private static final Location INLAND = Location.of(24, 14);
    private final GameModelPoDao dao = new GameModelPoDao();
    private final Gson gson = new Gson();
    @TempDir
    Path directory;
    private GameModelPo data;
    private Model model;

    private void prepare(final Location water) {
        data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (int x = 21; x <= 29; x++) {
            for (int y = 10; y <= 18; y++)
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
        }
        terrain.put(water, TerrainType.OCEAN);
        data.getModel().getMap().setTerrainType(terrain);
        if (water.equals(Location.of(26, 14))) {
            final var inland = data.getModel().getColonies().stream()
                    .filter(c -> c.getId() == 1).findFirst().orElseThrow();
            inland.setLocation(INLAND);
            // Keep the moved colony's worker clear of the port's worked square.
            for (final var field : inland.getColonyFields()) {
                field.setWorkerId(
                        field.getDirection().equals(Direction.north.getVector())
                                ? 6
                                : null);
                field.setProducedGoodsType(
                        field.getWorkerId() == null ? null : GoodsType.CORN);
            }
        }
        final java.util.Set<Location> trees = data.getModel().getMap()
                .getTreeSet();
        trees.remove(water);
        data.getModel().getMap().setTrees(trees);
        final java.util.Set<Location> fields = data.getModel().getMap()
                .getFieldSet();
        fields.remove(water);
        data.getModel().getMap().setFields(fields);
        data.getModel().getColonies().stream()
                .flatMap(colony -> colony.getColonyFields().stream())
                .filter(field -> field.getWorkerId() != null)
                .forEach(field -> field.setProducedGoodsType(GoodsType.CORN));
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 3).findFirst().orElseThrow();
        ship.setPlaceHighSeas(null);
        ship.setDepartureLocation(null);
        ship.setDepartureDirection(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(water);
        place.setOrientation(Direction.west);
        ship.setPlaceMap(place);
        ship.setAvailableMoves(6);
        ship.getCargo().getSlots().get(4).setGoodsType(GoodsType.ORE);
        ship.getCargo().getSlots().get(4).setAmount(7);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Ship ship() {
        return (Ship) model.getUnitById(3);
    }

    private String snapshot() {
        return gson.toJson(model.save());
    }

    private void step(final Location to) {
        model.moveUnit(ship(), org.microcol.model.Path.of(List.of(to)));
    }

    private void reload() {
        data.setModel(model.save());
        final Path file = directory.resolve("port.microcol");
        dao.saveToFile(file.toString(), data);
        data = dao.loadFromFile(file.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
    }

    private void runAi() {
        final SimpleAiPlayer ai = new SimpleAiPlayer(model,
                model.getCurrentPlayer(), new AnimationManager(new EventBus()));
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(2),
                    () -> ai.moveUnit(ship()));
        } finally {
            ai.stop();
        }
    }

    private String payload() {
        return gson.toJson(List.of(ship().save().getCargo(),
                model.getUnitById(16).save(), model.getUnitById(15).save(),
                model.getUnitById(14).save(), model.getUnitById(13).save()));
    }

    private void verifyPayload(final String expected) {
        assertEquals(expected, payload());
        for (int slot = 0; slot < 4; slot++) {
            final Unit passenger = model.getUnitById(16 - slot);
            assertTrue(passenger.isAtCargoSlot());
            assertSame(passenger, ship().getCargo().getSlotByIndex(slot)
                    .getUnit().orElseThrow());
            assertSame(ship().getCargo().getSlotByIndex(slot),
                    passenger.getPlaceCargoSlot().getCargoSlot());
        }
    }

    private Unit prepareGround() {
        prepare(Location.of(26, 14));
        data.setModel(model.save());
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (int x = 29; x <= 31; x++) {
            for (int y = 14; y <= 16; y++)
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
        }
        data.getModel().getMap().setTerrainType(terrain);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit unit = model.getUnitById(0);
        unit.placeToLocation(Location.of(30, 15));
        ((AbstractUnit) unit).setActionPoints(1);
        return unit;
    }

    private void verifyRejectedStep(final Unit unit,
            final Location destination) {
        final String before = snapshot();
        assertFalse(unit.isPossibleToMoveAt(destination));
        assertTrue(unit.getPath(destination).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> model.moveUnit(unit,
                org.microcol.model.Path.of(List.of(destination))));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    void testDirectionalMovement(Direction dir) {
        Location water = PORT.add(dir.getVector());
        prepare(water);
        String beforeSnapshot = snapshot();
        assertTrue(model.isOwnedCoastalPort(model.getCurrentPlayer(), PORT));
        assertTrue(ship().isPossibleToMoveAt(PORT));
        List<Location> path = ship().getPath(PORT).orElseThrow();
        assertEquals(List.of(PORT), path);
        assertTrue(ship().getAvailableLocations().contains(PORT));
        assertEquals(beforeSnapshot, snapshot());
        String beforePayload = payload();
        step(PORT);
        assertEquals(PORT, ship().getLocation());
        assertEquals(5, ship().getActionPoints());
        verifyPayload(beforePayload);
        step(water);
        assertEquals(water, ship().getLocation());
        assertEquals(4, ship().getActionPoints());
        verifyPayload(beforePayload);
    }

    @Test
    void testInlandRestrictions() {
        prepare(Location.of(26, 14));
        step(PORT);
        var colony = model.getColonyAt(INLAND).orElseThrow();
        assertEquals(colony.getOwner(), ship().getOwner());
        assertFalse(model.isOwnedCoastalPort(model.getCurrentPlayer(), INLAND));
        for (int id = 13; id <= 16; id++) {
            Unit passenger = model.getUnitById(id);
            ((AbstractUnit) passenger).setActionPoints(0);
        }
        String snapshotAfterSetup = snapshot();
        assertFalse(ship().getAvailableLocations().contains(INLAND));
        assertFalse(ship().isPossibleToMoveAt(INLAND));
        assertTrue(ship().getPath(INLAND).isEmpty());
        assertEquals(snapshotAfterSetup, snapshot());
        verifyRejectedStep(ship(), INLAND);
        verifyRejectedStep(ship(), Location.of(25, 15));
    }

    @Test
    void testReloadAfterMovement() {
        prepare(Location.of(26, 14));
        step(PORT);
        String shipJson = gson.toJson(ship().save());
        String beforePayload = payload();
        reload();
        assertEquals(PORT, ship().getLocation());
        assertEquals(5, ship().getActionPoints());
        assertEquals(shipJson, gson.toJson(ship().save()));
        verifyPayload(beforePayload);
        step(Location.of(26, 14));
        assertEquals(Location.of(26, 14), ship().getLocation());
        assertEquals(4, ship().getActionPoints());
        verifyPayload(beforePayload);
    }

    @Test
    void testMapBounds() {
        prepare(Location.of(26, 14));
        Location[] starts = { Location.of(25, 1), Location.of(25, 30),
                Location.of(1, 15), Location.of(50, 15) };
        Location[] targets = { Location.of(25, 0), Location.of(25, 31),
                Location.of(0, 15), Location.of(51, 15) };
        for (int i = 0; i < starts.length; i++) {
            ship().placeToLocation(starts[i]);
            ((AbstractUnit) ship()).setActionPoints(6);
            for (Location loc : ship().getAvailableLocations()) {
                assertTrue(model.getMap().isValid(loc));
            }
            verifyRejectedStep(ship(), targets[i]);
        }
    }

    @Test
    void testGroundMovement() {
        for (Direction dir : Direction.values()) {
            Unit unit = prepareGround();
            Location center = Location.of(30, 15);
            Location neighbor = center.add(dir.getVector());
            assertTrue(unit.isPossibleToMoveAt(neighbor));
            List<Location> path = unit.getPath(neighbor).orElseThrow();
            assertEquals(List.of(neighbor), path);
            model.moveUnit(unit, org.microcol.model.Path.of(List.of(neighbor)));
            assertEquals(neighbor, unit.getLocation());
            assertEquals(0, unit.getActionPoints());
            final String exhausted = snapshot();
            assertThrows(IllegalStateException.class, () -> model.moveUnit(unit,
                    org.microcol.model.Path.of(List.of(center))));
            assertEquals(exhausted, snapshot());
            // A foot colonist needs a fresh allowance for the return step.
            ((AbstractUnit) unit).setActionPoints(1);
            model.moveUnit(unit, org.microcol.model.Path.of(List.of(center)));
            assertEquals(center, unit.getLocation());
            assertEquals(0, unit.getActionPoints());
        }

        Unit unit = prepareGround();
        unit.placeToLocation(PORT);
        ((AbstractUnit) unit).setActionPoints(1);
        verifyRejectedStep(unit, Location.of(26, 14));

        Unit unitNorth = model.getUnitById(0);
        unitNorth.placeToLocation(Location.of(25, 1));
        ((AbstractUnit) unitNorth).setActionPoints(1);
        verifyRejectedStep(unitNorth, Location.of(25, 0));

        unitNorth.placeToLocation(Location.of(25, 30));
        ((AbstractUnit) unitNorth).setActionPoints(1);
        verifyRejectedStep(unitNorth, Location.of(25, 31));
    }

    @Test
    void testAiRouteToCoastalPort() {
        prepare(Location.of(26, 14));
        ((AbstractUnit) ship()).setActionPoints(1);
        String beforePayload = payload();
        runAi();
        assertEquals(PORT, ship().getLocation());
        assertEquals(0, ship().getActionPoints());
        verifyPayload(beforePayload);
    }

    @Test
    void testAiBlockedByEnemyFrigate() {
        prepare(Location.of(26, 14));
        step(PORT);
        ((AbstractUnit) ship()).setActionPoints(1);

        UnitPo enemy = ship().save();
        enemy.setId(900);
        enemy.setOwnerId("Dutch's King");
        enemy.setType(UnitType.FRIGATE);
        enemy.setAvailableMoves(1);
        enemy.setCargo(new org.microcol.model.store.CargoPo());
        PlaceMapPo place = new PlaceMapPo();
        place.setLocation(Location.of(26, 14));
        place.setOrientation(Direction.west);
        enemy.setPlaceMap(place);

        data.setModel(model.save());
        data.getModel().getUnits().add(enemy);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());

        assertFalse(model.isOwnedCoastalPort(model.getCurrentPlayer(), INLAND));
        assertFalse(ship().isPossibleToMoveAt(INLAND));
        assertFalse(ship().isPossibleToMoveAt(Location.of(26, 14)));

        String beforeSnapshot = snapshot();
        runAi();
        assertEquals(PORT, ship().getLocation());
        assertEquals(1, ship().getActionPoints());
        assertEquals(beforeSnapshot, snapshot());
    }
}
