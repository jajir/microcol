package org.microcol.model.campaign;

import java.util.List;
import java.util.function.Function;

import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

final class Default_2_mission extends AbstractMission<Default_2_goals> {

    Default_2_mission(final MissionCreationContext context, final Default_2_goals goals) {
        super(context, goals);
    }

    @Override
    protected List<Function<GameOverProcessingContext, String>> prepareProcessors() {
        return Lists.newArrayList(GameOverProcessors.TIME_IS_UP_PROCESSOR,
                GameOverProcessors.NO_COLONIES_PROCESSOR, context -> {
                    if (MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE
                            .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
                        fireEvent(new EventFinishMission(Missions.default_m2_done1,
                                Missions.default_m2_done2));
                        return "ok";
                    }
                    return null;
                });
    }

    public void onTurnStarted(final TurnStartedEvent event) {
        super.onTurnStarted();
        Player p = event.getModel().getPlayerByName("Dutch's King");
        /**
         * When King is conquered than sent this property in his data to true.
         */
        String s = (String) p.getExtraData().get("kingWasConquered");
        // TODO this is ugly. Redesign relation between AI - king and mission
        if (!Strings.isNullOrEmpty(s)) {
            if (Boolean.parseBoolean(s)) {
                getGoals().getGoalConquerRaf().setFinished(true);
            }
        }
    }

    public void onGameStarted() {
        if (isFirstTurn(getModel())) {
            fireEvent(new EventShowMessages(Missions.default_m1_start,
                    Missions.default_m2_declareIndependence));
        }
    }

    public void onIndependenceWasDeclared() {
        if (!getGoals().getGoalDeclareIndependence().isFinished()) {
            fireEvent(new EventShowMessages(Missions.default_m2_declareIndependence_done,
                    Missions.default_m2_portIsClosed));
            getGoals().getGoalDeclareIndependence().setFinished(true);
        }
    }
}