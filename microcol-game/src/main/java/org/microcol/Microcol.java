package org.microcol;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * Main Microcol class.
 * 
 */
public class Microcol {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(final String args[]) {
		/* Create and display the form */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame frame = new JFrame("Microcol");
				frame.setVisible(true);
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setSize(200, 100);
			}
		});
	}

}
