package org.microcol.gui.event;

import org.microcol.model.Model;

/**
 * New game listener.
 */
public interface NewGameListener {

	/**
	 * It's called when new game event is fired.
	 * 
	 * @param game
	 *            required new game
	 */
	void onNewGame(Model game);

}
