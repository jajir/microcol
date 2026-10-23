package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.ColonyAdvisorReport.ColonyStatus;
import org.microcol.model.ColonyAdvisorReport.GoodsLine;
import org.microcol.model.ColonyAdvisorReport.ProjectState;
import org.microcol.model.ColonyAdvisorReport.Uncertainty;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.turnevent.TurnEventConstructionWaitingForTools;

import com.google.gson.Gson;

/**
 * R-0200 colony advisor report: each owned colony shows its residents, the
 * goods of the next update, the expected new colonists or famine and the active
 * project. The figures agree with the actual update, the report changes nothing
 * and survives a save, and it reads only the viewer's own nation.
 */
class ColonyAdvisorReportTest {

    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String FREE_PLAY = "/maps/free-play.microcol";

    private static final String DUTCH = "Dutch";

    /** Food stored per colonist that one update creates a new colonist from. */
    private static final int FOOD_PER_NEW_COLONIST = 200;

    private Model model;
    private Colony delft;

    private static Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static GameModelPo thrive() {
        return new GameModelPoDao().loadFromClassPath(THRIVE);
    }

    @BeforeEach
    void prepare() {
        loadDelft();
    }

    /** Loads a fresh game whose Delft has four residents, as the shop needs. */
    private void loadDelft() {
        loadDelft(thrive());
    }

    /**
     * Saved game whose Delft stands on an arctic centre, so the unattended
     * commons give no food and one resident can really starve, as in
     * ColonyFamineTest.
     */
    private static GameModelPo withoutCommonsFood() {
        final GameModelPo data = thrive();
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(Location.of(22, 12), TerrainType.ARCTIC);
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setFields(Collections.emptySet());
        return data;
    }

    /**
     * Saved game whose Dutch nation has Peter Stuyvesant, for a custom house.
     */
    private static GameModelPo withStuyvesant() {
        final GameModelPo data = thrive();
        final var congress = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> DUTCH.equals(player.getName()))
                .findFirst().orElseThrow().getCongress();
        congress.setMembers(List.of(Founder.PETER_STUYVESANT));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.JAKOB_FUGGER,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setGeneration(2);
        return data;
    }

    private void loadDelft(final GameModelPo data) {
        model = load(data);
        delft = model.getColonyByName("Delft").orElseThrow();
        final Construction hall = delft
                .getConstructionByType(ConstructionType.TOWN_HALL);
        for (int slot = 0; slot < 3; slot++) {
            model.addUnitOutSideColony(delft)
                    .placeToColonyStructureSlot(hall.getSlotAt(slot));
        }
        assertEquals(4, delft.getPopulation());
    }

    /**
     * Leaves Delft with one resident, the last one, as famine can leave it. The
     * first resident works in a building, because buildings come before fields,
     * so it adds no food of its own.
     */
    private void leaveOneResident() {
        final List<Unit> residents = delft.getUnitsInsideColony();
        for (final Unit resident : residents.subList(1, residents.size())) {
            resident.getPlace().destroy();
            model.destroyUnit(resident);
        }
        assertEquals(1, delft.getPopulation());
    }

    private static ColonyStatus statusOf(final ColonyAdvisorReport report,
            final Colony colony) {
        return report.getColonies().stream()
                .filter(status -> status.getColony() == colony).findFirst()
                .orElseThrow();
    }

    /** The line of a good that the report shows; absent goods fail here. */
    private static GoodsLine lineOf(final ColonyStatus status,
            final GoodsType type) {
        return status.getGoods().stream().filter(line -> line.getType() == type)
                .findFirst().orElseThrow();
    }

    private static boolean isShown(final ColonyStatus status,
            final GoodsType type) {
        return status.getGoods().stream()
                .anyMatch(line -> line.getType() == type);
    }

    private void stock(final GoodsType type, final int amount) {
        delft.getWarehouse().setGoods(Goods.of(type, amount));
    }

    private int amount(final GoodsType type) {
        return delft.getWarehouse().getGoods(type).getAmount();
    }

    private List<Integer> waitingNoticeMissingTools() {
        return model.getTurnEventsLocalizedMessages(delft.getOwner()).stream()
                .filter(TurnEventConstructionWaitingForTools.class::isInstance)
                .map(TurnEventConstructionWaitingForTools.class::cast)
                .map(TurnEventConstructionWaitingForTools::getMissingTools)
                .collect(Collectors.toList());
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    /**
     * Raw food after the residents ate, without any birth or breeding. It grows
     * one for one with the stock, so the stock that reaches a target is known.
     */
    private static int foodOffset(final Colony colony) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 0));
        return colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter();
    }

    /** Stores the food that leaves the given amount after the residents ate. */
    private static void setFoodAfterResidents(final Colony colony,
            final int target) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HORSE, 0));
        colony.getWarehouse().setGoods(
                Goods.of(GoodsType.CORN, target - foodOffset(colony)));
    }

    /** Adds a warehouse so that goods above the basic limit can be stored. */
    private void addWarehouseForGoods() {
        if (!delft.wasConstructionBuilded(ConstructionType.WAREHOUSE)) {
            delft.createConstruction(ConstructionType.WAREHOUSE);
        }
    }

    @Test
    void aNationWithoutColoniesHasAnEmptyReport() {
        final Model freePlay = load(
                new GameModelPoDao().loadFromClassPath(FREE_PLAY));
        final ColonyAdvisorReport report = freePlay
                .getColonyAdvisorReport(freePlay.getPlayerByName(DUTCH));
        assertTrue(report.getColonies().isEmpty());
    }

    @Test
    void twoUnequalColoniesKeepTheirOwnFiguresInIdOrder() {
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        // The first colony staffs its chapel, so its crosses double at once.
        model.addUnitOutSideColony(first).placeToColonyStructureSlot(first
                .getConstructionByType(ConstructionType.CHAPEL).getSlotAt(0));

        final ColonyAdvisorReport report = model
                .getColonyAdvisorReport(first.getOwner());
        assertEquals(List.of(0, 1, 2), report.getColonies().stream()
                .map(ColonyStatus::getColonyId).collect(Collectors.toList()));
        final ColonyStatus staffed = statusOf(report, first);
        final ColonyStatus unattended = statusOf(report, second);
        assertEquals(2, lineOf(staffed, GoodsType.CROSS).getProduced());
        assertEquals(1, lineOf(unattended, GoodsType.CROSS).getProduced());
        assertEquals(first.getPopulation(), staffed.getPopulation());
        assertEquals(second.getPopulation(), unattended.getPopulation());
        assertEquals(first.getName(), staffed.getName());
    }

    @Test
    void foodBoundariesDecideFamineAndNewColonistsLikeTheUpdate() {
        final int required = delft.getRequiredFoodPerTurn();
        // Food left after the residents: famine below zero, one birth per 200
        // food, and no birth at 199.
        final int[] targets = { -1, 0, FOOD_PER_NEW_COLONIST - 1,
                FOOD_PER_NEW_COLONIST, FOOD_PER_NEW_COLONIST + 1 };
        final int[] births = { 0, 0, 0, 1, 1 };
        for (int index = 0; index < targets.length; index++) {
            setFoodAfterResidents(delft, targets[index]);
            final ColonyStatus status = statusOf(
                    model.getColonyAdvisorReport(delft.getOwner()), delft);
            assertEquals(targets[index] < 0, status.isFamine());
            assertEquals(births[index], status.getNewColonists());
            assertEquals(required, lineOf(status, GoodsType.CORN).getConsumed()
                    - births[index] * FOOD_PER_NEW_COLONIST);
            // Famine empties the food; a birth keeps the rest of the food.
            final int nextFood = targets[index] < 0 ? 0
                    : targets[index] % FOOD_PER_NEW_COLONIST;
            assertEquals(nextFood, lineOf(status, GoodsType.CORN).getNext());
            assertEquals(targets[index] < 0 ? Uncertainty.LOST_COLONIST
                    : Uncertainty.NONE, status.getUncertainty());
        }
    }

    /**
     * The growth at each boundary is the growth the update really makes: a
     * fresh game per food level is updated once and compared with the report.
     */
    @Test
    void growthAtTheBoundariesMatchesTheActualUpdate() {
        final int[] targets = { -1, 0, FOOD_PER_NEW_COLONIST - 1,
                FOOD_PER_NEW_COLONIST, FOOD_PER_NEW_COLONIST + 1,
                2 * FOOD_PER_NEW_COLONIST };
        for (final int target : targets) {
            loadDelft();
            setFoodAfterResidents(delft, target);
            final ColonyStatus status = statusOf(
                    model.getColonyAdvisorReport(delft.getOwner()), delft);
            final int residents = delft.getPopulation();
            final int outsideBefore = delft.getUnitsOutSideColony().size();

            delft.startTurn();
            assertEquals(status.getNewColonists(),
                    delft.getUnitsOutSideColony().size() - outsideBefore,
                    "New colonists at food " + target);
            assertEquals(residents - (status.isFamine() ? 1 : 0),
                    delft.getPopulation(), "Residents at food " + target);
            if (!status.isFamine()) {
                assertEquals(lineOf(status, GoodsType.CORN).getNext(),
                        amount(GoodsType.CORN),
                        "Food after the update at " + target);
            }
        }
    }

    /**
     * Famine loses a random resident before the output is recomputed, so the
     * report says the output and project are uncertain and shows no more than
     * the current stock as exact. Reading it changes nothing.
     */
    @Test
    void aFamineMakesTheOutputAndTheProjectUncertainWithoutChangingAnything() {
        delft.getColonyBuildingQueue()
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(GoodsType.HAMMERS, 64);
        stock(GoodsType.TOOLS, 20);
        setFoodAfterResidents(delft, -1);
        final String before = snapshot();

        ColonyStatus status = null;
        for (int i = 0; i < 3; i++) {
            status = statusOf(model.getColonyAdvisorReport(delft.getOwner()),
                    delft);
        }
        assertEquals(before, snapshot());
        assertTrue(status.isFamine());
        assertEquals(Uncertainty.LOST_COLONIST, status.getUncertainty());
        assertEquals(0, status.getNewColonists());
        assertEquals(4, status.getPopulation());
        // The stock is exact; the completion is only as if no resident is lost.
        assertEquals(64, amount(GoodsType.HAMMERS));
        assertEquals(ProjectState.COMPLETES, status.getProject().getState());
    }

    /**
     * Famine that takes the last resident ends the colony. On a centre that
     * gives no food one resident eats 2 and produces nothing, so one stored
     * food is exactly one short; the real update then removes the colony.
     */
    @Test
    void theLastResidentLostToFamineIsReportedAndTheUpdateRemovesTheColony() {
        loadDelft(withoutCommonsFood());
        leaveOneResident();
        setFoodAfterResidents(delft, -1);
        // The legitimate setup: the stored food is positive and still short.
        assertEquals(1, amount(GoodsType.CORN));
        final Player owner = delft.getOwner();
        final String before = snapshot();

        final ColonyStatus status = statusOf(
                model.getColonyAdvisorReport(owner), delft);
        assertEquals(before, snapshot());
        assertTrue(status.isFamine());
        assertEquals(Uncertainty.COLONY_LOST, status.getUncertainty());
        assertEquals(0, status.getNewColonists());
        assertEquals(1, status.getPopulation());
        // The stock shown is the stock now.
        assertEquals(1, lineOf(status, GoodsType.CORN).getStock());

        delft.startTurn();
        assertFalse(delft.isValid());
        assertTrue(model.getColonyAdvisorReport(owner).getColonies().stream()
                .noneMatch(other -> other.getColony() == delft));
    }

    /**
     * A custom house completes at the update and changes storage there, so the
     * report marks the projection uncertain. Reading it changes nothing, and
     * the real update completes the house and leaves the goods the report
     * named.
     */
    @Test
    void aCompletingCustomHouseMakesTheStorageUncertainAndChangesNothing() {
        loadDelft(withStuyvesant());
        delft.getColonyBuildingQueue().addAtEnd(ConstructionType.CUSTOM_HOUSE);
        stock(GoodsType.HAMMERS, 160);
        stock(GoodsType.TOOLS, 50);
        final String before = snapshot();

        ColonyStatus status = null;
        for (int i = 0; i < 3; i++) {
            status = statusOf(model.getColonyAdvisorReport(delft.getOwner()),
                    delft);
        }
        assertEquals(before, snapshot());
        assertEquals(ProjectState.COMPLETES, status.getProject().getState());
        assertEquals(Uncertainty.STORAGE_CHANGE, status.getUncertainty());
        assertFalse(status.isFamine());
        assertFalse(delft
                .isContainsConstructionByType(ConstructionType.CUSTOM_HOUSE));
        assertTrue(waitingNoticeMissingTools().isEmpty());

        final int hammersNext = lineOf(status, GoodsType.HAMMERS).getNext();
        final int toolsNext = lineOf(status, GoodsType.TOOLS).getNext();
        delft.startTurn();
        assertTrue(delft
                .isContainsConstructionByType(ConstructionType.CUSTOM_HOUSE));
        assertEquals(hammersNext, amount(GoodsType.HAMMERS));
        assertEquals(toolsNext, amount(GoodsType.TOOLS));
    }

    /**
     * A completing warehouse changes storage in the same update, so the report
     * does not present its export and next stock as exact.
     */
    @Test
    void aCompletingWarehouseMakesTheStorageUncertain() {
        delft.getColonyBuildingQueue().addAtEnd(ConstructionType.WAREHOUSE);
        stock(GoodsType.HAMMERS, 80);
        final ColonyStatus status = statusOf(
                model.getColonyAdvisorReport(delft.getOwner()), delft);
        assertEquals(ProjectState.COMPLETES, status.getProject().getState());
        assertEquals(Uncertainty.STORAGE_CHANGE, status.getUncertainty());
        assertFalse(status.isFamine());
    }

    @Test
    void aProjectWaitsForHammersThenToolsThenCompletesLikeTheNotice() {
        delft.getColonyBuildingQueue()
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);

        stock(GoodsType.HAMMERS, 63);
        stock(GoodsType.TOOLS, 19);
        ColonyAdvisorReport report = model
                .getColonyAdvisorReport(delft.getOwner());
        assertEquals(ProjectState.WAITING_HAMMERS,
                statusOf(report, delft).getProject().getState());
        assertEquals(0, statusOf(report, delft).getProject().getMissingTools());

        // Enough hammers, one tool short: the notice names the same quantity.
        stock(GoodsType.HAMMERS, 64);
        report = model.getColonyAdvisorReport(delft.getOwner());
        assertEquals(ProjectState.WAITING_TOOLS,
                statusOf(report, delft).getProject().getState());
        assertEquals(1, statusOf(report, delft).getProject().getMissingTools());
        // Reading the report raises no notice and changes no stock.
        assertTrue(waitingNoticeMissingTools().isEmpty());
        assertEquals(19, amount(GoodsType.TOOLS));

        stock(GoodsType.TOOLS, 20);
        report = model.getColonyAdvisorReport(delft.getOwner());
        final ColonyStatus completing = statusOf(report, delft);
        assertEquals(ProjectState.COMPLETES,
                completing.getProject().getState());
        assertEquals(64, lineOf(completing, GoodsType.HAMMERS).getSpent());
        assertEquals(20, lineOf(completing, GoodsType.TOOLS).getSpent());
    }

    @Test
    void theNextUpdateLeavesTheSameStocksAsTheReport() {
        final ColonyStatus before = statusOf(
                model.getColonyAdvisorReport(delft.getOwner()), delft);
        final int outsideBefore = delft.getUnitsOutSideColony().size();
        delft.startTurn();
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            final int shown = isShown(before, type)
                    ? lineOf(before, type).getNext()
                    : 0;
            assertEquals(shown, amount(type), "Next stock of " + type);
        }
        assertEquals(before.getNewColonists(),
                delft.getUnitsOutSideColony().size() - outsideBefore);
        assertEquals(before.getPopulation() - (before.isFamine() ? 1 : 0),
                delft.getPopulation());
    }

    @Test
    void aProjectCompletesAtTheUpdateAndTakesItsCost() {
        delft.getColonyBuildingQueue()
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(GoodsType.HAMMERS, 64);
        stock(GoodsType.TOOLS, 20);
        final ColonyStatus status = statusOf(
                model.getColonyAdvisorReport(delft.getOwner()), delft);
        assertEquals(ProjectState.COMPLETES, status.getProject().getState());
        // A shop does not change storage, so its figures are exact.
        assertEquals(Uncertainty.NONE, status.getUncertainty());

        delft.startTurn();
        assertTrue(delft.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertTrue(delft.getColonyBuildingQueue().getBuildingQueue().isEmpty());
        assertEquals(ProjectState.NONE,
                statusOf(model.getColonyAdvisorReport(delft.getOwner()), delft)
                        .getProject().getState());
    }

    @Test
    void aBlacksmithHouseShowsFullInputsAndShortInputsOnlyWhatIsThere() {
        if (!delft.wasConstructionBuilded(ConstructionType.BLACKSMITHS_HOUSE)) {
            delft.createConstruction(ConstructionType.BLACKSMITHS_HOUSE);
        }
        final Construction house = delft
                .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE);
        final ConstructionSlot emptySlot = house.getConstructionSlots().stream()
                .filter(ConstructionSlot::isEmpty).findFirst().orElseThrow();
        model.addUnitOutSideColony(delft).placeToColonyStructureSlot(emptySlot);

        stock(GoodsType.ORE, 10);
        final ColonyStatus full = statusOf(
                model.getColonyAdvisorReport(delft.getOwner()), delft);
        stock(GoodsType.ORE, 1);
        final ColonyStatus shortInput = statusOf(
                model.getColonyAdvisorReport(delft.getOwner()), delft);

        assertTrue(lineOf(full, GoodsType.TOOLS)
                .getProduced() > lineOf(shortInput, GoodsType.TOOLS)
                        .getProduced());
        // A short input limits the output, and the ore used is the ore there.
        assertEquals(1, lineOf(shortInput, GoodsType.ORE).getConsumed());
        assertTrue(lineOf(shortInput, GoodsType.TOOLS).getBlocked() > 0);
        assertEquals(0, lineOf(full, GoodsType.TOOLS).getBlocked());
    }

    @Test
    void theCustomHouseSendsOnlyWhatIsAboveTheReserveOfOneHundred() {
        addWarehouseForGoods();
        delft.createConstruction(ConstructionType.CUSTOM_HOUSE);
        delft.configureExportGoods(delft.getExportGoods(),
                Set.of(GoodsType.TOOLS));
        stock(GoodsType.TOOLS, 100);
        final GoodsLine atReserve = lineOf(
                statusOf(model.getColonyAdvisorReport(delft.getOwner()), delft),
                GoodsType.TOOLS);
        // At the reserve only this update's own production leaves.
        assertEquals(atReserve.getProduced(), atReserve.getExported());

        stock(GoodsType.TOOLS, 150);
        final GoodsLine above = lineOf(
                statusOf(model.getColonyAdvisorReport(delft.getOwner()), delft),
                GoodsType.TOOLS);
        assertEquals(50 + above.getProduced(), above.getExported());
        assertEquals(100, above.getNext());
        assertEquals(above.getNext(), above.getStock() + above.getChange());
    }

    @Test
    void readingTheReportChangesNoStockProjectOrNotice() {
        delft.getColonyBuildingQueue()
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(GoodsType.HAMMERS, 64);
        stock(GoodsType.TOOLS, 19);
        final String before = snapshot();
        for (int i = 0; i < 3; i++) {
            model.getColonyAdvisorReport(delft.getOwner());
        }
        assertEquals(before, snapshot());
        assertTrue(waitingNoticeMissingTools().isEmpty());
    }

    @Test
    void ownerIsTheGamesOwnColonialNationOnly() {
        final Model other = load(thrive());
        final Player sameName = other.getPlayerByName(DUTCH);
        // Player equality is by name, so identity is the only check.
        assertEquals(model.getPlayerByName(DUTCH), sameName);
        assertThrows(IllegalArgumentException.class,
                () -> model.getColonyAdvisorReport(sameName));

        final Model freePlay = load(
                new GameModelPoDao().loadFromClassPath(FREE_PLAY));
        final Player king = freePlay.getPlayers().stream()
                .filter(Player::isKing).findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> freePlay.getColonyAdvisorReport(king));
    }

    @Test
    void theReportListsOnlyTheViewersColonies() {
        final Player owner = delft.getOwner();
        final ColonyAdvisorReport report = model.getColonyAdvisorReport(owner);
        assertEquals(owner.getColonies().size(), report.getColonies().size());
        assertTrue(report.getColonies().stream()
                .allMatch(status -> status.getColony().getOwner() == owner));
        assertFalse(report.getColonies().isEmpty());
    }

    @Test
    void aSavedAndReloadedNationShowsTheSameFigures() {
        final ColonyAdvisorReport before = model
                .getColonyAdvisorReport(delft.getOwner());
        final Model reloaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ColonyAdvisorReport after = reloaded
                .getColonyAdvisorReport(reloaded.getPlayerByName(DUTCH));
        assertEquals(before.getColonies().size(), after.getColonies().size());
        for (int index = 0; index < before.getColonies().size(); index++) {
            assertEquals(before.getColonies().get(index).getPopulation(),
                    after.getColonies().get(index).getPopulation());
            assertEquals(before.getColonies().get(index).getGoods().size(),
                    after.getColonies().get(index).getGoods().size());
        }
    }
}
