package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Saved movement allowances through the real file format: fractional thirds
 * survive a save and reload, and invalid allowances are rejected on load for
 * every unit of the bundled map.
 */
public class MovementAllowanceSaveTest {

    private static final String MAP = "/maps/free-play.microcol";

    /** Far beyond any unit speed, so it is over-speed for every unit type. */
    private static final int OVER_SPEED_THIRDS = 1000;

    @TempDir
    Path directory;

    @Test
    public void test_fractionalMovement_survivesSaveAndReload()
            throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao.loadFromClassPath(MAP);
        // Unit 6 is a French colonist and unit 3 a Dutch galleon, both on the
        // map. A mounted colonist has five points, so seven thirds is a partly
        // spent allowance that is neither zero nor whole points; the galleon
        // keeps a different fraction.
        final UnitPo colonist = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 6).findFirst().orElseThrow();
        colonist.setMounted(true);
        colonist.setMovementThirds(7);
        data.getModel().getUnits().stream().filter(unit -> unit.getId() == 3)
                .findFirst().orElseThrow().setMovementThirds(4);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        // Saving needs a started game, and starting it restores every unit's
        // full allowance, so the fractional allowances are set afterwards.
        model.startGame();
        ((AbstractUnit) model.getUnitById(6)).setMovementThirds(7);
        ((AbstractUnit) model.getUnitById(3)).setMovementThirds(4);
        final Location colonistAt = model.getUnitById(6).getLocation();
        final Location galleonAt = model.getUnitById(3).getLocation();

        // The saved document is written from the live model, not the input.
        data.setModel(model.save());
        final File file = directory.resolve("fractional.microcol").toFile();
        dao.saveToFile(file.getAbsolutePath(), data);
        final String text = Files.readString(file.toPath());
        assertTrue(text.contains("\"movementThirds\": 7"));
        assertTrue(text.contains("\"movementThirds\": 4"));

        final GameModelPo reloaded = dao.loadFromFile(file);
        final Model restored = Model.make(reloaded.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist unit = (UnitFreeColonist) restored
                .getUnitById(6);
        assertEquals(colonistAt, unit.getLocation());
        assertEquals(7, unit.getMovementThirds());
        assertEquals(2, unit.getActionPoints());
        final Unit galleon = restored.getUnitById(3);
        assertEquals(galleonAt, galleon.getLocation());
        assertEquals(4, galleon.getMovementThirds());
        assertEquals(1, galleon.getActionPoints());
    }

    @Test
    public void test_missingNegativeAndOverSpeedAllowances_areRejectedForEveryUnit()
            throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final int units = dao.loadFromClassPath(MAP).getModel().getUnits()
                .size();
        for (int index = 0; index < units; index++) {
            for (final Integer invalid : new Integer[] { null, -1,
                    OVER_SPEED_THIRDS }) {
                final GameModelPo data = dao.loadFromClassPath(MAP);
                data.getModel().getUnits().get(index)
                        .setMovementThirds(invalid);
                final int position = index;
                assertThrows(IllegalArgumentException.class,
                        () -> Model.make(data.getModel(),
                                MissionImpl.getPredefinedGameOverEvaluators()),
                        () -> "unit index " + position + " allowance "
                                + invalid);
            }
        }
    }
}
