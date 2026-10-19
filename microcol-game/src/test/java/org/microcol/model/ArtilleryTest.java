package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.UnitActionFortifyPo;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.event.UnitAttackedEvent;

import com.google.gson.Gson;

class ArtilleryTest {
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location TARGET = Location.of(17, 11);

    private ModelPo scenario(boolean damaged) {
        ModelPo po = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        po.setCombatRandomState(3L);
        java.util.Map<Location, TerrainType> terrain = po.getMap()
                .getTerrainMap();
        terrain.put(Location.of(44, 15), TerrainType.GRASSLAND);
        po.getMap().setTerrainType(terrain);
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("Dutch");
        UnitPo defender = po.getUnits().stream()
                .filter(unit -> unit.getId() == 6).findFirst().orElseThrow();
        defender.setType(UnitType.ARTILLERY);
        defender.setWorkUpdates(null);
        defender.setMissionary(null);
        defender.setWorkSkill(null);
        defender.setArtilleryDamaged(damaged);
        defender.setHoldingGuns(false);
        defender.setMounted(false);
        defender.setTools(0);
        defender.getPlaceMap().setLocation(TARGET);
        defender.setAvailableMoves(1);
        po.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        return po;
    }

    private Model model(boolean damaged) {
        return model(damaged, 3L);
    }

    private Model model(boolean damaged, long randomState) {
        final ModelPo data = scenario(damaged);
        data.setCombatRandomState(randomState);
        Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        UnitFreeColonist attacker = (UnitFreeColonist) model.getUnitById(0);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    @Test
    void artilleryCanTargetWagonButNotShipsAndWagonCannotAttack() {
        assertTrue(UnitType.ARTILLERY.getAttackableUnitType()
                .contains(UnitType.WAGON));
        assertFalse(UnitType.ARTILLERY.getAttackableUnitType()
                .contains(UnitType.CARAVEL));
        assertFalse(UnitType.WAGON.canAttack());
    }

    @Test
    void fullDamageSurvivesAndSecondLossDestroys() {
        Model first = model(false);
        UnitArtillery gun = (UnitArtillery) first.getUnitById(6);
        UnitFreeColonist attacker = (UnitFreeColonist) first.getUnitById(0);
        CombatStrengths quote = attacker.prepareAttack(TARGET, true)
                .orElseThrow().getCombatStrengths().orElseThrow();
        assertEquals(5, quote.getDefender().getBase());
        assertEquals(-75, quote.getDefender().getArtilleryBonus());
        attacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertSame(gun, first.getUnitById(6));
        assertTrue(gun.isDamaged());
        assertEquals(TARGET, gun.getLocation());
        assertEquals(0, gun.getActionPoints());
        assertEquals(0, attacker.getActionPoints());
        Model continued = Model.make(first.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(((UnitArtillery) continued.getUnitById(6)).isDamaged());

        Model second = model(true);
        UnitFreeColonist secondAttacker = (UnitFreeColonist) second
                .getUnitById(0);
        assertEquals(3, secondAttacker.prepareAttack(TARGET, true).orElseThrow()
                .getCombatStrengths().orElseThrow().getDefender().getBase());
        secondAttacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertTrue(
                second.getAllUnits().stream().noneMatch(u -> u.getId() == 6));
    }

    @Test
    void armedWinnerAgainstFullOrDamagedArtilleryCanPromote() throws Exception {
        for (final boolean alreadyDamaged : new boolean[] { false, true }) {
            final Model battle = model(alreadyDamaged, 7L);
            final UnitFreeColonist soldier = (UnitFreeColonist) battle
                    .getUnitById(0);
            final java.util.List<UnitAttackedEvent> events = new java.util.ArrayList<>();
            battle.addListener(new ModelListenerAdapter() {
                @Override
                public void onUnitAttacked(final UnitAttackedEvent event) {
                    events.add(event);
                }
            });
            soldier.prepareAttack(TARGET, true).orElseThrow().execute();
            assertSame(soldier, battle.getUnitById(0));
            assertTrue(soldier.isVeteran());
            assertTrue(soldier.isHoldingGuns());
            assertEquals(0, soldier.getActionPoints());
            assertEquals(7L + 2 * 0x9E3779B97F4A7C15L,
                    battle.save().getCombatRandomState());
            assertEquals(1, events.size());
            assertEquals(0,
                    events.get(0).getPromotion().orElseThrow().getUnitId());
            assertEquals(alreadyDamaged, events.get(0).wasDestroyed());
            if (alreadyDamaged) {
                assertTrue(battle.getAllUnits().stream()
                        .noneMatch(unit -> unit.getId() == 6));
            } else {
                assertTrue(((UnitArtillery) battle.getUnitById(6)).isDamaged());
            }
            assertTrue(
                    ((UnitFreeColonist) Model
                            .make(battle.save(),
                                    MissionImpl
                                            .getPredefinedGameOverEvaluators())
                            .getUnitById(0)).isVeteran());
        }
    }

    @Test
    void invalidSavedDamageAndStaleQuoteRejectWithoutMutation() {
        ModelPo missing = scenario(false);
        missing.getUnits().stream().filter(u -> u.getId() == 6).findFirst()
                .orElseThrow().setArtilleryDamaged(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(missing,
                MissionImpl.getPredefinedGameOverEvaluators()));
        ModelPo extra = scenario(false);
        extra.getUnits().stream().filter(u -> u.getId() == 0).findFirst()
                .orElseThrow().setArtilleryDamaged(false);
        assertThrows(IllegalArgumentException.class, () -> Model.make(extra,
                MissionImpl.getPredefinedGameOverEvaluators()));
        ModelPo carrying = scenario(false);
        UnitPo invalidGun = carrying.getUnits().stream()
                .filter(u -> u.getId() == 6).findFirst().orElseThrow();
        CargoSlotPo goods = new CargoSlotPo();
        goods.setGoodsType(GoodsType.ORE);
        goods.setAmount(1);
        invalidGun.getCargo().getSlots().add(goods);
        assertThrows(IllegalArgumentException.class, () -> Model.make(carrying,
                MissionImpl.getPredefinedGameOverEvaluators()));

        Model model = model(false);
        UnitFreeColonist attacker = (UnitFreeColonist) model.getUnitById(0);
        AttackOrder order = attacker.prepareAttack(TARGET, true).orElseThrow();
        ((UnitArtillery) model.getUnitById(6))
                .placeToLocation(Location.of(17, 10));
        String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void artilleryPurchasePriceAndTransportType() {
        Model model = model(false);
        Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(2_000);
        assertEquals(500, buyer.getUnitPurchasePrice(UnitType.ARTILLERY));
        buyer.buy(UnitType.ARTILLERY, 500);
        assertEquals(550, buyer.getUnitPurchasePrice(UnitType.ARTILLERY));
        assertTrue(UnitType.ARTILLERY.isStorable());
        assertEquals(0, UnitType.ARTILLERY.getCargoCapacity());
        assertThrows(IllegalStateException.class,
                () -> buyer.buy(UnitType.ARTILLERY, 500));
        assertEquals(550,
                Model.make(model.save(),
                        MissionImpl.getPredefinedGameOverEvaluators())
                        .getPlayerByName("Dutch")
                        .getUnitPurchasePrice(UnitType.ARTILLERY));
    }

    @Test
    void artilleryCanFortifyAndOccupiesOneShipHold() {
        Model model = model(false);
        Player dutch = model.getPlayerByName("Dutch");
        dutch.setGold(2_000);
        dutch.buy(UnitType.ARTILLERY, 500);
        UnitArtillery gun = (UnitArtillery) model.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.ARTILLERY
                        && unit.getOwner() == dutch)
                .findFirst().orElseThrow();
        gun.placeToLocation(ORIGIN);
        assertTrue(model.canFortify(gun));
        model.fortify(gun);
        assertEquals(0, gun.getActionPoints());
        assertEquals(UnitActionType.fortify, gun.getAction().getType());
        assertFalse(model.isFortified(gun));
        Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        UnitArtillery waiting = (UnitArtillery) loaded.getUnitById(gun.getId());
        assertEquals(UnitActionType.fortify, waiting.getAction().getType());
        loaded.cancelFortify(waiting);
        assertEquals(0, waiting.getActionPoints());

        Model cargoModel = model(false);
        Player owner = cargoModel.getPlayerByName("Dutch");
        owner.setGold(2_000);
        owner.buy(UnitType.ARTILLERY, 500);
        UnitArtillery passenger = (UnitArtillery) cargoModel.getAllUnits()
                .stream().filter(unit -> unit.getType() == UnitType.ARTILLERY
                        && unit.getOwner() == owner)
                .findFirst().orElseThrow();
        Ship ship = (Ship) cargoModel.getUnitById(3);
        Location water = ship.getLocation();
        Location shore = water.getNeighbors().stream()
                .filter(at -> cargoModel.getMap().isValid(at)
                        && cargoModel.getMap().getTerrainTypeAt(at).isLand())
                .findFirst().orElseThrow();
        passenger.placeToLocation(shore);
        CargoSlot slot = ship.getCargo().getEmptyCargoSlot().orElseThrow();
        int occupied = (int) ship.getCargo().getSlots().stream()
                .filter(s -> !s.isEmpty()).count();
        assertTrue(passenger.canEmbark(slot));
        passenger.embarkFromLocation(slot);
        assertEquals(occupied + 1, ship.getCargo().getSlots().stream()
                .filter(s -> !s.isEmpty()).count());
        assertEquals(passenger.getId(), slot.getUnit().orElseThrow().getId());
        Model restored = Model.make(cargoModel.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.getUnitById(passenger.getId()).isAtCargoSlot());
    }

    @Test
    void armoryTierIsRequiredAtAdmissionAndCompletion() {
        ModelPo data = new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol")
                .getModel();
        Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        Colony colony = model.getColonyByName("Delft").orElseThrow();
        ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        colony.getConstructions().removeIf(
                building -> building.getType() == ConstructionType.ARMORY
                        || building.getType() == ConstructionType.MAGAZINE
                        || building.getType() == ConstructionType.ARSENAL);
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_ARMORY, queue
                .getUnitUnavailableReason(UnitType.ARTILLERY).orElseThrow());
        String before = new Gson().toJson(model.save());
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(UnitType.ARTILLERY));
        assertEquals(before, new Gson().toJson(model.save()));

        colony.getConstructions().add(
                Construction.build(model, colony, ConstructionType.ARMORY));
        assertTrue(
                queue.getUnitUnavailableReason(UnitType.ARTILLERY).isEmpty());
        queue.addAtEnd(UnitType.ARTILLERY);
        colony.getConstructions().removeIf(
                building -> building.getType() == ConstructionType.ARMORY);
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_ARMORY,
                queue.getCompletionUnavailableReason().orElseThrow());
        before = new Gson().toJson(model.save());
        assertTrue(queue.getRushQuote().isEmpty());
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void artilleryAttacksAtFullAndDamagedBasesWithOutdoorPenalty() {
        for (boolean damaged : new boolean[] { false, true }) {
            ModelPo data = scenario(false);
            UnitPo attacking = data.getUnits().stream()
                    .filter(unit -> unit.getId() == 0).findFirst()
                    .orElseThrow();
            UnitPo defending = data.getUnits().stream()
                    .filter(unit -> unit.getId() == 6).findFirst()
                    .orElseThrow();
            attacking.setType(UnitType.ARTILLERY);
            attacking.setWorkUpdates(null);
            attacking.setMissionary(null);
            attacking.setWorkSkill(null);
            attacking.setArtilleryDamaged(damaged);
            attacking.setHoldingGuns(false);
            defending.setType(UnitType.COLONIST);
            defending.setWorkUpdates(0);
            defending.setMissionary(false);
            defending.setArtilleryDamaged(null);
            defending.setHoldingGuns(true);
            Model battle = Model.make(data,
                    MissionImpl.getPredefinedGameOverEvaluators());
            UnitArtillery gun = (UnitArtillery) battle.getUnitById(0);
            gun.placeToLocation(ORIGIN);
            gun.getOwner().revealMapForUnit(gun);
            CombatStrengths strengths = gun.prepareAttack(TARGET, true)
                    .orElseThrow().getCombatStrengths().orElseThrow();
            assertEquals(damaged ? 5 : 7, strengths.getAttacker().getBase());
            assertEquals(-75, strengths.getAttacker().getArtilleryBonus());
            assertEquals(50, strengths.getAttacker().getAttackBonus());
            assertEquals((damaged ? 5 : 7) * 0.75,
                    strengths.getAttacker().getTotal());
            assertEquals(0, strengths.getDefender().getArtilleryBonus());
        }
    }

    @Test
    void fullArtillerySurvivesLegacyCivilianFightLoss() throws Exception {
        ModelPo data = scenario(false);
        UnitPo attacking = data.getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        UnitPo defending = data.getUnits().stream()
                .filter(unit -> unit.getId() == 6).findFirst().orElseThrow();
        attacking.setType(UnitType.ARTILLERY);
        attacking.setWorkUpdates(null);
        attacking.setMissionary(null);
        attacking.setWorkSkill(null);
        attacking.setArtilleryDamaged(false);
        attacking.setHoldingGuns(false);
        defending.setType(UnitType.COLONIST);
        defending.setWorkUpdates(0);
        defending.setMissionary(false);
        defending.setArtilleryDamaged(null);
        defending.setHoldingGuns(false);
        Model battle = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        UnitArtillery gun = (UnitArtillery) battle.getUnitById(0);
        gun.placeToLocation(ORIGIN);
        gun.getOwner().revealMapForUnit(gun);
        java.lang.reflect.Field random = AbstractUnit.class
                .getDeclaredField("random");
        random.setAccessible(true);
        ((java.util.Random) random.get(gun)).setSeed(0L);
        assertTrue(gun.prepareAttack(TARGET, true).orElseThrow()
                .getCombatStrengths().isEmpty());
        gun.prepareAttack(TARGET, true).orElseThrow().execute();
        assertSame(gun, battle.getUnitById(0));
        assertTrue(gun.isDamaged());
        assertEquals(ORIGIN, gun.getLocation());
        assertEquals(0, gun.getActionPoints());
    }

    @Test
    void personalFortifyDoesNotRemoveOutdoorPenalty() {
        ModelPo data = scenario(false);
        UnitPo gun = data.getUnits().stream().filter(u -> u.getId() == 6)
                .findFirst().orElseThrow();
        UnitActionFortifyPo action = new UnitActionFortifyPo();
        action.setFortified(true);
        gun.setAction(action);
        Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        UnitFreeColonist attacker = (UnitFreeColonist) model.getUnitById(0);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        CombatStrength defense = attacker.prepareAttack(TARGET, true)
                .orElseThrow().getCombatStrengths().orElseThrow().getDefender();
        assertEquals(50, defense.getFortificationBonus());
        assertEquals(-75, defense.getArtilleryBonus());
    }

    @Test
    void newColonyProtectionInvalidatesFrozenArtilleryQuote() {
        ModelPo data = scenario(false);
        data.getUnits().stream().filter(u -> u.getId() == 6).findFirst()
                .orElseThrow().getPlaceMap().setLocation(Location.of(17, 10));
        Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        UnitFreeColonist attacker = (UnitFreeColonist) model.getUnitById(0);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        AttackOrder offer = attacker.prepareAttack(Location.of(17, 10), true)
                .orElseThrow();
        assertEquals(-75, offer.getCombatStrengths().orElseThrow().getDefender()
                .getArtilleryBonus());
        Colony colony = model.getColonyByName("Quebec").orElseThrow();
        colony.getConstructions().add(
                Construction.build(model, colony, ConstructionType.STOCKADE));
        String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, offer::execute);
        assertEquals(before, new Gson().toJson(model.save()));
        assertEquals(3L, model.save().getCombatRandomState());
    }

}
