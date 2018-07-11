package org.microcol.model.campaign;

import java.util.function.Function;

import org.microcol.model.GameOverEvaluator;

/**
 * Hold functions that properly react on game over game reason.
 */
public final class GameOverProcessors {

	/**
	 * React on game over when user exceed given time.
	 */
	public final static Function<GameOverProcessingContext, String> TIME_IS_UP_PROCESSOR = context -> {
		if (GameOverEvaluator.REASON_TIME_IS_UP.equals(context.getEvent().getGameOverResult().getGameOverReason())) {
			context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
				callBackContext.showMessage("dialogGameOver.timeIsUp");
				callBackContext.goToGameMenu();
			});
			return "ok";
		}
		return null;
	};

	public final static Function<GameOverProcessingContext, String> NO_COLONIES_PROCESSOR = context -> {
		if (GameOverEvaluator.REASON_NO_COLONIES.equals(context.getEvent().getGameOverResult().getGameOverReason())) {
			context.getMissionCallBack().executeOnFrontEnd(callBackContext -> {
				callBackContext.showMessage("dialogGameOver.allColoniesAreLost");
				callBackContext.goToGameMenu();
			});
			return "ok";
		}
		return null;
	};

}
