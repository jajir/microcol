package org.microcol.model.store;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.Model;

import com.google.common.collect.Lists;

/**
 * Class helps to analyze invalid saved games.
 */
public class FindErrorTest {

    @Test
    public void test_cteni_chyboveho_soubory() throws Exception {
        final ModelDao modelDao = new ModelDao();
        final ModelPo modelPo = modelDao.loadPredefinedModel("/maps/Dutch-1574-chyba.microcol");
        assertThrows(IllegalArgumentException.class, () -> {
            Model.make(modelPo, Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        });
    }

}
