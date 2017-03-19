package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Ship {
	private Model model;

	private final Player owner;
	private final int maxMoves;

	private Location location;
	private int availableMoves;

	Ship(final Player owner, final int maxMoves, final Location location) {
		this.owner = Preconditions.checkNotNull(owner);

		Preconditions.checkArgument(maxMoves > 0, "Number of maximum moves (%s) must be positive.", maxMoves);
		this.maxMoves = maxMoves;

		this.location = location;
		this.availableMoves = maxMoves;
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
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

	void startTurn() {
		availableMoves = maxMoves;
	}

	public void moveTo(final Path path) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getStart().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getStart(), location);
		Preconditions.checkArgument(model.getWorld().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkState(model.isActive(), "Game must be active.");
		Preconditions.checkState(owner.equals(model.getCurrentPlayer()), "Current player (%s) is not owner (%s) of this ship (%s).", model.getCurrentPlayer(), owner, this);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyShipsAt().keySet()), "There is enemy ship on path (%s).", path);

		final Location start = location;
		final List<Location> pathBuilder = new ArrayList<>(); // TODO JKA Rename
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				break;
			}
			if (model.getWorld().getTerrainAt(newLocation) != Terrain.OCEAN) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only ocean (%s).", newLocation, model.getWorld().getTerrainAt(newLocation)));
			}
			pathBuilder.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!pathBuilder.isEmpty()) {
			model.fireShipMoved(this, start, Path.of(pathBuilder));
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
