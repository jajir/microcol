package org.microcol.model.campaign;

import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;

final class Default_2_modelListener extends ModelListenerAdapter {

    private final Default_2_mission mission;

    Default_2_modelListener(final Default_2_mission mission) {
        this.mission = Preconditions.checkNotNull(mission);
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        mission.onTurnStarted(event);
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        mission.onGameStarted();
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        mission.onGameFinished(event);
    }

    @Override
    public void onIndependenceWasDeclared(final IndependenceWasDeclaredEvent event) {
        mission.onIndependenceWasDeclared();
    }
}