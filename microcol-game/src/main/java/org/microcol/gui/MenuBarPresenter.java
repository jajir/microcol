package org.microcol.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

public class MenuBarPresenter {

  public interface Display {
    JMenuItem getMenuItemNewGame();

    JMenuItem getMenuItemLoadGame();

    JMenuItem getMenuItemSameGame();

    JMenuItem getMenuItemQuitGame();
  }

  public MenuBarPresenter(final MenuBarPresenter.Display display) {
    display.getMenuItemNewGame().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent evt) {

      }
    });
    display.getMenuItemQuitGame().addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent evt) {

      }
    });
  }

}
