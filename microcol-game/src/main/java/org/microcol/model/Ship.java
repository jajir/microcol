package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Ship {
	private final Player owner;
	private final int maxMoves;

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

	protected void startTurn() {
		availableMoves = maxMoves;
	}

	public void moveTo(final Path path) {
		// TODO JKA Check game state
		// TODO JKA Check currentPlayer
		// TODO JKA Check path valid on map
		// TODO JKA Check current location + path

		// TODO JKA Komplet předělat.
		final Location startLocation = location;
		List<Location> moves = new ArrayList<>();
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				break;
			}

			if (!location.equals(newLocation)) {
				moves.add(newLocation);
				location = newLocation;
				availableMoves--;
			}
		}
		if (!moves.isEmpty()) {
			Game.getInstance().getListenersManager().fireShipMoved(Game.getInstance(), this, startLocation, new Path(moves));
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
