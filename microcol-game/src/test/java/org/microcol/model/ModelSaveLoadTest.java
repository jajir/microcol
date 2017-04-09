package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ModelSaveLoadTest {
	@Test
	public void saveTest() {
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
		model.startGame();
		final Ship ship = model.getCurrentPlayer().getShips().get(0);
		*/

		final Gson gson = new GsonBuilder().create();
		final String json = gson.toJson(Location.of(1, 1));

		Assert.assertEquals("{\"x\":1,\"y\":1}", json);
	}

	@Test
	public void loadTest() {
		final Gson gson = new GsonBuilder().create();
		final Location location = gson.fromJson("{\"x\": 1,\"y\": 1}", Location.class);

		Assert.assertEquals(Location.of(1, 1), location);
	}
}
