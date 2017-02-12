package org.microcol;

import javax.swing.SwingUtilities;

import org.microcol.gui.MainFrame;
import org.microcol.gui.MicroColModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

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
      final Injector injector = Guice.createInjector(Stage.PRODUCTION, new MicroColModule());

      final MainFrame mainFrame = injector.getInstance(MainFrame.class);
      mainFrame.setVisible(true);
    });
  }
}
