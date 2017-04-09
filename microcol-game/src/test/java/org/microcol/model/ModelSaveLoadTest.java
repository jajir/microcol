package org.microcol.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;

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
		generator.writeStartObject();
		model.save("model", generator);
		generator.writeEnd();
		generator.close();
		final String json = writer.toString();
		System.out.println(json);
	}

	@Test
	public void loadTest() throws Exception {
		final JsonParser parser = Json.createParser(new BufferedReader(
			new InputStreamReader(WorldMap.class.getResourceAsStream("/saves/test-save-model-01.json"), "UTF-8")));
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		final Model model = Model.load(parser);
		parser.next(); // END_OBJECT
		parser.close();

		System.out.println(model);
	}
}
