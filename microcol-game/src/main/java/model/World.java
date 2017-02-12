package model;

import org.microcol.gui.Point;

public class World {

  public final static int WIDTH = 50;

  public final static int HEIGHT = 50;

  private final Tile[][] map = new Tile[WIDTH][HEIGHT];

  public World() {
    for (int i = 0; i < WIDTH; i++) {
      for (int j = 0; j < HEIGHT; j++) {
        map[i][j] = new Tile();
      }
    }
    map[10][10].getUnits().add(new Ship(1));
    map[20][20].getUnits().add(new Ship(0));
  }

  public Tile[][] getMap() {
    return map;
  }

  public Tile getAt(final Point point) {
    return map[point.getX()][point.getY()];
  }

}
