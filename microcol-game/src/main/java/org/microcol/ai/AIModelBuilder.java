package org.microcol.ai;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
import org.microcol.model.ShipType;

public class AIModelBuilder {
	static void createMinimal(final ModelBuilder builder) {
		builder.setMap(1, 1)
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1));
	}

	static void createHuge(final ModelBuilder builder) {
		builder.setMap(1000, 1000)
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1));
	}

	static void createMap01(final ModelBuilder builder) {
		builder
//			.setMap(15, 10)
			.setMap("/maps/map-01.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(4, 2))
//				.addShip("Player1", ShipType.FRIGATE, Location.of(3, 3))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(7, 7))
//				.addShip("Player2", ShipType.FRIGATE, Location.of(7, 9))
//				.addShip("Player2", ShipType.FRIGATE, Location.of(14, 9))
		;
	}

	static void createMap02(final ModelBuilder builder) {
		builder.setMap("/maps/map-02.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1))
				.addShip("Player1", ShipType.FRIGATE, Location.of(3, 1))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(3, 3))
				.addShip("Player2", ShipType.FRIGATE, Location.of(1, 3));
	}

	public static Model build() {
		final ModelBuilder builder = new ModelBuilder();
		builder.setCalendar(1570, 1600);
		createMap01(builder);

		return builder.build();
	}
}
