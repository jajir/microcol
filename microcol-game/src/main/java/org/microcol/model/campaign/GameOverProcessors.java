package org.microcol.model.campaign;

import java.util.function.Function;

import org.microcol.gui.dialog.Dialog;
import org.microcol.model.GameOverEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hold functions that properly react on game over game reason.
 */
public final class GameOverProcessors {

    private final static Logger LOGGER = LoggerFactory.getLogger(GameOverProcessors.class);
    
    /**
     * React on game over when user exceed given time.
     */
    public final static Function<GameOverProcessingContext, String> TIME_IS_UP_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_TIME_IS_UP
                .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, time is up.");
            context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
                callBackContext.showMessage(Dialog.gameOver_timeIsUp);
                callBackContext.goToGameMenu();
            });
            return "ok";
        }
        return null;
    };

    public final static Function<GameOverProcessingContext, String> NO_COLONIES_PROCESSOR = context -> {
        if (GameOverEvaluator.REASON_NO_COLONIES
                .equals(context.getEvent().getGameOverResult().getGameOverReason())) {
            LOGGER.info("Game over, no colonies.");
            context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
                callBackContext.showMessage(Dialog.gameOver_allColoniesAreLost);
                callBackContext.goToGameMenu();
            });
            return "ok";
        }
        return null;
    };

}
