package org.microcol.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import model.Ship;
import model.Tile;
import model.Unit;
import model.World;

public class GamePanel extends JPanel {

  /**
   * Default serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private final Logger logger = Logger.getLogger(GamePanel.class);

  private final static int TILE_WIDTH_IN_PX = 30;

  private final static int GRID_LINE_WIDTH = 1;

  private final static int TOTAL_TILE_WIDTH_IN_PX = TILE_WIDTH_IN_PX + GRID_LINE_WIDTH;

  private Image dbImage;

  private final Image tileSee;

  private final BufferedImage ship1;

  private final BufferedImage ship2;

  private final Cursor gotoModeCursor;

  private final World world = new World();

  private Point cursorTile;

  private boolean gotoMode = false;

  private Unit movedUnit;

  @Inject
  public GamePanel(final StatusBarMessageController statusBarMessageController,
      final KeyController keyController) {
    tileSee = getImage("tile-ocean.png");
    ship1 = getImage("tile-ship1.png");
    ship2 = getImage("tile-ship2.png");
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    gotoModeCursor = toolkit.createCustomCursor(getImage("cursor-goto.png"),
        new java.awt.Point(1, 1), "gotoModeCursor");
    dbImage = createImage(getGameMapWidth(), getGameMapHeight());
    cursorTile = null;
    final GamePanel map = this;

    keyController.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if ('c' == e.getKeyChar()) {
          // chci centrovat
        }
        if ('g' == e.getKeyChar()) {
          // chci posunout jednotku
          if (cursorTile != null) {
            final Tile tile = world.getAt(cursorTile);
            final Unit unit = tile.getFirstMovableUnit();
            // TODO in description panel show unit description
            if (unit != null) {
              switchToGoMode(unit);
            }
          }
        }
      }
    });

    MouseAdapter ma = new MouseAdapter() {

      private Point origin;

      @Override
      public void mousePressed(MouseEvent e) {
        origin = Point.make(e.getX(), e.getY());
        if (gotoMode) {
          switchToNormalMode(convertToTilesCoordinates(origin));
        } else {
          cursorTile = convertToTilesCoordinates(origin);
        }
        statusBarMessageController.fireStatusMessageWasChangedEvent("clicket at " + origin);
        repaint();
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

  private final void switchToGoMode(final Unit unit) {
    logger.debug("Switching '" + unit + "' to go mode.");
    movedUnit = unit;
    gotoMode = true;
    setCursor(gotoModeCursor);
  }

  private final void switchToNormalMode(final Point moveTo) {
    logger.debug("Switching to normalmode.");
    Tile from = world.getAt(cursorTile);
    Tile to = world.getAt(moveTo);
    from.getUnits().remove(movedUnit);
    to.getUnits().add(movedUnit);
    movedUnit = null;
    gotoMode = false;
    cursorTile = moveTo;
    setCursor(Cursor.getDefaultCursor());
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

  private Point convertToTilesCoordinates(final Point panelCoordinates) {
    return Point.make(panelCoordinates.getX() / TOTAL_TILE_WIDTH_IN_PX,
        panelCoordinates.getY() / TOTAL_TILE_WIDTH_IN_PX);
  }

  /**
   * Call original paint with antialising on.
   */
  @Override
  public void paint(final Graphics g) {
    final Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (dbImage == null) {
      dbImage = createImage(getGameMapWidth(), getGameMapHeight());
      if (dbImage == null) {
        return;
      } else {
      }
    }
    if (dbImage != null) {
      final Graphics2D dbg = (Graphics2D) dbImage.getGraphics();
      paintIntoGraphics(dbg);
      paintNet(dbg);
      paintCursor(dbg);
      g.drawImage(dbImage, 0, 0, null);
      // Sync the display on some systems.
      // (on Linux, this fixes event queue problems)
    }
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
        int x = i * TOTAL_TILE_WIDTH_IN_PX;
        int y = j * TOTAL_TILE_WIDTH_IN_PX;
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
  }

  private void paintNet(final Graphics2D graphics) {
    graphics.setColor(Color.LIGHT_GRAY);
    graphics.setStroke(new BasicStroke(1));
    for (int i = 1; i < World.WIDTH; i++) {
      int x = i * TOTAL_TILE_WIDTH_IN_PX - 1;
      graphics.drawLine(x, 0, x,
          World.HEIGHT * TILE_WIDTH_IN_PX + World.HEIGHT * (World.WIDTH - 1));
    }
    for (int j = 1; j < World.HEIGHT; j++) {
      int y = j * TOTAL_TILE_WIDTH_IN_PX - 1;
      graphics.drawLine(0, y, World.HEIGHT * TILE_WIDTH_IN_PX + World.WIDTH * (World.HEIGHT - 1),
          y);
    }
  }

  private void paintCursor(final Graphics2D graphics) {
    if (cursorTile != null) {
      graphics.setColor(Color.RED);
      graphics.setStroke(new BasicStroke(1));
      int x = cursorTile.getX() * TOTAL_TILE_WIDTH_IN_PX - 1;
      int y = cursorTile.getY() * TOTAL_TILE_WIDTH_IN_PX - 1;
      graphics.drawLine(x, y, x + TILE_WIDTH_IN_PX, y);
      graphics.drawLine(x, y, x, y + TILE_WIDTH_IN_PX);
      graphics.drawLine(x + TILE_WIDTH_IN_PX, y, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
      graphics.drawLine(x, y + TILE_WIDTH_IN_PX, x + TILE_WIDTH_IN_PX, y + TILE_WIDTH_IN_PX);
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
    return World.WIDTH * TOTAL_TILE_WIDTH_IN_PX - 1;
  }

  private int getGameMapHeight() {
    return World.HEIGHT * TOTAL_TILE_WIDTH_IN_PX - 1;
  }

}