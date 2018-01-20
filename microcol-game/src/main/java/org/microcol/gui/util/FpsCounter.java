package org.microcol.gui.util;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class helps measure Frame per second rate.
 */
public class FpsCounter {

	private final Logger logger = LoggerFactory.getLogger(FpsCounter.class);

	private final Timer timer;

	private int fps;

	private int counter;

	public FpsCounter() {
		timer = new Timer(1000, e -> {
			fps = counter;
			counter = 0;
			logger.debug("Fps is: " + fps);
		});
	}

	public void start() {
		counter = 0;
		fps = 0;
		timer.start();
	}

	public void stop() {
		timer.stop();
	}

	/**
	 * Should be called when screen was painted.
	 */
	public void screenWasPainted() {
		counter++;
	}

	/**
	 * Return actual FPS.
	 * 
	 * @return fps value
	 */
	public int getFps() {
		return fps;
	}

}
