package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ConstructionPo;
import org.microcol.model.turnevent.TurnEventCustomHouseExport;

import com.google.gson.Gson;

/** Saved custom-house selections and same-turn export accounting. */
class CustomHouseExportTest {
    private GameModelPo data() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getColonies()
                .forEach(colony -> colony.setExportGoods(List.of()));
        return data;
    }

    private Model model() {
        return Model.make(data().getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private Colony equipped(final Model model) {
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        colony.getConstructions().add(Construction.build(model, colony,
                ConstructionType.CUSTOM_HOUSE));
        return colony;
    }

    private void setCornAfterConsumption(final Colony colony,
            final int wanted) {
        final int current = colony.getWarehouse().getGoods(GoodsType.CORN)
                .getAmount();
        final int net = colony.getRawGoodsStats().getStatsByType(GoodsType.CORN)
                .getInWarehouseAfter() - current;
        colony.getWarehouse().setGoods(Goods.of(GoodsType.CORN, wanted - net));
        assertEquals(wanted, colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter());
    }

    @Test
    void savedSelectionIsStrictAndReadOnly() {
        final var invalidEvent = new org.microcol.model.store.TurnEventPo();
        invalidEvent.setType("TurnEventCustomHouseExport");
        invalidEvent.setPlayerName("Dutch");
        invalidEvent.setColonyId(0);
        invalidEvent.setColonyName("Delft");
        invalidEvent.setGoods(Goods.of(GoodsType.HAMMERS, 1));
        invalidEvent.setIncome(1);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventCustomHouseExport.tryLoad(invalidEvent));
        final GameModelPo absent = data();
        absent.getModel().getColonies().get(0).setExportGoods(null);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(absent.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        final GameModelPo duplicate = data();
        duplicate.getModel().getColonies().get(0)
                .setExportGoods(List.of(GoodsType.CORN, GoodsType.CORN));
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(duplicate.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        final GameModelPo untradable = data();
        untradable.getModel().getColonies().get(0)
                .setExportGoods(List.of(GoodsType.HAMMERS));
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(untradable.getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators()));

        final Model model = model();
        final Colony colony = equipped(model);
        final String before = new Gson().toJson(model.save());
        assertEquals(Set.of(), colony.getExportGoods());
        assertThrows(IllegalStateException.class,
                () -> model.getColonyByName("New Amsterdam").orElseThrow()
                        .configureExportGoods(Set.of(),
                                Set.of(GoodsType.CORN)));
        assertThrows(IllegalArgumentException.class, () -> colony
                .configureExportGoods(Set.of(), Set.of(GoodsType.HAMMERS)));
        assertEquals(before, new Gson().toJson(model.save()));
        colony.configureExportGoods(Set.of(), Set.of(GoodsType.CORN));
        final String selected = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, () -> colony
                .configureExportGoods(Set.of(), Set.of(GoodsType.LUMBER)));
        assertEquals(selected, new Gson().toJson(model.save()));
        assertEquals(Set.of(GoodsType.CORN), colony.getExportGoods());
        assertEquals(List.of(GoodsType.CORN), colony.save().getExportGoods());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(Set.of(GoodsType.CORN), restored.getColonyByName("Delft")
                .orElseThrow().getExportGoods());
    }

    @Test
    void cornSurplusUsesTaxedMarketPriceAndPersistsOneEvent() {
        final Model model = model();
        final Colony colony = equipped(model);
        colony.configureExportGoods(Set.of(), Set.of(GoodsType.CORN));
        setCornAfterConsumption(colony, 101);
        model.getEurope().getGoodsTradeForType(GoodsType.CORN).setSellPrice(7);
        model.getPlayerStore().getKingForPlayer(colony.getOwner()).orElseThrow()
                .setKingsTaxPercentage(10);
        final int afterFood = colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter();
        final int sold = Math.max(0, afterFood - 100);
        assertTrue(sold > 0);
        final Player owner = colony.getOwner();
        final int beforeGold = owner.getGold();
        final int income = owner
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, sold), false)
                .getTotal();
        assertEquals(1, sold);
        assertEquals(6, income);
        colony.startTurn();
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        assertEquals(beforeGold + income, owner.getGold());
        final List<TurnEventCustomHouseExport> exports = new ArrayList<>();
        model.getTurnEventStore().getForPlayer(owner).stream()
                .filter(event -> event instanceof TurnEventCustomHouseExport)
                .map(event -> (TurnEventCustomHouseExport) event)
                .forEach(exports::add);
        assertEquals(1, exports.size());
        assertEquals(colony.getId(), exports.get(0).getColonyId());
        assertEquals(colony.getName(), exports.get(0).getColonyName());
        assertEquals(Goods.of(GoodsType.CORN, sold), exports.get(0).getGoods());
        assertEquals(income, exports.get(0).getIncome());
        final String saved = new Gson().toJson(model.save());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(saved, new Gson().toJson(restored.save()));
    }

    @Test
    void independenceUsesHalfTaxedNetWithoutOpeningEurope() {
        final GameModelPo data = data();
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().setDeclaredIndependence(true);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = equipped(model);
        colony.configureExportGoods(Set.of(), Set.of(GoodsType.CORN));
        setCornAfterConsumption(colony, 101);
        model.getEurope().getGoodsTradeForType(GoodsType.CORN).setSellPrice(7);
        model.getPlayerStore().getKingForPlayer(colony.getOwner()).orElseThrow()
                .setKingsTaxPercentage(10);
        final int sold = colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter() - 100;
        assertTrue(sold > 0);
        final Player owner = colony.getOwner();
        assertFalse(owner.canTradeInEurope());
        final int income = owner
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, sold), false)
                .getTotal() / 2;
        assertEquals(1, sold);
        assertEquals(3, income);
        final int oldGold = owner.getGold();
        colony.startTurn();
        assertEquals(oldGold + income, owner.getGold());
    }

    @Test
    void productionAboveCapacityIsSoldBeforeTheOrdinaryClamp() {
        final Model model = model();
        final Colony colony = model.getColonyByName("New Amsterdam")
                .orElseThrow();
        colony.getConstructions().add(Construction.build(model, colony,
                ConstructionType.CUSTOM_HOUSE));
        model.getUnitById(0).placeToColonyField(
                colony.getColonyFieldInDirection(Direction.northWest),
                GoodsType.LUMBER);
        colony.configureExportGoods(Set.of(), Set.of(GoodsType.LUMBER));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.LUMBER, 100));
        final int produced = colony.getRawGoodsStats()
                .getStatsByType(GoodsType.LUMBER).getDiff();
        assertTrue(produced > 0);
        final Player owner = colony.getOwner();
        final int beforeGold = owner.getGold();
        final int income = owner
                .quoteEuropeTrade(Goods.of(GoodsType.LUMBER, produced), false)
                .getTotal();
        colony.startTurn();
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.LUMBER).getAmount());
        assertEquals(beforeGold + income, owner.getGold());
        assertEquals(1, model.getTurnEventStore().getForPlayer(owner).stream()
                .filter(event -> event instanceof TurnEventCustomHouseExport
                        && ((TurnEventCustomHouseExport) event)
                                .getColonyId() == colony.getId())
                .count());
    }

    @Test
    void thresholdsAndArithmeticFailureNeverPartiallySellTheBatch() {
        for (final int staged : new int[] { 99, 100, 101 }) {
            final Model model = model();
            final Colony colony = equipped(model);
            colony.configureExportGoods(Set.of(), Set.of(GoodsType.SILVER));
            colony.getWarehouse()
                    .stageProducedGoods(Goods.of(GoodsType.SILVER, staged));
            final int beforeGold = colony.getOwner().getGold();
            final int expectedIncome = staged > 100
                    ? colony.getOwner()
                            .quoteEuropeTrade(
                                    Goods.of(GoodsType.SILVER, staged - 100),
                                    false)
                            .getTotal()
                    : 0;
            colony.startTurn();
            assertEquals(Math.min(100, staged), colony.getWarehouse()
                    .getGoods(GoodsType.SILVER).getAmount());
            assertEquals(beforeGold + expectedIncome,
                    colony.getOwner().getGold());
        }

        final Model withoutHouse = model();
        final Colony ordinary = withoutHouse.getColonyByName("Delft")
                .orElseThrow();
        ordinary.getWarehouse()
                .stageProducedGoods(Goods.of(GoodsType.SILVER, 101));
        final int ordinaryGold = ordinary.getOwner().getGold();
        ordinary.startTurn();
        assertEquals(100,
                ordinary.getWarehouse().getGoods(GoodsType.SILVER).getAmount());
        assertEquals(ordinaryGold, ordinary.getOwner().getGold());

        final Model overflow = model();
        final Colony colony = equipped(overflow);
        colony.configureExportGoods(Set.of(),
                Set.of(GoodsType.SILVER, GoodsType.ORE));
        colony.getWarehouse()
                .stageProducedGoods(Goods.of(GoodsType.SILVER, 101));
        colony.getWarehouse().stageProducedGoods(Goods.of(GoodsType.ORE, 101));
        colony.getOwner().setGold(Integer.MAX_VALUE);
        assertThrows(ArithmeticException.class, colony::startTurn);
        assertEquals(Integer.MAX_VALUE, colony.getOwner().getGold());
        assertEquals(101,
                colony.getWarehouse().getGoods(GoodsType.SILVER).getAmount());
        assertEquals(101,
                colony.getWarehouse().getGoods(GoodsType.ORE).getAmount());
        assertEquals(0, overflow.getTurnEventStore()
                .getForPlayer(colony.getOwner()).stream()
                .filter(event -> event instanceof TurnEventCustomHouseExport)
                .count());
    }

    @Test
    void constructionConsumesToolsBeforeSelectedSurplusIsSold() {
        final Model model = model();
        final Colony colony = equipped(model);
        colony.configureExportGoods(Set.of(), Set.of(GoodsType.TOOLS));
        colony.getColonyBuildingQueue().addAtEnd(UnitType.WAGON);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 100));
        colony.getWarehouse().stageProducedGoods(Goods.of(GoodsType.TOOLS, 10));
        final Player owner = colony.getOwner();
        final int beforeGold = owner.getGold();
        final int beforeUnits = model.getAllUnits().size();
        colony.startTurn();
        assertEquals(70,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(beforeGold, owner.getGold());
        assertEquals(beforeUnits + 1, model.getAllUnits().size());
        assertEquals(0,
                model.getTurnEventStore().getForPlayer(owner).stream().filter(
                        event -> event instanceof TurnEventCustomHouseExport)
                        .count());
    }

    @Test
    void capturedColonialPortPaysItsNewOwnerWithoutRequiringTheFounderAgain() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getColonies()
                .forEach(saved -> saved.setExportGoods(List.of()));
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        final var quebec = data.getModel().getColonies().stream()
                .filter(saved -> saved.getName().equals("Quebec")).findFirst()
                .orElseThrow();
        final ConstructionPo house = new ConstructionPo();
        house.setType(ConstructionType.CUSTOM_HOUSE);
        house.setSlots(List.of());
        quebec.getConstructions().add(house);
        quebec.setExportGoods(List.of(GoodsType.SILVER));
        quebec.getColonyWarehouse().put("SILVER", 100);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = model.getPlayerByName("Dutch");
        final Player france = model.getPlayerByName("France");
        final Colony colony = model.getColonyByName("Quebec").orElseThrow();
        assertSame(france, colony.getOwner());
        assertFalse(
                dutch.getCongressMembers().contains(Founder.PETER_STUYVESANT));
        model.declareHostilities(dutch, france);
        model.getUnitById(6).placeToLocation(Location.of(17, 11));
        final Unit attacker = model.getUnitById(0);
        attacker.placeToLocation(colony.getLocation());
        colony.captureColony(attacker);
        assertSame(dutch, colony.getOwner());
        assertTrue(
                colony.wasConstructionBuilded(ConstructionType.CUSTOM_HOUSE));
        colony.getWarehouse().stageProducedGoods(Goods.of(GoodsType.SILVER, 1));
        final int frenchGold = france.getGold();
        final int dutchGold = dutch.getGold();
        final int income = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.SILVER, 1), false)
                .getTotal();
        colony.startTurn();
        assertEquals(frenchGold, france.getGold());
        assertEquals(dutchGold + income, dutch.getGold());
        assertEquals(100,
                colony.getWarehouse().getGoods(GoodsType.SILVER).getAmount());
        assertEquals(1,
                model.getTurnEventStore().getForPlayer(dutch).stream().filter(
                        event -> event instanceof TurnEventCustomHouseExport)
                        .count());
        assertEquals(0,
                model.getTurnEventStore().getForPlayer(france).stream().filter(
                        event -> event instanceof TurnEventCustomHouseExport)
                        .count());
    }
}
