package org.microcol.gui.panelview;

import java.util.concurrent.CountDownLatch;

/**
 * Allows to stop waiting threads until some operation is done. 
 */
public class AnimationLatch {

	private CountDownLatch latch;
	
	//TODO some parts of methods should be synchronized
	//TODO add some unit test

	/**
	 * Could be called from any thread. Just first call make it lock other calls
	 * even from different threads doesn't have any effect.
	 */
	void lock() {
		if (latch == null) {
			latch = new CountDownLatch(1);
		}
	}

	/**
	 * Release lock and all waiting processes could continue.
	 */
	void unlock() {
		if (latch != null) {
			latch.countDown();
			latch = null;
		}
	}

	/**
	 * Any thread that come to this method will be stopped until lock is
	 * released.
	 */
	void waitForUnlock() {
		if (latch != null) {
			try {
				latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
