package org.microcol.ai;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Define kings behavior.
 *
 */
public class King {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final Model model;

	private final Player kingPlayer;

	private final Player whosKingThisPlayerIs;

	private PathTool pathTool = new PathTool();

	/**
	 * Royal Expedition Forces was send to colonies after declaring
	 * independence.
	 */
	private boolean refWasSend = false;

	public King(final Model model, final Player kingPlayer, Player whosKingThisPlayerIs) {
		this.model = Preconditions.checkNotNull(model);
		this.kingPlayer = Preconditions.checkNotNull(kingPlayer);
		this.whosKingThisPlayerIs = Preconditions.checkNotNull(whosKingThisPlayerIs);
	}

	public void start() {
		model.addListener(new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().equals(kingPlayer)) {
					turn();
					kingPlayer.endTurn();
				}
			}
		});
		logger.info("AI engine started.");
	}

	/**
	 * When King is on turn this method is called. Method should perform all
	 * unit moving.
	 */
	void turn() {
		if (whosKingThisPlayerIs.isDeclaredIndependence()) {
			if (refWasSend) {
				// starting war for independence
				createRoyalArmyForces();
			} else {
				// find units on continent, attack city
				// XXX what if there is no city to attack???, disband unit and
				// create new in europe???
				final List<Continent> continentsToAttack = pathTool.findContinentsToAttack(model, whosKingThisPlayerIs);
				getKingsShipsOnSea().forEach(ship -> {
					if (ship.getCargo().isEmpty()) {
						// random move & attack any ship in sight
					} else {
						if (isPossibleToDisembark(ship)) {
							disembarkUnit(ship);
						} else {
							tryToReachSomeContinent(ship, continentsToAttack);
							//TODO zkusit vylodit jednotky.
						}
					}
				});
				getKingsRegularMoveableUnits().forEach(unit -> {
					//can unit immediatly attack some city?
					//find list of enemy cities on ontinent where is this unit.
					//select first city, go near
				});
			}
		}
	}

	private void tryToReachSomeContinent(final Unit ship, final List<Continent> continentsToAttack) {
		final Optional<List<Location>> oPath = findFasterPathToContinent(ship, continentsToAttack.get(0));
		if (oPath.isPresent()) {
			ship.moveTo(Path.of(oPath.get()));
		}
	}

	private Optional<List<Location>> findFasterPathToContinent(final Unit ship, Continent continent) {
		return continent.getLocations().stream()
			.map(loc -> ship.getPath(loc, true)).filter(oPath -> oPath.isPresent())
			.map(oPath -> oPath.get())
			.sorted((list1, list2) -> list1.size() - list2.size())
			.findFirst();
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

	private List<Unit> getKingsShipsOnSea() {
		return kingPlayer.getAllUnits().stream().filter(unit -> unit.getType().isShip() && unit.isAtPlaceLocation())
				.collect(ImmutableList.toImmutableList());
	}

	private List<Unit> getKingsRegularMoveableUnits() {
		return kingPlayer.getAllUnits().stream().filter(unit -> !unit.getType().isShip() && unit.isAtPlaceLocation())
				.collect(ImmutableList.toImmutableList());
	}

	private void createRoyalArmyForces() {
		int militaryForceToDeploy = getKingsMilitaryStrength();
		Unit cargoShip = null;
		while (militaryForceToDeploy > 0) {
			// create unit, is there ship to load it in? if no, crate new and
			// load it
			if (cargoShip == null || cargoShip.getCargo().isFull()) {
				cargoShip = model.createCargoShipForKing(kingPlayer);
			}
			model.createRoyalExpeditionForceUnit(kingPlayer, cargoShip);
			militaryForceToDeploy--;
		}
	}

	public int getKingsMilitaryStrength() {
		// XXX count it based on user's military strength multiplied by
		// coefficient.
		return 15;
	}
}
