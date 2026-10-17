package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

/** Actual Magellan membership controls ship allowance and new departures. */
class MagellanEffectTest {
    private static final UnitType[] SHIPS = { UnitType.CARAVEL,
            UnitType.MERCHANTMAN, UnitType.GALLEON, UnitType.PRIVATEER,
            UnitType.FRIGATE, UnitType.MAN_OF_WAR };

    private GameModelPo fixture(final boolean member) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        if (member) {
            final CongressPo congress = dutch.getCongress();
            congress.setMembers(List.of(Founder.FERDINAND_MAGELLAN));
            final List<Founder> candidates = new ArrayList<>(
                    congress.getCandidates());
            candidates.set(4, Founder.HERNANDO_DE_SOTO);
            congress.setCandidates(candidates);
        }
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void nextDutchTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    @Test
    void memberShipsOfEveryFamilyUseEffectiveAllowanceAndSaveSpentMoves() {
        final Model model = load(fixture(true));
        final Player dutch = model.getPlayerByName("Dutch");
        final Player king = model.getPlayers().stream().filter(Player::isKing)
                .findFirst().orElseThrow();
        final List<Ship> created = new ArrayList<>();
        for (final UnitType type : SHIPS) {
            final Ship ship = (Ship) model.addUnitInEurope(type, dutch);
            created.add(ship);
            assertEquals(type.getSpeed() + 1, ship.getSpeed());
            assertEquals(ship.getSpeed(), ship.getActionPoints());
            assertEquals(type.getSpeed(),
                    model.addUnitInEurope(type, king).getSpeed());
        }
        final Unit land = dutch.getAllUnits().stream()
                .filter(unit -> !unit.isShip()).findFirst().orElseThrow();
        assertEquals(land.getType().getSpeed(), land.getSpeed());
        final Ship first = created.get(0);
        first.setActionPoints(1);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertEquals(1, restored.getUnitById(first.getId()).getActionPoints());
        assertEquals(first.getSpeed(),
                restored.getUnitById(first.getId()).getSpeed());
        nextDutchTurn(model);
        for (final Ship ship : created) {
            assertEquals(ship.getSpeed(), ship.getActionPoints());
        }
    }

    @Test
    void actualCongressJoinRaisesAllowanceOnTheSameFreshOwnerTurn() {
        final GameModelPo data = fixture(false);
        final CongressPo congress = data.getModel().getGameManager()
                .getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getCongress();
        congress.setSelected(Founder.FERDINAND_MAGELLAN);
        congress.setBells(99);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        final int ordinaryVoyage = ((PlaceHighSea) ship.getPlace())
                .getOriginalTurns();
        assertEquals(ship.getType().getSpeed(), ship.getSpeed());
        nextDutchTurn(model);
        assertTrue(ship.getOwner().getCongressMembers()
                .contains(Founder.FERDINAND_MAGELLAN));
        assertEquals(ship.getType().getSpeed() + 1, ship.getSpeed());
        assertEquals(ship.getSpeed(), ship.getActionPoints());
        assertTrue(ship.isAtHighSea());
        assertEquals(ordinaryVoyage,
                ((PlaceHighSea) ship.getPlace()).getOriginalTurns());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        final Ship saved = (Ship) restored.getUnitById(3);
        assertEquals(ordinaryVoyage,
                ((PlaceHighSea) saved.getPlace()).getOriginalTurns());
        assertEquals(((PlaceHighSea) ship.getPlace()).getRemainigTurns(),
                ((PlaceHighSea) saved.getPlace()).getRemainigTurns());
    }

    @Test
    void onlyWestEdgeToEuropeHalvesOrdinarySampledDuration() {
        for (final UnitType ship : SHIPS) {
            for (int weather = -1; weather <= 1; weather++) {
                for (final boolean toEurope : new boolean[] { false, true }) {
                    for (final Direction edge : new Direction[] {
                            Direction.east, Direction.west }) {
                        final int ordinary = AtlanticVoyageDuration.calculate(
                                weather, toEurope, edge, ship.getSpeed());
                        final int expected = toEurope && edge == Direction.west
                                ? Math.max(1, (ordinary + 1) / 2)
                                : ordinary;
                        assertEquals(expected,
                                AtlanticVoyageDuration.calculate(weather,
                                        toEurope, edge, ship.getSpeed(), true));
                        assertEquals(ordinary,
                                AtlanticVoyageDuration.calculate(weather,
                                        toEurope, edge, ship.getSpeed(),
                                        false));
                    }
                }
            }
        }
    }

    @Test
    void actualWestDeparturePersistsSampledHalfWithoutReroll() {
        final GameModelPo data = fixture(true);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(1, 15), TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        final UnitPo shipPo = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        shipPo.setPlaceHighSeas(null);
        shipPo.setDepartureLocation(null);
        shipPo.setDepartureDirection(null);
        final PlaceMapPo onMap = new PlaceMapPo();
        onMap.setLocation(Location.of(1, 15));
        onMap.setOrientation(Direction.west);
        shipPo.setPlaceMap(onMap);
        final Model model = load(data);
        final Ship ship = (Ship) model.getUnitById(3);
        ship.placeToHighSeas(true);
        final PlaceHighSea voyage = (PlaceHighSea) ship.getPlace();
        final int ordinary = AtlanticVoyageDuration.calculate(
                voyage.getWeather(), true, Direction.west,
                ship.getType().getSpeed());
        assertEquals(Math.max(1, (ordinary + 1) / 2),
                voyage.getOriginalTurns());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        final PlaceHighSea saved = (PlaceHighSea) restored.getUnitById(3)
                .getPlace();
        assertEquals(voyage.getWeather(), saved.getWeather());
        assertEquals(voyage.getOriginalTurns(), saved.getOriginalTurns());
        assertEquals(voyage.getRemainigTurns(), saved.getRemainigTurns());
        assertFalse(saved.getOriginalTurns() > ordinary);
    }

    @Test
    void membershipDoesNotRewriteAnOrdinaryWestVoyageAlreadyUnderway() {
        final GameModelPo data = fixture(true);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(1, 15), TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        ship.setDepartureLocation(Location.of(1, 15));
        ship.setDepartureDirection(Direction.west);
        ship.getPlaceHighSeas().setOriginalToEurope(true);
        ship.getPlaceHighSeas().setTravelToEurope(true);
        ship.getPlaceHighSeas().setWeather(1);
        ship.getPlaceHighSeas().setOriginalTurns(3);
        ship.getPlaceHighSeas().setPosition(1);
        ship.getPlaceHighSeas().setRemainigTurns(2);
        final Model model = load(data);
        assertEquals(3, ((PlaceHighSea) model.getUnitById(3).getPlace())
                .getOriginalTurns());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        final PlaceHighSea saved = (PlaceHighSea) restored.getUnitById(3)
                .getPlace();
        assertEquals(3, saved.getOriginalTurns());
        assertEquals(2, saved.getRemainigTurns());
        ship.getPlaceHighSeas().setOriginalTurns(4);
        ship.getPlaceHighSeas().setRemainigTurns(3);
        assertThrows(IllegalArgumentException.class, () -> load(data));
    }

    @Test
    void savedMovementMustFitTheEffectiveOwnerAllowance() {
        final GameModelPo member = fixture(true);
        final UnitPo ship = member.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        ship.setAvailableMoves(UnitType.GALLEON.getSpeed() + 1);
        assertEquals(UnitType.GALLEON.getSpeed() + 1,
                load(member).getUnitById(3).getActionPoints());
        ship.setAvailableMoves(UnitType.GALLEON.getSpeed() + 2);
        assertThrows(IllegalArgumentException.class, () -> load(member));
    }
}
