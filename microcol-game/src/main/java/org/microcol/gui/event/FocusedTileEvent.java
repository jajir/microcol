package org.microcol.gui.event;

import java.util.Optional;

import org.microcol.gui.model.TileOcean;
import org.microcol.model.Model;
import org.microcol.model.Location;
import org.microcol.model.Ship;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused
 *
 */
public class FocusedTileEvent {

	private final Model game;

	private final Location location;

	private final TileOcean tile;

	public FocusedTileEvent(final Model game, final Location location, final TileOcean tile) {
		this.game = Preconditions.checkNotNull(game);
		this.location = Preconditions.checkNotNull(location);
		this.tile = Preconditions.checkNotNull(tile);
	}

	public boolean isTileContainsMovebleUnit() {
		final Optional<Ship> unit = game.getShipsAt(location).stream().findFirst();
		return unit.isPresent() && unit.get().getOwner().equals(game.getCurrentPlayer());
	}

	public Location getLocation() {
		return location;
	}

	public TileOcean getTile() {
		return tile;
	}

}
