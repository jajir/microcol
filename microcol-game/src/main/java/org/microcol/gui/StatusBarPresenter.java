package org.microcol.gui;

import javax.swing.JLabel;

import com.google.inject.Inject;

public class StatusBarPresenter {

  public interface Display {
    JLabel getStatusBarDescription();
  }

  @Inject
  public StatusBarPresenter(final StatusBarPresenter.Display display,
      final StatusBarMessageController statusBarMessageController) {
    statusBarMessageController.addStatusMessageListener(message -> {
      display.getStatusBarDescription().setText(message);
    });
  }

}
