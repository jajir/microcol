package model;

public class Ship extends Unit {

  private final int type;

  public Ship(final int type) {
    this.type = type;
  }

  public int getType() {
    return type;
  }

}
