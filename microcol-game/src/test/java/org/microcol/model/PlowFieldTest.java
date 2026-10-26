package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitActionPlowFieldPo;
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
        final int cornBefore = before.canProduceAmmount(GoodsType.CORN);
        nextOwnerTurn(model);
        final Terrain after = model.getMap().getTerrainAt(TARGET);
        assertEquals(cornBefore + 1, after.canProduceAmmount(GoodsType.CORN));
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
