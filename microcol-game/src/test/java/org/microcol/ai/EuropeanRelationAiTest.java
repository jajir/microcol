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
        assertEquals(count - 1, model.getAllUnits().size());
        assertNotEquals(model.tryGetUnitById(human.getId()).isPresent(),
                model.tryGetUnitById(rival.getId()).isPresent());
        model.tryGetUnitById(rival.getId())
                .ifPresent(unit -> assertEquals(0, unit.getActionPoints()));
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

    private Colony undefendedHumanColony() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        data.getModel().getColonies().stream().filter(c -> c.getId() == 0)
                .findFirst().orElseThrow().setOwnerName("Dutch");
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
