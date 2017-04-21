package org.microcol.gui;

import org.microcol.gui.event.AboutGameEvent;
import org.microcol.gui.event.AboutGameEventController;
import org.microcol.gui.event.ExitGameController;
import org.microcol.gui.event.ExitGameEvent;

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

	private final AboutGameEventController gameEventController;

	private final ExitGameController exitGameController;

	@Inject
	public AppleMenuListener(final AboutGameEventController mainMenuController,
			final ExitGameController exitGameController) {
		this.gameEventController = Preconditions.checkNotNull(mainMenuController);
		this.exitGameController = Preconditions.checkNotNull(exitGameController);
	}

	@Override
	public void handleAbout() {
		gameEventController.fireEvent(new AboutGameEvent());
	}

	@Override
	public void handleQuit() throws IllegalStateException {
		exitGameController.fireEvent(new ExitGameEvent());
	}

}
