package org.microcol.model;

import com.google.common.base.Preconditions;

public class Town {

	/**
	 * Town unique name.
	 */
	private String name;

	private final Player owner;

	private final Location location;

	private Model model;

	public Town(String name, final Player owner, final Location location) {
		this.name = name;
		this.owner = Preconditions.checkNotNull(owner);
		this.location = Preconditions.checkNotNull(location);
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

}
