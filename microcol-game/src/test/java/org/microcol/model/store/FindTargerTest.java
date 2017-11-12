package org.microcol.model.store;

import static org.junit.Assert.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.model.Model;
import org.microcol.model.Player;

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
		final ContinentTool pathTool = new ContinentTool();
		
		final Continents toAttack = pathTool.findContinents(model, enemyPlayer);
		assertNotNull(toAttack);
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
