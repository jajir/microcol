package org.microcol.model.campaign;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.model.Calendar;
import org.microcol.model.Difficulty;
import org.microcol.model.Direction;
import org.microcol.model.Europe;
import org.microcol.model.EuropeanNation;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.MovementPoints;
import org.microcol.model.NativeRelations;
import org.microcol.model.RecruitmentPool;
import org.microcol.model.RoyalRoster;
import org.microcol.model.Congress;
import org.microcol.model.TaxState;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.Preconditions;

/** Creates the shared four-nation starting position on supplied terrain. */
public final class ReferenceExpedition {

    private ReferenceExpedition() {
    }

    public static ModelPo create(final WorldMapPo terrain,
            final EuropeanNation humanNation, final Difficulty difficulty) {
        Preconditions.checkNotNull(terrain, "Terrain is null");
        Preconditions.checkNotNull(humanNation, "Human nation is null");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        Preconditions.checkArgument(
                terrain.getMaxX() > 0 && terrain.getMaxY() > 0,
                "Invalid map dimensions");
        final Map<Location, TerrainType> types = terrain.getTerrainMap();
        final int maxX = terrain.getMaxX();
        final int maxY = terrain.getMaxY();
        final Set<Location> reachable = new HashSet<>();
        final ArrayDeque<Location> queue = new ArrayDeque<>();
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX; x++) {
                final Location location = Location.of(x, y);
                if (!at(types, location).isSea()) {
                    for (final Location neighbor : location.getNeighbors()) {
                        if (valid(neighbor, maxX, maxY)
                                && at(types, neighbor).isSea()
                                && reachable.add(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        while (!queue.isEmpty()) {
            for (final Location neighbor : queue.remove().getNeighbors()) {
                if (valid(neighbor, maxX, maxY) && at(types, neighbor).isSea()
                        && reachable.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }
        final List<Location> candidates = new ArrayList<>();
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX; x++) {
                final Location location = Location.of(x, y);
                if (2 * (x - 1) >= maxX - 1 && reachable.contains(location)
                        && at(types, location) == TerrainType.HIGH_SEA) {
                    candidates.add(location);
                }
            }
        }
        Preconditions.checkArgument(candidates.size() >= 4,
                "Map needs four eastern high-sea starts with coastal access");
        candidates.sort(Comparator.comparingInt(Location::getY)
                .thenComparingInt(Location::getX));

        final ModelPo model = new ModelPo();
        model.setCombatRandomState(0L);
        model.setDifficulty(difficulty);
        model.setNextColonyId(0);
        model.setNextTradeRouteId(0);
        model.setTradeRoutes(new ArrayList<>());
        model.setNativeSettlements(new ArrayList<>());
        model.setNativeSettlementKnowledge(new ArrayList<>());
        model.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        model.setNativeLocalAlarm(
                NativeRelations.freshLocalAlarm(model.getNativeSettlements()));
        model.setNativeLandRights(new ArrayList<>());
        model.setNativeLandTakings(new ArrayList<>());
        model.getCalendar().setStartYear(1490);
        model.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        model.getCalendar().setScoredPlay(true);
        final WorldMapPo map = new WorldMapPo();
        map.setMaxX(maxX);
        map.setMaxY(maxY);
        map.setSeed(terrain.getSeed());
        map.setTerrainType(types);
        map.setTrees(terrain.getTreeSet());
        map.setFields(terrain.getFieldSet());
        map.setRivers(terrain.getRiverMap());
        map.setRiverSegments(terrain.getRiverSegments());
        map.setResources(terrain.getResourceMap());
        map.setMineralReserves(terrain.getMineralReserveMap());
        final VisibilityPo mapVisibility = new VisibilityPo();
        mapVisibility.setVisibility(Set.of(), maxX, maxY);
        map.setVisibility(mapVisibility);
        model.setMap(map);
        // A new expedition starts every national market at the catalogue state.
        model.setEurope(Europe.initialPo());
        final EuropeanNation[] nations = EuropeanNation.values();
        int id = 1;
        for (int i = 0; i < nations.length; i++) {
            final EuropeanNation nation = nations[i];
            final String owner = nation.getPlayerName();
            model.getGameManager().getPlayers()
                    .add(player(owner, nation != humanNation,
                            1000 - 250 * difficulty.ordinal(), null, maxX,
                            maxY));
            final PlayerPo crown = player(owner + "'s King", true, 0, owner,
                    maxX, maxY);
            crown.setRoyalRoster(RoyalRoster.initial(difficulty));
            model.getGameManager().getPlayers().add(crown);
            final UnitType shipType = nation == EuropeanNation.DUTCH
                    ? UnitType.MERCHANTMAN
                    : UnitType.CARAVEL;
            final UnitPo ship = unit(id++, owner, shipType);
            final PlaceMapPo position = new PlaceMapPo();
            position.setLocation(
                    candidates.get(i * (candidates.size() - 1) / 3));
            position.setOrientation(Direction.west);
            ship.setPlaceMap(position);
            if (nation == humanNation) {
                model.setFocusedField(position.getLocation());
            }
            model.getUnits().add(ship);
            final UnitPo soldier = unit(id++, owner, UnitType.COLONIST);
            soldier.setHoldingGuns(true);
            final UnitPo pioneer = unit(id++, owner, UnitType.COLONIST);
            pioneer.setTools(100);
            for (final UnitPo passenger : List.of(soldier, pioneer)) {
                passenger.setPlaceCargoSlot(new PlaceCargoSlotPo());
                final CargoSlotPo slot = new CargoSlotPo();
                slot.setUnitId(passenger.getId());
                ship.getCargo().getSlots().add(slot);
                model.getUnits().add(passenger);
            }
            for (int j = 0; j < i; j++) {
                final EuropeanRelationPo relation = new EuropeanRelationPo();
                relation.setFirstPlayer(nations[j].getPlayerName());
                relation.setSecondPlayer(owner);
                relation.setState(EuropeanRelationState.PEACE);
                if (model.getEuropeanRelations() == null) {
                    model.setEuropeanRelations(new ArrayList<>());
                }
                model.getEuropeanRelations().add(relation);
            }
        }
        model.getGameManager().setCurrentPlayer(humanNation.getPlayerName());
        return model;
    }

    private static PlayerPo player(final String name, final boolean computer,
            final int gold, final String crownOf, final int maxX,
            final int maxY) {
        final PlayerPo player = new PlayerPo();
        player.setName(name);
        player.setComputer(computer);
        player.setGold(gold);
        player.setWhosKingThisPlayerIs(crownOf);
        if (crownOf == null) {
            player.setRecruitment(RecruitmentPool.initial());
            player.setCongress(Congress.initial());
            player.setTax(TaxState.initial());
        }
        final VisibilityPo visibility = new VisibilityPo();
        visibility.setVisibility(Set.of(), maxX, maxY);
        player.setVisible(visibility);
        return player;
    }

    private static UnitPo unit(final int id, final String owner,
            final UnitType type) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setOwnerId(owner);
        unit.setType(type);
        if (type == UnitType.COLONIST) {
            unit.setWorkUpdates(0);
            unit.setMissionary(false);
        }
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(MovementPoints.ofPoints(type.getSpeed()));
        return unit;
    }

    private static TerrainType at(final Map<Location, TerrainType> types,
            final Location location) {
        return types.getOrDefault(location, TerrainType.OCEAN);
    }

    private static boolean valid(final Location location, final int maxX,
            final int maxY) {
        return location.getX() >= 1 && location.getX() <= maxX
                && location.getY() >= 1 && location.getY() <= maxY;
    }
}
