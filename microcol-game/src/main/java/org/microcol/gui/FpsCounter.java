package org.microcol.gui;

import javax.swing.Timer;

import org.apache.log4j.Logger;

/**
 * Class helps measure Frame per second rate.
 * 
 * @author jan
 *
 */
public class FpsCounter {

	private final Logger logger = Logger.getLogger(FpsCounter.class);
	
	private int fps;

	private int counter;

	public void start() {
		counter = 0;
		fps = 0;
		new Timer(1000, e -> {
			fps = counter;
			counter = 0;
			logger.debug("Fps is: " + fps);
		}).start();
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
