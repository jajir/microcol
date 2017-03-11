package org.microcol.gui.event;

import org.apache.log4j.Logger;
import org.microcol.gui.AboutDialog;
import org.microcol.gui.MusicController;
import org.microcol.gui.Text;
import org.microcol.gui.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class GameEventListenerImpl implements GameEventListener {

	private final Logger logger = Logger.getLogger(GameEventListenerImpl.class);

	private final ViewUtil viewUtil;

	private final Text text;

	private final MusicController musicController;

	@Inject
	public GameEventListenerImpl(final GameEventController gameEventController, final ViewUtil viewUtil,
			final Text text, final MusicController musicController) {
		gameEventController.addGameEventListener(this);
		this.viewUtil = Preconditions.checkNotNull(viewUtil);
		this.text = Preconditions.checkNotNull(text);
		this.musicController = Preconditions.checkNotNull(musicController);
	}

	@Override
	public void onGameExit() {
		logger.debug("onGameExit was triggered");
		musicController.stop();
	}

	@Override
	public void onAboutGame() {
		new AboutDialog(viewUtil, text).setVisible(true);
	}

}
