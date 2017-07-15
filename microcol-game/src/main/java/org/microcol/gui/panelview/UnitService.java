package org.microcol.gui.panelview;

import java.util.List;

import org.microcol.gui.event.model.GameController;
import org.microcol.model.CargoSlot;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Provide basic unit operations and checks.
 *
 */
public class UnitService {

	private final GameController gameController;

	@Inject
	public UnitService(final GameController gameController) {
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	/**
	 * Return true when given unit have free cargo slot for unit.
	 * 
	 * @param unit
	 *            required unit
	 * @return return <code>true</code> when at least one cargo slot is empty
	 *         and could be loaded otherwise return <code>false</code>
	 */
	public boolean isAtLeastOneCargoSlotEmpty(final Unit unit) {
		return unit.getType().getCargoCapacity() > 0
				&& unit.getHold().getSlots().stream().filter(cargoSlot -> cargoSlot.isEmpty()).findAny().isPresent();
	}

	/**
	 * Get info it unit could move to target location.
	 * 
	 * @param movingUnit
	 *            required moving unit
	 * @param moveToLocation
	 *            required target location
	 * @return return <code>true</code> when unit could move to target location
	 *         otherwise return <code>false</code>
	 */
	public boolean canMove(final Unit movingUnit, final Location moveToLocation) {
		final List<Unit> ships = gameController.getModel().getUnitsAt(moveToLocation);
		if (ships.isEmpty()) {
			return true;
		} else {
			return isSameOwner(movingUnit, ships);
		}
	}

	public boolean canEmbark(final Unit movingUnit, final Location moveToLocation) {
		if (movingUnit.isStorable()) {
			final List<Unit> units = gameController.getModel().getUnitsAt(moveToLocation);
			return isSameOwner(movingUnit, units)
					&& units.stream().filter(unit -> isAtLeastOneCargoSlotEmpty(unit)).findAny().isPresent();
		} else {
			return false;
		}
	}

	public boolean canDisembark(final Unit movingUnit, final Location moveToLocation) {
		if (movingUnit.getType().getCargoCapacity() > 0) {
			return movingUnit.getHold().getSlots().stream()
					.filter(cargoSlot -> canCargoDisembark(cargoSlot, moveToLocation)).findAny().isPresent();
		} else {
			return false;
		}
	}

	private boolean canCargoDisembark(final CargoSlot slot, final Location moveToLocation) {
		if (slot.isEmpty()) {
			return false;
		} else {
			final Unit unit = slot.getUnit().get();
			return unit.getAvailableMoves() > 0 && unit.getType().getMoveableTerrain()
					.equals(gameController.getModel().getMap().getTerrainAt(moveToLocation));
		}
	}

	public boolean canFight(final Unit movingUnit, final Location moveToLocation) {
		if (gameController.getModel().getUnitsAt(moveToLocation).isEmpty()) {
			return false;
		} else {
			if (movingUnit.getType().getMoveableTerrain()
					.equals(gameController.getModel().getMap().getTerrainAt(moveToLocation))) {
				if (isSameOwner(movingUnit, gameController.getModel().getUnitsAt(moveToLocation))) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}

	public boolean isSameOwner(final Unit unit, final List<Unit> units) {
		// XXX could it be rewritten to stream API?
		return units.size() > 0 && units.get(0).getOwner().equals(unit.getOwner());
	}

}
