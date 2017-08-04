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
			.addPlayer("Player1", true, 0)
				.addUnit(UnitType.GALLEON, "Player1", Location.of(4, 2))
				.addUnit(UnitType.FRIGATE, "Player1", Location.of(3, 3))
			.addPlayer("Player2", true, 0)
				.addUnit(UnitType.GALLEON, "Player2", Location.of(7, 7))
				.addUnit(UnitType.FRIGATE, "Player2", Location.of(7, 9))
				.addUnit(UnitType.FRIGATE, "Player2", Location.of(14, 9))
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
			final Unit unit = model.getUnits().get(i);
			final Unit loadedUnit = loadedModel.getUnits().get(i);
			Assert.assertEquals(unit.getType(), loadedUnit.getType());
			Assert.assertEquals(unit.getOwner(), loadedUnit.getOwner());
			Assert.assertEquals(unit.getLocation(), loadedUnit.getLocation());
			Assert.assertEquals(unit.getAvailableMoves(), loadedUnit.getAvailableMoves());
		}
		// TODO JKA Check GameManager.
	}
}
