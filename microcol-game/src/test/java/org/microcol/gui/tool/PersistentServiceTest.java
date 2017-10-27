package org.microcol.gui.tool;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.microcol.gui.util.PersistentSrevice;
import org.microcol.gui.util.Scenario;

public class PersistentServiceTest {

	@Test
	public void test_first_scenario() throws Exception {
		PersistentSrevice persistentSrevice = new PersistentSrevice();
		List<Scenario> scenarios = persistentSrevice.getScenarios();

		assertNotNull(scenarios);
		assertTrue(scenarios.size() > 0);
		Scenario sc = scenarios.get(0);
		assertEquals(sc.getName(), "Simple tiny world");
		assertEquals(sc.getFileName(), "/maps/map-small.json");
	}

}
