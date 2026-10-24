package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.gson.Gson;

/**
 * Cost and population boundaries for each buildable row of the original
 * Building Chart, PDF page 134. Each boundary is checked with the other inputs
 * sufficient. A hammer or tool shortage waits without spending; the exact cost
 * and a surplus each complete the project once. A population one above the
 * requirement is admitted and completes. Population below and exact admission
 * are in {@link ConstructionAdmissionTest}; predecessor and founder gates are
 * in {@link ConstructionAdmissionTest} and {@link FounderConstructionTest};
 * building effects belong to their owners.
 */
class BuildingChartCompletionTest {

    /** Tools one basic warehouse holds. */
    private static final int BASIC_WAREHOUSE_TOOLS = 100;

    /** Tools one ordinary warehouse holds. */
    private static final int ORDINARY_WAREHOUSE_TOOLS = 200;

    /** Hammers left after a surplus completion, kept for the next project. */
    private static final int HAMMER_SURPLUS = 3;

    /** Tools left after a surplus completion. */
    private static final int TOOL_SURPLUS = 1;

    private Model model;
    private Colony colony;
    private ColonyBuildingQueue queue;

    static Stream<Arguments> chartRows() {
        return Stream.of(Arguments.of("LUMBER_MILL", 52, 0, 3),
                Arguments.of("BLACKSMITHS_SHOP", 64, 20, 4),
                Arguments.of("IRON_WORKS", 240, 100, 8),
                Arguments.of("STOCKADE", 64, 0, 3),
                Arguments.of("FORT", 120, 100, 4),
                Arguments.of("FORTRESS", 320, 200, 8),
                Arguments.of("TOBACCONISTS_SHOP", 64, 20, 4),
                Arguments.of("CIGAR_FACTORY", 160, 100, 8),
                Arguments.of("WEAVERS_SHOP", 64, 20, 4),
                Arguments.of("TEXTILE_MILL", 160, 100, 8),
                Arguments.of("RUM_DISTILLERY", 64, 20, 4),
                Arguments.of("RUM_FACTORY", 160, 100, 8),
                Arguments.of("FUR_TRADING_POST", 56, 20, 3),
                Arguments.of("FUR_FACTORY", 160, 100, 6),
                Arguments.of("ARMORY", 52, 0, 1),
                Arguments.of("MAGAZINE", 120, 50, 8),
                Arguments.of("ARSENAL", 240, 100, 8),
                Arguments.of("DOCK", 52, 0, 1),
                Arguments.of("DRYDOCK", 80, 50, 6),
                Arguments.of("SHIPYARD", 240, 100, 8),
                Arguments.of("SCHOOLHOUSE", 64, 0, 4),
                Arguments.of("COLLEGE", 160, 50, 8),
                Arguments.of("UNIVERSITY", 200, 100, 10),
                Arguments.of("WAREHOUSE", 80, 0, 1),
                Arguments.of("WAREHOUSE_EXPANSION", 80, 20, 1),
                Arguments.of("STABLES", 64, 0, 1),
                Arguments.of("CHURCH", 52, 0, 3),
                Arguments.of("CATHEDRAL", 176, 100, 8),
                Arguments.of("PRINTING_PRESS", 80, 0, 1),
                Arguments.of("NEWSPAPER", 120, 50, 4),
                Arguments.of("CUSTOM_HOUSE", 160, 50, 0));
    }

    /**
     * Prepares the colony with the predecessor of the project and exactly one
     * warehouse. A warehouse upgrade keeps its own legal predecessor; other
     * projects get the smallest warehouse that holds the tools needed.
     */
    private void prepare(final ConstructionType type, final int neededTools) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        type.getRequiredFounder().ifPresent(founder -> {
            final var congress = data.getModel().getGameManager().getPlayers()
                    .stream().filter(player -> player.getName().equals("Dutch"))
                    .findFirst().orElseThrow().getCongress();
            congress.setMembers(List.of(founder));
            congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                    Founder.THOMAS_JEFFERSON, Founder.JAKOB_FUGGER,
                    Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
            congress.setGeneration(2);
        });
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
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
        colony.getConstructions()
                .removeIf(building -> ConstructionType.WAREHOUSES
                        .contains(building.getType()));
        colony.getConstructions().add(Construction.build(model, colony,
                warehouseFor(type, neededTools)));
        queue = colony.getColonyBuildingQueue();
    }

    private static ConstructionType warehouseFor(final ConstructionType type,
            final int neededTools) {
        if (type == ConstructionType.WAREHOUSE) {
            return ConstructionType.WAREHOUSE_BASIC;
        }
        if (type == ConstructionType.WAREHOUSE_EXPANSION) {
            return ConstructionType.WAREHOUSE;
        }
        if (neededTools <= BASIC_WAREHOUSE_TOOLS) {
            return ConstructionType.WAREHOUSE_BASIC;
        }
        if (neededTools <= ORDINARY_WAREHOUSE_TOOLS) {
            return ConstructionType.WAREHOUSE;
        }
        return ConstructionType.WAREHOUSE_EXPANSION;
    }

    private void startProject(final ConstructionType type, final int tools,
            final int population) {
        prepare(type, tools + TOOL_SURPLUS);
        growTo(population);
        queue.addAtEnd(type);
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

    private void stock(final int hammers, final int tools) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, hammers));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, tools));
    }

    private int goods(final GoodsType goodsType) {
        return colony.getWarehouse().getGoods(goodsType).getAmount();
    }

    private long countOf(final ConstructionType type) {
        return colony.getConstructions().stream()
                .filter(building -> building.getType() == type).count();
    }

    private void assertWaiting(final ConstructionType type, final int hammers,
            final int tools) {
        assertEquals(0, countOf(type));
        assertEquals(hammers, goods(GoodsType.HAMMERS));
        assertEquals(tools, goods(GoodsType.TOOLS));
        assertEquals(1, queue.getBuildingQueue().size());
    }

    private void assertCompletedOnce(final ConstructionType type,
            final int hammers, final int tools) {
        assertEquals(1, countOf(type));
        type.getUpgradeFrom().ifPresent(
                predecessor -> assertEquals(0, countOf(predecessor)));
        assertEquals(hammers, goods(GoodsType.HAMMERS));
        assertEquals(tools, goods(GoodsType.TOOLS));
        assertTrue(queue.getBuildingQueue().isEmpty());
        final String completed = snapshot();
        queue.startTurn();
        assertEquals(completed, snapshot());
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    @ParameterizedTest(name = "{0}: {1} hammers, {2} tools, population {3}")
    @MethodSource("chartRows")
    void hammerShortageWaitsAndExactAndSurplusHammersCompleteOnce(
            final String name, final int hammers, final int tools,
            final int population) {
        final ConstructionType type = ConstructionType.valueOf(name);
        startProject(type, tools, population);
        stock(hammers - 1, tools);
        queue.startTurn();
        assertWaiting(type, hammers - 1, tools);
        stock(hammers, tools);
        queue.startTurn();
        assertCompletedOnce(type, 0, 0);

        startProject(type, tools, population);
        stock(hammers + HAMMER_SURPLUS, tools);
        queue.startTurn();
        assertCompletedOnce(type, HAMMER_SURPLUS, 0);
    }

    @ParameterizedTest(name = "{0}: {1} hammers, {2} tools, population {3}")
    @MethodSource("chartRows")
    void toolShortageWaitsAndExactAndSurplusToolsCompleteOnce(final String name,
            final int hammers, final int tools, final int population) {
        final ConstructionType type = ConstructionType.valueOf(name);
        startProject(type, tools, population);
        if (tools > 0) {
            stock(hammers + HAMMER_SURPLUS, tools - 1);
            queue.startTurn();
            assertWaiting(type, hammers + HAMMER_SURPLUS, tools - 1);
        }
        stock(hammers, tools);
        queue.startTurn();
        assertCompletedOnce(type, 0, 0);

        startProject(type, tools, population);
        stock(hammers, tools + TOOL_SURPLUS);
        queue.startTurn();
        assertCompletedOnce(type, 0, TOOL_SURPLUS);
    }

    @ParameterizedTest(name = "{0}: {1} hammers, {2} tools, population {3}")
    @MethodSource("chartRows")
    void populationAboveRequirementIsAdmittedAndCompletesOnce(final String name,
            final int hammers, final int tools, final int population) {
        final ConstructionType type = ConstructionType.valueOf(name);
        startProject(type, tools, population + 1);
        stock(hammers, tools);
        queue.startTurn();
        assertCompletedOnce(type, 0, 0);
    }
}
