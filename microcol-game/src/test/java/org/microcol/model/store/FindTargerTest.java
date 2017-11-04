package org.microcol.model.store;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.ai.Continent;
import org.microcol.ai.Continents;
import org.microcol.ai.PathTool;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;

/**
 * Test create model with fluent API than store than model and finally load and
 * verify that is same as was defined.
 */
public class FindTargerTest {

	private Logger logger = Logger.getLogger(FindTargerTest.class);

	private final Logger modelDaoLogger = Logger.getLogger(ModelDao.class);

	@Test
	public void test_writing() throws Exception {
		final Model model = new ModelProvider().buildComplexModel();
		final Player enemyPlayer = model.getPlayerByName("Dutch");
		final PathTool pathTool = new PathTool();
		
		final List<Continent> toAttack = pathTool.findContinentsToAttack(model, enemyPlayer);
		
		System.out.println("konec");
		
		// XXX send REF
		// nalozim n lodi s jednotkama
		// kam maji jet?
		// najdu pevninu, pro kazdou pevninu sectu mesta podle military force
		// podle pomeru velikosti mest na pevninach udelam tri armady a ty poslu
		// na 3 nejvetsi pevniny
		// po dosazeni pevniny se vylodim
		// najdu prvni mesto a utocim
		// win game - rules
	}

	private Level level;

	@Before
	public void before() {
		level = modelDaoLogger.getLevel();
		modelDaoLogger.setLevel(Level.DEBUG);
		logger.setLevel(Level.DEBUG);
	}

	@After
	public void after() {
		modelDaoLogger.setLevel(level);
	}

}
