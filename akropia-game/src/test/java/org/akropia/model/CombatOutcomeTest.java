package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.event.UnitAttackedEvent;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.UnitActionFortifyPo;
import org.akropia.model.unit.UnitActionType;
import org.akropia.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class CombatOutcomeTest {
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location TARGET = Location.of(17, 11);
    private static final long STEP = 0x9E3779B97F4A7C15L;

    private Model game(long state, boolean defenderMounted) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel();
        data.setCombatRandomState(state);
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        attacker.placeToLocation(ORIGIN);
        defender.placeToLocation(TARGET);
        attacker.setHoldingWeapons(true);
        defender.setHoldingWeapons(true);
        defender.setMounted(defenderMounted);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    private Model fortifiedDefenderGame(boolean mounted) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia").getModel();
        data.setCombatRandomState(3L);
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer("Dutch");
        data.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        final org.akropia.model.store.UnitPo defenderPo = data.getUnits()
                .stream().filter(unit -> unit.getId() == 6).findFirst()
                .orElseThrow();
        defenderPo.getPlaceMap().setLocation(TARGET);
        defenderPo.setHoldingWeapons(true);
        defenderPo.setMounted(mounted);
        defenderPo.setAvailableMoves(1);
        final UnitActionFortifyPo action = new UnitActionFortifyPo();
        action.setFortified(true);
        defenderPo.setAction(action);
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        attacker.placeToLocation(ORIGIN);
        attacker.setHoldingWeapons(true);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    @Test
    void mountedLoserDropsHorsesAndTheEventReportsSurvivalAfterMutation() {
        final Model model = game(3L, true);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(UnitAttackedEvent event) {
                assertFalse(defender.isMounted());
                assertTrue(defender.isHoldingWeapons());
                events.add(event);
            }
        });
        final AttackOrder order = attacker.prepareAttack(TARGET, true)
                .orElseThrow();
        assertEquals(3L, model.save().getCombatRandomState());
        assertEquals(0, events.size());
        order.execute();
        assertEquals(3L + 2 * STEP, model.save().getCombatRandomState());
        assertEquals(1, events.size());
        assertSame(defender, events.get(0).getDefeated());
        assertFalse(events.get(0).wasDestroyed());
        assertNull(events.get(0).getDestroyed());
        assertEquals(ORIGIN, attacker.getLocation());
        assertEquals(TARGET, defender.getLocation());
        assertEquals(0, attacker.getActionPoints());
        assertEquals(0, defender.getActionPoints());
        assertEquals(7, model.getAllUnits().size());
        final String saved = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(saved, new Gson().toJson(model.save()));
    }

    @Test
    void soldierLoserDropsWeaponsAndContinueNeverDrawsAgain() {
        final Model model = game(0L, false);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        attacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertFalse(attacker.isHoldingWeapons());
        assertTrue(defender.isHoldingWeapons());
        assertEquals(0L + 2 * STEP, model.save().getCombatRandomState());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        assertFalse(((UnitFreeColonist) restored.getUnitById(0))
                .isHoldingWeapons());
        assertEquals(0, restored.getUnitById(0).getActionPoints());
        assertEquals(0L + 2 * STEP, restored.save().getCombatRandomState());
    }

    @Test
    void sameSavedStateProducesSameResolvedWorld() {
        final Model source = game(3L, false);
        final ModelPo snapshot = source.save();
        final Model first = Model.make(snapshot,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Model second = Model.make(snapshot,
                MissionImpl.getPredefinedGameOverEvaluators());
        first.getUnitById(0).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        second.getUnitById(0).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        assertEquals(new Gson().toJson(first.save()),
                new Gson().toJson(second.save()));
    }

    @Test
    void fortifiedFootLoserBecomesUnarmedAndClearsInvalidOrder() {
        final Model model = fortifiedDefenderGame(false);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        final Player owner = defender.getOwner();
        assertTrue(model.isFortified(defender));
        attacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertSame(defender, model.getUnitById(6));
        assertSame(owner, defender.getOwner());
        assertEquals(TARGET, defender.getLocation());
        assertFalse(defender.isHoldingWeapons());
        assertEquals(UnitActionType.noAction, defender.getAction().getType());
        assertFalse(model.isFortified(defender));
        assertEquals(0, defender.getActionPoints());
        assertEquals(ORIGIN, attacker.getLocation());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        final UnitFreeColonist continued = (UnitFreeColonist) restored
                .getUnitById(6);
        assertEquals(TARGET, continued.getLocation());
        assertEquals("France", continued.getOwner().getName());
        assertFalse(continued.isHoldingWeapons());
        assertEquals(UnitActionType.noAction, continued.getAction().getType());
        assertEquals(0, continued.getActionPoints());
    }

    @Test
    void fortifiedMountedLoserKeepsArmsAndEffectiveOrder() {
        final Model model = fortifiedDefenderGame(true);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        final Player owner = defender.getOwner();
        assertTrue(model.isFortified(defender));
        attacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertSame(defender, model.getUnitById(6));
        assertSame(owner, defender.getOwner());
        assertEquals(TARGET, defender.getLocation());
        assertFalse(defender.isMounted());
        assertTrue(defender.isHoldingWeapons());
        assertEquals(UnitActionType.fortify, defender.getAction().getType());
        assertTrue(model.isFortified(defender));
        assertEquals(0, defender.getActionPoints());
        assertEquals(ORIGIN, attacker.getLocation());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        final UnitFreeColonist continued = (UnitFreeColonist) restored
                .getUnitById(6);
        assertEquals(TARGET, continued.getLocation());
        assertEquals("France", continued.getOwner().getName());
        assertFalse(continued.isMounted());
        assertTrue(continued.isHoldingWeapons());
        assertEquals(UnitActionType.fortify, continued.getAction().getType());
        assertTrue(restored.isFortified(continued));
        assertEquals(0, continued.getActionPoints());
    }
}
