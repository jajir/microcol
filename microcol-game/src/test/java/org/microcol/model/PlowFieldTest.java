package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.store.UnitActionPlowFieldPo;
import org.microcol.model.turnevent.TurnEventFieldPlowed;
import org.microcol.model.unit.UnitActionConverter;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class PlowFieldTest {

    private static final Location TARGET = Location.of(24, 14);

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private final Gson gson = new Gson();

    private GameModelPo data(final boolean forest, final boolean plowed) {
        final GameModelPo data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        trees.remove(TARGET);
        if (forest) {
            trees.add(TARGET);
        }
        data.getModel().getMap().setTrees(trees);
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        fields.remove(TARGET);
        if (plowed) {
            fields.add(TARGET);
        }
        data.getModel().getMap().setFields(fields);
        return data;
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Model fixture() {
        return make(data(false, false));
    }

    private UnitFreeColonist pioneer(final Model model, final int tools,
            final Location at) {
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        pioneer.placeToLocation(at);
        pioneer.setTools(tools);
        pioneer.setActionPoints(pioneer.getSpeed());
        return pioneer;
    }

    private void nextOwnerTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private int remaining(final Unit unit) {
        return ((UnitActionPlowField) unit.getAction()).getRemainingTurns();
    }

    private UnitFreeColonist hardy(final Model model, final int tools,
            final Location at) {
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(UnitType.HARDY_PIONEER.getEuropePrice());
        buyer.buy(UnitType.HARDY_PIONEER);
        final UnitFreeColonist hardy = (UnitFreeColonist) buyer.getAllUnits()
                .stream()
                .filter(unit -> unit.getType() == UnitType.HARDY_PIONEER)
                .findFirst().orElseThrow();
        hardy.placeToLocation(at);
        hardy.setTools(tools);
        hardy.setActionPoints(hardy.getSpeed());
        return hardy;
    }

    private List<TurnEventFieldPlowed> notices(final Model model,
            final Player player) {
        return model.getTurnEventStore().getForPlayer(player).stream()
                .filter(TurnEventFieldPlowed.class::isInstance)
                .map(TurnEventFieldPlowed.class::cast)
                .collect(Collectors.toList());
    }

    private List<TurnEventFieldPlowed> dutchNotices(final Model model) {
        return notices(model, model.getPlayerByName("Dutch"));
    }

    private void verifyNotice(final TurnEventFieldPlowed notice,
            final Unit unit, final Location expectedField) {
        assertEquals("Dutch", notice.getPlayerName());
        assertEquals(unit.getId(), notice.getUnitId());
        assertEquals(unit.getType(), notice.getUnitType());
        assertEquals(expectedField, notice.getLocation());
    }

    @Test
    void ordinaryCompletionReportsOnceToTheOwnerOnly() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        assertTrue(dutchNotices(model).isEmpty());
        for (int turn = 0; turn < 3; turn++) {
            nextOwnerTurn(model);
            assertTrue(dutchNotices(model).isEmpty(),
                    "Progress must not be reported as completion");
        }
        nextOwnerTurn(model);
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        final List<TurnEventFieldPlowed> notices = dutchNotices(model);
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), pioneer, TARGET);
        model.getPlayers().stream()
                .filter(player -> !"Dutch".equals(player.getName()))
                .forEach(player -> assertTrue(notices(model, player).isEmpty(),
                        "Another player must not see the notice"));
        // The next owner update has no new completion and no repeat.
        nextOwnerTurn(model);
        assertTrue(dutchNotices(model).isEmpty());
    }

    @Test
    void hardyCompletionReportsAfterTwoUpdates() {
        final Model model = fixture();
        final UnitFreeColonist hardy = hardy(model, 100, TARGET);
        hardy.setActionType(UnitActionType.plowField);
        assertEquals(2, remaining(hardy));
        nextOwnerTurn(model);
        assertEquals(1, remaining(hardy));
        assertTrue(dutchNotices(model).isEmpty());
        nextOwnerTurn(model);
        final List<TurnEventFieldPlowed> notices = dutchNotices(model);
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), hardy, TARGET);
        assertEquals(UnitType.HARDY_PIONEER, notices.get(0).getUnitType());
    }

    @Test
    void canceledStaleAndToollessWorkReportNoCompletion() {
        final Model model = fixture();
        final UnitFreeColonist canceled = pioneer(model, 100, TARGET);
        canceled.setActionType(UnitActionType.plowField);
        nextOwnerTurn(model);
        model.cancelPlow(canceled);
        final UnitFreeColonist toolless = pioneer(model, 100, TARGET);
        toolless.setActionType(UnitActionType.plowField);
        toolless.setTools(5);
        for (int turn = 0; turn < 5; turn++) {
            nextOwnerTurn(model);
            assertTrue(dutchNotices(model).isEmpty());
        }
        final UnitFreeColonist stale = pioneer(model, 100, TARGET);
        stale.setActionType(UnitActionType.plowField);
        model.getMap().plowFiled(TARGET);
        nextOwnerTurn(model);
        assertEquals(UnitActionType.noAction, stale.getAction().getType());
        assertTrue(dutchNotices(model).isEmpty(),
                "A field made by someone else is not this unit's completion");
    }

    @Test
    void retainedCallbackAfterCompletionDoesNotRepeatTheNotice() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField action = (UnitActionPlowField) pioneer
                .getAction();
        action.setRemainingTurns(1);
        nextOwnerTurn(model);
        assertEquals(1, dutchNotices(model).size());
        assertTrue(action.startTurn(model, pioneer));
        assertEquals(1, dutchNotices(model).size());
    }

    @Test
    void everyCompletingPioneerGetsItsOwnNotice() {
        final GameModelPo data = data(false, false);
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        TARGET.getNeighbors().forEach(neighbor -> {
            trees.remove(neighbor);
            fields.remove(neighbor);
        });
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setFields(fields);
        final Model model = make(data);
        final UnitFreeColonist first = pioneer(model, 100, TARGET);
        final UnitFreeColonist second = pioneer(model, 100, TARGET);
        final Location other = TARGET.getNeighbors().stream()
                .filter(neighbor -> model.getMap().isValid(neighbor))
                .filter(neighbor -> model.getMap().getTerrainAt(neighbor)
                        .getTerrainType().isCanHaveField()
                        && model.getColonyAt(neighbor).isEmpty())
                .filter(neighbor -> {
                    second.placeToLocation(neighbor);
                    return model.getPlowProblem(second).isEmpty();
                }).findFirst().orElseThrow();
        second.placeToLocation(other);
        first.setActionType(UnitActionType.plowField);
        second.setActionType(UnitActionType.plowField);
        for (int turn = 0; turn < 4; turn++) {
            nextOwnerTurn(model);
        }
        final List<TurnEventFieldPlowed> notices = dutchNotices(model);
        assertEquals(2, notices.size());
        verifyNotice(notices.stream()
                .filter(notice -> notice.getUnitId() == first.getId())
                .findFirst().orElseThrow(), first, TARGET);
        verifyNotice(notices.stream()
                .filter(notice -> notice.getUnitId() == second.getId())
                .findFirst().orElseThrow(), second, other);
    }

    @Test
    void completionNoticeSurvivesDiskRoundtripWithoutDuplication() {
        final GameModelPo data = data(false, false);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final int id = pioneer.getId();
        pioneer.setActionType(UnitActionType.plowField);
        ((UnitActionPlowField) pioneer.getAction()).setRemainingTurns(1);
        nextOwnerTurn(model);
        assertEquals(1, dutchNotices(model).size());
        data.setModel(model.save());
        final Path file = directory.resolve("plowed.microcol");
        dao.saveToFile(file.toString(), data);
        model = make(dao.loadFromFile(file.toFile()));
        final String saved = gson.toJson(model.save());
        final List<TurnEventFieldPlowed> notices = dutchNotices(model);
        assertEquals(1, notices.size());
        verifyNotice(notices.get(0), model.getUnitById(id), TARGET);
        // Reading the restored report again changes nothing.
        assertEquals(1, dutchNotices(model).size());
        assertEquals(saved, gson.toJson(model.save()));
        nextOwnerTurn(model);
        assertTrue(dutchNotices(model).isEmpty());
    }

    @Test
    void incompleteOrInvalidSavedNoticeIsRejected() {
        final TurnEventPo other = new TurnEventPo();
        other.setType("TurnEventGoTo");
        assertNull(TurnEventFieldPlowed.tryLoad(other));
        for (int missing = 0; missing < 4; missing++) {
            final TurnEventPo po = new TurnEventPo();
            po.setType(TurnEventFieldPlowed.class.getSimpleName());
            po.setPlayerName("Dutch");
            po.setFieldPlowedUnitId(missing == 0 ? null : 7);
            po.setFieldPlowedUnitType(missing == 1 ? null : UnitType.COLONIST);
            po.setFieldPlowedX(missing == 2 ? null : 24);
            po.setFieldPlowedY(missing == 3 ? null : 14);
            assertThrows(IllegalArgumentException.class,
                    () -> TurnEventFieldPlowed.tryLoad(po));
        }
        final TurnEventPo negative = new TurnEventPo();
        negative.setType(TurnEventFieldPlowed.class.getSimpleName());
        negative.setPlayerName("Dutch");
        negative.setFieldPlowedUnitId(-1);
        negative.setFieldPlowedUnitType(UnitType.COLONIST);
        negative.setFieldPlowedX(24);
        negative.setFieldPlowedY(14);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventFieldPlowed.tryLoad(negative));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 19 })
    void tooFewToolsRejectWithoutMutation(final int tools) {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, tools, TARGET);
        final String before = gson.toJson(model.save());
        assertEquals(PlowProblem.NOT_ENOUGH_TOOLS,
                model.getPlowProblem(pioneer).orElseThrow());
        assertFalse(pioneer.canPlowFiled());
        assertThrows(IllegalStateException.class,
                () -> pioneer.setActionType(UnitActionType.plowField));
        assertEquals(before, gson.toJson(model.save()));
    }

    @ParameterizedTest
    @ValueSource(ints = { 20, 40, 100 })
    void enoughToolsPlowOnceAndChargeExactlyTwenty(final int tools) {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, tools, TARGET);
        assertTrue(model.getPlowProblem(pioneer).isEmpty());
        assertTrue(pioneer.canPlowFiled());
        pioneer.setActionType(UnitActionType.plowField);
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(4, remaining(pioneer));
        assertEquals(tools, pioneer.getTools());
        for (int expected = 3; expected >= 1; expected--) {
            nextOwnerTurn(model);
            assertEquals(expected, remaining(pioneer));
            assertEquals(tools, pioneer.getTools());
            assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
        }
        nextOwnerTurn(model);
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        assertEquals(tools - 20, pioneer.getTools());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        nextOwnerTurn(model);
        assertEquals(tools - 20, pioneer.getTools());
    }

    @Test
    void exactlyTwentyToolsLeavesZeroAndNoSecondCharge() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 20, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField action = (UnitActionPlowField) pioneer
                .getAction();
        for (int turn = 0; turn < 4; turn++) {
            nextOwnerTurn(model);
        }
        assertEquals(0, pioneer.getTools());
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        // A repeated completion callback must not charge or fail.
        assertTrue(action.startTurn(model, pioneer));
        assertEquals(0, pioneer.getTools());
        assertEquals(PlowProblem.NOT_ENOUGH_TOOLS,
                model.getPlowProblem(pioneer).orElseThrow());
    }

    @Test
    void forestFieldColonyAndShipRejectWithoutMutation() {
        final Model forest = make(data(true, false));
        final UnitFreeColonist inForest = pioneer(forest, 100, TARGET);
        assertEquals(PlowProblem.FOREST,
                forest.getPlowProblem(inForest).orElseThrow());
        final Model plowed = make(data(false, true));
        final UnitFreeColonist onField = pioneer(plowed, 100, TARGET);
        assertEquals(PlowProblem.ALREADY_PLOWED,
                plowed.getPlowProblem(onField).orElseThrow());
        final Model model = fixture();
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist inColony = pioneer(model, 100,
                colony.getLocation());
        assertEquals(PlowProblem.COLONY,
                model.getPlowProblem(inColony).orElseThrow());
        final Unit ship = model.getUnitById(3);
        assertThrows(IllegalStateException.class,
                () -> ship.setActionType(UnitActionType.plowField));
        for (final Object[] rejected : new Object[][] { { forest, inForest },
                { plowed, onField }, { model, inColony } }) {
            final Model owner = (Model) rejected[0];
            final String before = gson.toJson(owner.save());
            assertThrows(IllegalStateException.class, () -> ((Unit) rejected[1])
                    .setActionType(UnitActionType.plowField));
            assertEquals(before, gson.toJson(owner.save()));
        }
    }

    @Test
    void noMovesBusyAndDestroyedUnitsAreRejected() {
        final Model model = fixture();
        final UnitFreeColonist idle = pioneer(model, 100, TARGET);
        idle.setActionPoints(0);
        assertEquals(PlowProblem.NO_MOVES,
                model.getPlowProblem(idle).orElseThrow());
        idle.setActionPoints(idle.getSpeed());
        idle.setActionType(UnitActionType.plowField);
        assertEquals(PlowProblem.BUSY,
                model.getPlowProblem(idle).orElseThrow());
        final UnitFreeColonist gone = pioneer(model, 100, TARGET);
        model.destroyUnit(gone);
        assertEquals(PlowProblem.UNAVAILABLE,
                model.getPlowProblem(gone).orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> gone.setActionType(UnitActionType.plowField));
    }

    @Test
    void cancelDiscardsProgressWithoutRefund() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        assertFalse(model.canCancelPlow(pioneer));
        assertThrows(IllegalStateException.class,
                () -> model.cancelPlow(pioneer));
        pioneer.setActionType(UnitActionType.plowField);
        nextOwnerTurn(model);
        assertTrue(model.canCancelPlow(pioneer));
        model.cancelPlow(pioneer);
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(100, pioneer.getTools());
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
        assertFalse(model.canCancelPlow(pioneer));
        for (int turn = 0; turn < 5; turn++) {
            nextOwnerTurn(model);
        }
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
        assertEquals(100, pioneer.getTools());
    }

    @Test
    void cancelDoesNotRefundSpentMovement() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        model.cancelPlow(pioneer);
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(100, pioneer.getTools());
    }

    @Test
    void competingCompletedFieldEndsStaleWorkWithoutCharge() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        model.getMap().plowFiled(TARGET);
        nextOwnerTurn(model);
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(100, pioneer.getTools());
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        // The ended update spends the movement, so that is the first reason.
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(PlowProblem.NO_MOVES,
                model.getPlowProblem(pioneer).orElseThrow());
        // The unit is free again next update and the turn is not blocked.
        nextOwnerTurn(model);
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
        assertEquals(PlowProblem.ALREADY_PLOWED,
                model.getPlowProblem(pioneer).orElseThrow());
    }

    @Test
    void retainedCallbackAfterCancelDoesNotTouchTheUnit() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField old = (UnitActionPlowField) pioneer
                .getAction();
        model.cancelPlow(pioneer);
        final String before = gson.toJson(model.save());
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(before, gson.toJson(model.save()));
        assertEquals(100, pioneer.getTools());
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
    }

    @Test
    void retainedCallbackDoesNotSpendMovementOrChargeAReplacementOrder() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField old = (UnitActionPlowField) pioneer
                .getAction();
        model.cancelPlow(pioneer);
        nextOwnerTurn(model);
        pioneer.setActionType(UnitActionType.plowField);
        assertNotSame(old, pioneer.getAction());
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(4, remaining(pioneer));
        assertSame(UnitActionType.plowField, pioneer.getAction().getType());
        assertEquals(100, pioneer.getTools());
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
    }

    @Test
    void retainedCallbackAfterCompletionAndLaterUpdateKeepsNewMovement() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField old = (UnitActionPlowField) pioneer
                .getAction();
        for (int turn = 0; turn < 5; turn++) {
            nextOwnerTurn(model);
        }
        assertEquals(80, pioneer.getTools());
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
        assertEquals(80, pioneer.getTools());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
    }

    @Test
    void foreignModelOrWrongUnitCallbackLeavesTheOriginalOrderRunning() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final UnitFreeColonist other = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final UnitActionPlowField action = (UnitActionPlowField) pioneer
                .getAction();
        final Model foreign = fixture();
        assertTrue(action.startTurn(foreign, pioneer));
        assertTrue(action.startTurn(model, other));
        assertEquals(4, remaining(pioneer));
        assertSame(action, pioneer.getAction());
        assertEquals(UnitActionType.noAction, other.getAction().getType());
        assertEquals(other.getSpeed(), other.getActionPoints());
        for (int turn = 0; turn < 3; turn++) {
            nextOwnerTurn(model);
        }
        assertEquals(1, remaining(pioneer));
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
        nextOwnerTurn(model);
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        assertEquals(80, pioneer.getTools());
        assertEquals(100, other.getTools());
    }

    @Test
    void destroyedAndForeignModelCallbacksHaveNoEffect() {
        final Model model = fixture();
        final UnitFreeColonist doomed = pioneer(model, 100, TARGET);
        doomed.setActionType(UnitActionType.plowField);
        final UnitActionPlowField action = (UnitActionPlowField) doomed
                .getAction();
        final Model foreign = fixture();
        final String foreignBefore = gson.toJson(foreign.save());
        assertTrue(action.startTurn(foreign, doomed));
        assertEquals(foreignBefore, gson.toJson(foreign.save()));
        assertEquals(4, remaining(doomed));

        final UnitFreeColonist destroyed = pioneer(model, 100, TARGET);
        destroyed.setActionType(UnitActionType.plowField);
        final UnitActionPlowField last = (UnitActionPlowField) destroyed
                .getAction();
        model.destroyUnit(destroyed);
        assertTrue(last.startTurn(model, destroyed));
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
        assertEquals(100, destroyed.getTools());
    }

    @Test
    void lostToolsEndWorkWithoutFieldOrCharge() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        pioneer.setTools(5);
        for (int turn = 0; turn < 4; turn++) {
            nextOwnerTurn(model);
        }
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(5, pioneer.getTools());
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
    }

    @Test
    void countdownSurvivesDiskRoundtripWithoutAdvancingOnLoad() {
        final GameModelPo data = data(false, false);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final int id = pioneer.getId();
        pioneer.setActionType(UnitActionType.plowField);
        nextOwnerTurn(model);
        nextOwnerTurn(model);
        assertEquals(2, remaining(pioneer));
        data.setModel(model.save());
        final Path file = directory.resolve("plow.microcol");
        dao.saveToFile(file.toString(), data);
        final GameModelPo loaded = dao.loadFromFile(file.toFile());
        model = make(loaded);
        final String saved = gson.toJson(model.save());
        assertEquals(2, remaining(model.getUnitById(id)));
        assertEquals(100,
                ((UnitFreeColonist) model.getUnitById(id)).getTools());
        assertEquals(saved, gson.toJson(model.save()));
        nextOwnerTurn(model);
        nextOwnerTurn(model);
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasField());
        assertEquals(80, ((UnitFreeColonist) model.getUnitById(id)).getTools());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 5 })
    void invalidSavedCountdownIsRejected(final int countdown) {
        final UnitActionPlowFieldPo po = new UnitActionPlowFieldPo();
        po.setRemainingTurns(countdown);
        assertThrows(IllegalArgumentException.class,
                () -> UnitActionConverter.convert(po));
        assertThrows(IllegalArgumentException.class,
                () -> new UnitActionPlowField().setRemainingTurns(countdown));
    }

    @Test
    void plowedFieldImprovesTheFollowingColonyUpdate() {
        final Model model = fixture();
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        ((UnitActionPlowField) pioneer.getAction()).setRemainingTurns(1);
        final Terrain before = model.getMap().getTerrainAt(TARGET);
        final int grainBefore = before.canProduceAmmount(GoodsType.GRAIN);
        nextOwnerTurn(model);
        final Terrain after = model.getMap().getTerrainAt(TARGET);
        assertEquals(grainBefore + 1, after.canProduceAmmount(GoodsType.GRAIN));
        assertEquals(before.canProduceAmmount(GoodsType.LUMBER),
                after.canProduceAmmount(GoodsType.LUMBER));
    }

    @Test
    void riverPlowingIsRejectedBeforeSpendingToolsOrMovement() {
        final GameModelPo data = data(false, false);
        data.getModel().getMap()
                .setRivers(java.util.Map.of(TARGET, RiverSize.SMALL));
        final Model model = make(data);
        final UnitFreeColonist unit = pioneer(model, 100, TARGET);
        final String before = gson.toJson(model.save());
        assertEquals(PlowProblem.RIVER,
                model.getPlowProblem(unit).orElseThrow());
        assertThrows(IllegalArgumentException.class,
                () -> model.getMap().plowFiled(TARGET));
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void pendingPlowOnARiverEndsWithoutChargingTools() {
        final Model original = fixture();
        final UnitFreeColonist pioneer = pioneer(original, 100, TARGET);
        pioneer.setActionType(UnitActionType.plowField);
        final org.microcol.model.store.ModelPo saved = original.save();
        saved.getMap().setRivers(java.util.Map.of(TARGET, RiverSize.SMALL));
        final Model model = Model.make(saved,
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        nextOwnerTurn(model);
        final UnitFreeColonist restored = (UnitFreeColonist) model
                .getUnitById(pioneer.getId());
        assertEquals(UnitActionType.noAction, restored.getAction().getType());
        assertEquals(100, restored.getTools());
        assertFalse(model.getMap().getTerrainAt(TARGET).isHasField());
    }
}
