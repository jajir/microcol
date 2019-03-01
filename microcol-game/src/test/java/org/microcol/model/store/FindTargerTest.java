package org.microcol.model.store;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.ai.ContinentTool;
import org.microcol.ai.Continents;
import org.microcol.model.Model;
import org.microcol.model.Player;

/**
 * Test create model with fluent API than store than model and finally load and
 * verify that is same as was defined.
 */
public class FindTargerTest {

    @Test
    public void test_writing() throws Exception {
        final Model model = new ModelProvider().buildComplexModel();
        final Player enemyPlayer = model.getPlayerByName("Dutch");
        final ContinentTool pathTool = new ContinentTool();

        final Continents toAttack = pathTool.findContinents(model, enemyPlayer);
        assertNotNull(toAttack);
    }

}
