package org.microcol.model;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ModelSaveLoadTest {
	@Test
	public void saveTest() {
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

		final JsonGeneratorFactory prettyGeneratorFactory = Json.createGeneratorFactory(
			ImmutableMap.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE));
		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = prettyGeneratorFactory.createGenerator(writer);
		model.save(generator);
		generator.close();
		final String json = writer.toString();
		System.out.println(json);
	}

	@Test
	public void loadTest() {
		final JsonParser parser = Json.createParser(new StringReader("{\"x\": 1,\"y\": 1}"));
		final Location location = Location.load(parser);

		Assert.assertEquals(Location.of(1, 1), location);
	}
}
