package org.microcol.model.store;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Test create model with fluent API than store than model and finally load and
 * verify that is same as was defined.
 */
public class ModelDaoTest {
	
	private Logger logger = Logger.getLogger(ModelDaoTest.class);

	private final Logger modelDaoLogger = Logger.getLogger(ModelDao.class);
	
	@Test
	public void test_simple_gson() throws Exception {
		Gson gson = new GsonBuilder().create();

		GameModelDao gameModel = new GameModelDao();
		gameModel.setAge(32);
		gameModel.setName("Ahoj lidi");
		String str;

		str = gson.toJson(new String[] { "a", "b", "c" });
		logger.debug(str);

		str = gson.toJson(new Character[] { 'a', 'b', 'b' });
		logger.debug(str);

		str = gson.toJson(gameModel);
		logger.debug(str);
	}

	@Test
	public void test_writing() throws Exception {
		ModelPo modelPo = new ModelProvider().buildComplexModel().save();

		ModelDao modelDao = new ModelDao();
		modelDao.saveToFile("target/test.json", modelPo);
	}

	@Test
	public void test_writing_loading() throws Exception {
		ModelPo modelPo = new ModelProvider().buildComplexModel().save();

		ModelDao modelDao = new ModelDao();
		modelDao.saveToFile("target/test.json", modelPo);
		//TODO load from file
	}

	private Level level;
	
	@Before
	public void before() {
		level  = modelDaoLogger.getLevel();
		modelDaoLogger.setLevel(Level.DEBUG);
		logger.setLevel(Level.DEBUG);
	}

	@After
	public void after() {
		modelDaoLogger.setLevel(level);
	}
	

}
