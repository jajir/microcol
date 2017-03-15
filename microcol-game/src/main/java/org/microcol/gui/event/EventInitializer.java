package org.microcol.gui.event;

import org.apache.log4j.Logger;
import org.microcol.gui.MusicController;

import com.google.inject.Inject;

/**
 * Class wire up events. Class should not be used. Only after instantiated after
 * application startup.
 */
public class EventInitializer {

	private final Logger logger = Logger.getLogger(EventInitializer.class);

	@Inject
	public EventInitializer(final ExitGameController exitGameController,
			/**
			 * 
			 */
			final MusicController musicController) {
		logger.debug("Starting event initialization");
		exitGameController.addListener(event -> musicController.stop());
		exitGameController.addListener(event -> System.exit(0), 100);
	}

}
