package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.gui.util.PersistentService;
import org.microcol.gui.util.Scenario;

public class PersistentServiceTest {

    @Test
    public void test_first_scenario() throws Exception {
        PersistentService persistentSrevice = new PersistentService();
        List<Scenario> scenarios = persistentSrevice.getScenarios();

        assertNotNull(scenarios);
        assertTrue(scenarios.size() > 0);
        Scenario sc = scenarios.get(0);
        assertEquals(sc.getName(), "Simple tiny world");
        assertEquals(sc.getFileName(), "/maps/01-map-small.json");
    }

}
