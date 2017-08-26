package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Helps build town.
 */
public class TownBuilder {

	private final String name;
	
	private final PlayerBuilder playerBuilder;
	
	private Location location;

	public TownBuilder(final String name, final PlayerBuilder playerBuilder) {
		this.name = Preconditions.checkNotNull(name);
		this.playerBuilder = Preconditions.checkNotNull(playerBuilder);
	}

	public PlayerBuilder make() {
		return playerBuilder;
	}

	public TownBuilder setLocation(final Location location) {
		this.location = location;
		return this;
	}

	String getName() {
		return name;
	}

	Location getLocation() {
		return location;
	}

}
