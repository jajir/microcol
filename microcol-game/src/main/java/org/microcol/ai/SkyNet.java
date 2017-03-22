package org.microcol.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelAdapter;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Ship;
import org.microcol.model.event.TurnStartedEvent;

public class SkyNet {
	private final Model model;
	private final Random random;
	private final Map<Ship, Location> lastDirections;

	public SkyNet(final Model model) {
		this.model = model;
		this.random = new Random();
		this.lastDirections = new HashMap<>();
	}

	public void searchAndDestroy() {
		model.addListener(new ModelAdapter() {
			@Override
			public void turnStarted(TurnStartedEvent event) {
				if (event.getPlayer().isComputer()) {
					turn(event.getPlayer());
				}
			}
		});
	}

	private void turn(final Player player) {
		player.getShips().forEach(ship -> move(ship));
		player.endTurn();
	}

	private void move(final Ship ship) {
//		System.out.println("### " + ship.getLocation() + "  " + ship.getType().getSpeed());
//		long start = System.currentTimeMillis();
//		System.out.println(ship.getAvailableLocations());
//		System.out.println("FINISHED IN " + (System.currentTimeMillis() - start));

		if (lastDirections.get(ship) == null) {
			lastDirections.put(ship, Location.DIRECTIONS.get(random.nextInt(Location.DIRECTIONS.size())));
		}

		final List<Location> directions = new ArrayList<>(Location.DIRECTIONS);
		Location lastLocation = ship.getLocation();
		final List<Location> locations = new ArrayList<>();
		while (locations.size() < ship.getAvailableMoves()) {
			final Location lastDirection = lastDirections.get(ship);
			final Location newLocation = lastLocation.add(lastDirection);
			if (ship.isReachable(newLocation, true)) {
				locations.add(newLocation);
				lastLocation = newLocation;
			} else {
				directions.remove(lastDirection);
				if (directions.isEmpty()) {
					break;
				}
				lastDirections.put(ship, directions.get(random.nextInt(directions.size())));
			}
		}

		if (!locations.isEmpty()) {
			ship.moveTo(Path.of(locations));
		}
	}
}
