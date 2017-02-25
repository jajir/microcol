package org.microcol.gui;

import org.microcol.gui.event.GameEventController;

import com.apple.mrj.MRJAboutHandler;
import com.apple.mrj.MRJQuitHandler;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Methods are called when some Apple main menu specific action occurs.
 * 
 * @author jajir
 *
 */
public class AppleMenuListener implements MRJAboutHandler, MRJQuitHandler {

	private final GameEventController gameEventController;

	@Inject
	public AppleMenuListener(final GameEventController mainMenuController) {
		this.gameEventController = Preconditions.checkNotNull(mainMenuController);
	}

	@Override
	public void handleAbout() {
		gameEventController.fireAboutGameEvent();
	}

	@Override
	public void handleQuit() throws IllegalStateException {
		gameEventController.fireGameExit();
	}

}
