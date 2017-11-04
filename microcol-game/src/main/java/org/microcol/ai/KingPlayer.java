package org.microcol.ai;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

public class KingPlayer extends AbstractRobotPlayer {

	private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

	private final Player whosKingThisPlayerIs;

	private ContinentTool continentTool = new ContinentTool();

	/**
	 * Royal Expedition Forces was send to colonies after declaring
	 * independence.
	 */
	private boolean refWasSend = false;

	public KingPlayer(final Model model, final Player player) {
		super(model, player);
		this.whosKingThisPlayerIs = Preconditions.checkNotNull(player.getWhosKingThisPlayerIs());
		// TODO init refWasSend from extra data
	}

	@Override
	void moveUnit(Unit unit) {
		if (whosKingThisPlayerIs.isDeclaredIndependence()) {
			if (refWasSend) {
				createRoyalArmyForces();
			}
			final Continents continents = continentTool.findContinents(getModel(), whosKingThisPlayerIs);

			if (unit.isAtPlaceLocation()) {
				if (unit.getType().isShip()) {
					if (unit.getCargo().isEmpty()) {
						// random move & attack any ship in sight
					} else {
						if (isPossibleToDisembark(unit)) {
							disembarkUnit(unit);
						} else {
							tryToReachSomeContinent(unit, continents.getContinentsToAttack());
							// TODO zkusit vylodit jednotky.
						}
					}
				} else {
					seekAndDestroy(unit, continents);
				}
			}
		}
	}

	private void seekAndDestroy(final Unit unit, final Continents continents) {
		final Optional<Location> oLoc = continents.getContinentWhereIsUnitPlaced(unit).getClosesEnemyCityToAttack(unit);
		if (oLoc.isPresent()) {
			unit.moveTo(Path.of(unit.getPath(oLoc.get(), true).get()));
		}
		simpleUnitBehavior.tryToFight(unit);
	}

	private void tryToReachSomeContinent(final Unit ship, final List<Continent> continentsToAttack) {
		final Optional<List<Location>> oPath = findFasterPathToContinent(ship, continentsToAttack.get(0));
		if (oPath.isPresent()) {
			ship.moveTo(Path.of(oPath.get()));
		}
	}

	private Optional<List<Location>> findFasterPathToContinent(final Unit ship, Continent continent) {
		return continent.getLocations().stream().map(loc -> ship.getPath(loc, true)).filter(oPath -> oPath.isPresent())
				.map(oPath -> oPath.get()).sorted((list1, list2) -> list1.size() - list2.size()).findFirst();
	}

	private void disembarkUnit(final Unit ship) {
		final Location location = canDisembartAt(ship).get(0);
		ship.getCargo().getSlots().forEach(slot -> {
			if (slot.isLoadedUnit()) {
				slot.unload(location);
			}
		});
	}

	private boolean isPossibleToDisembark(final Unit ship) {
		return !canDisembartAt(ship).isEmpty();
	}

	private List<Location> canDisembartAt(final Unit ship) {
		return ship.getLocation().getNeighbors().stream().filter(loc -> ship.isPossibleToDisembarkAt(loc, true))
				.collect(Collectors.toList());
	}

	private void createRoyalArmyForces() {
		int militaryForceToDeploy = getKingsMilitaryStrength();
		Unit cargoShip = null;
		while (militaryForceToDeploy > 0) {
			// create unit, is there ship to load it in? if no, crate new and
			// load it
			if (cargoShip == null || cargoShip.getCargo().isFull()) {
				cargoShip = getModel().createCargoShipForKing(getPlayer());
			}
			getModel().createRoyalExpeditionForceUnit(getPlayer(), cargoShip);
			militaryForceToDeploy--;
		}
	}

	public int getKingsMilitaryStrength() {
		// XXX count it based on user's military strength multiplied by
		// coefficient.
		return 15;
	}
}
