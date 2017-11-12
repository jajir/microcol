package org.microcol.gui.event;

import java.util.Optional;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused
 *
 */
public class FocusedTileEvent {

	private final Model model;

	private final Location location;

	private final TerrainType terrain;

	public FocusedTileEvent(final Model game, final Location location, final TerrainType tile) {
		this.model = Preconditions.checkNotNull(game);
		this.location = Preconditions.checkNotNull(location);
		this.terrain = Preconditions.checkNotNull(tile);
	}

	public boolean isTileContainsMovebleUnit() {
		final Optional<Unit> unit = model.getUnitsAt(location).stream().findFirst();
		return unit.isPresent() && unit.get().getOwner().equals(model.getCurrentPlayer());
	}
	
	public boolean isPossibleToBuildColony(){
		if(isTileContainsMovebleUnit()){
			final Unit unit = model.getUnitsAt(location).stream().findFirst()
					.orElseThrow(() -> new IllegalStateException("It should not be here"));
			return unit.getType().canBuildColony() && unit.getAvailableMoves() > 0;
		}
		return false;
	}

	public Location getLocation() {
		return location;
	}

	public TerrainType getTerrain() {
		return terrain;
	}

	public Model getModel() {
		return model;
	}

}
