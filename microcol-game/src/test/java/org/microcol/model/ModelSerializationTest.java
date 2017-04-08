package org.microcol.model;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.junit.Test;

public class ModelSerializationTest {
	@Test
	public void serializeTest() {
		/*
		final ModelBuilder builder = new ModelBuilder();
		final Model model = builder
			.setCalendar(1570, 1600)
			.setMap("/maps/test-map-2islands-15x10.txt")
			.addPlayer("Player1", true)
				.addShip("Player1", ShipType.GALLEON, Location.of(4, 2))
				.addShip("Player1", ShipType.FRIGATE, Location.of(3, 3))
			.addPlayer("Player2", true)
				.addShip("Player2", ShipType.GALLEON, Location.of(7, 7))
				.addShip("Player2", ShipType.FRIGATE, Location.of(7, 9))
				.addShip("Player2", ShipType.FRIGATE, Location.of(14, 9))
			.build();
		*/

		final JsonbConfig config = new JsonbConfig()
			.withFormatting(true);
		final Jsonb jsonb = JsonbBuilder.create(config);
		final String result = jsonb.toJson(Location.of(1, 1));
		System.out.println(result);
	}
}
