package org.microcol.gui.util;

import java.io.File;

import org.microcol.gui.event.model.GameModelController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Service help with files for game saves.
 */
public class PersistingTool {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final static String SYSTEM_PROPERTY_USER_HOME = "user.home";

	private final static String SAVE_DIRECTORY = ".microcol";

	private final static String EXTENSION = ".microcol";

	private final GameModelController gameModelController;

	@Inject
	public PersistingTool(final GameModelController gameModelController) {
		this.gameModelController = Preconditions.checkNotNull(gameModelController);
	}

	/**
	 * 
	 * @return
	 */
	public File getRootSaveDirectory() {
		final File userDir = new File(System.getProperty(SYSTEM_PROPERTY_USER_HOME));
		Preconditions.checkState(userDir.exists());
		Preconditions.checkState(userDir.isDirectory());

		final File out = userDir.toPath().resolve(SAVE_DIRECTORY).toFile();
		if (!out.exists()) {
			logger.info("creating microCol save directory at '{}'", out.getAbsolutePath());
			Preconditions.checkState(out.mkdir(), "Unable to create file (%s)", out.getAbsolutePath());
		}

		Preconditions.checkState(out.exists());
		Preconditions.checkState(out.isDirectory());
		return out;
	}

	public String getSuggestedSaveFileName() {
		Preconditions.checkState(gameModelController.isModelReady(), "Can't suggest file name when model is not ready");

		String str = gameModelController.getCurrentPlayer().getName() + "-"
				+ gameModelController.getModel().getCalendar().getCurrentYear();

		str = str.replaceAll("[^a-zA-Z0-9\\.\\-]", "_") + EXTENSION;

		return str;
	}

}
