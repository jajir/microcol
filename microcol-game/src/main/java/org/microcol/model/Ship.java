package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Ship {
	private Model model;

	private final Player owner;
	private final ShipType type;

	private Location location;
	private int availableMoves;

	Ship(final Player owner, final ShipType type, final Location location) {
		this.owner = Preconditions.checkNotNull(owner);
		this.type = Preconditions.checkNotNull(type);
		this.location = Preconditions.checkNotNull(location);

		this.availableMoves = type.getSpeed();
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

	public ShipType getType() {
		return type;
	}

	public Location getLocation() {
		return location;
	}

	public int getAvailableMoves() {
		return availableMoves;
	}

	void startTurn() {
		availableMoves = type.getSpeed();
	}

	// Netestuje nedosažitelné lokace, pouze jestli je teoreticky možné na danou lokaci vstoupit
	public boolean isMoveable(final Location location) {
		Preconditions.checkNotNull(location);

		if (!model.getWorld().isValid(location)) {
			return false;
		}

		if (model.getWorld().getTerrainAt(location) != Terrain.OCEAN) {
			return false;
		}

		return owner.getEnemyShipsAt(location).isEmpty();
	}

	public List<Location> getAvailableLocations() {
		model.checkGameActive();
		model.checkCurrentPlayer(owner);

		if (availableMoves == 0) {
			return ImmutableList.of();
		}

		Set<Location> openSet = new HashSet<>();
		openSet.add(location);
		Set<Location> closedSet = new HashSet<>();
		for (int i = 0; i < availableMoves + 1; i++) {
			Set<Location> currentSet = new HashSet<>();
			for (Location location : openSet) {
				for (Location neighbor : location.getNeighbors()) {
					if (isMoveable(neighbor)) {
						currentSet.add(neighbor);
					}
				}
				closedSet.add(location);
			}
			openSet.clear();
			openSet.addAll(currentSet);
		}
		closedSet.remove(location);

		return ImmutableList.copyOf(closedSet);
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
			.add("type", type)
			.add("location", location)
			.add("availableMoves", availableMoves)
			.toString();
	}
}
