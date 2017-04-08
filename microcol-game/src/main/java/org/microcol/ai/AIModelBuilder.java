package org.microcol.ai;

import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
import org.microcol.model.ShipType;

public class AIModelBuilder {
	private final ModelBuilder builder;

	AIModelBuilder() {
		builder = new ModelBuilder();
	}

	void setCalendar(final int startYear, final int endYear) {
		builder.setCalendar(startYear, endYear);
	}

	void createMinimal() {
		builder.setMap("/maps/test-map-ocean-1x1.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1));
	}

	void createMedium() {
		builder.setMap("/maps/test-map-ocean-100x100.txt")
			.addPlayer("Player1", false)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(100, 100));
	}

	void createHuge() {
		builder.setMap("/maps/test-map-ocean-1000x1000.txt")
			.addPlayer("Player1", false)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(1000, 1000));
	}

	void createMap01_2ships() {
		builder
			.setMap("/maps/map-01.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(4, 2))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(7, 7));
	}

	void createMap01_5ships() {
		builder
			.setMap("/maps/map-01.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(4, 2))
				.addShip("Player1", ShipType.FRIGATE, Location.of(3, 3))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(7, 7))
				.addShip("Player2", ShipType.FRIGATE, Location.of(7, 9))
				.addShip("Player2", ShipType.FRIGATE, Location.of(14, 9));
	}

	void createMap02() {
		builder.setMap("/maps/map-02.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(1, 1))
				.addShip("Player1", ShipType.FRIGATE, Location.of(3, 1))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(3, 3))
				.addShip("Player2", ShipType.FRIGATE, Location.of(1, 3));
	}

	void createMap03() {
		builder.setMap("/maps/map-03.txt")
			.addPlayer("Player1", false)
				.addShip("Player1", ShipType.GALLEON, Location.of(6, 2))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(7, 5))
				.addShip("Player2", ShipType.FRIGATE, Location.of(9, 6));
	}

	Model buildImpl() {
		return builder.build();
	}

	public static Model build() {
		final AIModelBuilder builder = new AIModelBuilder();

		builder.setCalendar(1570, 1600);
//		builder.createMinimal();
//		builder.createMedium();
//		builder.createHuge();
//		builder.createMap01_2ships();
		builder.createMap01_5ships();
//		builder.createMap02();
//		builder.createMap03();

		return builder.buildImpl();
	}
}
