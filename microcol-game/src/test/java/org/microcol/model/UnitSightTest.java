package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitCreateRequest;
import org.microcol.model.unit.UnitFactory;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFrigate;

/** Sight depends on the current role, never on movement allowance. */
class UnitSightTest {

    private static Stream<Arguments> currentTypes() {
        return Stream.of(Arguments.of(UnitType.COLONIST, 1),
                Arguments.of(UnitType.MASTER_BLACKSMITH, 1),
                Arguments.of(UnitType.WAGON, 1),
                Arguments.of(UnitType.CARAVEL, 2),
                Arguments.of(UnitType.MERCHANTMAN, 2),
                Arguments.of(UnitType.GALLEON, 2),
                Arguments.of(UnitType.PRIVATEER, 2),
                Arguments.of(UnitType.FRIGATE, 2),
                Arguments.of(UnitType.MAN_OF_WAR, 2));
    }

    private Unit unit(final UnitType type, final Location center,
            final boolean guns, final boolean horses) {
        final Model model = mock(Model.class);
        final WorldMap map = mock(WorldMap.class);
        when(model.getMap()).thenReturn(map);
        when(map.isValid(any(Location.class))).thenAnswer(call -> {
            final Location location = call.getArgument(0);
            return location.getX() >= 1 && location.getX() <= 5
                    && location.getY() >= 1 && location.getY() <= 5;
        });
        final Player owner = mock(Player.class);
        final UnitCreateRequest request = new UnitCreateRequest(model,
                unit -> new PlaceLocation(unit, center, Direction.east), 1,
                type, owner, new UnitActionNoAction(), 1);
        request.setCargoBuilder(unit -> mock(Cargo.class));
        request.setHoldingGuns(guns);
        request.setMounted(horses);
        return new UnitFactory().createUnit(request);
    }

    @ParameterizedTest
    @MethodSource("currentTypes")
    void currentTypesHaveClippedSquareSightIndependentOfMoves(
            final UnitType type, final int radius) {
        final Unit unit = unit(type, Location.of(3, 3), false, false);
        assertEquals(radius, unit.getSightRadius());
        final List<Location> sight = unit.getVisibleLocations();
        assertEquals((2 * radius + 1) * (2 * radius + 1), sight.size());
        assertTrue(sight.contains(Location.of(3 + radius, 3 + radius)));
        assertFalse(sight.contains(Location.of(3 + radius + 1, 3)));
        ((AbstractUnit) unit).setActionPoints(0);
        assertEquals(sight, unit.getVisibleLocations());
        assertEquals(radius, unit.getSightRadius());
    }

    @ParameterizedTest
    @MethodSource("currentTypes")
    void sightIsClippedAtEveryMapCorner(final UnitType type, final int radius) {
        for (Location corner : List.of(Location.of(1, 1), Location.of(1, 5),
                Location.of(5, 1), Location.of(5, 5))) {
            final Unit unit = unit(type, corner, false, false);
            assertEquals((radius + 1) * (radius + 1),
                    unit.getVisibleLocations().size());
            assertTrue(unit.getVisibleLocations().contains(corner));
            assertTrue(unit.getVisibleLocations().stream()
                    .allMatch(location -> location.getX() >= 1
                            && location.getX() <= 5 && location.getY() >= 1
                            && location.getY() <= 5));
        }
    }

    private static Stream<Arguments> colonistRoles() {
        return Stream.of(Arguments.of(false, false, 1),
                Arguments.of(true, false, 1), Arguments.of(false, true, 2),
                Arguments.of(true, true, 1));
    }

    @ParameterizedTest
    @MethodSource("colonistRoles")
    void bothColonistTypesUseEquipmentRoleForSight(final boolean guns,
            final boolean horses, final int radius) {
        for (UnitType type : List.of(UnitType.COLONIST,
                UnitType.MASTER_BLACKSMITH)) {
            final Unit unit = unit(type, Location.of(3, 3), guns, horses);
            assertEquals(radius, unit.getSightRadius());
            assertEquals((2 * radius + 1) * (2 * radius + 1),
                    unit.getVisibleLocations().size());
        }
    }

    @Test
    void mountedScoutRevealsImmediatelyAndKnowledgeSurvivesRoleLossAndSave() {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        final UnitFreeColonist scout = (UnitFreeColonist) model.getUnitById(0);
        final Player dutch = scout.getOwner();
        final Location center = Location.of(17, 5);
        final Location frontier = Location.of(19, 7);
        scout.placeToLocation(center);
        dutch.revealMapForUnit(scout);
        assertFalse(dutch.isVisible(frontier));
        scout.setMounted(true);
        assertEquals(2, scout.getSightRadius());
        assertTrue(dutch.isVisible(frontier));
        scout.setHoldingGuns(true);
        assertEquals(1, scout.getSightRadius());
        assertTrue(dutch.isVisible(frontier));
        scout.setMounted(false);
        assertEquals(1, scout.getSightRadius());
        assertTrue(dutch.isVisible(frontier));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.getPlayerByName("Dutch").isVisible(frontier));
        assertEquals(1, restored.getUnitById(0).getSightRadius());
    }

    @Test
    void becomingScoutFromDragoonRevealsButOffMapEquipmentDoesNot() {
        final UnitFreeColonist dragoon = (UnitFreeColonist) unit(
                UnitType.COLONIST, Location.of(3, 3), true, true);
        final Player owner = dragoon.getOwner();
        assertEquals(1, dragoon.getSightRadius());
        dragoon.setHoldingGuns(false);
        assertEquals(2, dragoon.getSightRadius());
        verify(owner).revealMapForUnit(dragoon);

        final Model model = mock(Model.class);
        final Player offMapOwner = mock(Player.class);
        final UnitFreeColonist inEurope = new UnitFreeColonist(model, 2,
                PlaceEuropePier::new, offMapOwner, 1, new UnitActionNoAction(),
                0, false, false);
        inEurope.setMounted(true);
        assertEquals(2, inEurope.getSightRadius());
        verify(offMapOwner, never()).revealMapForUnit(any());
        assertThrows(IllegalStateException.class,
                inEurope::getVisibleLocations);

        final UnitFreeColonist inCargo = new UnitFreeColonist(model, 3,
                unit -> new PlaceCargoSlot(unit, mock(CargoSlot.class)),
                offMapOwner, 1, new UnitActionNoAction(), 0, false, false);
        inCargo.setMounted(true);
        verify(offMapOwner, never()).revealMapForUnit(any());
        assertThrows(IllegalStateException.class, inCargo::getVisibleLocations);

        final UnitFrigate atAtlantic = new UnitFrigate(
                unit -> mock(Cargo.class), model, 4,
                unit -> new PlaceHighSea(unit, false, 0, 2, 2), offMapOwner, 1,
                new UnitActionNoAction());
        assertThrows(IllegalStateException.class,
                atAtlantic::getVisibleLocations);
        verify(offMapOwner, never()).revealMapForUnit(any());
    }

    @Test
    void disarmingDragoonIntoToolsOrColonyWorkNeverRevealsTransientScoutSight() {
        for (boolean colonyWork : List.of(false, true)) {
            final var saved = new GameModelPoDao()
                    .loadFromClassPath(
                            "/maps/default-campaign-mission-2-thrive.microcol")
                    .getModel();
            saved.getGameManager().getPlayers().stream()
                    .filter(player -> "Dutch".equals(player.getName()))
                    .findFirst().orElseThrow().getVisible()
                    .setVisible(new String[0]);
            final Model model = Model.make(saved,
                    MissionImpl.getPredefinedGameOverEvaluators());
            model.startGame();
            final UnitFreeColonist dragoon = (UnitFreeColonist) model
                    .getUnitById(13);
            final Player owner = dragoon.getOwner();
            final Location colonyLocation = Location.of(25, 14);
            final Location frontier = Location.of(27, 16);
            dragoon.placeToLocation(colonyLocation);
            dragoon.setHoldingGuns(true);
            dragoon.setMounted(true);
            final ColonyWarehouse warehouse = model.getColonyAt(colonyLocation)
                    .orElseThrow().getWarehouse();
            warehouse.removeGoods(Goods.of(GoodsType.MUSKET, 50));
            warehouse.removeGoods(Goods.of(GoodsType.HORSE, 50));
            owner.revealMapForUnit(dragoon);
            assertFalse(owner.isVisible(frontier));

            if (colonyWork) {
                final ConstructionSlot slot = model.getColonyAt(colonyLocation)
                        .orElseThrow().getConstructions().stream()
                        .flatMap(construction -> construction
                                .getConstructionSlots().stream())
                        .filter(candidate -> candidate.canAccept(dragoon))
                        .findFirst().orElseThrow();
                dragoon.placeToColonyStructureSlot(slot);
            } else {
                dragoon.equipWithTools();
            }
            assertEquals(1, dragoon.getSightRadius());
            assertFalse(owner.isVisible(frontier));
        }
    }
}
