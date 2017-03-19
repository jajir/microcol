package org.microcol.gui.event;

import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Ship;
import org.microcol.model.Terrain;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused
 *
 */
public class FocusedTileEvent {

	private final Model game;

	private final Location location;

	private final Terrain terrain;

	public FocusedTileEvent(final Model game, final Location location, final Terrain tile) {
		this.game = Preconditions.checkNotNull(game);
		this.location = Preconditions.checkNotNull(location);
		this.terrain = Preconditions.checkNotNull(tile);
	}

	public boolean isTileContainsMovebleUnit() {
		final Optional<Ship> unit = game.getShipsAt(location).stream().findFirst();
		return unit.isPresent() && unit.get().getOwner().equals(game.getCurrentPlayer());
	}

	public Location getLocation() {
		return location;
	}

	public Terrain getTerrain() {
		return terrain;
	}

}
