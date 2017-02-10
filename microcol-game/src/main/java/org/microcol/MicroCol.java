package org.microcol;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * MicroCol's main class.
 */
public class MicroCol {
  /**
   * Creates and displays the form.
   * 
   * @param args
   *          the command line arguments
   */
  public static void main(final String args[]) {
    SwingUtilities.invokeLater(() -> {
      MicroColFrame frame = new MicroColFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setSize(600, 400);
      frame.setLocationRelativeTo(null);
      frame.setVisible(true);
    });
  }
}
