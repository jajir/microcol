package org.microcol.model.store;

import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.UnitType;
import org.microcol.model.builder.ModelBuilder;

/**
 * Help class that build model for tests.
 */
public class ModelProvider {

	public Model buildComplexModel() {
		ModelBuilder builder = new ModelBuilder();
		builder
			.setMap("/maps/test2.json")
			.setCalendar(1570, 1800)

			/**
			 * Human player
			 */
			.addPlayer("Dutch")
				.setComputerPlayer(false)
				.setGold(1108)
				.addColony("brunswick")
					.setLocation(Location.of(5, 4))
					.setDefaultConstructions()
					.setWorker(ConstructionType.RUM_DISTILLERS_HOUSE, 0, UnitType.COLONIST)
					.setWorker(ConstructionType.CARPENTERS_SHOP, 0, UnitType.COLONIST)
					.setWorker(ConstructionType.TOWN_HALL, 2, UnitType.COLONIST)
					.setWorker(Location.DIRECTION_NORTH_EAST, UnitType.COLONIST, GoodType.CORN)
					.setWorker(Location.DIRECTION_SOUTH_WEST, UnitType.COLONIST, GoodType.CORN)
					.setGood(GoodType.CIGARS, 33)
					.setGood(GoodType.COAT, 100)
					.setGood(GoodType.CORN, 75)
					.build()
				.build()
			.addUnit(UnitType.GALLEON, "Dutch", Location.of(5, 4))
			.addUnit(UnitType.FRIGATE, "Dutch", Location.of(4, 4))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(9, 4))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(6, 3))
			.addUnit(UnitType.COLONIST, "Dutch", Location.of(5, 4))

			.addUnit(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setPlayerName("Dutch")
					.setShipIncomingToEurope(4).build())
			.addUnit(builder.makeUnitBuilder().setType(UnitType.GALLEON).setPlayerName("Dutch")
					.setShipIncomingToColonies(2).build())

			/**
			 * Opponent player2
			 */
			.addPlayer("Player2")
				.setComputerPlayer(true)
				.setGold(100)
				.build()
			.addUnit(UnitType.GALLEON, "Player2", Location.of(8, 8))
			.addUnit(UnitType.FRIGATE, "Player2", Location.of(8, 10))
			.addUnit(UnitType.FRIGATE, "Player2", Location.of(15, 10))
			.addUnit(UnitType.COLONIST, "Player2", Location.of(8, 4))

			/**
			 * Europe port
			 */
			.addUnit(builder.makeUnitBuilder().setType(UnitType.COLONIST).setPlayerName("Dutch")
					.setUnitToEuropePortPier().build())
			.addUnit(builder.makeUnitBuilder().setType(UnitType.COLONIST).setPlayerName("Dutch")
					.setUnitToEuropePortPier().build())
			.getEuropeBuilder()
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.GALLEON).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.COTTON, 100)
					.addCargoUnit(UnitType.COLONIST).build())
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.CIGARS, 100).addCargoGood(GoodType.RUM, 100)
					.addCargoGood(GoodType.SILVER, 100).build())
			.build();

		return builder.build();
	}

	public Model buildSimpleModel() {
		ModelBuilder builder = new ModelBuilder();
		builder
			.setMap("/maps/test2.json")
			.setCalendar(1570, 1800)

			/**
			 * Human player
			 */
			.addPlayer("Dutch")
				.setComputerPlayer(false)
				.setGold(1108)
			.build()

			/**
			 * Opponent player2
			 */
			.addPlayer("Player2")
				.setComputerPlayer(true)
				.setGold(100)
				.build()

			/**
			 * Europe port
			 */
			.getEuropeBuilder()
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.GALLEON).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.COTTON, 100)
					.addCargoUnit(UnitType.COLONIST).build())
			.build();

		return builder.build();
	}
	
}
