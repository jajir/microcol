package org.akropia.model.campaign;

import org.akropia.model.ModelListenerAdapter;
import org.akropia.model.event.BeforeDeclaringIndependenceEvent;
import org.akropia.model.event.ColonyWasFoundEvent;
import org.akropia.model.event.GameFinishedEvent;
import org.akropia.model.event.GameStartedEvent;
import org.akropia.model.event.GoodsWasSoldInEuropeEvent;
import org.akropia.model.event.TurnStartedEvent;

import com.google.common.base.Preconditions;

final class Default_1_modelListener extends ModelListenerAdapter {

    private final Default_1_mission mission;

    Default_1_modelListener(final Default_1_mission mission) {
        this.mission = Preconditions.checkNotNull(mission);
    }

    @Override
    public void onGameStarted(final GameStartedEvent event) {
        mission.onGameStarted(event);
    }

    @Override
    public void onGameFinished(final GameFinishedEvent event) {
        mission.onGameFinished(event);
    }

    @Override
    public void onBeforeDeclaringIndependence(
            final BeforeDeclaringIndependenceEvent event) {
        mission.onBeforeDeclaringIndependence(event);
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        mission.onColonyWasFounded(event);
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        mission.onGoodsWasSoldInEurope(event);
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        mission.onTurnStarted(event);
    }
}