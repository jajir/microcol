package org.microcol.model.store;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.model.store.ModelPo;

public class SaveToModelPoTest {
	
	@Test
	public void test_save() throws Exception {
		final ModelPo modelPo = new ModelProvider().buildComplexModel().save();
		
		assertNotNull(modelPo);
		assertEquals(2, modelPo.getPlayers().size());
		assertEquals(20, modelPo.getUnits().size());
	}
	
}
