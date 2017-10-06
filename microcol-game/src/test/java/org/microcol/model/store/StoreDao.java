package org.microcol.model.store;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ModelBuilder;
import org.microcol.model.UnitType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StoreDao {

	private Model buildComplexModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.setCalendar(1570, 1800)
			.setMap("/maps/test2.json")

			/**
			 * Human player
			 */
			.addPlayer("Dutch")
				.setComputerPlayer(false)
				.setGold(1108)
				.addColony("brunswick")
					.setLocation(Location.of(5, 4))
					.setDefaultConstructions(true)
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
					.addCargoUnit(UnitType.COLONIST, true, false, false).build())
			.addShipToPort(builder.makeUnitBuilder().setType(UnitType.FRIGATE).setLocation(Location.of(2, 2))
					.setPlayerName("Dutch").addCargoGood(GoodType.CIGARS, 100).addCargoGood(GoodType.RUM, 100)
					.addCargoGood(GoodType.SILVER, 100).build())
			.build();

		return builder.build();
	}
	
	@Test
	public void test_simple() throws Exception {
		Gson gson = new GsonBuilder()
				.create();
		
		GameModelDao gameModel = new GameModelDao();
		gameModel.setAge(32);
		gameModel.setName("Ahoj lidi");
		String str;
		
		str = gson.toJson(new String[]{"a","b","c"});
		System.out.println(str);

		str = gson.toJson(new Character[]{'a','b','b'});
		System.out.println(str);

		str = gson.toJson(gameModel);
		System.out.println(str);
	}

	@Test
	public void test_complex1() throws Exception {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		GamePo game = new GamePo();
		game.getUnits().add(UnitPo.make(1, UnitType.COLONIST.name(), "a", null));
		game.getUnits().add(UnitPo.make(2, UnitType.FRIGATE.name(), "b", null));
//		game.getMap().setTerrainType(new HashMap<Location,TerrainType>(), 5, 3);
//		game.getMap().getTiles()[0][0] = TerrainType.ARCTIC.getCode();
		
		String str;
		
		str = gson.toJson(game);
		System.out.println(str);
	}

	@Test
	public void test_complex2() throws Exception {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		Model model = buildComplexModel();
		GamePo game = model.save();
		
		String str = gson.toJson(game);
//		System.out.println(str);
		
		FileWriter fileWriter = new FileWriter(new File("target/test2.json"));
		fileWriter.write(str);
		fileWriter.close();
		
		ModelBuilder builder = new ModelBuilder();
		Model model2 = builder.setCalendar(1570, 1800).setMap("/maps/test2.json").addPlayer("karel").build()
				.getEuropeBuilder().build().build();
		GamePo game2 = model2.save();
		String str2 = gson.toJson(game2);
		System.out.println(str2);
		
		
	}
	
	
}
