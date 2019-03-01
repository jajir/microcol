package org.microcol.model.store;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

public class SaveToModelPoTest {

    @Test
    public void test_save_complex() throws Exception {
        final Model model = new ModelProvider().buildComplexModel();
        assertNotNull(model);
        model.startGame();
        assertEquals(2, model.getPlayers().size());
        assertEquals(21, model.getAllUnits().size());

        final ModelPo modelPo = model.save();
        assertNotNull(modelPo);
        assertEquals(2, modelPo.getGameManager().getPlayers().size());
        assertEquals(21, modelPo.getUnits().size());
    }

    @Test
    public void test_save_simpple() throws Exception {
        final Model model = new ModelProvider().buildSimpleModel();
        assertNotNull(model);
        model.startGame();
        assertEquals(2, model.getPlayers().size());
        assertEquals(2, model.getAllUnits().size());

        final ModelPo modelPo = model.save();
        assertNotNull(modelPo);
        assertEquals(2, modelPo.getGameManager().getPlayers().size());
        assertEquals(2, modelPo.getUnits().size());
    }

}
