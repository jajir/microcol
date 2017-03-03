package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Ship {
	private final Player owner;
	private final int maxMoves;

	private Game game;
	private Location location;
	private int availableMoves;

	protected Ship(final Player owner, final int maxMoves, final Location location) {
		this.owner = Preconditions.checkNotNull(owner);

		Preconditions.checkArgument(maxMoves > 0, "Number of maximum moves must be positive: %s", maxMoves);
		this.maxMoves = maxMoves;

		this.location = location;
		this.availableMoves = maxMoves;
	}

	public Player getOwner() {
		return owner;
	}

	public int getMaxMoves() {
		return maxMoves;
	}

	public Location getLocation() {
		return location;
	}

	public int getAvailableMoves() {
		return availableMoves;
	}

	protected void startGame(final Game game) {
		this.game = game;
	}

	protected void startTurn() {
		availableMoves = maxMoves;
	}

	public void moveTo(final Path path) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getFirstLocation().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getFirstLocation(), location);
		Preconditions.checkArgument(game.getMap().isValid(path), "Invalid path: %s", path);
		Preconditions.checkState(game.isActive(), "Game must be active.");
		Preconditions.checkState(owner.equals(game.getCurrentPlayer()), "Current player (%s) is not ship owner (%s).", game.getCurrentPlayer(), owner);

		// TODO JKA Use streams
		for (Ship ship : game.getShips()) {
			if (!owner.equals(ship.getOwner()) && path.contains(ship.getLocation())) {
				Preconditions.checkArgument(false, "Enemy ship (%s) on path (%s).", ship, path);
			}
		}

		final Location startLocation = location;
		final PathBuilder newPath = new PathBuilder();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				break;
			}
			newPath.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!newPath.isEmpty()) {
			game.getListenersManager().fireShipMoved(game, this, startLocation, newPath.build());
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("owner", owner)
			.add("maxMoves", maxMoves)
			.add("location", location)
			.add("availableMoves", availableMoves)
			.toString();
	}
}
