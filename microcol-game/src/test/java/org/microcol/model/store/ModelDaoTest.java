package org.microcol.model.store;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Test create model with fluent API than store than model and finally load and
 * verify that is same as was defined.
 */
public class ModelDaoTest {


    private final Logger logger = LoggerFactory.getLogger(ModelDaoTest.class);

    @Test
    public void test_simple_gson() throws Exception {
        Gson gson = new GsonBuilder().create();

        GameModelDao gameModel = new GameModelDao();
        gameModel.setAge(32);
        gameModel.setName("Ahoj lidi");
        String str;

        str = gson.toJson(new String[] { "a", "b", "c" });
        logger.info(str);

        str = gson.toJson(new Character[] { 'a', 'b', 'b' });
        logger.info(str);

        str = gson.toJson(gameModel);
        logger.info(str);
    }

    @Test
    public void test_writing() throws Exception {
        ModelPo modelPo = new ModelProvider().buildComplexModel().save();

        ModelDao modelDao = new ModelDao();
        modelDao.saveToFile("target/test.json", modelPo);
    }

    @Test
    public void test_simple_writing() throws Exception {
        ModelPo modelPo = new ModelProvider().buildSimpleModel().save();

        assertEquals(2, modelPo.getUnits().size());

        ModelDao modelDao = new ModelDao();
        modelDao.saveToFile("target/test.json", modelPo);
    }

    @Test
    public void test_writing_loading() throws Exception {
        ModelPo modelPo = new ModelProvider().buildComplexModel().save();

        ModelDao modelDao = new ModelDao();
        modelDao.saveToFile("target/test.json", modelPo);
    }

}
