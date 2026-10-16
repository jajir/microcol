package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

class VoyageReversalTest {
    @TempDir
    Path directory;
    private final GameModelPoDao files = new GameModelPoDao();
    private GameModelPo data;
    private Model model;

    private Ship ship() {
        return (Ship) model.getUnitById(3);
    }

    private PlaceHighSea voyage() {
        return (PlaceHighSea) ship().getPlace();
    }

    private void load(final boolean originalToEurope, final int duration) {
        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        final PlaceHighSeasPo voyage = ship.getPlaceHighSeas();
        if (duration == 4) {
            ship.setType(UnitType.MERCHANTMAN);
            ship.setAvailableMoves(5);
            ship.getCargo().getSlots().remove(4);
            data.getModel().getUnits().removeIf(unit -> unit.getId() == 13);
            final var goods = ship.getCargo().getSlots().get(3);
            goods.setUnitId(null);
            goods.setGoodsType(GoodsType.ORE);
            goods.setAmount(7);
            data.getModel().getUnits().stream()
                    .filter(unit -> unit.getId() == 16).findFirst()
                    .orElseThrow().setTools(80);
            voyage.setWeather(1);
        } else {
            voyage.setWeather(-1);
        }
        voyage.setOriginalTurns(duration);
        voyage.setRemainigTurns(duration);
        voyage.setPosition(0);
        voyage.setOriginalToEurope(originalToEurope);
        voyage.setTravelToEurope(originalToEurope);
        if (originalToEurope) {
            final var terrain = data.getModel().getMap().getTerrainMap();
            terrain.put(Location.of(2, 15), TerrainType.HIGH_SEA);
            data.getModel().getMap().setTerrainType(terrain);
            ship.setDepartureLocation(Location.of(2, 15));
            ship.setDepartureDirection(Direction.west);
        }
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void ownerUpdate() {
        model.endTurn();
        model.endTurn();
    }

    private void reload() {
        data.setModel(model.save());
        final Path save = directory.resolve("reversed.microcol");
        files.saveToFile(save.toString(), data);
        data = files.loadFromFile(save.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void fourTurnVoyageReversesAfterOneUpdateAndDoubleFlipKeepsProgress() {
        for (final boolean originalToEurope : new boolean[] { false, true }) {
            load(originalToEurope, 4);
            ownerUpdate();
            assertEquals(1, voyage().getPosition());
            final VoyageReversalOffer first = model.offerVoyageReversal(ship());
            assertEquals(1, first.getRemainingAfterReverse());
            model.reverseVoyage(first);
            assertEquals(1, voyage().getRemainigTurns());
            assertEquals(1, voyage().getPosition());
            assertThrows(IllegalStateException.class,
                    () -> model.reverseVoyage(first));
            final VoyageReversalOffer second = model
                    .offerVoyageReversal(ship());
            assertEquals(3, second.getRemainingAfterReverse());
            model.reverseVoyage(second);
            assertThrows(IllegalStateException.class,
                    () -> model.reverseVoyage(first));
            assertEquals(3, voyage().getRemainigTurns());
            assertEquals(1, voyage().getPosition());
            reload();
            assertEquals(1, voyage().getPosition());
            assertEquals(3, voyage().getRemainigTurns());
            assertEquals(1, voyage().getWeather());
            for (int update = 0; update < 3; update++)
                ownerUpdate();
            assertEquals(originalToEurope, ship().isAtEuropePort());
            assertEquals(!originalToEurope, ship().isAtPlaceLocation());
            assertEquals(4, ship().getCargo().getSlots().size());
        }
    }

    @Test
    void reversalAtDepartureReturnsImmediatelyAndStaleOffersReject() {
        for (final boolean originalToEurope : new boolean[] { false, true }) {
            load(originalToEurope, 1);
            final Ship sameShip = ship();
            final PlaceHighSea sameVoyage = voyage();
            final VoyageReversalOffer offered = model
                    .offerVoyageReversal(sameShip);
            assertEquals(0, offered.getRemainingAfterReverse());
            final int moves = sameShip.getActionPoints();
            model.reverseVoyage(offered);
            assertEquals(!originalToEurope, sameShip.isAtEuropePort());
            assertEquals(originalToEurope, sameShip.isAtPlaceLocation());
            assertEquals(moves, sameShip.getActionPoints());
            assertThrows(IllegalStateException.class,
                    () -> model.reverseVoyage(offered));
            assertThrows(IllegalStateException.class,
                    () -> model.offerVoyageReversal(sameShip));
            assertEquals(0, sameVoyage.getPosition());
        }
    }

    @Test
    void reversedDirectionAndCargoSurviveDiskBeforeReturningToSource() {
        for (final boolean originalToEurope : new boolean[] { false, true }) {
            load(originalToEurope, 4);
            final Location source = originalToEurope ? Location.of(2, 15)
                    : Location.of(49, 15);
            final java.util.List<Integer> passengers = ship().getCargo()
                    .getSlots().stream()
                    .map(slot -> slot.getUnit().map(Unit::getId).orElse(null))
                    .collect(java.util.stream.Collectors.toList());
            final String cargo = new com.google.gson.Gson()
                    .toJson(ship().getCargo().save());
            final int tools = ((org.microcol.model.unit.UnitFreeColonist) model
                    .getUnitById(16)).getTools();
            assertEquals(80, tools);
            ownerUpdate();
            model.reverseVoyage(model.offerVoyageReversal(ship()));
            assertEquals(1, voyage().getRemainigTurns());
            assertEquals(!originalToEurope, voyage().isTravelToEurope());
            reload();
            assertEquals(source, ship().getDepartureLocation().orElseThrow());
            assertEquals(1, voyage().getPosition());
            assertEquals(1, voyage().getRemainigTurns());
            assertEquals(4, voyage().getOriginalTurns());
            assertEquals(1, voyage().getWeather());
            assertEquals(originalToEurope, voyage().isOriginalToEurope());
            assertEquals(!originalToEurope, voyage().isTravelToEurope());
            assertEquals(passengers, ship().getCargo().getSlots().stream()
                    .map(slot -> slot.getUnit().map(Unit::getId).orElse(null))
                    .collect(java.util.stream.Collectors.toList()));
            assertEquals(cargo, new com.google.gson.Gson()
                    .toJson(ship().getCargo().save()));
            assertEquals(tools,
                    ((org.microcol.model.unit.UnitFreeColonist) model
                            .getUnitById(16)).getTools());
            ownerUpdate();
            assertEquals(!originalToEurope, ship().isAtEuropePort());
            assertEquals(originalToEurope, ship().isAtPlaceLocation());
            if (originalToEurope) {
                assertEquals(source, ship().getLocation());
            }
            assertEquals(passengers, ship().getCargo().getSlots().stream()
                    .map(slot -> slot.getUnit().map(Unit::getId).orElse(null))
                    .collect(java.util.stream.Collectors.toList()));
            assertEquals(cargo, new com.google.gson.Gson()
                    .toJson(ship().getCargo().save()));
            assertEquals(tools,
                    ((org.microcol.model.unit.UnitFreeColonist) model
                            .getUnitById(16)).getTools());
        }
    }

    @Test
    void offerExpiresAfterOwnerProgressWithoutMutation() {
        load(false, 4);
        final VoyageReversalOffer stale = model.offerVoyageReversal(ship());
        ownerUpdate();
        final PlaceHighSea same = voyage();
        assertEquals(1, same.getPosition());
        assertThrows(IllegalStateException.class,
                () -> model.reverseVoyage(stale));
        assertSame(same, ship().getPlace());
        assertEquals(1, same.getPosition());
        assertFalse(same.isTravelToEurope());
    }

    @Test
    void occupiedSourceWaitsAtZeroThenRetriesTheSameSquare() {
        load(true, 4);
        final UnitPo blocker = new UnitPo();
        blocker.setId(100);
        blocker.setOwnerId("Dutch's King");
        blocker.setType(UnitType.GALLEON);
        blocker.setAction(new UnitActionNoAction().save());
        blocker.setAvailableMoves(6);
        final PlaceMapPo at = new PlaceMapPo();
        at.setLocation(Location.of(2, 15));
        at.setOrientation(Direction.east);
        blocker.setPlaceMap(at);
        model.createUnit(model.save(), blocker);
        final Ship original = ship();
        final PlaceHighSea sameVoyage = voyage();
        final int cargo = original.getCargo().getSlots().size();
        model.reverseVoyage(model.offerVoyageReversal(original));
        assertSame(sameVoyage, original.getPlace());
        assertEquals(0, sameVoyage.getRemainigTurns());
        assertEquals(cargo, original.getCargo().getSlots().size());
        ownerUpdate();
        assertSame(sameVoyage, original.getPlace());
        model.destroyUnit(model.getUnitById(100));
        ownerUpdate();
        assertEquals(Location.of(2, 15), original.getLocation());
        assertEquals(cargo, original.getCargo().getSlots().size());
    }

    @Test
    void foreignTurnAndStoppedGameRejectWithoutChangingVoyage() {
        load(false, 4);
        final VoyageReversalOffer offer = model.offerVoyageReversal(ship());
        model.endTurn();
        assertThrows(IllegalStateException.class,
                () -> model.reverseVoyage(offer));
        assertEquals(0, voyage().getPosition());
        assertFalse(voyage().isTravelToEurope());
        model.stop();
        assertThrows(IllegalStateException.class,
                () -> model.reverseVoyage(offer));
        assertEquals(0, voyage().getPosition());
    }

    @Test
    void missingOrInconsistentSavedRouteStateIsRejected() {
        load(false, 4);
        data.setModel(model.save());
        final PlaceHighSeasPo saved = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow()
                .getPlaceHighSeas();
        saved.setTravelToEurope(null);
        assertInvalidSavedVoyage();
        saved.setTravelToEurope(false);
        saved.setOriginalToEurope(null);
        assertInvalidSavedVoyage();
        saved.setOriginalToEurope(false);
        saved.setPosition(null);
        assertInvalidSavedVoyage();
        saved.setPosition(-1);
        assertInvalidSavedVoyage();
        saved.setPosition(5);
        assertInvalidSavedVoyage();
        saved.setPosition(0);
        saved.setRemainigTurns(3);
        assertInvalidSavedVoyage();
    }

    private void assertInvalidSavedVoyage() {
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(data.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
