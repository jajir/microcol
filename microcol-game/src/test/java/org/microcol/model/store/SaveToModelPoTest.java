package org.microcol.model.store;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.model.Model;
import org.microcol.model.store.ModelPo;

public class SaveToModelPoTest {
	
	@Test
	public void test_save_complex() throws Exception {
		final Model model = new ModelProvider().buildComplexModel();
		assertNotNull(model);
		assertEquals(2, model.getPlayers().size());
		assertEquals(21, model.getAllUnits().size());
		
		final ModelPo modelPo = model.save();
		assertNotNull(modelPo);
		assertEquals(2, modelPo.getPlayers().size());
		assertEquals(21, modelPo.getUnits().size());
	}
	
	@Test
	public void test_save_simpple() throws Exception {
		final Model model = new ModelProvider().buildSimpleModel();
		assertNotNull(model);
		assertEquals(2, model.getPlayers().size());
		assertEquals(2, model.getAllUnits().size());
		
		final ModelPo modelPo = model.save();
		assertNotNull(modelPo);
		assertEquals(2, modelPo.getPlayers().size());
		assertEquals(2, modelPo.getUnits().size());
	}
	
}
