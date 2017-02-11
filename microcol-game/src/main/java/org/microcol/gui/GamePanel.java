package org.microcol.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class GamePanel extends JPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  // used for buffer
  private Graphics dbg;

  private Image dbImage = null;

  private final Image background;

  private final MainFrame microColFrame;

  private int diffX;

  private int diffY;

  private Point point;

  public GamePanel(final MainFrame frame) {
    this.microColFrame = frame;
    background = getImage("BirdinPineTree_2560x1600.jpg");

    final GamePanel map = this;

    MouseAdapter ma = new MouseAdapter() {

      private Point origin;

      @Override
      public void mousePressed(MouseEvent e) {
        origin = Point.make(e.getX(), e.getY());
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mouseDragged(MouseEvent e) {
        if (origin != null) {
          JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, map);
          if (viewPort != null) {
            Point delta = origin.substract(Point.make(e.getX(), e.getY()));
            Rectangle view = viewPort.getViewRect();
            view.x += delta.getX();
            view.y += delta.getY();
            map.scrollRectToVisible(view);
          }
        }
      }
    };

    map.addMouseListener(ma);
    map.addMouseMotionListener(ma);

    setAutoscrolls(true);
  }

  /**
   * Simplify loading image from resource. Path should look like: <code>
   * org/microcol/images/unit-60x60.gif
   * </code>
   * 
   * @param path
   *          path at classpath where is stored image
   * @return image object
   */
  public Image getImage(final String path) {
    try {
      ClassLoader cl = this.getClass().getClassLoader();
      return ImageIO.read(cl.getResourceAsStream(path));
    } catch (IOException e) {
      final String msg = "Unable to load file '" + path + "'.";
      System.out.println(msg);
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Call original paint with antialising on.
   */
  @Override
  public void paint(Graphics g) {
    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    if (dbImage == null) {
      dbImage = createImage(2560, 1600);
      if (dbImage == null) {
        return;
      } else
        dbg = dbImage.getGraphics();
    }
    paintIntoGraphics(dbg);
    g.setColor(Color.black);
    g.fillRect(0, 0, 2560, 1600);
    g.drawImage(dbImage, 0, 0, null);
    // Sync the display on some systems.
    // (on Linux, this fixes event queue problems)
    Toolkit.getDefaultToolkit().sync();
  }

  /**
   * Draw main game view.
   * 
   * @param g
   */
  private void paintIntoGraphics(final Graphics graphics) {
    graphics.drawImage(background, 0, 0, this);
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(2560, 1600);
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(2560, 1600);
  }

}
