package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

/** Controlled ship encounters use the shared saved combat random stream. */
class NavalCombatTest {
    private static final Location ATTACKER = Location.of(45, 15);
    private static final Location DEFENDER = Location.of(44, 15);
    private static final long STEP = 0x9E3779B97F4A7C15L;

    private Model game(final long state, final UnitType attackerType,
            final UnitType defenderType) {
        return game(state, attackerType, defenderType, false, false);
    }

    private Model game(final long state, final UnitType attackerType,
            final UnitType defenderType, final boolean passenger) {
        return game(state, attackerType, defenderType, passenger, false);
    }

    private Model game(final long state, final UnitType attackerType,
            final UnitType defenderType, final boolean passenger,
            final boolean franceIndependent) {
        return game(state, attackerType, defenderType, passenger,
                franceIndependent, null);
    }

    private Model game(final long state, final UnitType attackerType,
            final UnitType defenderType, final boolean passenger,
            final boolean franceIndependent, final UnitType secondDefender) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        data.setCombatRandomState(state);
        data.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow().setDeclaredIndependence(franceIndependent);
        final UnitPo original = data.getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        original.setType(attackerType);
        original.setAvailableMoves(attackerType.getSpeed());
        original.getCargo().getSlots().clear();
        final UnitPo originalPassenger = data.getUnits().stream()
                .filter(unit -> unit.getId() == 0).findFirst().orElseThrow();
        data.getUnits().removeIf(unit -> unit.getId() <= 2);
        for (int i = 0; i < attackerType.getCargoCapacity(); i++) {
            original.getCargo().getSlots().add(new CargoSlotPo());
        }
        final UnitPo enemy = new UnitPo();
        enemy.setId(100);
        enemy.setType(defenderType);
        enemy.setOwnerId("France");
        enemy.setAvailableMoves(defenderType.getSpeed());
        enemy.setAction(new UnitActionNoAction().save());
        final PlaceMapPo at = new PlaceMapPo();
        at.setLocation(DEFENDER);
        at.setOrientation(Direction.east);
        enemy.setPlaceMap(at);
        final CargoPo cargo = new CargoPo();
        for (int i = 0; i < defenderType.getCargoCapacity(); i++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        enemy.setCargo(cargo);
        data.getUnits().add(enemy);
        if (secondDefender != null) {
            final UnitPo second = new UnitPo();
            second.setId(102);
            second.setType(secondDefender);
            second.setOwnerId("France");
            second.setAvailableMoves(secondDefender.getSpeed());
            second.setAction(new UnitActionNoAction().save());
            final PlaceMapPo secondPlace = new PlaceMapPo();
            secondPlace.setLocation(DEFENDER);
            secondPlace.setOrientation(Direction.east);
            second.setPlaceMap(secondPlace);
            final CargoPo secondCargo = new CargoPo();
            for (int i = 0; i < secondDefender.getCargoCapacity(); i++) {
                secondCargo.getSlots().add(new CargoSlotPo());
            }
            second.setCargo(secondCargo);
            data.getUnits().add(second);
        }
        if (passenger) {
            originalPassenger.setId(101);
            originalPassenger.setOwnerId("France");
            originalPassenger.setPlaceCargoSlot(new PlaceCargoSlotPo());
            cargo.getSlots().get(0).setUnitId(101);
            data.getUnits().add(originalPassenger);
        }
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        ((Ship) model.getUnitById(3)).getOwner()
                .revealMapForUnit(model.getUnitById(3));
        return model;
    }

    @Test
    void allSixBaseStrengthsUseOnlyAttackerBonus() {
        final UnitType[] types = { UnitType.CARAVEL, UnitType.MERCHANTMAN,
                UnitType.GALLEON, UnitType.PRIVATEER, UnitType.FRIGATE,
                UnitType.MAN_OF_WAR };
        final int[] bases = { 2, 6, 10, 8, 16, 24 };
        for (int i = 0; i < types.length; i++) {
            final Model model = game(3L, types[i], types[i]);
            final CombatStrengths value = CombatStrengths.forBattle(model,
                    model.getUnitById(3), model.getUnitById(100)).orElseThrow();
            assertEquals(bases[i] * 1.5, value.getAttacker().getTotal());
            assertEquals(bases[i], value.getDefender().getTotal());
            assertEquals(0, value.getDefender().getTerrainBonus());
            assertEquals(0, value.getDefender().getColonyBonus());
        }
    }

    @Test
    void winnerAndSinkDrawsAdvanceExactlyTwiceWithImmutableEvent() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        final AttackOrder order = attacker.prepareAttack(DEFENDER, true)
                .orElseThrow();
        assertTrue(order.getNavalPreview().isPresent());
        assertEquals(3L, model.save().getCombatRandomState());
        order.execute();
        assertEquals(3L + 2 * STEP, model.save().getCombatRandomState());
        assertEquals(0, attacker.getActionPoints());
        assertEquals(0, defender.getActionPoints());
        assertEquals(1, events.size());
        assertEquals(ATTACKER, events.get(0).getNavalOutcome().orElseThrow()
                .getAttackerLocation());
        assertEquals(DEFENDER, events.get(0).getNavalOutcome().orElseThrow()
                .getDefenderLocation());
        assertThrows(IllegalStateException.class, order::execute);
    }

    @Test
    void lootUsesMarketValueAndDamagedShipKeepsLeftovers() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        attacker.getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 90));
        defender.getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 20));
        defender.getCargo().getSlotByIndex(1)
                .addGoods(Goods.of(GoodsType.SILVER, 100));
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        final AttackOrder order = attacker.prepareAttack(DEFENDER, true)
                .orElseThrow();
        assertTrue(model.getEurope().getGoodsTradeForType(GoodsType.ORE)
                .getSellPrice() > model.getEurope()
                        .getGoodsTradeForType(GoodsType.SILVER).getSellPrice());
        assertEquals(List.of(Goods.of(GoodsType.ORE, 10),
                Goods.of(GoodsType.ORE, 10), Goods.of(GoodsType.SILVER, 100)),
                order.getNavalPreview().orElseThrow()
                        .getPotentialLootIfAttackerWins());
        order.execute();
        final NavalCombatOutcome outcome = events.get(0).getNavalOutcome()
                .orElseThrow();
        assertEquals(attacker.getId(), outcome.getWinnerId());
        assertFalse(outcome.isSunk());
        assertEquals(0, outcome.getLostPassengers());
        assertTrue(outcome.getLostGoods().isEmpty());
        assertEquals(110, attacker.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedGood)
                .filter(slot -> slot.getGoods().orElseThrow()
                        .getType() == GoodsType.ORE)
                .mapToInt(slot -> slot.getGoods().orElseThrow().getAmount())
                .sum());
    }

    @Test
    void changedCargoRejectsConfirmationBeforeRandomOrMovement() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        final AttackOrder order = attacker.prepareAttack(DEFENDER, true)
                .orElseThrow();
        defender.getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 1));
        final long random = model.save().getCombatRandomState();
        final int attackerMoves = attacker.getActionPoints();
        final int defenderMoves = defender.getActionPoints();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(random, model.save().getCombatRandomState());
        assertEquals(attackerMoves, attacker.getActionPoints());
        assertEquals(defenderMoves, defender.getActionPoints());
        assertEquals(Goods.of(GoodsType.ORE, 1),
                defender.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
    }

    @Test
    void sinkingLosesUncapturedGoodsAndKeepsNoRemainingCargo() {
        final Model model = game(7L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        for (final CargoSlot slot : attacker.getCargo().getSlots()) {
            slot.addGoods(Goods.of(GoodsType.ORE, 100));
        }
        defender.getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.SILVER, 50));
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        attacker.prepareAttack(DEFENDER, true).orElseThrow().execute();
        final NavalCombatOutcome outcome = events.get(0).getNavalOutcome()
                .orElseThrow();
        assertEquals(attacker.getId(), outcome.getWinnerId());
        assertTrue(outcome.isSunk());
        assertTrue(outcome.getCapturedGoods().isEmpty());
        assertTrue(outcome.getRemainingGoods().isEmpty());
        assertEquals(List.of(Goods.of(GoodsType.SILVER, 50)),
                outcome.getLostGoods());
        assertFalse(model.getAllUnits().contains(defender));
    }

    @Test
    void defenderCanWinAndSinkAttacker() {
        final Model model = game(0L, UnitType.FRIGATE, UnitType.MAN_OF_WAR);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        attacker.prepareAttack(DEFENDER, true).orElseThrow().execute();
        final NavalCombatOutcome outcome = events.get(0).getNavalOutcome()
                .orElseThrow();
        assertEquals(defender.getId(), outcome.getWinnerId());
        assertTrue(outcome.isSunk());
        assertFalse(model.getAllUnits().contains(attacker));
        assertEquals(DEFENDER, defender.getLocation());
    }

    @Test
    void damagedLoserPreservesPassengerAndRoundTripDoesNotReplayBattle() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON, true);
        final Ship attacker = (Ship) model.getUnitById(3);
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        attacker.prepareAttack(DEFENDER, true).orElseThrow().execute();
        final NavalCombatOutcome outcome = events.get(0).getNavalOutcome()
                .orElseThrow();
        assertFalse(outcome.isSunk());
        assertEquals(1, outcome.getPreservedPassengers());
        assertEquals(0, outcome.getLostPassengers());
        assertTrue(((Ship) model.getUnitById(100)).isDamaged());
        assertTrue(model.getUnitById(101).isAtCargoSlot());
        final long random = model.save().getCombatRandomState();
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(random, restored.save().getCombatRandomState());
        assertTrue(((Ship) restored.getUnitById(100)).isDamaged());
        assertTrue(restored.getUnitById(101).isAtCargoSlot());
    }

    @Test
    void noRepairEndpointSinksEvenWhenSecondDrawWouldDamage() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON, true,
                true);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        assertTrue(model.planShipRepair(defender).isEmpty());
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        attacker.prepareAttack(DEFENDER, true).orElseThrow().execute();
        final NavalCombatOutcome outcome = events.get(0).getNavalOutcome()
                .orElseThrow();
        assertTrue(outcome.isSunk());
        assertEquals(1, outcome.getLostPassengers());
        assertEquals(0, outcome.getPreservedPassengers());
        assertFalse(model.getAllUnits().contains(defender));
        assertTrue(model.getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == 101));
    }

    @Test
    void strongestDefenderWinsSelectionBeforeStableIdTie() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.CARAVEL, false,
                false, UnitType.MAN_OF_WAR);
        final Ship attacker = (Ship) model.getUnitById(3);
        assertEquals(102, attacker.prepareAttack(DEFENDER, true).orElseThrow()
                .getDefender().getId());

        final Model sameStrength = game(3L, UnitType.FRIGATE, UnitType.CARAVEL,
                false, false, UnitType.CARAVEL);
        assertEquals(100,
                sameStrength.getUnitById(3).prepareAttack(DEFENDER, true)
                        .orElseThrow().getDefender().getId());
    }

    @Test
    void changedMarketPriceRejectsWithoutRandomMovementDiplomacyOrCargo() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        defender.getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 12));
        final AttackOrder order = attacker.prepareAttack(DEFENDER, true)
                .orElseThrow();
        final GoodsTrade ore = model.getEurope()
                .getGoodsTradeForType(GoodsType.ORE);
        ore.setSellPrice(ore.getSellPrice() + 1);
        assertRejectedWithoutBattleEffects(model, attacker, defender, order);
    }

    @Test
    void changedRepairDurationRejectsWithoutRandomMovementDiplomacyOrCargo() {
        final Model model = game(3L, UnitType.FRIGATE, UnitType.GALLEON);
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship defender = (Ship) model.getUnitById(100);
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("France"); i++) {
            model.endTurn();
        }
        assertEquals("France", model.getCurrentPlayer().getName());
        final Unit founder = model.getUnitById(6);
        founder.placeToLocation(Location.of(26, 15));
        final Colony port = model.buildColony(founder.getOwner(), founder,
                "New Port");
        port.createConstruction(ConstructionType.DOCK);
        port.createConstruction(ConstructionType.DRYDOCK);
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        final AttackOrder order = attacker.prepareAttack(DEFENDER, true)
                .orElseThrow();
        assertEquals(3,
                order.getNavalPreview().orElseThrow()
                        .getDefenderRepairDestination().orElseThrow()
                        .getRepairDuration());
        port.createConstruction(ConstructionType.SHIPYARD);
        assertEquals(2, model.planShipRepair(defender).orElseThrow()
                .getRepairDuration());
        assertRejectedWithoutBattleEffects(model, attacker, defender, order);
    }

    private void assertRejectedWithoutBattleEffects(final Model model,
            final Ship attacker, final Ship defender, final AttackOrder order) {
        final long random = model.save().getCombatRandomState();
        final int attackerMoves = attacker.getActionPoints();
        final int defenderMoves = defender.getActionPoints();
        final EuropeanRelationState relation = model
                .getEuropeanRelation(attacker.getOwner(), defender.getOwner());
        final List<String> cargo = List.of(
                attacker.getCargo().getSlots().toString(),
                defender.getCargo().getSlots().toString());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(random, model.save().getCombatRandomState());
        assertEquals(attackerMoves, attacker.getActionPoints());
        assertEquals(defenderMoves, defender.getActionPoints());
        assertEquals(relation, model.getEuropeanRelation(attacker.getOwner(),
                defender.getOwner()));
        assertEquals(cargo, List.of(attacker.getCargo().getSlots().toString(),
                defender.getCargo().getSlots().toString()));
        assertSame(defender, model.getUnitById(100));
    }
}
