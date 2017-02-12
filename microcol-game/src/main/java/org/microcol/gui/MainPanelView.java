package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.inject.Inject;

public class MainPanelView extends JPanel {

  /**
   * Default serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  @Inject
  public MainPanelView(final GamePanel gamePanel, final StatusBarView statusBar) {
    this.setLayout(new GridBagLayout());
    JScrollPane scrollPaneGamePanel = new JScrollPane(gamePanel,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scrollPaneGamePanel, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D,
        GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

    add(statusBar, new GridBagConstraints(0, 1, 1, 1, 1.0D, 0.0D, GridBagConstraints.NORTH,
        GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

  }

}
