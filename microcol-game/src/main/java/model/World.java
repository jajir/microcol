package model;

import java.util.Arrays;

import org.microcol.gui.NextTurnController;
import org.microcol.gui.Point;

import com.google.inject.Inject;

public class World {

  public final static int WIDTH = 50;

  public final static int HEIGHT = 50;

  private final Tile[][] map = new Tile[WIDTH][HEIGHT];

  private int currentYear;

  private final NextTurnController nextTurnController;

  @Inject
  public World(final NextTurnController nextTurnController) {
    this.nextTurnController = nextTurnController;
    currentYear = 1590;
    for (int i = 0; i < WIDTH; i++) {
      for (int j = 0; j < HEIGHT; j++) {
        map[i][j] = new Tile();
      }
    }
    map[5][5].getUnits().add(new Ship(1));
    map[10][10].getUnits().add(new Ship(0));
  }

  public void nextTurn() {
    Arrays.stream(map).forEach(tileArray -> {
      Arrays.stream(tileArray).forEach(tile -> {
        resetActionPoints(tile);
      });
    });
    currentYear++;
    nextTurnController.fireNextTurnEvent(this);
  }

  private final void resetActionPoints(final Tile tile) {
    tile.getUnits().stream().filter(unit -> unit instanceof Ship).forEach(ship -> {
      ((Ship) ship).resetActionPoints();
    });
  }

  public Tile[][] getMap() {
    return map;
  }

  public Tile getAt(final Point point) {
    return map[point.getX()][point.getY()];
  }

  public int getCurrentYear() {
    return currentYear;
  }

}
