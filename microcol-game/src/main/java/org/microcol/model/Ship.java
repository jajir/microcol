package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

// TODO JKA Documentation
// TODO JKA Tests
public class Ship {
	private Game game;

	private final Player owner;
	private final int maxMoves;

	private Location location;
	private int availableMoves;

	protected Ship(final Player owner, final int maxMoves, final Location location) {
		this.owner = Preconditions.checkNotNull(owner);

		Preconditions.checkArgument(maxMoves > 0, "Number of maximum moves (%s) must be positive.", maxMoves);
		this.maxMoves = maxMoves;

		this.location = location;
		this.availableMoves = maxMoves;
	}

	protected void setGame(final Game game) {
		this.game = Preconditions.checkNotNull(game);
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

	protected void startTurn() {
		availableMoves = maxMoves;
	}

	public void moveTo(final Path path) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getFirstLocation().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getFirstLocation(), location);
		Preconditions.checkArgument(game.getMap().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkState(game.isActive(), "Game must be active.");
		Preconditions.checkState(owner.equals(game.getCurrentPlayer()), "Current player (%s) is not owner (%s) of this ship (%s).", game.getCurrentPlayer(), owner, this);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyShipsAt().keySet()), "There is enemy ship on path (%s).", path);

		final Location startLocation = location;
		final PathBuilder pathBuilder = new PathBuilder();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				break;
			}
			pathBuilder.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!pathBuilder.isEmpty()) {
			game.fireShipMoved(game, this, startLocation, pathBuilder.build());
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
