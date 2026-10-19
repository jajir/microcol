package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.store.UnitActionClearForestPo;
import org.microcol.model.turnevent.TurnEventForestLumberLost;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.unit.UnitActionClearForest;
import org.microcol.model.unit.UnitActionConverter;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Forest clearing: confirmed start, timing, the single completion transaction,
 * the recipient resolved at completion, warehouse limits, persistence and the
 * permanently changed terrain output.
 */
class ClearForestTest {

    /** One square from New Amsterdam (id 0), two from Delft (id 1). */
    private static final Location TARGET = Location.of(24, 14);

    /** Two squares from both colonies, so only the colony ID separates them. */
    private static final Location TIE = Location.of(24, 12);

    /** Three squares from New Amsterdam and six from Delft. */
    private static final Location FAR = Location.of(28, 14);

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private final Gson gson = new Gson();

    private int saves;

    private GameModelPo data(final TerrainType terrain,
            final Location... forests) {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        final Set<Location> fields = data.getModel().getMap().getFieldSet();
        for (final Location at : List.of(TARGET, TIE, FAR)) {
            types.put(at, terrain);
            trees.remove(at);
            fields.remove(at);
        }
        trees.addAll(List.of(forests));
        data.getModel().getMap().setTerrainType(types);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getMap().setFields(fields);
        return data;
    }

    private GameModelPo data(final Location... forests) {
        return data(TerrainType.GRASSLAND, forests);
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Model fixture(final Location... forests) {
        return make(data(forests));
    }

    /** Save through the real file format and load a fresh model. */
    private Model reload(final GameModelPo data, final Model model,
            final Consumer<ModelPo> edit) {
        data.setModel(model.save());
        edit.accept(data.getModel());
        final Path file = directory.resolve("clear-" + saves++ + ".microcol");
        dao.saveToFile(file.toString(), data);
        return make(dao.loadFromFile(file.toFile()));
    }

    private Model reload(final GameModelPo data, final Model model) {
        return reload(data, model, saved -> {
        });
    }

    private static ColonyPo colonyPo(final ModelPo saved, final int id) {
        return saved.getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private static void setWarehouse(final ModelPo saved, final int colonyId,
            final ConstructionType warehouse) {
        colonyPo(saved, colonyId).getConstructions().stream().filter((
                final ConstructionPo construction) -> ConstructionType.WAREHOUSES
                        .contains(construction.getType()))
                .findFirst().orElseThrow().setType(warehouse);
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

    private ClearForestOffer start(final Model model,
            final UnitFreeColonist pioneer) {
        final ClearForestOffer offer = model.getClearForestOffer(pioneer)
                .orElseThrow();
        model.clearForest(pioneer, offer);
        return offer;
    }

    private void nextOwnerTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private UnitActionClearForest action(final Unit unit) {
        return (UnitActionClearForest) unit.getAction();
    }

    /** Run the last owner update of this order without any other update. */
    private void completeNow(final Model model,
            final UnitFreeColonist pioneer) {
        final UnitActionClearForest action = action(pioneer);
        action.setRemainingTurns(1);
        assertTrue(action.startTurn(model, pioneer));
    }

    private int lumber(final Model model, final int colonyId) {
        return model.getColonyById(colonyId).orElseThrow().getWarehouse()
                .getGoods(GoodsType.LUMBER).getAmount();
    }

    private List<TurnEventForestLumberLost> notices(final Model model) {
        return model.getTurnEventStore()
                .getForPlayer(model.getPlayerByName("Dutch")).stream()
                .filter(TurnEventForestLumberLost.class::isInstance)
                .map(TurnEventForestLumberLost.class::cast)
                .collect(Collectors.toList());
    }

    private boolean forest(final Model model, final Location at) {
        return model.getMap().getTerrainAt(at).isHasTrees();
    }

    // ---- Admission, tool boundaries and the confirmed start ---------------

    @ParameterizedTest
    @ValueSource(ints = { 0, 19 })
    void tooFewToolsRejectWithoutMutation(final int tools) {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, tools, TARGET);
        final String before = gson.toJson(model.save());
        assertEquals(ClearForestProblem.NOT_ENOUGH_TOOLS,
                model.getClearForestProblem(pioneer).orElseThrow());
        assertFalse(model.canClearForest(pioneer));
        assertTrue(model.getClearForestOffer(pioneer).isEmpty());
        final ClearForestOffer forged = new ClearForestOffer(pioneer.getId(),
                TARGET, 4, 20, 20);
        assertThrows(IllegalStateException.class,
                () -> model.clearForest(pioneer, forged));
        assertEquals(before, gson.toJson(model.save()));
    }

    @ParameterizedTest
    @ValueSource(ints = { 20, 40, 100 })
    void enoughToolsStartOnlyMovementAndChargeNothingYet(final int tools) {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, tools, TARGET);
        final int lumberBefore = lumber(model, 0);
        assertTrue(model.getClearForestProblem(pioneer).isEmpty());
        final ClearForestOffer offer = model.getClearForestOffer(pioneer)
                .orElseThrow();
        assertEquals(pioneer.getId(), offer.getUnitId());
        assertEquals(TARGET, offer.getLocation());
        assertEquals(4, offer.getUpdates());
        assertEquals(20, offer.getTools());
        assertEquals(20, offer.getLumber());
        model.clearForest(pioneer, offer);
        assertEquals(UnitActionType.clearForest, pioneer.getAction().getType());
        assertEquals(4, action(pioneer).getRemainingTurns());
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(tools, pioneer.getTools());
        assertTrue(forest(model, TARGET));
        assertEquals(lumberBefore, lumber(model, 0));
        assertTrue(model.canCancelClearForest(pioneer));
    }

    @Test
    void hardyOfferPromisesTwoUpdates() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist hardy = hardy(model, 100, TARGET);
        assertEquals(2,
                model.getClearForestOffer(hardy).orElseThrow().getUpdates());
    }

    @Test
    void startNeedsTheExactOfferThePlayerConfirmed() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final UnitFreeColonist other = pioneer(model, 100, TARGET);
        final String before = gson.toJson(model.save());
        assertThrows(NullPointerException.class,
                () -> model.clearForest(pioneer, null));
        for (final ClearForestOffer wrong : List.of(
                new ClearForestOffer(pioneer.getId(), TARGET, 4, 20, 21),
                new ClearForestOffer(pioneer.getId(), TARGET, 4, 19, 20),
                new ClearForestOffer(pioneer.getId(), TARGET, 2, 20, 20),
                new ClearForestOffer(pioneer.getId(), TIE, 4, 20, 20),
                new ClearForestOffer(other.getId(), TARGET, 4, 20, 20))) {
            assertThrows(IllegalStateException.class,
                    () -> model.clearForest(pioneer, wrong));
        }
        // The generic order command cannot bypass the confirmation.
        assertThrows(IllegalStateException.class,
                () -> pioneer.setActionType(UnitActionType.clearForest));
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void offerBecomesStaleWhenToolsForestOrOwnerChange() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final ClearForestOffer offer = model.getClearForestOffer(pioneer)
                .orElseThrow();
        pioneer.setTools(19);
        String before = gson.toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> model.clearForest(pioneer, offer));
        assertEquals(before, gson.toJson(model.save()));
        pioneer.setTools(100);
        model.getMap().clearForest(TARGET);
        before = gson.toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> model.clearForest(pioneer, offer));
        assertEquals(before, gson.toJson(model.save()));
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
    }

    @Test
    void invalidSquaresAndUnitsRejectWithoutMutation() {
        final Model plain = fixture();
        final UnitFreeColonist onBare = pioneer(plain, 100, TARGET);
        assertEquals(ClearForestProblem.NO_FOREST,
                plain.getClearForestProblem(onBare).orElseThrow());
        final Model hill = make(data(TerrainType.HILL));
        final UnitFreeColonist onHill = pioneer(hill, 100, TARGET);
        assertEquals(ClearForestProblem.NO_FOREST,
                hill.getClearForestProblem(onHill).orElseThrow());
        final Model colony = fixture(TARGET);
        final UnitFreeColonist inColony = pioneer(colony, 100,
                colony.getColonyById(0).orElseThrow().getLocation());
        assertEquals(ClearForestProblem.COLONY,
                colony.getClearForestProblem(inColony).orElseThrow());
        // The galleon is not on the map, so it is not an available unit.
        final Unit ship = colony.getUnitById(3);
        assertEquals(ClearForestProblem.UNAVAILABLE,
                colony.getClearForestProblem(ship).orElseThrow());
        for (final Object[] rejected : new Object[][] { { plain, onBare },
                { hill, onHill }, { colony, inColony } }) {
            final Model owner = (Model) rejected[0];
            final String before = gson.toJson(owner.save());
            final Unit unit = (Unit) rejected[1];
            assertTrue(owner.getClearForestOffer(unit).isEmpty());
            assertThrows(IllegalStateException.class,
                    () -> owner.clearForest(unit, new ClearForestOffer(
                            unit.getId(), unit.getLocation(), 4, 20, 20)));
            assertEquals(before, gson.toJson(owner.save()));
        }
    }

    @Test
    void noMovesBusyDestroyedAndForeignUnitsAreRejected() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist idle = pioneer(model, 100, TARGET);
        idle.setActionPoints(0);
        assertEquals(ClearForestProblem.NO_MOVES,
                model.getClearForestProblem(idle).orElseThrow());
        idle.setActionPoints(idle.getSpeed());
        start(model, idle);
        assertEquals(ClearForestProblem.BUSY,
                model.getClearForestProblem(idle).orElseThrow());
        final UnitFreeColonist gone = pioneer(model, 100, TARGET);
        model.destroyUnit(gone);
        assertEquals(ClearForestProblem.UNAVAILABLE,
                model.getClearForestProblem(gone).orElseThrow());
        assertThrows(IllegalStateException.class, () -> model.clearForest(gone,
                new ClearForestOffer(gone.getId(), TARGET, 4, 20, 20)));
        // Another nation's unit is not the current player's pioneer.
        final Player king = model.getPlayerByName("Dutch's King");
        final UnitFreeColonist foreign = pioneer(model, 100, TARGET);
        foreign.takeOver(king);
        assertEquals(ClearForestProblem.UNAVAILABLE,
                model.getClearForestProblem(foreign).orElseThrow());
    }

    // ---- Timing -----------------------------------------------------------

    @Test
    void ordinaryWorkCompletesAfterFourOwnerUpdates() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        for (int expected = 3; expected >= 1; expected--) {
            nextOwnerTurn(model);
            assertEquals(expected, action(pioneer).getRemainingTurns());
            assertEquals(100, pioneer.getTools());
            assertTrue(forest(model, TARGET));
        }
        nextOwnerTurn(model);
        assertFalse(forest(model, TARGET));
        assertTrue(model.getMap().getTerrainAt(TARGET).isCleared());
        assertEquals(80, pioneer.getTools());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        nextOwnerTurn(model);
        assertEquals(80, pioneer.getTools());
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
    }

    @Test
    void hardyWorkCompletesAfterTwoOwnerUpdates() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist hardy = hardy(model, 100, TARGET);
        start(model, hardy);
        assertEquals(2, action(hardy).getRemainingTurns());
        nextOwnerTurn(model);
        assertEquals(1, action(hardy).getRemainingTurns());
        assertTrue(forest(model, TARGET));
        assertEquals(100, hardy.getTools());
        nextOwnerTurn(model);
        assertFalse(forest(model, TARGET));
        assertEquals(80, hardy.getTools());
        assertEquals(UnitActionType.noAction, hardy.getAction().getType());
    }

    // ---- Single transaction, cancellation and stale work ------------------

    @Test
    void completionRemovesForestChargesOnceAndRewardsOnce() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final UnitActionClearForest order = action(pioneer);
        final int before0 = lumber(model, 0);
        completeNow(model, pioneer);
        assertFalse(forest(model, TARGET));
        assertEquals(80, pioneer.getTools());
        assertEquals(before0 + 20, lumber(model, 0));
        assertTrue(notices(model).isEmpty());
        // A repeated completion callback charges and rewards nothing.
        final String saved = gson.toJson(model.save());
        assertTrue(order.startTurn(model, pioneer));
        assertEquals(saved, gson.toJson(model.save()));
        assertEquals(80, pioneer.getTools());
        assertEquals(before0 + 20, lumber(model, 0));
    }

    @Test
    void competingPioneersChargeAndRewardOnlyOnce() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist first = pioneer(model, 100, TARGET);
        final UnitFreeColonist second = pioneer(model, 100, TARGET);
        start(model, first);
        start(model, second);
        final int before0 = lumber(model, 0);
        completeNow(model, first);
        completeNow(model, second);
        assertEquals(80, first.getTools());
        assertEquals(100, second.getTools());
        assertEquals(before0 + 20, lumber(model, 0));
        assertFalse(forest(model, TARGET));
    }

    @Test
    void explicitCancelDiscardsProgressWithoutRefundOrTerrainChange() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        assertFalse(model.canCancelClearForest(pioneer));
        assertThrows(IllegalStateException.class,
                () -> model.cancelClearForest(pioneer));
        start(model, pioneer);
        nextOwnerTurn(model);
        assertEquals(3, action(pioneer).getRemainingTurns());
        model.cancelClearForest(pioneer);
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(0, pioneer.getActionPoints());
        assertEquals(100, pioneer.getTools());
        assertTrue(forest(model, TARGET));
        assertFalse(model.canCancelClearForest(pioneer));
        for (int update = 0; update < 5; update++) {
            nextOwnerTurn(model);
        }
        assertTrue(forest(model, TARGET));
        assertFalse(model.getMap().getTerrainAt(TARGET).isCleared());
        assertEquals(100, pioneer.getTools());
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void staleOrLostToolWorkEndsWithoutChargeTerrainOrNotice() {
        final Model model = fixture(TARGET, TIE);
        final UnitFreeColonist competing = pioneer(model, 100, TARGET);
        start(model, competing);
        final UnitFreeColonist poor = pioneer(model, 100, TIE);
        start(model, poor);
        poor.setTools(5);
        // Someone else's completed clearing makes the first order stale.
        model.getMap().clearForest(TARGET);
        nextOwnerTurn(model);
        for (final UnitFreeColonist ended : List.of(competing, poor)) {
            assertEquals(UnitActionType.noAction, ended.getAction().getType());
        }
        assertEquals(100, competing.getTools());
        assertEquals(5, poor.getTools());
        assertTrue(forest(model, TIE));
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void retainedForeignAndCanceledCallbacksHaveNoEffect() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final UnitFreeColonist other = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final UnitActionClearForest old = action(pioneer);
        final Model foreign = fixture(TARGET);
        assertTrue(old.startTurn(foreign, pioneer));
        assertTrue(old.startTurn(model, other));
        assertEquals(4, old.getRemainingTurns());
        assertSame(old, pioneer.getAction());
        model.cancelClearForest(pioneer);
        final String before = gson.toJson(model.save());
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(before, gson.toJson(model.save()));
        // A replacement order is not advanced by the retained callback.
        nextOwnerTurn(model);
        start(model, pioneer);
        assertTrue(old.startTurn(model, pioneer));
        assertEquals(4, action(pioneer).getRemainingTurns());
        assertNotSame(old, pioneer.getAction());
        assertEquals(100, pioneer.getTools());
        assertTrue(forest(model, TARGET));
    }

    // ---- Recipient resolved at completion ---------------------------------

    @Test
    void eightNeighborDistanceIncludesTheDiagonalBoundary() {
        final Location colony = Location.of(22, 12);
        assertEquals(0, TARGET.getDistanceChebyshev(TARGET));
        assertEquals(2, TARGET.getDistanceChebyshev(colony));
        assertEquals(3, TARGET.getDistanceChebyshev(Location.of(27, 14)));
        // The rounded Euclidean distance would wrongly exclude this square.
        assertEquals(3, TARGET.getDistance(colony));
    }

    @Test
    void previewNamesTheNearestOwnedColonyWithinTwoSquares() {
        final Model model = fixture(TARGET, TIE, FAR);
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(0, model.getForestClearingRecipient(dutch, TARGET)
                .orElseThrow().getId());
        assertEquals(0, model.getForestClearingRecipient(dutch, TIE)
                .orElseThrow().getId());
        assertTrue(model.getForestClearingRecipient(dutch, FAR).isEmpty());
        model.destroyColony(model.getColonyById(0).orElseThrow());
        // Delft lies exactly two squares away on the diagonal.
        assertEquals(1, model.getForestClearingRecipient(dutch, TARGET)
                .orElseThrow().getId());
        assertEquals(1, model.getForestClearingRecipient(dutch, TIE)
                .orElseThrow().getId());
        assertTrue(model.getForestClearingRecipient(dutch, FAR).isEmpty());
    }

    @Test
    void equalDistanceChoosesTheLowestColonyId() {
        final Model model = fixture(TIE);
        final UnitFreeColonist pioneer = pioneer(model, 100, TIE);
        start(model, pioneer);
        final int before0 = lumber(model, 0);
        final int before1 = lumber(model, 1);
        completeNow(model, pioneer);
        assertEquals(before0 + 20, lumber(model, 0));
        assertEquals(before1, lumber(model, 1));
    }

    @Test
    void destroyedPreviewColonyIsReplacedByTheNextNearestAtCompletion() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final Colony previewed = model
                .getForestClearingRecipient(pioneer.getOwner(), TARGET)
                .orElseThrow();
        assertEquals(0, previewed.getId());
        final int before1 = lumber(model, 1);
        model.destroyColony(previewed);
        completeNow(model, pioneer);
        assertEquals(before1 + 20, lumber(model, 1));
        assertEquals(80, pioneer.getTools());
        assertFalse(forest(model, TARGET));
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void capturedPreviewColonyIsNoLongerOwnedAndIsSkipped() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final Colony previewed = model.getColonyById(0).orElseThrow();
        final Player king = model.getPlayerByName("Dutch's King");
        final Unit force = model.addUnitOutSideColony(previewed);
        force.takeOver(king);
        ColonyBattles.capture(model, previewed, force);
        assertSame(king, previewed.getOwner());
        final int capturedBefore = lumber(model, 0);
        final int before1 = lumber(model, 1);
        completeNow(model, pioneer);
        assertEquals(capturedBefore, lumber(model, 0));
        assertEquals(before1 + 20, lumber(model, 1));
    }

    @Test
    void noOwnedColonyInReachDiscardsAllTwentyAndRecordsTheLoss() {
        final Model model = fixture(FAR);
        final UnitFreeColonist pioneer = pioneer(model, 100, FAR);
        start(model, pioneer);
        final int before0 = lumber(model, 0);
        final int before1 = lumber(model, 1);
        completeNow(model, pioneer);
        assertFalse(forest(model, FAR));
        assertEquals(80, pioneer.getTools());
        assertEquals(before0, lumber(model, 0));
        assertEquals(before1, lumber(model, 1));
        final List<TurnEventForestLumberLost> notices = notices(model);
        assertEquals(1, notices.size());
        assertEquals(Goods.of(GoodsType.LUMBER, 20), notices.get(0).getGoods());
        assertEquals(FAR, notices.get(0).getLocation());
        assertTrue(notices.get(0).getColonyId().isEmpty());
        assertTrue(notices.get(0).getColonyName().isEmpty());
    }

    @Test
    void colonyOfAnotherNationNeverReceivesTheLumber() {
        final Model model = fixture(TARGET);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player king = model.getPlayerByName("Dutch's King");
        assertTrue(model.getForestClearingRecipient(king, TARGET).isEmpty());
        assertEquals(0, model.getForestClearingRecipient(dutch, TARGET)
                .orElseThrow().getId());
    }

    // ---- Warehouse capacity ------------------------------------------------

    @ParameterizedTest
    @CsvSource({ "0,20,0", "80,20,0", "81,19,1", "95,5,15", "99,1,19",
            "100,0,20" })
    void currentCapacityStoresWhatFitsAndDiscardsTheRest(final int stock,
            final int stored, final int lost) {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final Colony colony = model.getColonyById(0).orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.LUMBER, stock));
        completeNow(model, pioneer);
        assertEquals(stock + stored, lumber(model, 0));
        final List<TurnEventForestLumberLost> notices = notices(model);
        if (lost == 0) {
            assertTrue(notices.isEmpty());
            return;
        }
        assertEquals(1, notices.size());
        assertEquals(Goods.of(GoodsType.LUMBER, lost),
                notices.get(0).getGoods());
        assertEquals(TARGET, notices.get(0).getLocation());
        assertEquals(0, notices.get(0).getColonyId().orElseThrow());
        assertEquals(colony.getName(),
                notices.get(0).getColonyName().orElseThrow());
    }

    @Test
    void capacityGrowthBeforeCompletionFitsMoreButNeverRestoresLaterLoss() {
        final GameModelPo data = data(TARGET);
        Model model = make(data);
        UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        model.getColonyById(0).orElseThrow().getWarehouse()
                .setGoods(Goods.of(GoodsType.LUMBER, 95));
        start(model, pioneer);
        final int id = pioneer.getId();
        // Today only five would fit; the larger warehouse takes all twenty.
        model = reload(data, model, saved -> setWarehouse(saved, 0,
                ConstructionType.WAREHOUSE_EXPANSION));
        pioneer = (UnitFreeColonist) model.getUnitById(id);
        completeNow(model, pioneer);
        assertEquals(115, lumber(model, 0));
        assertTrue(notices(model).isEmpty());
    }

    @Test
    void capacityShrinkBeforeCompletionLosesTheRestForGood() {
        final GameModelPo data = data(TARGET);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getColonyWarehouse().put("LUMBER", 90);
        setWarehouse(data.getModel(), 0, ConstructionType.WAREHOUSE_EXPANSION);
        Model model = make(data);
        UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        final int id = pioneer.getId();
        // The smaller warehouse holds 100 of 300 before the work finishes.
        model = reload(data, model, saved -> setWarehouse(saved, 0,
                ConstructionType.WAREHOUSE_BASIC));
        pioneer = (UnitFreeColonist) model.getUnitById(id);
        completeNow(model, pioneer);
        assertEquals(100, lumber(model, 0));
        assertEquals(1, notices(model).size());
        assertEquals(10, notices(model).get(0).getGoods().getAmount());
        // The finished order is released as in a real owner update.
        pioneer.setActionType(UnitActionType.noAction);
        // A later expansion does not bring the discarded lumber back.
        model = reload(data, model, saved -> setWarehouse(saved, 0,
                ConstructionType.WAREHOUSE_EXPANSION));
        assertEquals(100, lumber(model, 0));
        assertEquals(1, notices(model).size());
    }

    @Test
    void overfullWarehouseTakesNothingAndDiscardsAllTwenty() {
        final GameModelPo data = data(TARGET);
        data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst().orElseThrow()
                .getColonyWarehouse().put("LUMBER", 100);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        completeNow(model, pioneer);
        assertEquals(100, lumber(model, 0));
        assertEquals(20, notices(model).get(0).getGoods().getAmount());
    }

    // ---- Persistence --------------------------------------------------------

    @Test
    void countdownAndClearedStateSurviveDiskRoundtripWithoutAdvancing() {
        final GameModelPo data = data(TARGET);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        final int id = pioneer.getId();
        start(model, pioneer);
        nextOwnerTurn(model);
        nextOwnerTurn(model);
        assertEquals(2, action(pioneer).getRemainingTurns());
        model = reload(data, model);
        final String saved = gson.toJson(model.save());
        assertEquals(2, action(model.getUnitById(id)).getRemainingTurns());
        assertEquals(100,
                ((UnitFreeColonist) model.getUnitById(id)).getTools());
        assertTrue(forest(model, TARGET));
        assertEquals(saved, gson.toJson(model.save()));
        nextOwnerTurn(model);
        nextOwnerTurn(model);
        assertFalse(forest(model, TARGET));
        assertEquals(80, ((UnitFreeColonist) model.getUnitById(id)).getTools());
        model = reload(data, model);
        assertTrue(model.getMap().getTerrainAt(TARGET).isCleared());
        assertFalse(forest(model, TARGET));
        assertEquals(80, ((UnitFreeColonist) model.getUnitById(id)).getTools());
    }

    @Test
    void lossNoticeIsReportedOnceAndSurvivesContinueWithoutRepeatedDisposal() {
        final GameModelPo data = data(FAR);
        Model model = make(data);
        final UnitFreeColonist pioneer = pioneer(model, 100, FAR);
        final int id = pioneer.getId();
        start(model, pioneer);
        for (int update = 0; update < 3; update++) {
            nextOwnerTurn(model);
            assertTrue(notices(model).isEmpty());
        }
        // Unfinished work saved and continued: still no notice, no disposal.
        model = reload(data, model);
        assertTrue(notices(model).isEmpty());
        assertEquals(1, action(model.getUnitById(id)).getRemainingTurns());
        nextOwnerTurn(model);
        assertEquals(1, notices(model).size());
        assertEquals(20, notices(model).get(0).getGoods().getAmount());
        final int stock0 = lumber(model, 0);
        final int stock1 = lumber(model, 1);
        // Continue shows the same single notice and disposes of nothing more.
        model = reload(data, model);
        assertEquals(1, notices(model).size());
        assertEquals(FAR, notices(model).get(0).getLocation());
        assertEquals(20, notices(model).get(0).getGoods().getAmount());
        assertEquals(stock0, lumber(model, 0));
        assertEquals(stock1, lumber(model, 1));
        assertEquals(80, ((UnitFreeColonist) model.getUnitById(id)).getTools());
        // The notice belongs to this owner turn only.
        model.endTurn();
        assertTrue(notices(model).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0, 5 })
    void invalidSavedCountdownIsRejected(final int countdown) {
        final UnitActionClearForestPo po = new UnitActionClearForestPo();
        po.setRemainingTurns(countdown);
        assertThrows(IllegalArgumentException.class,
                () -> UnitActionConverter.convert(po));
        assertThrows(IllegalArgumentException.class,
                () -> new UnitActionClearForest().setRemainingTurns(countdown));
    }

    @Test
    void lossNoticeRoundtripsAndRejectsInvalidSavedData() {
        final Player dutch = fixture().getPlayerByName("Dutch");
        final Colony colony = fixture().getColonyById(1).orElseThrow();
        final Goods lost = Goods.of(GoodsType.LUMBER, 7);
        final TurnEventForestLumberLost withColony = (TurnEventForestLumberLost) TurnEventProvider
                .getForestLumberLost(dutch, lost, TARGET, colony);
        final TurnEventForestLumberLost loadedWith = TurnEventForestLumberLost
                .tryLoad(withColony.save());
        assertEquals(lost, loadedWith.getGoods());
        assertEquals(TARGET, loadedWith.getLocation());
        assertEquals(1, loadedWith.getColonyId().orElseThrow());
        assertEquals(colony.getName(),
                loadedWith.getColonyName().orElseThrow());
        final TurnEventForestLumberLost withoutColony = (TurnEventForestLumberLost) TurnEventProvider
                .getForestLumberLost(dutch, lost, FAR, null);
        final TurnEventForestLumberLost loadedWithout = TurnEventForestLumberLost
                .tryLoad(withoutColony.save());
        assertTrue(loadedWithout.getColonyId().isEmpty());
        assertEquals(FAR, loadedWithout.getLocation());

        final TurnEventPo other = new TurnEventPo();
        other.setType("TurnEventGoodsWasThrownAway");
        assertNull(TurnEventForestLumberLost.tryLoad(other));
        final TurnEventPo noSquare = withColony.save();
        noSquare.setClearedX(null);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventForestLumberLost.tryLoad(noSquare));
        final TurnEventPo nothingLost = withColony.save();
        nothingLost.setGoods(Goods.of(GoodsType.LUMBER, 0));
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventForestLumberLost.tryLoad(nothingLost));
        final TurnEventPo wrongGoods = withColony.save();
        wrongGoods.setGoods(Goods.of(GoodsType.FUR, 3));
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventForestLumberLost.tryLoad(wrongGoods));
        final TurnEventPo halfColony = withColony.save();
        halfColony.setColonyName(null);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventForestLumberLost.tryLoad(halfColony));
    }

    // ---- Terrain output -----------------------------------------------------

    @Test
    void clearedSquareKeepsBareYieldsButNeverProducesLumberOrFurAgain() {
        final GameModelPo data = data(TARGET);
        Model model = make(data);
        final Terrain forest = model.getMap().getTerrainAt(TARGET);
        assertEquals(6, forest.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(2, forest.canProduceAmmount(GoodsType.FUR));
        assertEquals(3, forest.canProduceAmmount(GoodsType.CORN));
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        completeNow(model, pioneer);
        // The finished order is released as in a real owner update.
        pioneer.setActionType(UnitActionType.noAction);
        final Terrain cleared = model.getMap().getTerrainAt(TARGET);
        assertTrue(cleared.isCleared());
        assertFalse(cleared.isHasTrees());
        assertEquals(0, cleared.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, cleared.canProduceAmmount(GoodsType.FUR));
        // Existing bare agricultural yields of grassland.
        assertEquals(4, cleared.canProduceAmmount(GoodsType.CORN));
        assertEquals(1, cleared.canProduceAmmount(GoodsType.SUGAR));
        assertEquals(3, cleared.canProduceAmmount(GoodsType.TOBACCO));
        assertEquals(2, cleared.canProduceAmmount(GoodsType.COTTON));
        // Unrelated natural bare grassland keeps its aid-card output, which has
        // no lumber or fur.
        final Terrain natural = model.getMap().getTerrainAt(TIE);
        assertFalse(natural.isCleared());
        assertEquals(0, natural.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, natural.canProduceAmmount(GoodsType.FUR));
        assertEquals(4, natural.canProduceAmmount(GoodsType.CORN));
        // The output change is permanent, also across Continue and next update.
        model = reload(data, model);
        nextOwnerTurn(model);
        final Terrain later = model.getMap().getTerrainAt(TARGET);
        assertTrue(later.isCleared());
        assertEquals(0, later.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, later.canProduceAmmount(GoodsType.FUR));
        assertEquals(4, later.canProduceAmmount(GoodsType.CORN));
        assertTrue(later.getProduction().stream().noneMatch(
                production -> production.getGoodsType() == GoodsType.LUMBER
                        || production.getGoodsType() == GoodsType.FUR));
    }

    @Test
    void laterPlowingOfAClearedSquareKeepsTheCropBonusAndNoLumber() {
        final Model model = fixture(TARGET);
        final UnitFreeColonist pioneer = pioneer(model, 100, TARGET);
        start(model, pioneer);
        completeNow(model, pioneer);
        assertTrue(model.getMap().getTerrainAt(TARGET).isCleared());
        model.getMap().plowFiled(TARGET);
        final Terrain plowed = model.getMap().getTerrainAt(TARGET);
        assertEquals(5, plowed.canProduceAmmount(GoodsType.CORN));
        assertEquals(0, plowed.canProduceAmmount(GoodsType.LUMBER));
        assertEquals(0, plowed.canProduceAmmount(GoodsType.FUR));
    }

    @Test
    void onlyForestedSquaresCanBeClearedOrMarkedCleared() {
        final Model model = fixture();
        assertThrows(IllegalArgumentException.class,
                () -> model.getMap().clearForest(TARGET));
        final Terrain hill = new Terrain(TARGET, TerrainType.HILL);
        assertThrows(IllegalArgumentException.class,
                () -> hill.setCleared(true));
    }

    @Test
    void malformedClearedBlockIsRejectedAndMissingBlockMeansNothingCleared() {
        final GameModelPo missing = data();
        missing.getModel().getMap().setCleared((String[]) null);
        assertFalse(make(missing).getMap().getTerrainAt(TARGET).isCleared());

        final GameModelPo unknown = data();
        unknown.getModel().getMap().setCleared(Set.of(TARGET));
        unknown.getModel().getMap().getCleared()[TARGET.getY()] = unknown
                .getModel().getMap().getCleared()[TARGET.getY()]
                .replace("c", "x");
        assertThrows(IllegalArgumentException.class, () -> make(unknown));

        final GameModelPo shortRows = data();
        shortRows.getModel().getMap().setCleared(Set.of(TARGET));
        final String[] rows = shortRows.getModel().getMap().getCleared();
        shortRows.getModel().getMap()
                .setCleared(java.util.Arrays.copyOf(rows, rows.length - 1));
        assertThrows(IllegalArgumentException.class, () -> make(shortRows));

        final GameModelPo ocean = data();
        ocean.getModel().getMap().setCleared(Set.of(Location.of(1, 1)));
        assertThrows(IllegalArgumentException.class, () -> make(ocean));

        final GameModelPo overlap = data(TARGET);
        overlap.getModel().getMap().setCleared(Set.of(TARGET));
        assertThrows(IllegalArgumentException.class, () -> make(overlap));

        final GameModelPo valid = data();
        valid.getModel().getMap().setCleared(Set.of(TIE));
        assertTrue(make(valid).getMap().getTerrainAt(TIE).isCleared());
    }

    @Test
    void aStandingForestAndNaturalBareSquaresAreNotMarkedCleared() {
        final Model model = fixture(TARGET);
        assertFalse(model.getMap().getTerrainAt(TARGET).isCleared());
        assertFalse(model.getMap().getTerrainAt(TIE).isCleared());
        assertTrue(model.getMap().getTerrainAt(TARGET).isHasTrees());
    }
}
