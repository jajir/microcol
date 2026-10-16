package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class ExpertPurchaseTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();
    private GameModelPo data = dao.loadFromClassPath(
            "/maps/default-campaign-mission-2-thrive.microcol");
    private Model model = Model.make(data.getModel(),
            MissionImpl.getPredefinedGameOverEvaluators());

    private Player buyer() {
        return model.getPlayerStore().getPlayerByName("Dutch");
    }

    private List<Unit> experts() {
        return buyer().getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.MASTER_BLACKSMITH)
                .collect(Collectors.toList());
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private void reload() {
        data.setModel(model.save());
        Path file = directory.resolve("expert.microcol");
        dao.saveToFile(file.toString(), data);
        data = dao.loadFromFile(file.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
    }

    @Test
    void exactFundsCreateOneRealExpertOnPierAndPersistIdentity() {
        buyer().setGold(2200);
        int count = buyer().getAllUnits().size();
        assertTrue(buyer().canBuyUnit(UnitType.MASTER_BLACKSMITH));
        buyer().buy(UnitType.MASTER_BLACKSMITH);
        assertEquals(0, buyer().getGold());
        assertEquals(count + 1, buyer().getAllUnits().size());
        assertEquals(1, experts().size());
        Unit expert = experts().get(0);
        assertTrue(expert instanceof UnitFreeColonist);
        assertTrue(expert.isAtEuropePier());
        assertSame(buyer(), expert.getOwner());
        assertEquals(2.0F,
                expert.getType().getProductionModifier(GoodsType.TOOLS));
        assertEquals(1.0F,
                expert.getType().getProductionModifier(GoodsType.CORN));
        int id = expert.getId();
        reload();
        assertEquals(1, experts().size());
        assertEquals(id, experts().get(0).getId());
        assertTrue(experts().get(0).isAtEuropePier());
        assertEquals(0, buyer().getGold());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 2199 })
    void insufficientFundsLeaveTreasuryRegistryAndNextIdentityUnchanged(
            int gold) {
        buyer().setGold(gold);
        String before = snapshot();
        assertThrows(NotEnoughtGoldException.class,
                () -> buyer().buy(UnitType.MASTER_BLACKSMITH));
        assertEquals(before, snapshot());
    }

    @Test
    void unsupportedOrUnpricedTypeDoesNotChargeOrAllocateAnIdentity() {
        buyer().setGold(10000);
        int nextId = model.getAllUnits().stream().mapToInt(Unit::getId).max()
                .orElseThrow() + 1;
        for (UnitType type : new UnitType[] { UnitType.WAGON,
                UnitType.MAN_OF_WAR }) {
            String before = snapshot();
            assertThrows(IllegalArgumentException.class,
                    () -> buyer().buy(type));
            assertFalse(buyer().canBuyUnit(type));
            assertEquals(before, snapshot());
        }
        buyer().buy(UnitType.COLONIST);
        assertSame(UnitType.COLONIST, model.getUnitById(nextId).getType());
    }

    @Test
    void foreignTurnCannotBuyForTheHumanPlayer() {
        buyer().setGold(2200);
        model.endTurn();
        assertNotSame(buyer(), model.getCurrentPlayer());
        String before = snapshot();
        assertThrows(IllegalStateException.class,
                () -> buyer().buy(UnitType.MASTER_BLACKSMITH));
        assertEquals(before, snapshot());
    }

    @Test
    void independenceClosesSpecialistHiringWithoutMutatingState() {
        data.getModel().getGameManager().getPlayers().stream()
                .filter(p -> p.getName().equals("Dutch")).findFirst()
                .orElseThrow().setDeclaredIndependence(true);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        buyer().setGold(2200);
        String before = snapshot();
        assertThrows(IllegalStateException.class,
                () -> buyer().buy(UnitType.MASTER_BLACKSMITH));
        assertEquals(before, snapshot());
    }

    @Test
    void unstartedGameCannotCreateOrChargeForAnExpert() {
        data.getModel().getGameManager().setGameStarted(false);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        buyer().setGold(2200);
        String before = new Gson().toJson(buyer().save());
        int count = buyer().getAllUnits().size();
        assertThrows(IllegalStateException.class,
                () -> buyer().buy(UnitType.MASTER_BLACKSMITH));
        assertEquals(before, new Gson().toJson(buyer().save()));
        assertEquals(count, buyer().getAllUnits().size());
        assertFalse(model.isGameRunning());
    }

    @Test
    void removedBuyerCannotUseItsOldPlayerReference() {
        Player removed = buyer();
        removed.setGold(2200);
        model.getPlayerStore().getPlayers().remove(removed);
        int count = removed.getAllUnits().size();
        assertThrows(IllegalStateException.class,
                () -> removed.buy(UnitType.MASTER_BLACKSMITH));
        assertEquals(2200, removed.getGold());
        assertEquals(count, removed.getAllUnits().size());
    }

    @Test
    void independentPurchasesCreateDistinctExpertsAndKeepOtherPierColonists() {
        buyer().setGold(4400);
        List<Integer> previous = buyer().getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toList());
        buyer().buy(UnitType.MASTER_BLACKSMITH);
        buyer().buy(UnitType.MASTER_BLACKSMITH);
        assertEquals(0, buyer().getGold());
        assertEquals(2, experts().size());
        assertNotEquals(experts().get(0).getId(), experts().get(1).getId());
        for (int id : previous) {
            assertNotNull(model.getUnitById(id));
        }
    }

    @Test
    void serializedEquipmentDoesNotReplaceTheSpecialty() {
        buyer().setGold(2200);
        buyer().buy(UnitType.MASTER_BLACKSMITH);
        int id = experts().get(0).getId();
        data.setModel(model.save());
        UnitPo saved = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
        saved.setMounted(true);
        saved.setHoldingGuns(true);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        reload();
        UnitFreeColonist expert = (UnitFreeColonist) model.getUnitById(id);
        assertSame(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertTrue(expert.isMounted());
        assertTrue(expert.isHoldingGuns());
        assertEquals(2.0F,
                expert.getType().getProductionModifier(GoodsType.TOOLS));
    }

    @Test
    void equipmentAndExpertiseSurviveCargoLandingAndSmithyAssignment() {
        buyer().setGold(20000);
        buyer().buy(UnitType.MASTER_BLACKSMITH);
        buyer().buy(UnitType.GALLEON);
        UnitFreeColonist expert = (UnitFreeColonist) experts().get(0);
        int expertId = expert.getId();
        org.microcol.model.unit.UnitWithCargo carrier = (org.microcol.model.unit.UnitWithCargo) buyer()
                .getAllUnits().stream()
                .filter(unit -> unit.isShip() && unit.isAtEuropePort())
                .findFirst().orElseThrow();
        int carrierId = carrier.getId();
        expert.tradeEquipmentInEurope(GoodsType.TOOLS, true,
                expert.getEuropeEquipmentQuote(GoodsType.TOOLS, true));
        carrier.getCargo().getSlotByIndex(0).store(expert);
        reload();
        expert = (UnitFreeColonist) model.getUnitById(expertId);
        assertSame(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertEquals(100, expert.getTools());
        assertEquals(carrierId,
                expert.getPlaceCargoSlot().getOwnerUnit().getId());

        // Prepare a completed voyage at the colony; the full trip is a UI
        // scenario.
        data.setModel(model.save());
        UnitPo savedCarrier = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == carrierId).findFirst()
                .orElseThrow();
        savedCarrier.setPlaceEuropePort(null);
        org.microcol.model.store.PlaceMapPo arrival = new org.microcol.model.store.PlaceMapPo();
        Location colonyAt = Location.of(25, 14);
        arrival.setLocation(colonyAt);
        arrival.setOrientation(Direction.west);
        savedCarrier.setPlaceMap(arrival);
        org.microcol.model.store.ColonyPo arrivalColony = data.getModel()
                .getColonies().stream()
                .filter(colony -> colony.getLocation().equals(colonyAt))
                .findFirst().orElseThrow();
        java.util.Map<String, Integer> warehouse = new java.util.HashMap<>(
                arrivalColony.getColonyWarehouse());
        warehouse.put("TOOLS", 0);
        arrivalColony.setColonyWarehouse(warehouse);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        expert = (UnitFreeColonist) model.getUnitById(expertId);
        expert.disembarkToLocation(colonyAt);
        assertTrue(expert.isAtPlaceLocation());
        assertEquals(colonyAt, expert.getLocation());
        assertEquals(100, expert.getTools());
        reload();
        expert = (UnitFreeColonist) model.getUnitById(expertId);
        Colony colony = model.getColonyAt(colonyAt).orElseThrow();
        ConstructionSlot work = colony.getConstructions().stream()
                .filter(construction -> construction.getType()
                        .getProductionPerTurn()
                        .map(goods -> goods.getType() == GoodsType.TOOLS)
                        .orElse(false))
                .flatMap(construction -> construction.getConstructionSlots()
                        .stream())
                .filter(ConstructionSlot::isEmpty).findFirst().orElseThrow();
        expert.placeToColonyStructureSlot(work);
        assertEquals(2.0F, work.getProductionModifier());
        assertEquals(0, expert.getTools());
        assertSame(UnitType.MASTER_BLACKSMITH, expert.getType());
        reload();
        expert = (UnitFreeColonist) model.getUnitById(expertId);
        assertSame(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertFalse(expert.isAtPlaceLocation());
        assertEquals(0, expert.getTools());
    }

    @Test
    void ordinaryColonistAndShipsRemainPurchasableAtTheirExistingPrices() {
        buyer().setGold(20000);
        int expected = 20000;
        int count = buyer().getAllUnits().size();
        for (UnitType type : new UnitType[] { UnitType.COLONIST,
                UnitType.FRIGATE, UnitType.GALLEON }) {
            buyer().buy(type);
            expected -= type.getEuropePrice();
            assertEquals(expected, buyer().getGold());
        }
        assertEquals(count + 3, buyer().getAllUnits().size());
    }
}
