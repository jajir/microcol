package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.GameOverResult;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Unit;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.store.CampaignsDao;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.mockito.ArgumentCaptor;

import com.google.common.eventbus.EventBus;

class MissionEventsTest {

    private final EventBus eventBus = mock(EventBus.class);

    @Test
    void timeLimitEmitsTimeLimitOutcome() {
        final String result = GameOverProcessors.TIME_IS_UP_PROCESSOR
                .apply(gameOver(
                        org.microcol.model.GameOverEvaluator.REASON_TIME_IS_UP));
        assertEquals("ok", result);
        final ArgumentCaptor<EventFinishMission> event = ArgumentCaptor
                .forClass(EventFinishMission.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(Missions.gameOver_timeIsUp),
                event.getValue().getMessages());
    }

    @Test
    void lostColoniesEmitLostColoniesOutcome() {
        final String result = GameOverProcessors.NO_COLONIES_PROCESSOR
                .apply(gameOver(
                        org.microcol.model.GameOverEvaluator.REASON_NO_COLONIES));
        assertEquals("ok", result);
        final ArgumentCaptor<EventFinishMission> event = ArgumentCaptor
                .forClass(EventFinishMission.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(Missions.gameOver_allColoniesAreLost),
                event.getValue().getMessages());
    }

    @Test
    void unrelatedReasonEmitsNoOutcome() {
        assertNull(GameOverProcessors.TIME_IS_UP_PROCESSOR
                .apply(gameOver("other")));
        assertNull(GameOverProcessors.NO_COLONIES_PROCESSOR
                .apply(gameOver("other")));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void freePlayIndependenceEmitsItsOutcome() {
        final FreePlay_0_mission mission = new FreePlay_0_mission(
                context(mock(Model.class), new GameModelPo()),
                new MissionGoalsEmpty());
        mission.onIndependenceWasDeclared();
        final ArgumentCaptor<EventShowMessages> event = ArgumentCaptor
                .forClass(EventShowMessages.class);
        verify(eventBus).post(event.capture());
        assertEquals(List.of(Missions.independenceWasDeclared_caption),
                event.getValue().getMessages());
    }

    @Test
    void tutorialStartsAndDiscoversTheContinentOnce() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-0-findNewWorld.microcol");
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Default_0_goals goals = new Default_0_goals(new HashMap<>());
        final Default_0_mission mission = new Default_0_mission(
                context(model, data), goals);
        mission.onGameStarted(new GameStartedEvent(model));
        final Unit unit = mock(Unit.class);
        when(unit.getActionPoints()).thenReturn(1);
        when(unit.isAtPlaceLocation()).thenReturn(true);
        when(unit.getLocation()).thenReturn(Location.of(45, 15));
        final UnitMoveFinishedEvent movement = new UnitMoveFinishedEvent(model,
                unit,
                Path.of(List.of(Location.of(45, 15), Location.of(44, 15))));
        mission.onUnitMoveFinished(movement);
        assertFalse(goals.getGoalFindNewWorld().isFinished());
        when(unit.getLocation()).thenReturn(Location.of(19, 15));
        final UnitMoveFinishedEvent near = new UnitMoveFinishedEvent(model,
                unit,
                Path.of(List.of(Location.of(18, 15), Location.of(19, 15))));
        mission.onUnitMoveFinished(near);
        assertTrue(goals.getGoalFindNewWorld().isFinished());
        mission.onUnitMoveFinished(near);
        final ArgumentCaptor<EventShowDialogWithMan> messages = ArgumentCaptor
                .forClass(EventShowDialogWithMan.class);
        verify(eventBus, times(2)).post(messages.capture());
        assertEquals(List.of(Missions.default_m0_start),
                messages.getAllValues().get(0).getMessages());
        assertEquals(List.of(Missions.default_m0_continentInSight),
                messages.getAllValues().get(1).getMessages());
    }

    private GameOverProcessingContext gameOver(final String reason) {
        return new GameOverProcessingContext(new GameFinishedEvent(
                mock(Model.class), new GameOverResult(reason)), eventBus);
    }

    private MissionCreationContext context(final Model model,
            final GameModelPo data) {
        return new MissionCreationContext(eventBus, model, data,
                new CampaignManager(List.of(), mock(CampaignsDao.class)),
                new CampaignMission(mock(MissionName.class), 0),
                new ExternalGameOver("test"));
    }
}
