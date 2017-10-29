package org.microcol.ai;

import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Define kings behavior.
 *
 */
public class King {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Model model;

	private final Player kingPlayer;

	private final Player whosKingThisPlayerIs;

	/**
	 * Royal Expedition Forces was send to colonies after declaring
	 * independence.
	 */
	private boolean refWasSend = false;

	public King(final Model model, final Player kingPlayer, Player whosKingThisPlayerIs) {
		this.model = Preconditions.checkNotNull(model);
		this.kingPlayer = Preconditions.checkNotNull(kingPlayer);
		this.whosKingThisPlayerIs = Preconditions.checkNotNull(whosKingThisPlayerIs);
	}

	public void start() {
		model.addListener(new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().equals(kingPlayer)) {
					turn();
					kingPlayer.endTurn();
				}
			}
		});
		logger.info("AI engine started.");
	}

	/**
	 * When King is on turn this method is called. Method should perform all
	 * unit moving.
	 */
	void turn() {
		if (whosKingThisPlayerIs.isDeclaredIndependence() && !refWasSend) {
			//XXX send REF
		}
	}
}
