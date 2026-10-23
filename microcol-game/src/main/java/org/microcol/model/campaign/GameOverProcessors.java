package org.microcol.model.campaign;

import java.util.function.Function;

import org.microcol.model.GameOverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold functions that properly react on game over game reason.
 */
final class GameOverProcessors {

    private final static Logger LOGGER = LoggerFactory
            .getLogger(GameOverProcessors.class);

    /**
     * React on game over when user exceed given time.
     */
    public final static Function<GameOverProcessingContext, String> TIME_IS_UP_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_TIME_IS_UP.equals(
                context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, time is up.");
            context.fireEvent(
                    new EventFinishMission(Missions.gameOver_timeIsUp));
            return "ok";
        }
        return null;
    };

    /**
     * React on the end of free play at spring 1850 (R-0193). The frozen score
     * is the result, so the message names no outcome of its own.
     */
    public final static Function<GameOverProcessingContext, String> FREE_PLAY_ENDED_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_FREE_PLAY_ENDED.equals(
                context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, free play ended in spring 1850.");
            context.fireEvent(
                    new EventFinishMission(Missions.gameOver_freePlayEnded));
            return "ok";
        }
        return null;
    };

    /**
     * React on the confirmed retirement of free play (R-0194). The result
     * window shows the frozen score, so the message names no outcome of its
     * own.
     */
    public final static Function<GameOverProcessingContext, String> RETIRED_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_RETIRED.equals(
                context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, the nation retired.");
            context.fireEvent(
                    new EventFinishMission(Missions.gameOver_retired));
            return "ok";
        }
        return null;
    };

    public final static Function<GameOverProcessingContext, String> NO_COLONIES_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_NO_COLONIES.equals(
                context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, no colonies.");
            context.fireEvent(new EventFinishMission(
                    Missions.gameOver_allColoniesAreLost));
            return "ok";
        }
        return null;
    };

    /**
     * React on a declared rebellion lost to the Crown (R-0192); each reason has
     * its own explicit message.
     */
    public final static Function<GameOverProcessingContext, String> REBELLION_LOST_PROCESSOR = context -> {
        final String reason = context.getEvent().getGameOverResult()
                .getGameOverReason();
        final Missions message;
        if (GameOverEvaluator.REASON_REBELLION_NO_COLONIES.equals(reason)) {
            message = Missions.gameOver_rebellionNoColonies;
        } else if (GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN
                .equals(reason)) {
            message = Missions.gameOver_rebellionAllColoniesCrown;
        } else if (GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN
                .equals(reason)) {
            message = Missions.gameOver_rebellionCoastalCrown;
        } else if (GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN
                .equals(reason)) {
            message = Missions.gameOver_rebellionResidentsCrown;
        } else if (GameOverEvaluator.REASON_REBELLION_UNRESOLVED
                .equals(reason)) {
            message = Missions.gameOver_rebellionUnresolved;
        } else {
            return null;
        }
        LOGGER.info("Game over, rebellion lost: {}.", reason);
        context.fireEvent(new EventFinishMission(message));
        return "ok";
    };

    /**
     * React on a declared rebellion won by the rebels (R-0191) with its own
     * explicit message.
     */
    public final static Function<GameOverProcessingContext, String> REBELLION_WON_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_REBELLION_VICTORY.equals(
                context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, rebellion won.");
            context.fireEvent(
                    new EventFinishMission(Missions.gameOver_rebellionVictory));
            return "ok";
        }
        return null;
    };

}
