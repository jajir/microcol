package org.microcol.model;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ModelSerializationTest {
	@Test
	public void serializeTest() throws JsonProcessingException {
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
		final ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		final String result = mapper.writeValueAsString(Location.of(1, 1));
		System.out.println(result);
	}
}
