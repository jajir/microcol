package org.microcol.gui;

import javax.swing.JLabel;

import com.google.inject.Inject;

public class StatusBarPresenter {

  public interface Display {
    JLabel getStatusBarDescription();

    JLabel getLabelEra();
  }

  @Inject
  public StatusBarPresenter(final StatusBarPresenter.Display display,
      final StatusBarMessageController statusBarMessageController,
      final NextTurnController nextTurnController) {
    statusBarMessageController.addStatusMessageListener(message -> {
      display.getStatusBarDescription().setText(message);
    });
    nextTurnController.addNextTurnListener(world -> {
      display.getLabelEra().setText("Year: " + world.getCurrentYear() + " AD");
    });
  }

}
