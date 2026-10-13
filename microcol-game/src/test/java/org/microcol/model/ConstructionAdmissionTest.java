package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

class ConstructionAdmissionTest {
    private Model model;
    private Colony colony;
    private ColonyBuildingQueue queue;

    private void prepare(final ConstructionType type,
            final String currentPlayer) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getGameManager().setCurrentPlayer(currentPlayer);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        colony = model.getColonyByName("Delft").orElseThrow();
        colony.getConstructions().removeIf(building -> type.getUpgradeChain()
                .contains(building.getType()));
        type.getUpgradeFrom().ifPresent(predecessor -> {
            if (colony.getConstructions().stream()
                    .noneMatch(building -> building.getType() == predecessor)) {
                colony.getConstructions()
                        .add(Construction.build(model, colony, predecessor));
            }
        });
        queue = colony.getColonyBuildingQueue();
    }

    private ConstructionSlot freeSlot() {
        return colony.getConstructions().stream()
                .flatMap(building -> building.getConstructionSlots().stream())
                .filter(ConstructionSlot::isEmpty).findFirst().orElseThrow();
    }

    private void growTo(final int population) {
        while (colony.getUnitsInsideColony().size() < population) {
            model.addUnitOutSideColony(colony)
                    .placeToColonyStructureSlot(freeSlot());
        }
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    static Stream<ConstructionType> projects() {
        return ConstructionType.ALL.stream()
                .filter(ConstructionType::canBeConstructed);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("projects")
    void everyProjectUsesItsPopulationBoundaryAndSharedAdmission(
            final ConstructionType type) {
        prepare(type, "Dutch");
        final int population = type.getRequiredColonyPopulation();
        if (population > 1) {
            growTo(population - 1);
            final Unit visitor = model.addUnitOutSideColony(colony);
            assertEquals(ColonyBuildingQueue.UnavailableReason.POPULATION,
                    queue.getUnavailableReason(type).orElseThrow());
            final String before = snapshot();
            assertThrows(IllegalArgumentException.class,
                    () -> queue.addAtEnd(type));
            assertEquals(before, snapshot());
            assertTrue(queue.getBuildigItemsConstruction().stream()
                    .noneMatch(item -> item.getConstructionType() == type));
            visitor.placeToColonyStructureSlot(freeSlot());
        }
        assertTrue(queue.getUnavailableReason(type).isEmpty());
        assertTrue(queue.getBuildigItemsConstruction().stream()
                .anyMatch(item -> item.getConstructionType() == type));
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(type);
        assertEquals(type, item.getConstructionType());
        assertEquals(List.of(item), queue.getBuildingQueue());
        assertEquals(ColonyBuildingQueue.UnavailableReason.ALREADY_QUEUED,
                queue.getUnavailableReason(type).orElseThrow());
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(type));
        assertEquals(before, snapshot());
    }

    static Stream<ConstructionType> upgrades() {
        return projects().filter(type -> type.getUpgradeFrom().isPresent());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("upgrades")
    void everyUpgradeRequiresItsImmediatePredecessor(
            final ConstructionType type) {
        prepare(type, "Dutch");
        colony.getConstructions().removeIf(building -> building
                .getType() == type.getUpgradeFrom().orElseThrow());
        growTo(Math.max(1, type.getRequiredColonyPopulation()));
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_PREDECESSOR,
                queue.getUnavailableReason(type).orElseThrow());
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(type));
        assertEquals(before, snapshot());
    }

    @Test
    void initialTypesAndAnUnknownTypeCannotBeAdmitted() {
        prepare(ConstructionType.WAREHOUSE, "Dutch");
        assertEquals(ColonyBuildingQueue.UnavailableReason.NOT_A_PROJECT,
                queue.getUnavailableReason(ConstructionType.TOWN_HALL)
                        .orElseThrow());
        assertEquals(ColonyBuildingQueue.UnavailableReason.UNKNOWN_TYPE, queue
                .getUnavailableReason((ConstructionType) null).orElseThrow());
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.TOWN_HALL));
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd((ConstructionType) null));
        assertEquals(before, snapshot());
    }

    @Test
    void anotherPlayersTurnAndAnAbandonedColonyRejectAdmission() {
        prepare(ConstructionType.WAREHOUSE, "Dutch's King");
        assertEquals(ColonyBuildingQueue.UnavailableReason.COLONY_UNAVAILABLE,
                queue.getUnavailableReason(ConstructionType.WAREHOUSE)
                        .orElseThrow());
        String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.WAREHOUSE));
        assertEquals(before, snapshot());
        prepare(ConstructionType.WAREHOUSE, "Dutch");
        model.destroyColony(colony);
        assertEquals(ColonyBuildingQueue.UnavailableReason.COLONY_UNAVAILABLE,
                queue.getUnavailableReason(ConstructionType.WAREHOUSE)
                        .orElseThrow());
        before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.WAREHOUSE));
        assertEquals(before, snapshot());
    }

    @Test
    void existingHigherTierAndConflictingQueueCannotBeBypassed() {
        prepare(ConstructionType.WAREHOUSE, "Dutch");
        colony.getConstructions().add(Construction.build(model, colony,
                ConstructionType.WAREHOUSE_EXPANSION));
        assertEquals(ColonyBuildingQueue.UnavailableReason.ALREADY_BUILT,
                queue.getUnavailableReason(ConstructionType.WAREHOUSE)
                        .orElseThrow());
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.WAREHOUSE));
        assertEquals(before, snapshot());
        colony.getConstructions().removeIf(building -> building
                .getType() == ConstructionType.WAREHOUSE_EXPANSION);
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        assertEquals(ColonyBuildingQueue.UnavailableReason.ALREADY_QUEUED,
                queue.getUnavailableReason(ConstructionType.WAREHOUSE_EXPANSION)
                        .orElseThrow());
        final String queued = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.WAREHOUSE_EXPANSION));
        assertEquals(queued, snapshot());
    }
}
