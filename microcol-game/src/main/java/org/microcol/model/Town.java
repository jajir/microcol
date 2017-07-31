package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Town {

	/**
	 * Town unique name.
	 */
	private String name;

	private final Player owner;

	private final Location location;

	private final List<TownSection> townSection;

	private Model model;

	public Town(String name, final Player owner, final Location location) {
		this.name = name;
		this.owner = Preconditions.checkNotNull(owner);
		this.location = Preconditions.checkNotNull(location);
		townSection = new ArrayList<>();
		location.getNeighbors().forEach(loc -> townSection.add(new TownSection(loc, null)));
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public Player getOwner() {
		return owner;
	}

	public List<TownSection> getTownSection() {
		return townSection;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Town.class).add("name", name).add("location", location).toString();
	}
}
