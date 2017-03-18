package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

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
		Preconditions.checkArgument(path.getStart().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getStart(), location);
		Preconditions.checkArgument(game.getMap().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkState(game.isActive(), "Game must be active.");
		Preconditions.checkState(owner.equals(game.getCurrentPlayer()), "Current player (%s) is not owner (%s) of this ship (%s).", game.getCurrentPlayer(), owner, this);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyShipsAt().keySet()), "There is enemy ship on path (%s).", path);

		final Location startLocation = location;
		final List<Location> pathBuilder = new ArrayList<>(); // TODO JKA Rename
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				break;
			}
			if (game.getMap().getTerrainAt(newLocation) != Terrain.OCEAN) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only ocean (%s).", newLocation, game.getMap().getTerrainAt(newLocation)));
			}
			pathBuilder.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!pathBuilder.isEmpty()) {
			game.fireShipMoved(game, this, startLocation, Path.of(pathBuilder));
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
