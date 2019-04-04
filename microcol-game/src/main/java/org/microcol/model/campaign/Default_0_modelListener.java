package org.microcol.model.campaign;

import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.event.BeforeDeclaringIndependenceEvent;
import org.microcol.model.event.BeforeEndTurnEvent;
import org.microcol.model.event.ColonyWasFoundEvent;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.GameStartedEvent;
import org.microcol.model.event.GoodsWasSoldInEuropeEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.UnitMoveStartedEvent;

import com.google.common.base.Preconditions;

final class Default_0_modelListener extends ModelListenerAdapter {

    private final Default_0_mission mission;

    Default_0_modelListener(final Default_0_mission mission) {
        this.mission = Preconditions.checkNotNull(mission);
    }

    @Override
    public void onBeforeDeclaringIndependence(final BeforeDeclaringIndependenceEvent event) {
        mission.onBeforeDeclaringIndependence(event);
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
    public void onUnitMoveStarted(final UnitMoveStartedEvent event) {
        mission.onUnitMoveStarted(event);
    }

    @Override
    public void onUnitMoveFinished(final UnitMoveFinishedEvent event) {
        mission.onUnitMoveFinished(event);
    }

    @Override
    public void onBeforeEndTurn(final BeforeEndTurnEvent event) {
        mission.onBeforeEndTurn();
    }

    @Override
    public void onTurnStarted(final TurnStartedEvent event) {
        mission.onTurnStarted();
    }

    @Override
    public void onGoodsWasSoldInEurope(final GoodsWasSoldInEuropeEvent event) {
        mission.onGoodsWasSoldInEurope(event);
    }

    @Override
    public void onColonyWasFounded(final ColonyWasFoundEvent event) {
        mission.onColonyWasFounded(event);
    }

}