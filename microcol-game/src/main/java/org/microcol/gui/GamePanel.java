package org.microcol.gui;

import java.awt.BasicStroke;
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
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import model.Ship;
import model.Unit;
import model.World;

public class GamePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private final static int TILE_WIDTH_IN_PX = 30;

  private final static int GRID_LINE_WIDTH = 1;

  // used for buffer
  private Graphics dbg;

  private Image dbImage = null;

  private final Image background;

  private final Image tileSee;

  private final BufferedImage ship1;

  private final BufferedImage ship2;

  private final MainFrame microColFrame;

  private int diffX;

  private int diffY;

  private Point point;

  private final World world = new World();

  public GamePanel(final MainFrame frame) {
    this.microColFrame = frame;
    background = getImage("BirdinPineTree_2560x1600.jpg");
    tileSee = getImage("tile-ocean.png");
    ship1 = getImage("tile-ship1.png");
    ship2 = getImage("tile-ship2.png");

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
  public BufferedImage getImage(final String path) {
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
    paintIntoGraphics((Graphics2D) dbg);
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
  private void paintIntoGraphics(final Graphics2D graphics) {
    for (int i = 0; i < World.WIDTH; i++) {
      for (int j = 0; j < World.HEIGHT; j++) {
        int x = i * TILE_WIDTH_IN_PX + i * GRID_LINE_WIDTH;
        int y = j * TILE_WIDTH_IN_PX + j * GRID_LINE_WIDTH;
        graphics.drawImage(tileSee, x, y, this);

        if (!world.getMap()[i][j].getUnits().isEmpty()) {
          Unit u = world.getMap()[i][j].getUnits().get(0);
          if (u instanceof Ship) {
            Ship s = (Ship) u;
            if (s.getType() == 0) {
              graphics.drawImage(ship1, x, y, this);
            } else {
              graphics.drawImage(ship2, x, y, this);
            }
          }
        }

      }
    }
    paintNet(graphics);
  }

  private void paintNet(final Graphics2D graphics) {
    graphics.setColor(Color.RED);
    graphics.setStroke(new BasicStroke(1));
    for (int i = 1; i < World.WIDTH; i++) {
      int x = i * TILE_WIDTH_IN_PX + i * GRID_LINE_WIDTH - 1;
      graphics.drawLine(x, 0, x,
          World.HEIGHT * TILE_WIDTH_IN_PX + World.HEIGHT * (World.WIDTH - 1));
    }
    for (int j = 1; j < World.HEIGHT; j++) {
      int y = j * TILE_WIDTH_IN_PX + j * GRID_LINE_WIDTH - 1;
      graphics.drawLine(0, y, World.HEIGHT * TILE_WIDTH_IN_PX + World.WIDTH * (World.HEIGHT - 1),
          y);
    }
  }

  @Override
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  @Override
  public Dimension getMinimumSize() {
    return new Dimension(getGameMapWidth(), getGameMapHeight());
  }

  private int getGameMapWidth() {
    return World.WIDTH * (TILE_WIDTH_IN_PX + GRID_LINE_WIDTH);
  }

  private int getGameMapHeight() {
    return World.HEIGHT * (TILE_WIDTH_IN_PX + GRID_LINE_WIDTH);
  }

}
