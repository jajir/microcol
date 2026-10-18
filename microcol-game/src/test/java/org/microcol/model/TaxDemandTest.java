package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.TaxPo;

/**
 * The Crown's demand pauses one national update, is answered exactly once and
 * resumes only the remaining work.
 */
class TaxDemandTest {

    private static PlayerPo player(final ModelPo model, final String name) {
        return model.getGameManager().getPlayers().stream()
                .filter(candidate -> name.equals(candidate.getName()))
                .findFirst().orElseThrow();
    }

    /**
     * The thrive scenario with Dutch taxed at {@code rate}; the demand falls
     * due after {@code updatesToDemand} more Dutch updates.
     */
    private GameModelPo data(final Difficulty difficulty, final int rate,
            final int updatesToDemand) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().setDifficulty(difficulty);
        final PlayerPo dutch = player(data.getModel(), "Dutch");
        dutch.getRecruitment().setCrosses(0);
        final TaxPo tax = TaxState.initial();
        tax.setRate(rate);
        tax.setActivated(true);
        tax.setUpdates(TaxState.interval(difficulty) - updatesToDemand);
        dutch.setTax(tax);
        return data;
    }

    /** Only the named tradable stock remains; corn and bells are untouched. */
    private static void stock(final GameModelPo data, final int colonyId,
            final Map<GoodsType, Integer> goods) {
        final ColonyPo colony = data.getModel().getColonies().stream()
                .filter(candidate -> candidate.getId() == colonyId).findFirst()
                .orElseThrow();
        GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(type -> !type.equals(GoodsType.CORN))
                .forEach(type -> colony.getColonyWarehouse().put(type.name(),
                        0));
        goods.forEach((type, amount) -> colony.getColonyWarehouse()
                .put(type.name(), amount));
    }

    private static void expandWarehouse(final GameModelPo data,
            final int colonyId) {
        data.getModel().getColonies().stream()
                .filter(candidate -> candidate.getId() == colonyId)
                .flatMap(candidate -> candidate.getConstructions().stream())
                .filter(construction -> ConstructionType.WAREHOUSE_BASIC
                        .equals(construction.getType()))
                .forEach(construction -> construction
                        .setType(ConstructionType.WAREHOUSE));
    }

    /** Colony 0 and 1 tie on value and goods order; colony 0 must win. */
    private GameModelPo tieData() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR, 0, 1);
        stock(data, 0, Map.of(GoodsType.RUM, 60, GoodsType.SILK, 60));
        stock(data, 1, Map.of(GoodsType.RUM, 60));
        stock(data, 2, Map.of());
        return data;
    }

    private Model load(final ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void nextDutchTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private Model pending(final GameModelPo data) {
        final Model model = load(data.getModel());
        nextDutchTurn(model);
        assertTrue(model.isAwaitingTaxDecision());
        return model;
    }

    private static Map<String, Integer> stocks(final Model model) {
        final Map<String, Integer> out = new TreeMap<>();
        model.getColonies()
                .forEach(colony -> GoodsType.GOOD_TYPES.forEach(type -> out.put(
                        colony.getId() + ":" + type.name(),
                        colony.getWarehouse().getGoods(type).getAmount())));
        return out;
    }

    private static Map<Integer, Long> liberty(final Model model) {
        final Map<Integer, Long> out = new TreeMap<>();
        model.getColonies().forEach(
                colony -> out.put(colony.getId(), colony.getLiberty()));
        return out;
    }

    private static int peopleAtPier(final Player player) {
        return (int) player.getAllUnits().stream()
                .filter(unit -> unit.isAtEuropePier() && !unit.isShip())
                .count();
    }

    private static void accept(final Model model) {
        final Player dutch = model.getPlayerByName("Dutch");
        dutch.acceptTaxDemand(dutch.getTaxDemand().orElseThrow());
    }

    private static ColonyPo colonyPo(final GameModelPo data,
            final int colonyId) {
        return data.getModel().getColonies().stream()
                .filter(candidate -> candidate.getId() == colonyId).findFirst()
                .orElseThrow();
    }

    /**
     * The accepted control for a protest: the protested colony already holds
     * the protest liberty, so the political production of the resumed update is
     * the same as after a protest, and any remaining difference in stock is the
     * destroyed goods alone.
     */
    private Model acceptedWithLiberty(final Supplier<GameModelPo> fixture,
            final int colonyId, final long gain) {
        final GameModelPo data = fixture.get();
        final ColonyPo colony = colonyPo(data, colonyId);
        colony.setLiberty(colony.getLiberty() + gain);
        final Model control = pending(data);
        accept(control);
        return control;
    }

    @Test
    void dueUpdatePausesBeforeProductionAndSurvivesSaveBeforeOneAcceptance() {
        final Model model = load(tieData().getModel());
        final Player dutch = model.getPlayerByName("Dutch");
        final Map<String, Integer> stocksBefore = stocks(model);
        final int bellsBefore = dutch.getCongressBells();
        final int goldBefore = dutch.getGold();
        final int unitsBefore = model.getAllUnits().size();
        nextDutchTurn(model);
        assertTrue(model.isAwaitingTaxDecision());
        assertFalse(model.isGameRunning());
        assertFalse(model.isAwaitingImmigrationChoice());
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        assertEquals(0, demand.getCurrentRate());
        assertEquals(2, demand.getProposedRate());
        assertEquals(1, demand.getSerial());
        assertEquals(stocksBefore, stocks(model));
        assertEquals(bellsBefore, dutch.getCongressBells());
        assertEquals(unitsBefore, model.getAllUnits().size());
        assertThrows(IllegalStateException.class, model::endTurn);
        assertThrows(TurnPreparationException.class,
                () -> dutch.buyGoods(Goods.of(GoodsType.TOBACCO, 1)));
        assertEquals(goldBefore, dutch.getGold());

        final Model restored = load(model.save());
        restored.startGame();
        assertTrue(restored.isAwaitingTaxDecision());
        assertFalse(restored.isGameRunning());
        final Player owner = restored.getPlayerByName("Dutch");
        final TaxDemand reopened = owner.getTaxDemand().orElseThrow();
        assertEquals(2, reopened.getProposedRate());
        assertEquals(1, reopened.getSerial());
        assertEquals(stocksBefore, stocks(restored));
        assertEquals(bellsBefore, owner.getCongressBells());
        assertEquals(unitsBefore, restored.getAllUnits().size());

        final Model control = pending(tieData());
        accept(control);
        owner.acceptTaxDemand(reopened);
        assertTrue(restored.isGameRunning());
        assertFalse(restored.isAwaitingTaxDecision());
        assertEquals(2, owner.getTaxRate());
        assertEquals(stocks(control), stocks(restored));
        assertEquals(control.getPlayerByName("Dutch").getCongressBells(),
                owner.getCongressBells());
        assertEquals(control.getPlayerByName("Dutch").getGold(),
                owner.getGold());
        assertEquals(control.getAllUnits().size(),
                restored.getAllUnits().size());
        assertEquals(control.getPlayerByName("Dutch").getImmigrationCrosses(),
                owner.getImmigrationCrosses());
        assertTrue(owner.getCongressBells() > bellsBefore);

        final Map<String, Integer> afterAnswer = stocks(restored);
        assertThrows(IllegalStateException.class,
                () -> owner.acceptTaxDemand(reopened));
        assertThrows(IllegalStateException.class,
                () -> owner.protestTaxDemand(reopened));
        assertFalse(owner.canAcceptTaxDemand(reopened));
        assertTrue(owner.getTaxDemand().isEmpty());
        assertEquals(2, owner.getTaxRate());
        assertEquals(afterAnswer, stocks(restored));

        final Model reloaded = load(restored.save());
        reloaded.startGame();
        final Player again = reloaded.getPlayerByName("Dutch");
        assertTrue(reloaded.isGameRunning());
        assertEquals(2, again.getTaxRate());
        assertEquals(afterAnswer, stocks(reloaded));
        final TaxPo saved = again.save().getTax();
        assertEquals(1, saved.getIssued());
        assertEquals(null, saved.getPending());
        assertEquals(0, saved.getUpdates());
    }

    @Test
    void protestDestroysTheTiedStockOnceAndAddsLibertyWithoutCongressCredit() {
        final Model model = pending(tieData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        final TaxProtest protest = demand.getProtest().orElseThrow();
        // Equal value 660 for RUM in colonies 0 and 1 and SILK in colony 0.
        assertEquals(GoodsType.RUM, protest.getGoods());
        assertEquals(0, protest.getColonyId());
        assertEquals("New Amsterdam", protest.getColonyName());
        assertEquals(60, protest.getStock());
        assertEquals(60, protest.getQuantity());
        assertEquals(11, protest.getBid());
        assertEquals(1320, protest.getArrears());
        assertEquals(100, protest.getLibertyGain());
        assertTrue(dutch.canProtestTaxDemand(demand));

        dutch.protestTaxDemand(demand);
        // The +100 liberty legitimately changes the resumed update through
        // political production, so the control already holds that liberty.
        final Model control = acceptedWithLiberty(this::tieData, 0,
                protest.getLibertyGain());
        assertTrue(model.isGameRunning());
        assertEquals(0, dutch.getTaxRate());
        assertEquals(Map.of(GoodsType.RUM, 1320), dutch.getTaxArrears());
        assertTrue(dutch.isBoycotted(GoodsType.RUM));
        assertFalse(dutch.isBoycotted(GoodsType.SILK));
        final Map<String, Integer> expected = new TreeMap<>(stocks(control));
        expected.merge("0:RUM", -60, Integer::sum);
        assertEquals(expected, stocks(model));
        assertEquals(liberty(control), liberty(model));
        assertEquals(control.getPlayerByName("Dutch").getCongressBells(),
                dutch.getCongressBells());
        assertEquals(control.getPlayerByName("Dutch").getGold(),
                dutch.getGold());

        final Map<String, Integer> afterwards = stocks(model);
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        assertEquals(afterwards, stocks(model));
        assertEquals(Map.of(GoodsType.RUM, 1320), dutch.getTaxArrears());

        // A later price never changes the recorded arrears.
        model.getEurope().getGoodsTradeForType(GoodsType.RUM).setSellPrice(3);
        assertEquals(Map.of(GoodsType.RUM, 1320), dutch.getTaxArrears());

        final Model reloaded = load(model.save());
        reloaded.startGame();
        assertTrue(reloaded.isGameRunning());
        assertEquals(Map.of(GoodsType.RUM, 1320),
                reloaded.getPlayerByName("Dutch").getTaxArrears());
        assertEquals(afterwards, stocks(reloaded));
    }

    /** Colony 1 has a larger warehouse and the single most valuable stock. */
    private GameModelPo largeStockData() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR, 0, 1);
        expandWarehouse(data, 1);
        stock(data, 0, Map.of(GoodsType.RUM, 100));
        stock(data, 1, Map.of(GoodsType.COAT, 150));
        stock(data, 2, Map.of());
        return data;
    }

    @Test
    void greatestSaleValueBeatsOrderAndOnlyOneHundredOfALargerStockIsLost() {
        final Model model = pending(largeStockData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxProtest protest = dutch.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow();
        assertEquals(GoodsType.COAT, protest.getGoods());
        assertEquals(1, protest.getColonyId());
        assertEquals(150, protest.getStock());
        assertEquals(100, protest.getQuantity());
        assertEquals(9, protest.getBid());
        assertEquals(1800, protest.getArrears());
        dutch.protestTaxDemand(dutch.getTaxDemand().orElseThrow());
        // Delft gains the protest liberty before its resumed update produces,
        // which can change its political production; the control holds the same
        // liberty so only the destroyed goods differ.
        final Model control = acceptedWithLiberty(this::largeStockData, 1,
                protest.getLibertyGain());
        final Map<String, Integer> expected = new TreeMap<>(stocks(control));
        expected.merge("1:COAT", -100, Integer::sum);
        assertEquals(expected, stocks(model));
        assertEquals(liberty(control), liberty(model));
        assertEquals(Map.of(GoodsType.COAT, 1800), dutch.getTaxArrears());
    }

    @Test
    void saleValueIsTheGrossFullStockTimesBidNotTheTaxedNet() {
        // Gross 40 * 11 = 440 (RUM) and 49 * 9 = 441 (COAT). At 50% tax both
        // would sell for 220, but the rule compares the untaxed full-stock
        // value, so COAT wins although it is later in the goods order.
        final GameModelPo data = data(Difficulty.CONQUISTADOR, 50, 1);
        stock(data, 0, Map.of(GoodsType.COAT, 49, GoodsType.RUM, 40));
        stock(data, 1, Map.of());
        stock(data, 2, Map.of());
        final Model model = pending(data);
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(220,
                dutch.quoteEuropeTrade(Goods.of(GoodsType.RUM, 40), false)
                        .getTotal());
        assertEquals(220,
                dutch.quoteEuropeTrade(Goods.of(GoodsType.COAT, 49), false)
                        .getTotal());
        final TaxProtest protest = dutch.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow();
        assertEquals(GoodsType.COAT, protest.getGoods());
        assertEquals(52, dutch.getTaxDemand().orElseThrow().getProposedRate());
    }

    /**
     * Corn has no storage limit, so a huge stock with a huge bid is possible.
     */
    private Model pendingWithCorn(final int stock, final int bid) {
        final GameModelPo data = tieData();
        colonyPo(data, 0).getColonyWarehouse().put("CORN", stock);
        final Model model = load(data.getModel());
        model.getEurope().getGoodsTradeForType(GoodsType.CORN)
                .setSellPrice(bid);
        nextDutchTurn(model);
        assertTrue(model.isAwaitingTaxDecision());
        return model;
    }

    @Test
    void aFullStockValueBeyondTheIntRangeStillWinsWithRepresentableArrears() {
        // 250 * 10,000,000 exceeds an int, but 2 * 100 * 10,000,000 does not.
        final Model model = pendingWithCorn(250, 10_000_000);
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxProtest protest = dutch.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow();
        assertEquals(GoodsType.CORN, protest.getGoods());
        assertEquals(0, protest.getColonyId());
        assertEquals(250, protest.getStock());
        assertEquals(100, protest.getQuantity());
        assertEquals(10_000_000, protest.getBid());
        assertEquals(2_000_000_000, protest.getArrears());
        dutch.protestTaxDemand(dutch.getTaxDemand().orElseThrow());
        assertEquals(Map.of(GoodsType.CORN, 2_000_000_000),
                dutch.getTaxArrears());
        assertEquals(0, dutch.getTaxRate());
    }

    @Test
    void unrepresentableArrearsOfTheWinnerNeverProtestAnotherCommodity() {
        // The corn stock is worth the most; its arrears 4,000,000,000 do not
        // fit, so there is no protest at all and the rum stays untouched.
        final Model model = pendingWithCorn(250, 20_000_000);
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        assertTrue(demand.getProtest().isEmpty());
        assertFalse(dutch.canProtestTaxDemand(demand));
        final Map<String, Integer> before = stocks(model);
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        assertStillPending(model, before);
        assertEquals(60, model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.RUM).getAmount());
        dutch.acceptTaxDemand(demand);
        assertEquals(2, dutch.getTaxRate());
        assertTrue(dutch.getTaxArrears().isEmpty());
    }

    @Test
    void anAlreadyBoycottedCommodityIsNotOfferedAndItsArrearsSurvive() {
        final GameModelPo data = tieData();
        player(data.getModel(), "Dutch").getTax().getArrears().put("RUM", 200);
        final Model model = pending(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxProtest protest = dutch.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow();
        // RUM would win both colonies, but it is already boycotted.
        assertEquals(GoodsType.SILK, protest.getGoods());
        assertEquals(0, protest.getColonyId());
        dutch.protestTaxDemand(dutch.getTaxDemand().orElseThrow());
        assertEquals(Map.of(GoodsType.RUM, 200, GoodsType.SILK, 1320),
                dutch.getTaxArrears());
        assertEquals(60, model.getColonyById(0).orElseThrow().getWarehouse()
                .getGoods(GoodsType.RUM).getAmount());
    }

    @Test
    void successiveProtestsBoycottDifferentCommoditiesAndKeepEarlierArrears() {
        final Model first = pending(tieData());
        final Player dutch = first.getPlayerByName("Dutch");
        assertEquals(GoodsType.RUM, dutch.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow().getGoods());
        dutch.protestTaxDemand(dutch.getTaxDemand().orElseThrow());
        assertEquals(Map.of(GoodsType.RUM, 1320), dutch.getTaxArrears());

        final ModelPo saved = first.save();
        player(saved, "Dutch").getTax()
                .setUpdates(TaxState.interval(Difficulty.CONQUISTADOR) - 1);
        final Model second = load(saved);
        second.startGame();
        nextDutchTurn(second);
        assertTrue(second.isAwaitingTaxDecision());
        final Player owner = second.getPlayerByName("Dutch");
        final TaxDemand demand = owner.getTaxDemand().orElseThrow();
        assertEquals(2, demand.getSerial());
        assertEquals(0, demand.getCurrentRate());
        assertNotEquals(GoodsType.RUM,
                demand.getProtest().orElseThrow().getGoods());
        final GoodsType again = demand.getProtest().orElseThrow().getGoods();
        owner.protestTaxDemand(demand);
        assertEquals(2, owner.getTaxArrears().size());
        assertEquals(1320, owner.getTaxArrears().get(GoodsType.RUM));
        assertTrue(owner.isBoycotted(again));
        assertEquals(0, owner.getTaxRate());
    }

    @Test
    void whenEveryTradableCommodityIsBoycottedOnlyAcceptanceIsOffered() {
        final GameModelPo data = tieData();
        final TaxPo tax = player(data.getModel(), "Dutch").getTax();
        GoodsType.BUYABLE_GOOD_TYPES
                .forEach(type -> tax.getArrears().put(type.name(), 100));
        final Model model = pending(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        assertTrue(demand.getProtest().isEmpty());
        assertFalse(dutch.canProtestTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        final Map<GoodsType, Integer> arrears = dutch.getTaxArrears();
        dutch.acceptTaxDemand(demand);
        assertEquals(2, dutch.getTaxRate());
        assertEquals(arrears, dutch.getTaxArrears());
    }

    @Test
    void libertyGainStopsAtTheColonyPopulationCap() {
        final Model probe = load(tieData().getModel());
        final int population = probe.getColonyByName("New Amsterdam")
                .orElseThrow().getPopulation();
        final GameModelPo data = tieData();
        data.getModel().getColonies().stream()
                .filter(candidate -> candidate.getId() == 0).findFirst()
                .orElseThrow().setLiberty(population * 200L - 30);
        final Model model = pending(data);
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(30, dutch.getTaxDemand().orElseThrow().getProtest()
                .orElseThrow().getLibertyGain());
        dutch.protestTaxDemand(dutch.getTaxDemand().orElseThrow());
        final Colony colony = model.getColonyByName("New Amsterdam")
                .orElseThrow();
        assertEquals(colony.getPopulation() * 200L, colony.getLiberty());
    }

    @Test
    void aForeignActorOrForeignSnapshotCannotAnswerTheDemand() {
        final Model model = pending(tieData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        final Map<String, Integer> before = stocks(model);
        final Player crown = model.getPlayerByName("Dutch's King");
        assertFalse(crown.canAcceptTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> crown.acceptTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> crown.protestTaxDemand(demand));
        assertStillPending(model, before);

        final TaxDemand foreign = pending(tieData()).getPlayerByName("Dutch")
                .getTaxDemand().orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> dutch.acceptTaxDemand(foreign));
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(foreign));
        assertStillPending(model, before);
        assertThrows(NullPointerException.class,
                () -> dutch.acceptTaxDemand(null));
        assertStillPending(model, before);
    }

    @Test
    void aChangedRateRejectsBothAnswers() {
        final Model model = pending(tieData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        final Map<String, Integer> before = stocks(model);
        ((PlayerKing) model.getPlayerByName("Dutch's King"))
                .setKingsTaxPercentage(10);
        assertFalse(dutch.canAcceptTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> dutch.acceptTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        assertEquals(10, dutch.getTaxRate());
        assertStillPending(model, before);
    }

    @Test
    void aChangedStockRejectsProtestButAcceptanceStillResolvesTheDemand() {
        final Model model = pending(tieData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        model.getColonyByName("New Amsterdam").orElseThrow().getWarehouse()
                .removeGoods(Goods.of(GoodsType.RUM, 1));
        final Map<String, Integer> before = stocks(model);
        assertFalse(dutch.canProtestTaxDemand(demand));
        assertTrue(dutch.canAcceptTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        assertStillPending(model, before);
        dutch.acceptTaxDemand(demand);
        assertTrue(model.isGameRunning());
        assertEquals(2, dutch.getTaxRate());
    }

    @Test
    void aChangedPriceRejectsProtestWithoutBoycottOrLoss() {
        final Model model = pending(tieData());
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxDemand demand = dutch.getTaxDemand().orElseThrow();
        model.getEurope().getGoodsTradeForType(GoodsType.RUM).setSellPrice(12);
        final Map<String, Integer> before = stocks(model);
        assertFalse(dutch.canProtestTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> dutch.protestTaxDemand(demand));
        assertStillPending(model, before);
        dutch.acceptTaxDemand(demand);
        assertEquals(2, dutch.getTaxRate());
        assertTrue(dutch.getTaxArrears().isEmpty());
    }

    private void assertStillPending(final Model model,
            final Map<String, Integer> expectedStocks) {
        final Player dutch = model.getPlayerByName("Dutch");
        assertTrue(model.isAwaitingTaxDecision());
        assertFalse(model.isGameRunning());
        assertTrue(dutch.getTaxDemand().isPresent());
        assertTrue(dutch.getTaxArrears().isEmpty());
        assertEquals(expectedStocks, stocks(model));
    }

    @Test
    void aNationWithoutTradableStockCanOnlyAccept() {
        final Model model = mock(Model.class);
        when(model.getDifficulty()).thenReturn(Difficulty.CONQUISTADOR);
        final TaxPo po = TaxState.initial();
        po.setActivated(true);
        po.setUpdates(TaxState.interval(Difficulty.CONQUISTADOR) - 1);
        final Player player = new PlayerHuman("Dutch", false, 100, model, false,
                new HashMap<>(), new HashSet<>(), null, Congress.initial(), po);
        when(model.isAwaitingTaxDecision()).thenReturn(true);
        when(model.getCurrentPlayer()).thenReturn(player);
        when(model.getPlayers()).thenReturn(List.of(player));

        assertFalse(player.startTurn());
        assertTrue(player.hasPendingTaxDemand());
        final TaxDemand demand = player.getTaxDemand().orElseThrow();
        assertTrue(demand.getProtest().isEmpty());
        assertFalse(player.canProtestTaxDemand(demand));
        assertThrows(IllegalStateException.class,
                () -> player.protestTaxDemand(demand));
        assertEquals(0, player.getTaxRate());
        assertTrue(player.getTaxArrears().isEmpty());
        assertTrue(player.canAcceptTaxDemand(demand));

        player.acceptTaxDemand(demand);
        assertEquals(2, player.getTaxRate());
        assertFalse(player.hasPendingTaxDemand());
        verify(model).finishTaxChoice(player);
    }

    @Test
    void computerNationAcceptsTheSameOutcomeWithoutPausing() {
        final GameModelPo data = tieData();
        player(data.getModel(), "Dutch").setComputer(true);
        final Model model = load(data.getModel());
        final Model control = pending(tieData());
        accept(control);
        nextDutchTurn(model);
        final Player dutch = model.getPlayerByName("Dutch");
        assertTrue(model.isGameRunning());
        assertFalse(model.isAwaitingTaxDecision());
        assertEquals(2, dutch.getTaxRate());
        assertTrue(dutch.getTaxArrears().isEmpty());
        assertTrue(dutch.getTaxDemand().isEmpty());
        assertEquals(stocks(control), stocks(model));
        final TaxPo saved = dutch.save().getTax();
        assertEquals(1, saved.getIssued());
        assertEquals(null, saved.getPending());
    }

    @Test
    void cadenceFollowsEveryDifficultyAndNeverExceedsTheCap() {
        for (final Difficulty difficulty : Difficulty.values()) {
            final Model model = load(data(difficulty, 0, 2).getModel());
            nextDutchTurn(model);
            assertTrue(model.isGameRunning(), difficulty.name());
            assertEquals(TaxState.interval(difficulty) - 1, model
                    .getPlayerByName("Dutch").save().getTax().getUpdates());
            nextDutchTurn(model);
            assertTrue(model.isAwaitingTaxDecision(), difficulty.name());
            accept(model);
            assertEquals(2, model.getPlayerByName("Dutch").getTaxRate());
            assertEquals(0, model.getPlayerByName("Dutch").save().getTax()
                    .getUpdates());
        }
        final Model nearCap = pending(data(Difficulty.CONQUISTADOR, 74, 1));
        assertEquals(75, nearCap.getPlayerByName("Dutch").getTaxDemand()
                .orElseThrow().getProposedRate());
        accept(nearCap);
        assertEquals(75, nearCap.getPlayerByName("Dutch").getTaxRate());

        final Model capped = load(
                data(Difficulty.CONQUISTADOR, 75, 1).getModel());
        nextDutchTurn(capped);
        assertTrue(capped.isGameRunning());
        assertFalse(capped.isAwaitingTaxDecision());
        final TaxPo saved = capped.getPlayerByName("Dutch").save().getTax();
        assertEquals(75, saved.getRate());
        assertEquals(0, saved.getIssued());
        assertEquals(0, saved.getUpdates());
    }

    @Test
    void firstColonyActivatesTheCadenceAndIndependenceSuppressesDemands() {
        final GameModelPo fresh = data(Difficulty.CONQUISTADOR, 0, 1);
        player(fresh.getModel(), "Dutch").setTax(TaxState.initial());
        final Model started = load(fresh.getModel());
        nextDutchTurn(started);
        assertTrue(started.isGameRunning());
        final TaxPo activated = started.getPlayerByName("Dutch").save()
                .getTax();
        assertTrue(activated.getActivated());
        assertEquals(1, activated.getUpdates());
        assertEquals(0, activated.getRate());
        assertEquals(0, activated.getIssued());

        final GameModelPo revolt = data(Difficulty.CONQUISTADOR, 0, 1);
        player(revolt.getModel(), "Dutch").setDeclaredIndependence(true);
        final Model independent = load(revolt.getModel());
        nextDutchTurn(independent);
        assertTrue(independent.isGameRunning());
        final TaxPo unchanged = independent.getPlayerByName("Dutch").save()
                .getTax();
        assertEquals(TaxState.interval(Difficulty.CONQUISTADOR) - 1,
                unchanged.getUpdates());
        assertEquals(0, unchanged.getIssued());
    }

    @Test
    void earnedImmigrationThenTaxResumeOnlyTheRemainingPreparation() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR, 0, 1);
        final PlayerPo dutchPo = player(data.getModel(), "Dutch");
        final CongressPo congress = dutchPo.getCongress();
        congress.setMembers(List.of(Founder.WILLIAM_BREWSTER));
        congress.setSelected(null);
        dutchPo.getRecruitment().setCrosses(230);
        dutchPo.getRecruitment().getCandidates().forEach(candidate -> {
            if (candidate.getType() == UnitType.INDENTURED_SERVANT
                    || candidate.getType() == UnitType.PETTY_CRIMINAL) {
                candidate.setType(UnitType.COLONIST);
            }
        });
        final Model model = load(data.getModel());
        final Player dutch = model.getPlayerByName("Dutch");
        final int pierBefore = peopleAtPier(dutch);
        final int bellsBefore = dutch.getCongressBells();
        final Map<String, Integer> stocksBefore = stocks(model);
        nextDutchTurn(model);
        assertTrue(model.isAwaitingImmigrationChoice());
        assertFalse(model.isAwaitingTaxDecision());
        assertTrue(dutch.getTaxDemand().isEmpty());
        // An immigration-only snapshot is a legitimate saved boundary even
        // though a tax demand will follow it.
        final Model between = load(model.save());
        between.startGame();
        assertTrue(between.isAwaitingImmigrationChoice());
        assertFalse(between.isAwaitingTaxDecision());
        assertEquals(230,
                between.getPlayerByName("Dutch").getImmigrationCrosses());
        assertEquals(0,
                between.getPlayerByName("Dutch").save().getTax().getUpdates()
                        - (TaxState.interval(Difficulty.CONQUISTADOR) - 1));
        dutch.chooseEarnedImmigrant(dutch.getEarnedImmigrationOffers().get(0));
        assertTrue(model.isAwaitingImmigrationChoice());
        assertTrue(dutch.getTaxDemand().isEmpty());
        dutch.chooseEarnedImmigrant(dutch.getEarnedImmigrationOffers().get(0));

        assertFalse(model.isAwaitingImmigrationChoice());
        assertTrue(model.isAwaitingTaxDecision());
        assertFalse(model.isGameRunning());
        assertEquals(pierBefore + 2, peopleAtPier(dutch));
        assertEquals(5, dutch.getImmigrationCrosses());
        assertEquals(bellsBefore, dutch.getCongressBells());
        assertEquals(stocksBefore, stocks(model));
        assertEquals(2, dutch.save().getRecruitment().getEarnedImmigrants());

        final Model restored = load(model.save());
        restored.startGame();
        final Player owner = restored.getPlayerByName("Dutch");
        assertTrue(restored.isAwaitingTaxDecision());
        assertFalse(restored.isAwaitingImmigrationChoice());
        assertEquals(pierBefore + 2, peopleAtPier(owner));
        assertEquals(5, owner.getImmigrationCrosses());
        assertEquals(2, owner.save().getRecruitment().getEarnedImmigrants());
        owner.acceptTaxDemand(owner.getTaxDemand().orElseThrow());
        assertTrue(restored.isGameRunning());
        assertEquals(2, owner.getTaxRate());
        assertEquals(pierBefore + 2, peopleAtPier(owner));
        assertEquals(2, owner.save().getRecruitment().getEarnedImmigrants());
        assertTrue(owner.getCongressBells() > bellsBefore);
        assertTrue(owner.getImmigrationCrosses() >= 5);
    }

    @Test
    void fuggerArrivalClearsExistingArrearsOnceAndLaterProtestsStillBoycott() {
        final GameModelPo data = data(Difficulty.CONQUISTADOR, 0, 5);
        final PlayerPo dutchPo = player(data.getModel(), "Dutch");
        dutchPo.getTax().getArrears().put("RUM", 200);
        dutchPo.getTax().getArrears().put("TOBACCO", 100);
        final CongressPo congress = Congress.initial();
        congress.setActivated(true);
        congress.setBells(100);
        congress.setMembers(List.of());
        congress.setCandidates(List.of(Founder.HERNAN_CORTES,
                Founder.THOMAS_JEFFERSON, Founder.JAKOB_FUGGER,
                Founder.WILLIAM_PENN, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(Founder.JAKOB_FUGGER);
        congress.setGeneration(1);
        dutchPo.setCongress(congress);
        final Model model = load(data.getModel());
        final Player dutch = model.getPlayerByName("Dutch");
        assertTrue(dutch.isBoycotted(GoodsType.RUM));
        assertTrue(dutch.isBoycotted(GoodsType.TOBACCO));
        nextDutchTurn(model);
        assertTrue(model.isGameRunning());
        assertTrue(dutch.getCongressMembers().contains(Founder.JAKOB_FUGGER));
        assertTrue(dutch.getTaxArrears().isEmpty());
        assertFalse(dutch.isBoycotted(GoodsType.RUM));

        // Reloading never replays the arrival, and the next protest is not
        // forgiven by the father who already joined.
        final ModelPo saved = model.save();
        final TaxPo tax = player(saved, "Dutch").getTax();
        tax.setUpdates(TaxState.interval(Difficulty.CONQUISTADOR) - 1);
        final Model later = load(saved);
        later.startGame();
        assertTrue(later.getPlayerByName("Dutch").getTaxArrears().isEmpty());
        nextDutchTurn(later);
        assertTrue(later.isAwaitingTaxDecision());
        final Player owner = later.getPlayerByName("Dutch");
        final GoodsType protested = owner.getTaxDemand().orElseThrow()
                .getProtest().orElseThrow().getGoods();
        owner.protestTaxDemand(owner.getTaxDemand().orElseThrow());
        assertTrue(owner.isBoycotted(protested));
        assertTrue(owner.getCongressMembers().contains(Founder.JAKOB_FUGGER));

        final Model reloaded = load(later.save());
        reloaded.startGame();
        assertTrue(reloaded.getPlayerByName("Dutch").isBoycotted(protested));
        assertEquals(1,
                reloaded.getPlayerByName("Dutch").getTaxArrears().size());
    }

    private ModelPo savedPending() {
        return pending(tieData()).save();
    }

    private void assertRejected(final ModelPo saved) {
        assertThrows(IllegalArgumentException.class, () -> load(saved));
    }

    @Test
    void contradictorySavedStateIsRejectedRatherThanGuessed() {
        // The untouched position loads.
        final Model fine = load(savedPending());
        fine.startGame();
        assertTrue(fine.isAwaitingTaxDecision());

        ModelPo saved = savedPending();
        player(saved, "Dutch").setTax(null);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch's King").setTax(TaxState.initial());
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().getPending().setProposedRate(5);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().getPending().getProtest()
                .setColonyId(99);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().getPending().getProtest()
                .setQuantity(59);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().getPending().getProtest()
                .setGoods(GoodsType.LUMBER);
        assertRejected(saved);

        // A demand falls due when the counter restarts, never mid-interval.
        saved = savedPending();
        player(saved, "Dutch").getTax().setUpdates(3);
        assertRejected(saved);

        // An already boycotted commodity is never offered.
        saved = savedPending();
        player(saved, "Dutch").getTax().getArrears().put("RUM", 200);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().setActivated(false);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").setComputer(true);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").setDeclaredIndependence(true);
        assertRejected(saved);

        saved = savedPending();
        saved.getGameManager().setGameStarted(false);
        assertRejected(saved);

        saved = savedPending();
        saved.getGameManager().setCurrentPlayer("Dutch's King");
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().getArrears().put("RUM", 99);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().setIssued(0);
        assertRejected(saved);

        saved = savedPending();
        player(saved, "Dutch").getTax().setRate(null);
        assertRejected(saved);
    }

    @Test
    void savedPendingDemandIsNotRewrittenByLoading() {
        final ModelPo saved = savedPending();
        final String before = new com.google.gson.Gson().toJson(saved);
        final Model restored = load(saved);
        restored.startGame();
        assertEquals(before,
                new com.google.gson.Gson().toJson(restored.save()));
        assertNotEquals(null,
                restored.getPlayerByName("Dutch").getTaxDemand().orElse(null));
    }
}
