package org.microcol.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

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
    diffX = 0;
    diffY = 0;
    addMouseListener(new MouseListener() {

      @Override
      public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mousePressed(MouseEvent e) {
        onMousePressed(e.getXOnScreen(), e.getYOnScreen());
      }

      @Override
      public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

      }
    });
    addMouseMotionListener(new MouseMotionListener() {

      @Override
      public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseDragged(MouseEvent e) {
        onMouseDragged(e.getX(), e.getY());
      }
    });
  }

  private final void onMousePressed(final int x, final int y) {
    point = Point.make(x, y);
  }

  private final void onMouseDragged(final int x, final int y) {
    Point to = Point.make(x, y);
    Point diff = to.substract(point);
    System.out.println("diff " + diff.toString() + " to: " + to.toString());
    microColFrame.mouseDragged(diff.getX(), diff.getY());
    point = Point.make(to.getX() - diff.getX(), to.getY() - diff.getY());
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
