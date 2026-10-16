package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.gson.Gson;

/** Privateers permit naval fighting without changing European relations. */
class PrivateerExceptionTest {
    private static final Location TARGET = Location.of(44, 15);

    private Model game(final UnitType attackerType, final UnitType targetType,
            final String targetOwner) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        data.setCombatRandomState(3L);
        final UnitPo attacker = data.getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        attacker.setType(attackerType);
        attacker.setAvailableMoves(attackerType.getSpeed());
        attacker.getCargo().getSlots().clear();
        data.getUnits().removeIf(unit -> unit.getId() <= 2);
        for (int i = 0; i < attackerType.getCargoCapacity(); i++) {
            attacker.getCargo().getSlots().add(new CargoSlotPo());
        }
        final UnitPo target = new UnitPo();
        target.setId(100);
        target.setType(targetType);
        target.setOwnerId(targetOwner);
        target.setAvailableMoves(targetType.getSpeed());
        target.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(TARGET);
        place.setOrientation(Direction.east);
        target.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int i = 0; i < targetType.getCargoCapacity(); i++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        target.setCargo(cargo);
        data.getUnits().add(target);
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        model.getUnitById(3).getOwner().revealMapForUnit(model.getUnitById(3));
        return model;
    }

    @Test
    void privateerAttackKeepsPeaceAndSurvivesReload() {
        final Model model = game(UnitType.PRIVATEER, UnitType.FRIGATE,
                "France");
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship target = (Ship) model.getUnitById(100);
        final Player dutch = attacker.getOwner();
        final Player french = target.getOwner();
        assertFalse(model.areHostile(dutch, french));
        assertTrue(model.isPrivateerNavalException(attacker, target));
        assertTrue(attacker.isPossibleToAttackAt(TARGET));
        final String before = new Gson().toJson(model.save());
        final AttackOrder order = attacker.prepareAttack(TARGET, false)
                .orElseThrow();
        assertTrue(order.isPrivateerException());
        assertFalse(order.isDeclarationRequired());
        assertEquals(before, new Gson().toJson(model.save()));
        order.execute();
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, french));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(EuropeanRelationState.PEACE,
                restored.getEuropeanRelation(restored.getUnitById(3).getOwner(),
                        restored.getUnitById(100).getOwner()));
        assertEquals(model.save().getCombatRandomState(),
                restored.save().getCombatRandomState());
    }

    @Test
    void armedShipCanCounterattackPrivateerWithoutWar() {
        final Model model = game(UnitType.FRIGATE, UnitType.PRIVATEER,
                "France");
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship target = (Ship) model.getUnitById(100);
        final AttackOrder order = attacker.prepareAttack(TARGET, false)
                .orElseThrow();
        assertTrue(order.isPrivateerException());
        assertFalse(order.isDeclarationRequired());
        order.execute();
        assertEquals(EuropeanRelationState.PEACE, model
                .getEuropeanRelation(attacker.getOwner(), target.getOwner()));
    }

    @Test
    void existingWarIsPreservedAndOrdinaryFrigateStillDeclares() {
        final Model atWar = game(UnitType.PRIVATEER, UnitType.FRIGATE,
                "France");
        final Ship privateer = (Ship) atWar.getUnitById(3);
        final Ship rival = (Ship) atWar.getUnitById(100);
        atWar.declareHostilities(privateer.getOwner(), rival.getOwner());
        final AttackOrder wartime = privateer.prepareAttack(TARGET, false)
                .orElseThrow();
        assertTrue(wartime.isPrivateerException());
        wartime.execute();
        assertEquals(EuropeanRelationState.WAR, atWar
                .getEuropeanRelation(privateer.getOwner(), rival.getOwner()));

        final Model ordinary = game(UnitType.FRIGATE, UnitType.GALLEON,
                "France");
        final Ship frigate = (Ship) ordinary.getUnitById(3);
        assertFalse(frigate.isPossibleToAttackAt(TARGET));
        final AttackOrder declaration = frigate.prepareAttack(TARGET, true)
                .orElseThrow();
        assertFalse(declaration.isPrivateerException());
        assertTrue(declaration.isDeclarationRequired());
        declaration.execute();
        assertEquals(EuropeanRelationState.WAR, ordinary.getEuropeanRelation(
                frigate.getOwner(), ordinary.getUnitById(100).getOwner()));
    }

    @Test
    void stalePeaceToWarRejectsBeforeDrawMovementOrCargoChange() {
        final Model model = game(UnitType.PRIVATEER, UnitType.FRIGATE,
                "France");
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship target = (Ship) model.getUnitById(100);
        final AttackOrder order = attacker.prepareAttack(TARGET, false)
                .orElseThrow();
        model.declareHostilities(attacker.getOwner(), target.getOwner());
        final String changed = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(changed, new Gson().toJson(model.save()));
    }

    @Test
    void staleWarToPeaceAlsoRejectsBeforeBattle() {
        final Model model = game(UnitType.FRIGATE, UnitType.PRIVATEER,
                "France");
        final Ship attacker = (Ship) model.getUnitById(3);
        final Ship target = (Ship) model.getUnitById(100);
        model.declareHostilities(attacker.getOwner(), target.getOwner());
        final AttackOrder order = attacker.prepareAttack(TARGET, false)
                .orElseThrow();
        model.acceptPeace(attacker.getOwner(), target.getOwner());
        final String changed = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(changed, new Gson().toJson(model.save()));
    }

    @Test
    void noncombatantCannotExploitExceptionAndCrownHostilityStillWorks() {
        final Model merchant = game(UnitType.MERCHANTMAN, UnitType.PRIVATEER,
                "France");
        assertFalse(merchant.isPrivateerNavalException(merchant.getUnitById(3),
                merchant.getUnitById(100)));
        assertTrue(
                merchant.getUnitById(3).prepareAttack(TARGET, true).isEmpty());

        final Model crown = game(UnitType.FRIGATE, UnitType.PRIVATEER,
                "France's King");
        assertFalse(crown.isPrivateerNavalException(crown.getUnitById(3),
                crown.getUnitById(100)));
        assertTrue(
                crown.getUnitById(3).prepareAttack(TARGET, false).isPresent());
    }
}
