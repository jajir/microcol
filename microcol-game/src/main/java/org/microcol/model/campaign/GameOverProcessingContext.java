package org.microcol.model.campaign;

import org.microcol.gui.event.model.MissionCallBack;
import org.microcol.model.event.GameFinishedEvent;

import com.google.common.base.Preconditions;

/**
 * Hold context for processing game over event.
 */
final class GameOverProcessingContext {

	private final GameFinishedEvent event;

	private final MissionCallBack missionCallBack;

	GameOverProcessingContext(final GameFinishedEvent event, final MissionCallBack missionCallBack) {
		this.event = Preconditions.checkNotNull(event);
		this.missionCallBack = Preconditions.checkNotNull(missionCallBack);
	}

	/**
	 * @return the event
	 */
	public GameFinishedEvent getEvent() {
		return event;
	}

	/**
	 * @return the missionCallBack
	 */
	public MissionCallBack getMissionCallBack() {
		return missionCallBack;
	}

}