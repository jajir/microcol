package org.microcol.gui.gamepanel;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;

/**
 * Contains info about focused tile at map.
 *
 */
public class TileWasSelectedEvent {

	private final Model model;

	private final Location location;

	private final TerrainType terrain;
	
	private final boolean isDiscovered; 
	
	public TileWasSelectedEvent(final GameModelController gameModelController, final Location location) {
		this(gameModelController.getModel(), location,
				gameModelController.getModel().getMap().getTerrainAt(location).getTerrainType(),
				gameModelController.getHumanPlayer().isVisible(location));
	}
	
	public TileWasSelectedEvent(final Model game, final Location location, final TerrainType tile,
			final boolean isDiscovered) {
		this.model = Preconditions.checkNotNull(game);
		this.location = Preconditions.checkNotNull(location);
		this.terrain = Preconditions.checkNotNull(tile);
		this.isDiscovered = isDiscovered;
	}

	//TODO use selectedUnitController
	@Deprecated
	public boolean isTileContainsMovebleUnit() {
		final Optional<Unit> unit = model.getUnitsAt(location).stream().findFirst();
		return unit.isPresent() && unit.get().getOwner().equals(model.getCurrentPlayer());
	}
	
	public boolean isPossibleToBuildColony() {
		if (isTileContainsMovebleUnit()) {
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

	/**
	 * If user already explored selected tile.
	 * 
	 * @return return <code>true</code> when selected location was already
	 *         discovered otherwise return <code>false</code>.
	 */
	public boolean isDiscovered() {
		return isDiscovered;
	}

}
