package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.TaxPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventCustomHouseExport;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFreeColonist.EquipmentProblem;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * A boycott closes both directions of one commodity on every trade path, while
 * other commodities, arithmetic quotes, stocked tools and the revolutionary
 * custom house keep working.
 */
class TaxBoycottTest {

    private static final Location COLONY = Location.of(22, 12);
    private static final int WAGON_ID = 5000;

    private GameModelPo data(final GoodsType... boycotted) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies()
                .forEach(colony -> colony.setExportGoods(List.of()));
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        final TaxPo tax = TaxState.initial();
        for (final GoodsType type : boycotted) {
            tax.getArrears().put(type.name(), 150);
        }
        dutch.setTax(tax);
        return data;
    }

    private static UnitPo wagon() {
        final UnitPo saved = new UnitPo();
        saved.setId(WAGON_ID);
        saved.setType(UnitType.WAGON);
        saved.setOwnerId("Dutch");
        saved.setAvailableMoves(1);
        saved.setAction(new UnitActionNoActionPo());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.east);
        saved.setPlaceMap(place);
        saved.getCargo().getSlots().add(new CargoSlotPo());
        saved.getCargo().getSlots().add(new CargoSlotPo());
        return saved;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Model modelWithWagon(final GoodsType... boycotted) {
        final GameModelPo data = data(boycotted);
        data.getModel().getUnits().add(wagon());
        final Model model = load(data);
        model.getPlayerByName("Dutch").setGold(100000);
        return model;
    }

    private static CargoSlot slot(final Model model, final int index) {
        return ((UnitWithCargo) model.getUnitById(WAGON_ID)).getCargo()
                .getSlotByIndex(index);
    }

    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private static int amountIn(final CargoSlot slot) {
        return slot.getGoods().map(Goods::getAmount).orElse(0);
    }

    @Test
    void boycottClosesPurchaseAndSaleOfThatCommodityOnly() {
        final Model model = modelWithWagon(GoodsType.CORN);
        final Player dutch = model.getPlayerByName("Dutch");
        final CargoSlot target = slot(model, 0);
        final CargoSlot source = slot(model, 1);
        source.addGoods(Goods.of(GoodsType.CORN, 30));
        final String before = snapshot(model);
        final int gold = dutch.getGold();

        assertTrue(dutch.isBoycotted(GoodsType.CORN));
        assertThrows(IllegalStateException.class, () -> target
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.CORN, 20)));
        assertThrows(IllegalStateException.class,
                () -> model.sellGoods(source, Goods.of(GoodsType.CORN, 10)));
        assertThrows(IllegalStateException.class,
                () -> dutch.buyGoods(Goods.of(GoodsType.CORN, 1)));
        assertThrows(IllegalStateException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.CORN, 1)));
        assertEquals(before, snapshot(model));
        assertEquals(gold, dutch.getGold());
        assertTrue(target.isEmpty());
        assertEquals(30, amountIn(source));
        assertTrue(dutch.getMaxBuyableGoods(GoodsType.CORN).isZero());

        // The arithmetic quote stays usable for the revolutionary exception.
        assertTrue(dutch.quoteEuropeTrade(Goods.of(GoodsType.CORN, 10), false)
                .getTotal() > 0);

        // Another commodity trades normally.
        assertFalse(dutch.getMaxBuyableGoods(GoodsType.ORE).isZero());
        final int price = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.ORE, 20), true).getTotal();
        target.buyAndStoreFromEuropePort(Goods.of(GoodsType.ORE, 20));
        assertEquals(20, amountIn(target));
        assertEquals(gold - price, dutch.getGold());
        final int income = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.ORE, 20), false)
                .getTotal();
        model.sellGoods(target, Goods.of(GoodsType.ORE, 20));
        assertTrue(target.isEmpty());
        assertEquals(gold - price + income, dutch.getGold());
    }

    @Test
    void aBoycottThatBeginsAfterTheQuantityWasChosenRejectsBeforeAnyMutation() {
        final Model model = modelWithWagon();
        final Player dutch = model.getPlayerByName("Dutch");
        final CargoSlot target = slot(model, 0);
        final CargoSlot source = slot(model, 1);
        source.addGoods(Goods.of(GoodsType.ORE, 30));
        assertFalse(dutch.getMaxBuyableGoods(GoodsType.ORE).isZero());

        dutch.addTaxArrears(GoodsType.ORE, 100);
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> target
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.ORE, 20)));
        assertThrows(IllegalStateException.class,
                () -> model.sellGoods(source, Goods.of(GoodsType.ORE, 30)));
        assertEquals(before, snapshot(model));
        assertTrue(dutch.getMaxBuyableGoods(GoodsType.ORE).isZero());
        assertFalse(dutch.getMaxBuyableGoods(GoodsType.CORN).isZero());
    }

    @Test
    void boycottSurvivesSaveAndLoad() {
        final Model model = modelWithWagon(GoodsType.ORE, GoodsType.SILVER);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = restored.getPlayerByName("Dutch");
        assertTrue(dutch.isBoycotted(GoodsType.ORE));
        assertTrue(dutch.isBoycotted(GoodsType.SILVER));
        assertFalse(dutch.isBoycotted(GoodsType.CORN));
        assertEquals(snapshot(model), snapshot(restored));
    }

    @Test
    void ordinaryCustomHouseSkipsABoycottedExportButSellsTheOthers() {
        final Model model = load(data(GoodsType.ORE));
        final Colony colony = equipped(model);
        colony.configureExportGoods(Set.of(),
                Set.of(GoodsType.SILVER, GoodsType.ORE));
        colony.getWarehouse()
                .stageProducedGoods(Goods.of(GoodsType.SILVER, 150));
        colony.getWarehouse().stageProducedGoods(Goods.of(GoodsType.ORE, 150));
        final Player owner = colony.getOwner();
        final int gold = owner.getGold();
        final int silver = owner
                .quoteEuropeTrade(Goods.of(GoodsType.SILVER, 50), false)
                .getTotal();

        colony.startTurn();

        assertEquals(gold + silver, owner.getGold());
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.SILVER).getAmount());
        // The boycotted goods were neither sold nor reported; the ordinary
        // overflow clamp still applies.
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        final List<Goods> exported = new java.util.ArrayList<>();
        model.getTurnEventStore().getForPlayer(owner).stream()
                .filter(event -> event instanceof TurnEventCustomHouseExport)
                .forEach(event -> exported
                        .add(((TurnEventCustomHouseExport) event).getGoods()));
        assertEquals(List.of(Goods.of(GoodsType.SILVER, 50)), exported);
        assertTrue(owner.isBoycotted(GoodsType.ORE));
    }

    @Test
    void revolutionaryCustomHouseBypassesTheBoycottWithHalfTheNetProceeds() {
        final GameModelPo data = data(GoodsType.ORE);
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().setDeclaredIndependence(true);
        final Model model = load(data);
        final Colony colony = equipped(model);
        colony.configureExportGoods(Set.of(),
                Set.of(GoodsType.SILVER, GoodsType.ORE));
        colony.getWarehouse()
                .stageProducedGoods(Goods.of(GoodsType.SILVER, 150));
        colony.getWarehouse().stageProducedGoods(Goods.of(GoodsType.ORE, 150));
        final Player owner = colony.getOwner();
        assertFalse(owner.canTradeInEurope());
        final int gold = owner.getGold();
        final int silver = owner
                .quoteEuropeTrade(Goods.of(GoodsType.SILVER, 50), false)
                .getTotal() / 2;
        final int ore = owner
                .quoteEuropeTrade(Goods.of(GoodsType.ORE, 50), false).getTotal()
                / 2;
        assertTrue(ore > 0);

        colony.startTurn();

        assertEquals(gold + silver + ore, owner.getGold());
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        // Bypassing the closed port does not lift the boycott itself.
        assertTrue(owner.isBoycotted(GoodsType.ORE));
        assertThrows(IllegalStateException.class,
                () -> owner.sellGoods(Goods.of(GoodsType.ORE, 1)));
    }

    private Colony equipped(final Model model) {
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        colony.getConstructions().add(Construction.build(model, colony,
                ConstructionType.CUSTOM_HOUSE));
        return colony;
    }

    @Test
    void toolsBoycottBlocksOnlyTheImportOfMissingRushTools() {
        final Model model = load(data(GoodsType.TOOLS));
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        final Player owner = colony.getOwner();
        queue.addAtEnd(UnitType.WAGON);
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 10));
        owner.setGold(1000);
        final String before = snapshot(model);
        assertTrue(queue.getRushQuote().isEmpty());
        assertEquals(before, snapshot(model));

        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 100));
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertEquals(0, quote.getMissingTools());
        final int units = model.getAllUnits().size();
        final int gold = owner.getGold();
        queue.rush(quote);
        assertEquals(units + 1, model.getAllUnits().size());
        assertEquals(gold, owner.getGold());
        assertTrue(owner.isBoycotted(GoodsType.TOOLS));
    }

    @Test
    void aToolsBoycottAfterTheQuoteRejectsTheRushWithoutCharging() {
        final Model model = load(data());
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        final Player owner = colony.getOwner();
        queue.addAtEnd(UnitType.WAGON);
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 10));
        owner.setGold(1000);
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertEquals(30, quote.getMissingTools());

        owner.addTaxArrears(GoodsType.TOOLS, 100);
        final String before = snapshot(model);
        assertTrue(queue.getRushQuote().isEmpty());
        assertThrows(IllegalStateException.class, () -> queue.rush(quote));
        assertEquals(before, snapshot(model));
        assertEquals(1000, owner.getGold());
    }

    // --- pier equipment -------------------------------------------------

    private final Model mockModel = mock(Model.class);
    private Player pierOwner;
    private UnitFreeColonist colonist;

    private void preparePier(final GoodsType... boycotted) {
        final TaxPo tax = TaxState.initial();
        for (final GoodsType type : boycotted) {
            tax.getArrears().put(type.name(), 100);
        }
        final Europe europe = new Europe(mockModel);
        when(mockModel.getEurope()).thenReturn(europe);
        pierOwner = new PlayerHuman("Dutch", false, 1000, mockModel, false,
                new HashMap<>(), new HashSet<>(), null, Congress.initial(),
                tax);
        colonist = new UnitFreeColonist(mockModel, 4, PlaceEuropePier::new,
                pierOwner, MovementPoints.ofPoints(1), new UnitActionNoAction(),
                0, false, false);
        when(mockModel.isGameRunning()).thenReturn(true);
        when(mockModel.getCurrentPlayer()).thenReturn(pierOwner);
        when(mockModel.getUnitsOwnedBy(pierOwner, true))
                .thenReturn(List.of(colonist));
    }

    @Test
    void pierEquipmentIsClosedInBothDirectionsForTheBoycottedCommodityOnly() {
        preparePier(GoodsType.MUSKET);
        assertEquals(Optional.of(EquipmentProblem.BOYCOTTED),
                colonist.getEuropeEquipmentProblem(GoodsType.MUSKET, true));
        final EuropeTradeQuote offer = colonist
                .getEuropeEquipmentQuote(GoodsType.MUSKET, true);
        assertThrows(IllegalStateException.class, () -> colonist
                .tradeEquipmentInEurope(GoodsType.MUSKET, true, offer));
        assertEquals(1000, pierOwner.getGold());
        assertFalse(colonist.isHoldingGuns());

        colonist.setHoldingGuns(true);
        assertEquals(Optional.of(EquipmentProblem.BOYCOTTED),
                colonist.getEuropeEquipmentProblem(GoodsType.MUSKET, false));
        final EuropeTradeQuote sale = colonist
                .getEuropeEquipmentQuote(GoodsType.MUSKET, false);
        assertThrows(IllegalStateException.class, () -> colonist
                .tradeEquipmentInEurope(GoodsType.MUSKET, false, sale));
        assertEquals(1000, pierOwner.getGold());
        assertTrue(colonist.isHoldingGuns());
        colonist.setHoldingGuns(false);

        // Horses and tools stay tradable.
        assertEquals(Optional.empty(),
                colonist.getEuropeEquipmentProblem(GoodsType.HORSE, true));
        colonist.tradeEquipmentInEurope(GoodsType.HORSE, true,
                colonist.getEuropeEquipmentQuote(GoodsType.HORSE, true));
        assertTrue(colonist.isMounted());
        assertEquals(750, pierOwner.getGold());
    }

    @Test
    void anOpenEquipmentOfferIsRejectedWhenTheCommodityIsBoycottedMeanwhile() {
        preparePier();
        final EuropeTradeQuote offer = colonist
                .getEuropeEquipmentQuote(GoodsType.HORSE, true);
        assertEquals(Optional.empty(),
                colonist.getEuropeEquipmentProblem(GoodsType.HORSE, true));

        pierOwner.addTaxArrears(GoodsType.HORSE, 100);
        assertEquals(Optional.of(EquipmentProblem.BOYCOTTED),
                colonist.getEuropeEquipmentProblem(GoodsType.HORSE, true));
        assertThrows(IllegalStateException.class, () -> colonist
                .tradeEquipmentInEurope(GoodsType.HORSE, true, offer));
        assertEquals(1000, pierOwner.getGold());
        assertFalse(colonist.isMounted());
    }
}
