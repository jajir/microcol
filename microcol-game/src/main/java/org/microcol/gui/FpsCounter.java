package org.microcol.gui;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class helps measure Frame per second rate.
 * 
 * @author jan
 *
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
