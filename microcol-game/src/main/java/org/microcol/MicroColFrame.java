package org.microcol;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class MicroColFrame extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final JScrollPane scrollPaneGamePanel;

  public MicroColFrame() {
    super("MicroCol");
    GamePanel gamePanel = new GamePanel(this);

    scrollPaneGamePanel = new JScrollPane(gamePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    add(scrollPaneGamePanel);
  }

  public void mouseDragged(final int x, final int y) {
    scrollPaneGamePanel.getVerticalScrollBar()
        .setValue(scrollPaneGamePanel.getVerticalScrollBar().getValue() - y);
    scrollPaneGamePanel.getHorizontalScrollBar()
        .setValue(scrollPaneGamePanel.getHorizontalScrollBar().getValue() - x);
  }

}
