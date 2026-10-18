package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Colony;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

class EuropeanRelationAiTest {
    private Model model;
    private Unit rival;
    private Unit human;
    private final SimpleUnitBehavior behavior = new SimpleUnitBehavior();

    @BeforeEach
    void prepareAdjacentArmies() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        rival = model.getUnitById(6);
        rival.placeToLocation(Location.of(17, 11));
        human = model.getUnitById(0);
        human.placeToLocation(Location.of(18, 11));
        ((UnitFreeColonist) human).setHoldingGuns(true);
        assertEquals(1, rival.getActionPoints());
    }

    private Player france() {
        return model.getPlayerByName("France");
    }

    private Player dutch() {
        return model.getPlayerByName("Dutch");
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private void moveWithRealAi() {
        final SimpleAiPlayer ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5),
                    () -> ai.moveUnit(rival));
        } finally {
            ai.stop();
        }
    }

    @Test
    void routineFightSelectionLeavesPeacefulNeighboursUntouched() {
        final String before = snapshot();
        behavior.tryToFight(rival);
        assertEquals(before, snapshot());
    }

    @Test
    void immediatePlannerDoesNotStopForAPeacefulAttackTarget() {
        final Location start = rival.getLocation();
        final int count = model.getAllUnits().size();
        final String humanBefore = new Gson().toJson(human.save());
        moveWithRealAi();
        assertNotEquals(start, rival.getLocation());
        assertEquals(0, rival.getActionPoints());
        assertEquals(count, model.getAllUnits().size());
        assertEquals(humanBefore, new Gson().toJson(human.save()));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france(), dutch()));
    }

    @Test
    void immediatePlannerUsesExistingFightAtWar() {
        model.declareHostilities(france(), dutch());
        final int count = model.getAllUnits().size();
        moveWithRealAi();
        assertEquals(count, model.getAllUnits().size());
        assertSame(human, model.getUnitById(human.getId()));
        assertSame(rival, model.getUnitById(rival.getId()));
        assertEquals(0, human.getActionPoints());
        assertEquals(0, rival.getActionPoints());
        assertTrue(!((UnitFreeColonist) human).isHoldingGuns()
                || !((UnitFreeColonist) rival).isHoldingGuns());
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(france(), dutch()));
    }

    @Test
    void acceptedPeaceStopsRoutineFightingAndImmediateAttackPlanning() {
        model.declareHostilities(france(), dutch());
        model.acceptPeace(france(), dutch());
        final String before = snapshot();
        behavior.tryToFight(rival);
        assertEquals(before, snapshot());
        final Location start = rival.getLocation();
        final int count = model.getAllUnits().size();
        moveWithRealAi();
        assertNotEquals(start, rival.getLocation());
        assertEquals(count, model.getAllUnits().size());
        assertEquals(0, rival.getActionPoints());
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france(), dutch()));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(booleans = { false, true })
    void mountedWandererRechecksCombatAfterEachActualStep(
            final boolean stopAfterStep) throws Exception {
        rival.placeToLocation(Location.of(18, 12));
        human.placeToLocation(Location.of(18, 14));
        ((UnitFreeColonist) rival).setMounted(true);
        ((org.microcol.model.AbstractUnit) rival).setActionPoints(3);
        rival.getOwner().revealMapForUnit(rival);
        model.declareHostilities(france(), dutch());
        final int count = model.getAllUnits().size();
        model = org.mockito.Mockito.spy(model);
        final SimpleAiPlayer ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
        // Keep the ordinary random direction deterministic, towards the enemy.
        final java.lang.reflect.Field navigatorField = SimpleAiPlayer.class
                .getDeclaredField("unitDirections");
        navigatorField.setAccessible(true);
        final java.lang.reflect.Field directionsField = UnitMoveNavigator.class
                .getDeclaredField("lastDirections");
        directionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final java.util.Map<Unit, org.microcol.model.Direction> directions = (java.util.Map<Unit, org.microcol.model.Direction>) directionsField
                .get(navigatorField.get(ai));
        directions.put(rival, org.microcol.model.Direction.south);
        final java.util.concurrent.atomic.AtomicInteger steps = new java.util.concurrent.atomic.AtomicInteger();
        model.addListener(new org.microcol.model.ModelListenerAdapter() {
            @Override
            public void onUnitMovedStepFinished(
                    final org.microcol.model.event.UnitMovedStepFinishedEvent event) {
                steps.incrementAndGet();
            }
        });
        // Stop between model commands, after the real move has returned from
        // event dispatch.
        org.mockito.Mockito.doAnswer(invocation -> {
            invocation.callRealMethod();
            if (stopAfterStep) {
                ai.stop();
            }
            return null;
        }).when(model).moveUnit(org.mockito.ArgumentMatchers.eq(rival),
                org.mockito.ArgumentMatchers
                        .any(org.microcol.model.Path.class));
        final String defenderBefore = new Gson().toJson(human.save());
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5),
                    () -> ai.moveUnit(rival));
        } finally {
            ai.stop();
        }
        assertEquals(1, steps.get());
        if (stopAfterStep) {
            assertEquals(count, model.getAllUnits().size());
            assertEquals(Location.of(18, 13), rival.getLocation());
            assertTrue(rival.getActionPoints() > 0);
            assertEquals(defenderBefore, new Gson().toJson(human.save()));
            return;
        }
        assertEquals(count, model.getAllUnits().size());
        assertSame(human, model.getUnitById(human.getId()));
        assertSame(rival, model.getUnitById(rival.getId()));
        assertTrue(!((UnitFreeColonist) human).isHoldingGuns()
                || !((UnitFreeColonist) rival).isMounted());
        assertEquals(0, rival.getActionPoints());
        assertEquals(0, human.getActionPoints());
    }

    private Colony undefendedHumanColony() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final var colonyPo = data.getModel().getColonies().stream()
                .filter(c -> c.getId() == 0).findFirst().orElseThrow();
        colonyPo.setOwnerName("Dutch");
        colonyPo.setLastColonialAffiliation("Dutch");
        data.getModel().getUnits().stream().filter(u -> u.getId() == 4)
                .findFirst().orElseThrow().setOwnerId("Dutch");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        rival = model.getUnitById(6);
        rival.placeToLocation(Location.of(18, 10));
        return model.getColonyById(0).orElseThrow();
    }

    @Test
    void routineCaptureCannotTakeAPeacefulUnguardedColony() {
        final Colony colony = undefendedHumanColony();
        final String before = snapshot();
        behavior.tryToCaptureColony(model, rival, colony.getLocation());
        assertEquals(before, snapshot());
    }

    @Test
    void acceptedPeaceClosesTheRoutineCapturePathAgain() {
        final Colony colony = undefendedHumanColony();
        model.declareHostilities(france(), dutch());
        assertTrue(rival.isPossibleToCaptureColonyAt(colony.getLocation()));
        model.acceptPeace(france(), dutch());
        final String before = snapshot();
        behavior.tryToCaptureColony(model, rival, colony.getLocation());
        assertEquals(before, snapshot());
    }

    @Test
    void wartimeRoutineCaptureRetainsTheArmedForceAndNewOwnership() {
        final Colony colony = undefendedHumanColony();
        model.declareHostilities(france(), dutch());
        behavior.tryToCaptureColony(model, rival, colony.getLocation());
        assertSame(france(), colony.getOwner());
        assertSame(france(), model.getUnitById(4).getOwner());
        assertSame(rival, model.getUnitById(6));
        assertTrue(((UnitFreeColonist) rival).isHoldingGuns());
        assertTrue(rival.isAtPlaceLocation());
        assertEquals(colony.getLocation(), rival.getLocation());
        assertEquals(0, rival.getActionPoints());
    }

    @Test
    void routineCaptureSkipsAPortWhoseShipCannotEscape() {
        final Colony colony = undefendedHumanColony();
        model.declareHostilities(france(), dutch());
        // The reference settlement has no adjacent sea for this vessel.
        model.getUnitById(3).placeToLocation(colony.getLocation());
        final String before = snapshot();
        behavior.tryToCaptureColony(model, rival, colony.getLocation());
        assertEquals(before, snapshot());
    }
}
