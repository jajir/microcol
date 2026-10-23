package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;

class ReferenceExpeditionTest {

    private WorldMapPo terrain(final int highSeaCount, final boolean coast) {
        final WorldMapPo map = new WorldMapPo();
        map.setMaxX(8);
        map.setMaxY(8);
        map.setSeed(1);
        final Map<Location, TerrainType> types = new HashMap<>();
        if (coast) {
            types.put(Location.of(1, 4), TerrainType.GRASSLAND);
        }
        for (int y = 1; y <= highSeaCount; y++) {
            types.put(Location.of(8, y), TerrainType.HIGH_SEA);
        }
        map.setTerrainType(types);
        map.setTrees(Set.of());
        map.setFields(Set.of());
        final VisibilityPo authoredVisibility = new VisibilityPo();
        authoredVisibility.setVisibility(Set.of(Location.of(1, 4)), 8, 8);
        map.setVisibility(authoredVisibility);
        return map;
    }

    @Test
    void completeRosterAcrossNationsAndDifficulties() {
        final WorldMapPo source = terrain(8, true);
        final String[] sourceTiles = source.getTiles().clone();
        final String[] sourceTrees = source.getTrees().clone();
        final String[] sourceFields = source.getFields().clone();
        for (final EuropeanNation selected : EuropeanNation.values()) {
            for (final Difficulty difficulty : Difficulty.values()) {
                final ModelPo po = ReferenceExpedition.create(source, selected,
                        difficulty);
                final ModelPo repeat = ReferenceExpedition.create(source,
                        selected, difficulty);
                assertEquals(
                        po.getUnits().stream()
                                .filter(unit -> unit.getType().isShip())
                                .map(unit -> unit.getPlaceMap().getLocation())
                                .collect(Collectors.toList()),
                        repeat.getUnits().stream()
                                .filter(unit -> unit.getType().isShip())
                                .map(unit -> unit.getPlaceMap().getLocation())
                                .collect(Collectors.toList()));
                assertEquals(8, po.getGameManager().getPlayers().size());
                assertEquals(12, po.getUnits().size());
                assertEquals(6, po.getEuropeanRelations().size());
                assertTrue(po.getEuropeanRelations().stream()
                        .allMatch(relation -> relation
                                .getState() == EuropeanRelationState.PEACE));
                assertEquals(0, po.getNextColonyId());
                assertTrue(po.getColonies().isEmpty());
                assertTrue(po.getMap().getVisibility().getVisibilitySet()
                        .isEmpty());
                assertTrue(po.getGameManager().getPlayers().stream()
                        .allMatch(player -> player.getVisible()
                                .getVisibilitySet().isEmpty()));
                assertEquals(1490, po.getCalendar().getStartYear());
                assertEquals(1850, po.getCalendar().getEndYear());
                assertTrue(po.getCalendar().isScoredPlay());
                assertFalse(po.getGameManager().isGameStarted());
                assertEquals(selected.getPlayerName(),
                        po.getGameManager().getCurrentPlayer());
                assertTrue(po.getUnits().stream().allMatch(
                        unit -> unit.getAction() instanceof UnitActionNoActionPo
                                && unit.getAvailableMoves() == unit.getType()
                                        .getSpeed()));
                final Set<Location> starts = new HashSet<>();
                for (final EuropeanNation nation : EuropeanNation.values()) {
                    final String owner = nation.getPlayerName();
                    assertEquals(1000 - 250 * difficulty.ordinal(), po
                            .getGameManager().getPlayers().stream()
                            .filter(player -> owner.equals(player.getName()))
                            .findFirst().orElseThrow().getGold());
                    final UnitPo ship = po.getUnits().stream()
                            .filter(unit -> owner.equals(unit.getOwnerId())
                                    && unit.getType().isShip())
                            .findFirst().orElseThrow();
                    assertEquals(nation == EuropeanNation.DUTCH
                            ? UnitType.MERCHANTMAN
                            : UnitType.CARAVEL, ship.getType());
                    assertEquals(ship.getType().getSpeed(),
                            ship.getAvailableMoves());
                    assertEquals(2, ship.getCargo().getSlots().size());
                    assertTrue(starts.add(ship.getPlaceMap().getLocation()));
                    if (nation == selected) {
                        assertEquals(ship.getPlaceMap().getLocation(),
                                po.getFocusedField());
                    }
                    final var passengers = po.getUnits().stream()
                            .filter(unit -> owner.equals(unit.getOwnerId())
                                    && unit.getType() == UnitType.COLONIST)
                            .collect(Collectors.toList());
                    assertEquals(2, passengers.size());
                    assertEquals(1, passengers.stream()
                            .filter(UnitPo::isHoldingGuns).count());
                    assertEquals(1, passengers.stream()
                            .filter(unit -> unit.getTools() == 100).count());
                    assertTrue(
                            passengers.stream().noneMatch(UnitPo::isMounted));
                    assertTrue(passengers.stream().allMatch(unit -> ship
                            .getCargo().containsUnitInCargo(unit.getId())));
                }
                final Model loaded = Model.make(po,
                        MissionImpl.getPredefinedGameOverEvaluators());
                loaded.startGame();
                final ModelPo saved = loaded.save();
                assertEquals(12, saved.getUnits().size());
                assertEquals(8, saved.getGameManager().getPlayers().size());
                assertEquals(6, saved.getEuropeanRelations().size());
                final Model restored = Model.make(saved,
                        MissionImpl.getPredefinedGameOverEvaluators());
                assertEquals(difficulty, restored.getDifficulty());
                assertEquals("England", loaded.getCurrentPlayer().getName());
                assertEquals(loaded.getCurrentPlayer().getName(),
                        restored.getCurrentPlayer().getName());
                assertEquals(12, restored.getAllUnits().size());
                for (final EuropeanNation nation : EuropeanNation.values()) {
                    final String owner = nation.getPlayerName();
                    assertEquals(1000 - 250 * difficulty.ordinal(),
                            restored.getPlayerByName(owner).getGold());
                    assertEquals(nation != selected,
                            restored.getPlayerByName(owner).isComputer());
                    final Ship ship = (Ship) restored.getAllUnits().stream()
                            .filter(unit -> unit.isShip()
                                    && owner.equals(unit.getOwner().getName()))
                            .findFirst().orElseThrow();
                    assertEquals(nation == EuropeanNation.DUTCH
                            ? UnitType.MERCHANTMAN
                            : UnitType.CARAVEL, ship.getType());
                    assertEquals(2, ship.getCargo().getSlots().stream()
                            .filter(slot -> slot.isLoadedUnit()).count());
                    final var people = restored.getAllUnits().stream()
                            .filter(unit -> unit.getType() == UnitType.COLONIST
                                    && owner.equals(unit.getOwner().getName()))
                            .map(unit -> (UnitFreeColonist) unit)
                            .collect(Collectors.toList());
                    assertEquals(2, people.size());
                    assertTrue(people.stream().allMatch(Unit::isAtCargoSlot));
                    assertEquals(1, people.stream()
                            .filter(UnitFreeColonist::isHoldingGuns).count());
                    assertEquals(1, people.stream()
                            .filter(unit -> unit.getTools() == 100).count());
                    assertTrue(people.stream()
                            .noneMatch(UnitFreeColonist::isMounted));
                }
            }
        }
        assertEquals(8, source.getMaxX());
        assertEquals(8, source.getMaxY());
        assertArrayEquals(sourceTiles, source.getTiles());
        assertArrayEquals(sourceTrees, source.getTrees());
        assertArrayEquals(sourceFields, source.getFields());
        assertEquals(1, source.getSeed());
        assertEquals(Set.of(Location.of(1, 4)),
                source.getVisibility().getVisibilitySet());
    }

    @Test
    void unsuitableTerrainIsRejectedWithoutMutation() {
        final WorldMapPo few = terrain(3, true);
        final String[] original = few.getTiles().clone();
        assertThrows(IllegalArgumentException.class, () -> ReferenceExpedition
                .create(few, EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
        assertArrayEquals(original, few.getTiles());
        assertThrows(IllegalArgumentException.class,
                () -> ReferenceExpedition.create(terrain(8, false),
                        EuropeanNation.DUTCH, Difficulty.CONQUISTADOR));
    }
}
