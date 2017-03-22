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

		final Terrain terrain = this.model.getWorld().getTerrainAt(location);
		if (terrain != Terrain.OCEAN) {
			throw new IllegalStateException(String.format("Ship must start on ocen (%s -> %s).", location, terrain));
		}
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

	public boolean isReachable(final Location location, final boolean includeShips) {
		Preconditions.checkNotNull(location);

		if (!model.getWorld().isValid(location)) {
			return false;
		}

		if (model.getWorld().getTerrainAt(location) != Terrain.OCEAN) {
			return false;
		}

		// TODO JKA FIND PATH

		if (!includeShips) {
			return true;
		}

		return model.getEnemyShipsAt(owner, location).isEmpty();
	}

	public int movementCost(final Location toLocation) {
		Preconditions.checkNotNull(toLocation);

		return movementCost(location, toLocation);
	}

	public int movementCost(final Location fromLocation, final Location toLocation) {
		Preconditions.checkNotNull(fromLocation);
		Preconditions.checkNotNull(toLocation);
		Preconditions.checkArgument(fromLocation.isAdjacent(toLocation), "Locations (%s - %s) are not adjacent.", fromLocation, toLocation);

		return model.getWorld().getTerrainAt(toLocation) == Terrain.OCEAN ? 1 : -1;
	}

	public void moveTo(final Path path) {
		model.checkGameActive();
		model.checkCurrentPlayer(owner);

		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.getStart().isAdjacent(location), "Path (%s) must be adjacent to current location (%s).", path.getStart(), location);
		Preconditions.checkArgument(model.getWorld().isValid(path), "Path (%s) must be valid.", path);
		Preconditions.checkArgument(!path.containsAny(owner.getEnemyShipsAt().keySet()), "There is enemy ship on path (%s).", path);

		final Location start = location;
		final List<Location> locations = new ArrayList<>();
		// TODO JKA Use streams
		for (Location newLocation : path.getLocations()) {
			if (availableMoves <= 0) {
				// TODO JKA neumožnit zadat delší cestu, než je povolený počet
				break;
			}
			if (model.getWorld().getTerrainAt(newLocation) != Terrain.OCEAN) {
				throw new IllegalArgumentException(String.format("Path (%s) must contain only ocean (%s).", newLocation, model.getWorld().getTerrainAt(newLocation)));
			}
			locations.add(newLocation);
			location = newLocation;
			availableMoves--;
		}
		if (!locations.isEmpty()) {
			model.fireShipMoved(this, start, Path.of(locations));
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
