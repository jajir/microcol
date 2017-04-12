package org.microcol.model;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ModelSaveLoadTest {
	private String toJson(final Model model) {
		final Map<String, ?> config = ImmutableMap.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		final JsonGeneratorFactory factory = Json.createGeneratorFactory(config);
		final StringWriter writer = new StringWriter();
		final JsonGenerator generator = factory.createGenerator(writer);
		generator.writeStartObject();
		model.save("model", generator);
		generator.writeEnd();
		generator.close();

		return writer.toString();
	}

	private Model fromJson(final String json) {
		final JsonParser parser = Json.createParser(new StringReader(json));
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		final Model model = Model.load(parser);
		parser.next(); // END_OBJECT
		parser.close();

		return model;
	}

	@Test
	public void saveLoadTest() {
		final ModelBuilder builder = new ModelBuilder();
		final Model model = builder
			.setCalendar(1570, 1600)
			.setMap("/maps/test-map-2islands-15x10.txt")
			.addPlayer("Player1", true)
				.addUnit("Player1", UnitType.GALLEON, Location.of(4, 2))
				.addUnit("Player1", UnitType.FRIGATE, Location.of(3, 3))
			.addPlayer("Player2", true)
				.addUnit("Player2", UnitType.GALLEON, Location.of(7, 7))
				.addUnit("Player2", UnitType.FRIGATE, Location.of(7, 9))
				.addUnit("Player2", UnitType.FRIGATE, Location.of(14, 9))
			.build();
		model.startGame();

		final String json = toJson(model);
		final Model loadedModel = fromJson(json);

		Assert.assertEquals(model.getCalendar().getStartYear(), loadedModel.getCalendar().getStartYear());
		Assert.assertEquals(model.getCalendar().getEndYear(), loadedModel.getCalendar().getEndYear());
		Assert.assertEquals(model.getCalendar().getCurrentYear(), loadedModel.getCalendar().getCurrentYear());
		Assert.assertEquals(model.getMap().getFileName(), loadedModel.getMap().getFileName());
		Assert.assertEquals(model.getMap().getMaxX(), loadedModel.getMap().getMaxX());
		Assert.assertEquals(model.getMap().getMaxY(), loadedModel.getMap().getMaxY());
		// TODO JKA TERRAIN?
		Assert.assertEquals(model.getPlayers(), loadedModel.getPlayers());
		Assert.assertEquals(model.getUnits().size(), loadedModel.getUnits().size());
		for (int i = 0; i < model.getUnits().size(); i++) {
			final Unit ship = model.getUnits().get(i);
			final Unit loadedShip = loadedModel.getUnits().get(i);
			Assert.assertEquals(ship.getType(), loadedShip.getType());
			Assert.assertEquals(ship.getOwner(), loadedShip.getOwner());
			Assert.assertEquals(ship.getLocation(), loadedShip.getLocation());
			Assert.assertEquals(ship.getAvailableMoves(), loadedShip.getAvailableMoves());
		}
		// TODO JKA Check GameManager.
	}
}
