package org.microcol.model.store;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.microcol.gui.MicroColException;
import org.microcol.model.GameOverEvaluator;
import org.microcol.model.Model;

import com.google.common.collect.Lists;

/**
 * Class helps to analyze invalid saved games.
 */
public class FindErrorTest {

    @Test(expected = MicroColException.class)
    public void test_cteni_chyboveho_soubory() throws Exception {
        ModelDao modelDao = new ModelDao();
        final ModelPo modelPo = modelDao.loadPredefinedModel("/maps/Dutch-1574-chyba.microcol");
        final Model model = Model.make(modelPo,
                Lists.newArrayList(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
        assertNotNull(modelPo);
        modelPo.getUnits().stream().filter(unitPo -> unitPo.getPlaceMap() != null)
                .forEach(unitPo -> {
                    System.out.println(unitPo);
                    System.out.println(unitPo.getPlaceMap());
                });
        assertNotNull(model);
        model.getAllUnits().stream().filter(unit -> unit.isAtPlaceLocation()).forEach(unit -> {
            System.out.println(unit);
            System.out.println(unit.getLocation());
        });
    }

}
