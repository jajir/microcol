package org.microcol.model.campaign;

import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.IndependenceWasDeclaredEvent;

import com.google.common.base.Preconditions;

final class FreePlay_0_modelListener extends ModelListenerAdapter {

    private final FreePlay_0_mission mission;

    FreePlay_0_modelListener(final FreePlay_0_mission mission) {
        this.mission = Preconditions.checkNotNull(mission);
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
