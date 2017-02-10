package org.microcol;

import javax.swing.SwingUtilities;

import org.microcol.gui.MainFrame;

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
      MainFrame mainFrame = new MainFrame();
      mainFrame.setVisible(true);
    });
  }
}
