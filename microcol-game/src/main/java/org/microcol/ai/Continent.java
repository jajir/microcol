package org.microcol.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Continent {

	private final List<Location> locations = new ArrayList<>();

	private final Model model;

	private final Player enemyPlayer;

	Continent(final Model model, final Player enemyPlayer) {
		this.model = Preconditions.checkNotNull(model);
		this.enemyPlayer = Preconditions.checkNotNull(enemyPlayer);
	}

	public void add(final Location location) {
		locations.add(location);
	}

	public boolean contains(final Location location) {
		return locations.contains(location);
	}
	
	public List<Location> getLocations() {
		return ImmutableList.copyOf(locations);
	}

	public int getColonyWeight() {
		int out = 0;
		for (final Location loc : locations) {
			Optional<Colony> oColony = model.getColoniesAt(loc, enemyPlayer);
			if (oColony.isPresent()) {
				Colony col = oColony.get();
				out += col.getMilitaryForce();
			}
		}
		return out;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Continents.class).add("weight", getColonyWeight()).add("locations", locations)
				.toString();
	}

}
